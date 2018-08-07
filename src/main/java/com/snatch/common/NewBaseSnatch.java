package com.snatch.common;

import com.silita.service.INoticeService;
import com.snatch.common.util.ChineseCompressUtil;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.exception.SnatchFilterException;
import com.snatch.model.Notice;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import static com.snatch.common.utils.SnatchUtils.*;

public abstract class NewBaseSnatch extends IJobHandler implements PageProcessor {

    @Autowired
    private INoticeService noticeService;
    ChineseCompressUtil util = new ChineseCompressUtil();

    public abstract ReturnT<String> execute(String... params) ;

    @Override
    public void process(Page page) {

    }

    public void insertNoticeDate(Notice notice) {
        try {
            boolean isContinue = isContinue(notice);
            if (isContinue) {
                if (notice == null || SnatchUtils.isNull(notice.getTitle())
                        || SnatchUtils.isNull(notice.getContent())
                        || (SnatchUtils.isNull(SnatchUtils.deleteHtmlTag(util.getPlainText(notice.getContent()))) && !notice.getContent().contains("img"))) {
                    String warnInfo = "标题、内容获取失败，跳过。[url：" + notice.getUrl() + "][catchType:" + notice.getCatchType() + "]";
                    throw new Exception(warnInfo);
                }

                if (SnatchUtils.isNull(notice.getCatchType())) {
                    notice = setCatchTypeByTitle(notice, notice.getTitle());
                }
                // catchType为空， 进行公告详情以及标题判断并设置其catchType
                if (SnatchUtils.isNull(notice.getCatchType())) {
                    notice = parseCatchType(notice, noticeService);
                    if (SnatchUtils.isNull(notice.getCatchType())) {
                        String warnInfo = "catchType = null，公告类型判断失败，不进行抓取！[tile:" + notice.getTitle() + "][url=" + notice.getUrl() + "]";
                        throw new Exception(warnInfo);
                    }
                }

                //招标公告：政府采购资质判断
                if (notice.getType().equals(SnatchContent.CAI_GOU_TYPE)) {
                    if (notice.getCatchType().equals(SnatchContent.ZHAO_BIAO_TYPE)) {
                        //招标公告:标题判断绿色通道判断
                        if (!isZhaobiaoGreenChannel(notice.getTitle())) {
                            boolean existsAptitude = existsAptitude(notice, noticeService);
                            if (!existsAptitude) {
                                String warnInfo = "资质不符，跳过！[tile:" + notice.getTitle() + "][catchType:" + notice.getCatchType() + "][url=" + notice.getUrl() + "]";
                                throw new SnatchFilterException(warnInfo);
                            }
                        }
                    } else if (notice.getCatchType().equals(SnatchContent.OTHER_TYPE)) {
                        String[] regs = {"(登记公告)"};
                        boolean excludeFlag = SnatchUtils.matchStringArray(regs, notice.getTitle());
                        if (excludeFlag) {
                            throw new SnatchFilterException("标题中包含排除关键字，跳过。[tile:" + notice.getTitle() + "][catchType:" + notice.getCatchType() + "][url=" + notice.getUrl() + "]");
                        }
                    } else if (notice.getCatchType().equals(SnatchContent.ZHONG_BIAO_TYPE)) {
                        //全部抓取
                    }
                } else {//非政府采购类公告
                    System.out.println("非政府采购类,进行抓取[url：" + notice.getUrl() + "][catchType:" + notice.getCatchType() + "]");
                }
                noticeService.insertNoticeDate(notice);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Site getSite() {
        return Site.me().setRetryTimes(10).setCycleRetryTimes(10).setSleepTime(100).setRetrySleepTime(500).setTimeOut(60000)
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.4549.400 QQBrowser/9.7.12900.400")
                .addHeader("Connection", "keep-alive")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8");
    }

    public boolean isContinue(Notice notice) {
        boolean exists = true;
        noticeService.createNoticeByTableName(notice.getSource());
        int count = noticeService.getNoticeCountByOpenDateAndUrl(notice, notice.getSource());
        if (count > 0) {
            exists = false;
            System.out.println("已经存在..." + notice.getTitle() + "---" + notice.getOpendate());
        }
        return exists;
    }


}
