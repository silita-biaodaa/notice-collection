package com.silita.service.impl;

import com.silita.commons.kafka.KafkaProducerUtil;
import com.silita.commons.redisJMS.RedisQueue;
import com.silita.commons.utils.RedisQueueUtil;
import com.silita.commons.utils.SaveNewestDataSpecifyCount;
import com.silita.dao.*;
import com.silita.service.INoticeService;
import com.snatch.model.*;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("noticeService")
public class NoticeServiceImpl implements INoticeService {

    private static Logger logger = Logger.getLogger(NoticeServiceImpl.class);

    @Autowired
    RedisQueue<Notice> redisQueue;
    @Autowired
    @Qualifier("redisTemplate")
    RedisTemplate<String, Notice> redisTemplate;

    @Autowired
    @Qualifier("jedisPool")
    JedisPool pool;

    @Autowired
    KafkaProducerUtil kafkaProducerUtil;


    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private AllPageIncrementMapper allPageIncrementMapper;
    @Autowired
    private SnatchExceptionMapper snatchExceptionMapper;
    @Autowired
    private AllZhMapper allZhMapper;
    @Autowired
    private SnatchStatisticsMapper snatchStatisticsMapper;
    @Autowired
    private AreaMapper areaMapper;


    @Override
    public PageIncrement getLastPageIncrement(String url) {
        PageIncrement pageIncrement = null;
        List<Map<String, Object>> result = allPageIncrementMapper.listAllPageIncrement(url);

        if (result.size() > 0) {
            Map<String, Object> row = result.get(0);
            pageIncrement = new PageIncrement();
            if (row.get("currentAllPage") != null && !"".equals(row.get("currentAllPage"))) {
                pageIncrement.setCurrentAllPage(Integer.parseInt(row.get("currentAllPage").toString()));
            }
            if (row.get("snatch_opendate") != null && !"".equals(row.get("snatch_opendate"))) {
                pageIncrement.setSnatchOpendate(row.get("snatch_opendate").toString());
            }
        }
        if (pageIncrement != null) {
            XxlJobLogger.log("lastAllPage:" + pageIncrement.getCurrentAllPage() + " ## snatch_opendate:" + pageIncrement.getSnatchOpendate());
        }
        return pageIncrement;
    }

    @Override
    public void insertIncrementByAllPage(int allPage, int LAST_ALLPAGE, String openDate, String url) {
        Map params = new HashMap();
        params.put("currentAllPage", allPage);
        params.put("lastAllPage", LAST_ALLPAGE);
        params.put("openDate", openDate);
        params.put("url", url);
        allPageIncrementMapper.insertAllPageIncrement(params);
    }

    @Override
    public Integer getNoticeCountByOpenDateAndUrl(Notice notice, String tableName) {
        Map params = new HashMap();
        params.put("tableName", tableName);
        params.put("url", notice.getUrl());
        params.put("openDate", notice.getOpendate());
        Integer count = noticeMapper.getNoticeTotalByUrlAndOpenDate(params);
        return count;
    }


    @Override
    public synchronized void insertNoticeDate(Notice notice) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(
                "notice_queue",
                redisTemplate.getConnectionFactory());
        long original = 0L; // 第一次，设置初始值
        original = redisAtomicLong.get(); // 获取 code 值
        if (original == 0L) { // 第一次，设置初始值
            redisAtomicLong.set(5L);
        }
        long nowId = redisAtomicLong.incrementAndGet(); // 获得加1后的值
        notice.setRedisId((int) nowId);

        //存mysql
        int flag = this.insertNoticeToMysql(notice);
        //判断数据是否插入成功和数据是否是异常表数据
        if (flag == 1 && notice.getExpFlag() == 1) {
            //更新异常表数据状态
            updateSnatchExceptionStatus(notice.getUrl());
        }

        try {
            // push到队列
            RedisQueueUtil.pushToRedisQueue(notice, redisQueue);
        } catch (InterruptedException e) {
            e.printStackTrace();
//            logger.error(e.getMessage());
        }
        //发送kafka消息
//        this.sendkafkaMsg(notice);
        String noticestr = RedisQueueUtil.getNoticeString(notice);
        //存入redis
        this.saveNewestNotice(noticestr);
    }


    public void createNoticeByTableName(String tableName) {
        Integer isExist = noticeMapper.checkTableIsExist(tableName);
        if (isExist == 0) {
            noticeMapper.createNoticeTable(tableName);
        }
    }

    @Override
    public void insertSnatchException(SnatchException snatchException) {
        if(snatchException!=null && snatchException.getExName()!=null && snatchException.getExName().length() >= 100){
            snatchException.setExName(snatchException.getExName().substring(0,100));
        }
        int count = snatchExceptionMapper.getSnatchExceptionTotalByUrl(snatchException.getExUrl());
        if(count == 0) {
            snatchExceptionMapper.insertSnatchException(snatchException);
        } else {
            snatchExceptionMapper.updateSnatchException(snatchException);
        }
    }

    @Override
    public void updateSnatchExceptionStatus(String url) {
        snatchExceptionMapper.updateSnatchExceptionStatus(url);
    }

    @Override
    public List<Map<String, Object>> listAllZh() {
        return allZhMapper.listAllZh();
    }

    @Override
    public Integer getSnatchExceptionTotal(String siteClassify) {
        return snatchExceptionMapper.getSnatchExceptionTotal(siteClassify);
    }

    @Override
    public void insertSnatchStatistics(TbSnatchStatistics snatchStatistics) {
        snatchStatisticsMapper.insertSnatchStatistics(snatchStatistics);
    }

    @Override
    public Map<String, Number> getNoticeTotalBySnatchDate(Map params) {
        Map<String, Number> counts = noticeMapper.getNoticeTotalsByUrlAndSnatchDate(params);
        return counts;
    }

    @Override
    public List<SnatchException> listSnatchExceptionByExClass(String exClass) {
        return snatchExceptionMapper.listSnatchExceptionByExClass(exClass);
    }

    @Override
    public List<Map<String, Object>> querysCityCode(String province) {
        return areaMapper.querysCityCode(province);
    }

    /**
     * 添加公告到mysql
     * @param notice
     * @return
     */
    public int insertNoticeToMysql(Notice notice) {
        String source = notice.getSource();
        String tbName = "mishu_snatch." + source;
        Dimension dimension = notice.getDimension();
        noticeMapper.insertNotice(notice, tbName, dimension);
        return notice.getId();
    }

    /**
     * 发送公告到kafka
     * @param no
     */
    public void sendkafkaMsg(Notice no) {
        try {
            kafkaProducerUtil.sendMsg(no);//kafka消息发送
            logger.info("kafka发送消息finished。redisId:" + no.getRedisId() + "##title:" + no.getTitle() + "##Opendate:" + no.getOpendate() + "##type:" + no.getType());
        } catch (Exception e) {
            logger.error("kafka发送消息失败。" + e.getMessage(), e);
        } finally {
            no = null;
            System.gc();
        }
    }

    /**
     * 保存到redis
     * @param noticeData
     */
    public void saveNewestNotice(String noticeData) {
        SaveNewestDataSpecifyCount andsc = new SaveNewestDataSpecifyCount(3000);
        Jedis jedis = pool.getResource();
        try {
            andsc.saveData(jedis, "newestNotice", noticeData);
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

}
