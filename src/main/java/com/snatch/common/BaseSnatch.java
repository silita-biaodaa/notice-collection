package com.snatch.common;

import com.alibaba.fastjson.JSONObject;
import com.silita.service.INoticeService;
import com.snatch.common.util.ChineseCompressUtil;
import com.snatch.common.utils.DateUtils;
import com.snatch.common.utils.SnatchLogger;
import com.snatch.common.utils.SnatchUtils;
import com.snatch.exception.SnatchFilterException;
import com.snatch.model.Notice;
import com.snatch.model.PageIncrement;
import com.snatch.model.SnatchException;
import com.snatch.model.TbSnatchStatistics;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.snatch.common.utils.SnatchUtils.*;


/**
 * Created by dh on 2017/8/7.
 */
public abstract class BaseSnatch extends IJobHandler {
    @Autowired
    private INoticeService noticeService;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

    ChineseCompressUtil util = new ChineseCompressUtil();

    static final int EX_NOTICE_RE_COUNTY = 50;  //异常数据重抓次数

    protected int LAST_ALLPAGE = 0;//上次抓取的公告的总页数

    protected String LAST_SNATCH_OPENDATE=null;//上次调度抓取到的最大公示时间

    protected String CURRENT_SNATCH_OPENDATE=null;//本次抓取到的最大公示时间

    protected int CURRENT_PAGE=0;//本次抓取的总页数

    protected boolean isUpdateIncrement=true;//本次抓取是否保存增量记录。

    protected String CURRENT_PAGE_LAST_OPENDATE=null;//当前页面抓取的最后一条记录的公示时间

    private boolean isFirstRow=true;//是否本页第一行

    private boolean isLastRow=true;//是否本页最后一行

    private final int OPENDATE_MAX_TURNCOUNT=200;//根据公示时间判断，增加翻页的最大值

    private int openDateTurnPage=0;

    private boolean timeOutFlag = false;   //抓取超时开关

    private int retryCount = 5;  //网络异常重试次数

    private int turneDay = 3;    // 公示时间最大翻页天数

    private Date MIN_OPENDATE=null;//抓取公告的最小公示时间

    private boolean isMinOpendate=false;//是否已抓取到最小日期

    /**
     * 清理类变量
     */
    protected void clearClassParam(){
        LAST_ALLPAGE=0;
        LAST_SNATCH_OPENDATE=null;
        CURRENT_SNATCH_OPENDATE=null;
        CURRENT_PAGE=0;
        isUpdateIncrement=true;
        CURRENT_PAGE_LAST_OPENDATE=null;
        openDateTurnPage=0;
        isMinOpendate=false;
        System.gc();
    }

    @Override
    public ReturnT<String> execute(String... params) throws Exception {
        Long startTime  = System.currentTimeMillis();
        ExecutorService exec = Executors.newFixedThreadPool(1);
        try {
            if(params!=null && params.length>0){
                String argStr = params[0].trim();
                JSONObject jsonObject= JSONObject.parseObject(argStr);
                String timeOutFlagStr = jsonObject.getString("timeOutFlag");
                String lastSnatchOpendate = jsonObject.getString("lastSnatchOpendate");
                String minOpendate = jsonObject.getString("minOpendate");
                //超时设置
                if(timeOutFlagStr!=null && timeOutFlagStr.equals("true")){
                    timeOutFlag = true;
                }
                //增量时间设置
                if(lastSnatchOpendate!=null ) {
                    LAST_SNATCH_OPENDATE =lastSnatchOpendate;
                }
                //抓取公告的最小公示时间
                if(minOpendate!=null ) {
                    MIN_OPENDATE = DateUtils.parseDate(minOpendate);
                }

            }
            if(timeOutFlag){
                // 抓取时长监控，超60分钟自动结束本次任务
                Callable<String> call = new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        startSnatch();
                        return "over";
                    }
                };
                Future<String> future = exec.submit(call);
                future.get(1000*60*60, TimeUnit.MILLISECONDS); //超时时间60分钟
            }else{
                startSnatch();
            }

            String timeCost = "本次执行结束，耗时：" + (System.currentTimeMillis() - startTime) + "ms";
            SnatchLogger.debug(timeCost);
            return ReturnT.SUCCESS;
        }catch (TimeoutException e){
            String info = "抓取时长超过60分钟，自动终止任务。";
            SnatchLogger.warn(info);
            throw new InterruptedException();
        }catch (InterruptedException e){
            throw e;
        }catch (Exception e){
            SnatchLogger.error(e.getMessage(),e);
            return ReturnT.FAIL;
        }finally {
            clearClassParam();
            exec.shutdown();
        }
    }

    /**
     * 执行抓取任务
     * @throws Exception
     */
    private void startSnatch()throws Exception {
        run();
        disposeExpUrl();
    }

    private void validateMinDate(){
        if(!isMinOpendate) {
            //当前页面的公示时间小于设置的最小时间后则不考虑继续翻页，仅抓完本页面
            Date currentOpenDate = DateUtils.parseDate(CURRENT_PAGE_LAST_OPENDATE);
            SnatchLogger.debug("validateMinDate...[CURRENT_PAGE_LAST_OPENDATE:"+CURRENT_PAGE_LAST_OPENDATE+"][MIN_OPENDATE:"+MIN_OPENDATE+"]");
            if (MIN_OPENDATE != null
                    && DateUtils.getDistanceOfTwoDate(currentOpenDate, MIN_OPENDATE) > 0) {
                isMinOpendate = true;
            }
        }
    }

    /**
     * 保存公告详情数据，返回需要循环的总页数
     * @param notice
     * @param pageNum
     * @param pageSize
     * @param rowNum
     * @param rowSize
     * @return
     */
    protected int detailHandle(Notice notice, int pageNum, int pageSize, int rowNum, int rowSize)throws Exception{
        isFirstRow = rowNum == 0;
        isLastRow = rowNum + 1 == rowSize;
        boolean notExist=true;
        notice.setType(SnatchContent.PU_TONG_TYPE);
        notExist = standardDetail(notice.getUrl(), notice, notice.getCatchType());
        CURRENT_PAGE_LAST_OPENDATE = notice.getOpendate();
        //当前页面的公示时间小于设置的最小时间后则不考虑继续翻页，仅抓完本页面
        validateMinDate();
        if(isMinOpendate) {
            return pageNum;//爬完本页之后终止
        }

        if (notExist) {
            if (pageNum == pageSize && isLastRow) {
                pageSize++;
            }
        }
        Thread.sleep(10);
        return pageSize;
    }

    /**
     * 政府采购类网站，是否抓取需要判断详情页的资质内容
     * @param notice
     * @param pageNum
     * @param pageSize
     * @param rowNum
     * @param rowSize
     * @return
     * @throws Exception
     */
    protected int govDetailHandle(Notice notice,int pageNum,int pageSize, int rowNum,int rowSize)throws Exception{
        isFirstRow = rowNum == 0;
        isLastRow = rowNum + 1 == rowSize;
        boolean notExist=true;
        notice.setType(SnatchContent.CAI_GOU_TYPE);
        notExist = standardDetail(notice.getUrl(), notice, notice.getCatchType(), SnatchContent.ZF_CG);
        CURRENT_PAGE_LAST_OPENDATE = notice.getOpendate();
        //当前页面的公示时间小于设置的最小时间后则不考虑继续翻页，仅抓完本页面
        validateMinDate();
        if(isMinOpendate) {
            return pageNum;//爬完本页之后终止
        }

        if (notExist) {
            if (pageNum == pageSize && isLastRow) {
                pageSize++;
            }
        }
        Thread.sleep(10);
        return pageSize;
    }

    /**
     * 根据公示时间进行判断，考虑是否继续翻页
     * @param page
     * @return
     */
    protected int turnPageEstimate(int page)throws Exception{
        Date currentOpenDate = DateUtils.parseDate(CURRENT_PAGE_LAST_OPENDATE);
        Date LastOpenDate = DateUtils.parseDate(LAST_SNATCH_OPENDATE);

        //当前页面的公示时间小于设置的最小时间后则不考虑继续翻页
        if(isMinOpendate) {
            return page;
        }else {
            if (LastOpenDate != null && currentOpenDate != null) {
                if (DateUtils.getDistanceOfTwoDate(LastOpenDate, currentOpenDate) >= turneDay) {
                    SnatchLogger.debug("[CURRENT_PAGE_LAST_OPENDATE：" + CURRENT_PAGE_LAST_OPENDATE + "][LAST_SNATCH_OPENDATE:" + LAST_SNATCH_OPENDATE + "]");
                    openDateTurnPage++;
                    if (openDateTurnPage > OPENDATE_MAX_TURNCOUNT) {
                        String info = "公示时间判断翻页取消，已达到公示时间翻页上线。" + OPENDATE_MAX_TURNCOUNT;
                        SnatchLogger.debug(info);
                    } else {
                        if (page < CURRENT_PAGE + 5) {
                            page++;
                        }
                    }
                }
            } else {
                String info = "turnPageEstimate error,公示时间不能为空![CURRENT_PAGE_LAST_OPENDATE:" + CURRENT_PAGE_LAST_OPENDATE + "]" +
                        "[LAST_SNATCH_OPENDATE:" + LAST_SNATCH_OPENDATE + "]";
                SnatchLogger.debug(info);
            }
            return page;
        }
    }

    /**
     * 查询url前次抓取的情况（最大页数与公示时间）
     * @param url
     */
    protected void queryBeforeSnatchState(String url)throws Exception{
        clearClassParam();
        PageIncrement pageIncrement = noticeService.getLastPageIncrement(url);
        if(pageIncrement !=null) {
            LAST_ALLPAGE = pageIncrement.getCurrentAllPage();
            if (SnatchUtils.isNull(LAST_SNATCH_OPENDATE)) {
                if (SnatchUtils.isNull(pageIncrement.getSnatchOpendate())) {
                    pageIncrement.setSnatchOpendate(DateUtils.getDate());//没有历史公示时间，获取当前日期
                }
                if(LAST_SNATCH_OPENDATE==null) {
                    LAST_SNATCH_OPENDATE = pageIncrement.getSnatchOpendate();
                }
            }
        }else{
            if(LAST_SNATCH_OPENDATE==null) {
                LAST_SNATCH_OPENDATE = DateUtils.getDate();//没有历史公示时间，获取当前日期
            }
        }
        String info = "#######***[LAST_ALLPAGE:"+LAST_ALLPAGE+"][LAST_SNATCH_OPENDATE:"+LAST_SNATCH_OPENDATE+"]";
        SnatchLogger.debug(info);
    }

    /**
     * 计算本次应该抓取的总页数
     * @param currentPage
     * @param lastAllPage
     * @return
     */
    protected  int computeTotalPage(int currentPage,int lastAllPage)throws Exception{
        CURRENT_PAGE=currentPage;
        int offset = 5;
        if(currentPage - lastAllPage>=0&&lastAllPage>0){
            currentPage = currentPage - lastAllPage+offset;
        }else if(currentPage - lastAllPage < 0){
            currentPage =offset;
        }

        if(currentPage > CURRENT_PAGE){
            currentPage=CURRENT_PAGE;
        }

        return currentPage;
    }

    public abstract void run() throws Exception;

    public abstract Notice detail(String href, Notice notice, String catchType)throws Exception;



    private void remeberMinOpenDate(String openDate)throws Exception{
        if (verifyOpenDate(openDate)) {
            CURRENT_PAGE_LAST_OPENDATE = openDate.trim();
            String info = "remeberMinOpenDate ..[CURRENT_PAGE_LAST_OPENDATE:"+CURRENT_PAGE_LAST_OPENDATE+"][openDate:"+openDate+"]";
            SnatchLogger.debug(info);
        }else{
            String info = "remeberMinOpenDate[openDate:"+openDate+"]";
//            SnatchLogger.warn(info);
        }

    }

    /**
     * 记录本次抓取最大的openDate
     * @param openDate
     */
    private void rememberMaxOpenDate(String openDate)throws Exception{
        try {
            if (verifyOpenDate(openDate)) {
                Date newOpenDate = DateUtils.parseDate(openDate);
                if (verifyOpenDate(CURRENT_SNATCH_OPENDATE)) {
                    Date currentOpenDate = DateUtils.parseDate(CURRENT_SNATCH_OPENDATE);
                    if(DateUtils.getDistanceOfTwoDate(currentOpenDate,newOpenDate)>0){
                        CURRENT_SNATCH_OPENDATE = openDate.trim();
                    }
                } else {
                    CURRENT_SNATCH_OPENDATE = openDate.trim();
                }

            }else{
                String info =" rememberMaxOpenDate execute...[openDate:" + openDate + "]";
//                SnatchLogger.warn(info);
            }
        }finally{
            String info ="rememberMaxOpenDate execute...[CURRENT_SNATCH_OPENDATE:"+CURRENT_SNATCH_OPENDATE+"][openDate:"+openDate+"]";
//            SnatchLogger.debug(info);
        }
    }

    /**
     * 是否为标准的yyyy-mm-dd日期
     * @param openDate
     * @return
     */
    private boolean verifyOpenDate(String openDate){
        return SnatchUtils.isNotNull(openDate) && openDate.trim().length() == 10;
    }

    protected void saveAllPageIncrement(String url){
        if(isUpdateIncrement) {
            if (CURRENT_PAGE > 0) {
                //本次抓取的页面所有都是已存在记录，且页内有跳过规则时，需要取上次抓取成功的最大公示时间
                if(CURRENT_SNATCH_OPENDATE==null){
                    CURRENT_SNATCH_OPENDATE= LAST_SNATCH_OPENDATE;
                }
                noticeService.insertIncrementByAllPage(CURRENT_PAGE, LAST_ALLPAGE, CURRENT_SNATCH_OPENDATE, url);
                String info = "本次增量记录保存成功。[CURRENT_PAGE:"+CURRENT_PAGE+"]" +
                        "[LAST_ALLPAGE:"+LAST_ALLPAGE+"][CURRENT_SNATCH_OPENDATE:"+CURRENT_SNATCH_OPENDATE+"][url:"+url+"]";
                SnatchLogger.info(info);
            }else{
                String info = "###本次增量记录保存取消。[CURRENT_PAGE:"+CURRENT_PAGE+"]" +
                        "[LAST_ALLPAGE:"+LAST_ALLPAGE+"][CURRENT_SNATCH_OPENDATE:"+CURRENT_SNATCH_OPENDATE+"][url:"+url+"]";
                SnatchLogger.warn(info);
            }

        }else{
            String info ="本次执行有异常跳过的列表页，不保存增量记录。。。";
            SnatchLogger.warn(info);
        }
        clearClassParam();
    }

    /**
     * 判断是否继续抓取公告
     * @param notice
     * @return
     * @throws Exception
     */
    public boolean isContinue(Notice notice) throws Exception{
        boolean notExists = true;
        noticeService.createNoticeByTableName(notice.getSource());
        int count = noticeService.getNoticeCountByOpenDateAndUrl(notice,notice.getSource());
        if(count > 0) {
            notExists = false;
            SnatchLogger.warn("已经存在..."+notice.getTitle() + "---" + notice.getOpendate());
        } else {
            notExists = true;
        }
        return notExists;
    }



    protected boolean standardDetail(String href, Notice notice, String catchType,String... flags)  throws Exception{
        boolean isContinue = isContinue(notice);
        String errMsg = null;//异常说明
        String exRank = null;//异常等级
        if(SnatchUtils.isNotNull(notice.getSyncUrl())){
            href =notice.getSyncUrl();
        }else{
            href=notice.getUrl();
        }

        try {
            Notice noticeDetail = null;
            if(isContinue) {
                if(SnatchUtils.isNull(href)){
                    throw new IOException("公告url为空，无法抓取详情内容！[href:"+href+"]" +
                            "[notice.getUrl():"+notice.getUrl()+"][notice.getSyncUrl():"+notice.getSyncUrl()+"]");
                }

                noticeDetail = detail(href, notice, catchType);

                //防止单页内有跳过规则，新记录抓取时记录公示时间
                if(!isFirstRow && !isLastRow){
                    rememberMaxOpenDate(noticeDetail.getOpendate());
                    remeberMinOpenDate(noticeDetail.getOpendate());
                }

                if (noticeDetail == null || SnatchUtils.isNull(noticeDetail.getTitle())
                        ||  SnatchUtils.isNull(noticeDetail.getContent())
                        || (SnatchUtils.isNull(SnatchUtils.deleteHtmlTag(util.getPlainText(noticeDetail.getContent()))) && !noticeDetail.getContent().contains("img"))) {
                    String warnInfo = "标题、内容获取失败，跳过。[url："+href+"][catchType:"+catchType+"]";
                    throw new Exception(warnInfo);
                }

                //根据标题进行catchtype判断。
                if (SnatchUtils.isNull(noticeDetail.getCatchType())) {
                    noticeDetail = setCatchTypeByTitle(noticeDetail, noticeDetail.getTitle());
                }
                // catchType为空， 进行公告详情以及标题判断并设置其catchType
                if (SnatchUtils.isNull(noticeDetail.getCatchType())) {
                    noticeDetail = parseCatchType(noticeDetail,noticeService);
                    if (SnatchUtils.isNull(noticeDetail.getCatchType())) {
                        String warnInfo = "catchType = null，公告类型判断失败，不进行抓取！[tile:"+noticeDetail.getTitle()+"][url=" + noticeDetail.getUrl() + "]";
                        // catchType为空，不进行此次抓取
                        throw new Exception(warnInfo);
                    }
                }

                catchType = noticeDetail.getCatchType();

                //招标公告：政府采购资质判断
                if (noticeDetail.getType().equals(SnatchContent.CAI_GOU_TYPE)) {
//                    if(catchType.equals(SnatchContent.ZHAO_BIAO_TYPE)) {
//                        //招标公告:标题判断绿色通道判断
//                        if(!isZhaobiaoGreenChannel( noticeDetail.getTitle())){
//                            if (flags != null && flags.length > 0 && flags[0] != null) {
//                                boolean existsAptitude = existsAptitude(noticeDetail,noticeService);
//                                if (!existsAptitude) {
//                                    String warnInfo = "资质不符，跳过！[tile:"+noticeDetail.getTitle()+"][catchType:"+catchType+"][url=" + noticeDetail.getUrl() + "]";
//                                    throw new SnatchFilterException(warnInfo);
//                                }
//                            }
//                        }
//                    }else if(catchType.equals(SnatchContent.OTHER_TYPE)){
//                        String[] regs = {"(登记公告)"};
//                        boolean excludeFlag = SnatchUtils.matchStringArray(regs,noticeDetail.getTitle());
//                        if(excludeFlag){
//                            throw new SnatchFilterException("标题中包含排除关键字，跳过。[tile:"+noticeDetail.getTitle()+"][catchType:"+catchType+"][url=" + noticeDetail.getUrl() + "]");
//                        }
//                    }else if(catchType.equals(SnatchContent.ZHONG_BIAO_TYPE)){
//                        //全部抓取
//                    }
                    SnatchLogger.debug("政府采购类,进行抓取[url："+href+"][catchType:"+catchType+"]");
                }else{//非政府采购类公告
                    SnatchLogger.debug("非政府采购类,进行抓取[url："+href+"][catchType:"+catchType+"]");
                }

                if (noticeDetail != null) {
                    String info ="开始抓取-->title:"+noticeDetail.getTitle() + "###CatchType:"+noticeDetail.getCatchType()+"---openDate:" + noticeDetail.getOpendate()+"$$ href:"+href;
                    SnatchLogger.info(info);
                    String title = noticeDetail.getTitle();
                    long noticeTime = 0;
                    try {
                        if(SnatchUtils.isNotNull(noticeDetail.getOpendate())) {
                            noticeTime = sdf2.parse(noticeDetail.getOpendate()).getTime(); //公告公示时间
                        }
                    }catch (java.text.ParseException e) {
                        SnatchLogger.error("公示时间转换失败",e);
                    }
                    long sysTime = System.currentTimeMillis();   //当前系统时间

                    // 删除title中的[]标签.【】标签
                    if (StringUtils.isNotBlank(title)) {
                        if ((title.charAt(0) == '[' && title.contains("]"))
                                || (title.charAt(title.length()-1) == ']' && title.contains("["))) {
                            title = SnatchUtils.excludeStringByKey(title,"[","]");
                        }
                        if ((title.charAt(0) == '【' && title.contains("】"))
                                || (title.charAt(title.length()-1) == '】' && title.contains("【"))) {
                            title = SnatchUtils.excludeStringByKey(title,"【","】");
                        }
                        noticeDetail.setTitle(title);
                    }

                    // 处理解析pdf后的换行问题
                    if (noticeDetail.getContent().contains("\r\n")) {
                        noticeDetail.setContent("<p>" + noticeDetail.getContent().replaceAll("\r\n", "</p><p>") + "</p>");
                    }

                    // 删除公告详情的script标签
                    noticeDetail.setContent(SnatchUtils.excludeStringByKey(noticeDetail.getContent(),"<script","</script>"));

                    // 将公告详情中的相对路径转换为绝对路径
                    noticeDetail.setContent(SnatchUtils.relative2AbsolutePath(noticeDetail.getContent(),noticeDetail.getUrl()));

                    //存入数据库
                    if (StringUtils.isNotBlank(noticeDetail.getContent()) && StringUtils.isNotBlank(title) && noticeTime <= sysTime) {
                        try {
                            noticeDetail.setContent(replaceTable(noticeDetail.getContent()));
                            noticeService.insertNoticeDate(noticeDetail);
                            Thread.sleep(1000);
                        } catch (JedisConnectionException e) {
                            errMsg = "数据插入失败，reids连接异常";
                            exRank = "2";
                            SnatchLogger.error("[exRank:" + exRank + "]" + errMsg + "\n" ,e);

                            //将网址存入异常表
                            SnatchException exception = new SnatchException();
                            exception.setExRank(exRank);
                            exception.setExName(errMsg);
                            exception.setExUrl(href);
                            exception.setExTime(sdf.format(new Date()));
                            exception.setExClass(this.getClass().getName());//异常所在的类
                            exception.setNoticeTitle(notice.getTitle());//异常信息标题
                            exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
                            exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
                            exception.setProvinceCode(notice.getProvinceCode());//省code
                            exception.setCityCode(notice.getCityCode());//市code
                            exception.setCountyCode(notice.getCountyCode());//县code
                            exception.setType(notice.getType());
                            exception.setSityClassify(notice.getSityClassify());
                            noticeService.insertSnatchException(exception);
                            SnatchLogger.debug("-------------------发生网络异常，开始休眠5S-----------------------------");
                            Thread.sleep(5000);
                            detail(href, notice, catchType);
                        }
                    } else {
                        errMsg = "网站能访问，并且code为200，但是公告没内容";
                        if(noticeTime > sysTime){
                            errMsg = "公示日期大于当前日期";
                        }
                        exRank = "3";
                        SnatchLogger.warn("[exRank:" + exRank + "]" + errMsg);
                        //将网址存入异常表
                        SnatchException exception = new SnatchException();
                        exception.setExRank(exRank);
                        exception.setExName(errMsg);
                        exception.setExUrl(href);
                        exception.setExTime(sdf.format(new Date()));
                        exception.setExClass(this.getClass().getName());//异常所在的类
                        exception.setNoticeTitle(notice.getTitle());//异常信息标题
                        exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
                        exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
                        exception.setProvinceCode(notice.getProvinceCode());//省code
                        exception.setCityCode(notice.getCityCode());//市code
                        exception.setCountyCode(notice.getCountyCode());//县code
                        exception.setType(notice.getType());
                        exception.setSityClassify(notice.getSityClassify());
                        noticeService.insertSnatchException(exception);
                    }
                } else {
                    SnatchLogger.warn("请检查detail方法，没有返回notice对象。[notice:" + noticeDetail + "]");
                }
            }else{//已存在，不做入库处理
                if(isFirstRow || isLastRow) {
                    SnatchLogger.info("被过滤掉或已存在数据--遇到边界记录，获取detail");
                    noticeDetail = detail(href, notice, catchType);
                }
            }

            if(isFirstRow){
                rememberMaxOpenDate(noticeDetail.getOpendate());
            }
            if(isLastRow){
                remeberMinOpenDate(noticeDetail.getOpendate());
            }

        } catch (ConnectException e) {
            Thread.sleep(5 * 1000);
            SnatchLogger.warn("-------------------发生网络异常，休眠后恢复[retryCount:"+retryCount+"]-----------------------------");
            if(retryCount>0) {
                retryCount--;
                standardDetail(href, notice, catchType);
            }else{
                retryCount = 5;  //重试次数重置
                errMsg = "-----超过重试次数---";
                exRank = "5";
                SnatchLogger.warn(errMsg);
                SnatchException exception = new SnatchException();
                exception.setExRank(exRank);
                exception.setExName(errMsg);
                exception.setExUrl(href);
                exception.setExTime(sdf.format(new Date()));
                exception.setExClass(this.getClass().getName());//异常所在的类
                exception.setNoticeTitle(notice.getTitle());//异常信息标题
                exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
                exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
                exception.setProvinceCode(notice.getProvinceCode());//省code
                exception.setCityCode(notice.getCityCode());//市code
                exception.setCountyCode(notice.getCountyCode());//县code
                exception.setType(notice.getType());
                exception.setSityClassify(notice.getSityClassify());
                noticeService.insertSnatchException(exception);
            }
        } catch (FileNotFoundException e) {
            errMsg = "网站能访问，并且code为200，但是公告已被删除";
            exRank = "1";
            SnatchLogger.error("[exRank:" + exRank + "][catchType:"+catchType+"][title:"+notice.getTitle()+"][flags:"+(flags != null? flags[0]:null)+"]异常url:"+href+"\n"+errMsg,e);
            SnatchException exception = new SnatchException();
            exception.setExRank(exRank);
            exception.setExName(errMsg);
            exception.setExUrl(href);
            exception.setExTime(sdf.format(new Date()));
            exception.setExClass(this.getClass().getName());//异常所在的类
            exception.setNoticeTitle(notice.getTitle());//异常信息标题
            exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
            exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
            exception.setProvinceCode(notice.getProvinceCode());//省code
            exception.setCityCode(notice.getCityCode());//市code
            exception.setCountyCode(notice.getCountyCode());//县code
            exception.setType(notice.getType());
            exception.setSityClassify(notice.getSityClassify());
            noticeService.insertSnatchException(exception);
        } catch (JsonParseException e) {
            errMsg = "Json数据解析异常";
            exRank = "0";
            SnatchLogger.warn("[" + notice.getTitle() + "----内容解析失败!]");
            SnatchException exception = new SnatchException();
            exception.setExRank(exRank);
            exception.setExName(errMsg);
            exception.setExUrl(href);
            exception.setExTime(sdf.format(new Date()));
            exception.setExClass(this.getClass().getName());//异常所在的类
            exception.setNoticeTitle(notice.getTitle());//异常信息标题
            exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
            exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
            exception.setProvinceCode(notice.getProvinceCode());//省code
            exception.setCityCode(notice.getCityCode());//市code
            exception.setCountyCode(notice.getCountyCode());//县code
            exception.setType(notice.getType());
            exception.setSityClassify(notice.getSityClassify());
            noticeService.insertSnatchException(exception);
        } catch (IOException e) {
            errMsg = "公告详情获取异常";
            exRank = "0";
            SnatchLogger.error("[exRank:" + exRank + "][catchType:"+catchType+"][title:"+notice.getTitle()+"][flags:"+(flags != null? flags[0]:null)+"]异常url:"+href+"\n"+errMsg+e.getMessage(),e);
            SnatchException exception = new SnatchException();
            exception.setExRank(exRank);
            exception.setExName(errMsg+e.getMessage());
            exception.setExUrl(href);
            exception.setExTime(sdf.format(new Date()));
            exception.setExClass(this.getClass().getName());//异常所在的类
            exception.setNoticeTitle(notice.getTitle());//异常信息标题
            exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
            exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
            exception.setProvinceCode(notice.getProvinceCode());//省code
            exception.setCityCode(notice.getCityCode());//市code
            exception.setCountyCode(notice.getCountyCode());//县code
            exception.setType(notice.getType());
            exception.setSityClassify(notice.getSityClassify());
            noticeService.insertSnatchException(exception);
        }catch (InterruptedException e){
            throw e;
        }catch (SnatchFilterException e){
            exRank = "55";
//            SnatchLogger.warn("[exRank:" + exRank + "][catchType:"+catchType+"][title:"+notice.getTitle()+"][flags:"+(flags != null? flags[0]:null)+"]url:"+href+"\n");
            SnatchLogger.warn(e.getMessage());
            SnatchException exception = new SnatchException();
            exception.setExRank(exRank);
            exception.setExName(e.getMessage());
            exception.setExUrl(href);
            exception.setExTime(sdf.format(new Date()));
            exception.setExClass(this.getClass().getName());//异常所在的类
            exception.setNoticeTitle(notice.getTitle());//异常信息标题
            exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
            exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
            exception.setProvinceCode(notice.getProvinceCode());//省code
            exception.setCityCode(notice.getCityCode());//市code
            exception.setCountyCode(notice.getCountyCode());//县code
            exception.setType(notice.getType());
            exception.setSityClassify(notice.getSityClassify());
            noticeService.insertSnatchException(exception);
        } catch (Exception e){
            errMsg = "公告抓取，其他异常|";
            exRank = "99";
            SnatchLogger.warn("[exRank:" + exRank + "][catchType:"+catchType+"][type:"+notice.getType()+"][title:"+notice.getTitle()+"]异常url:"+href+"\n");
            SnatchLogger.error(errMsg+e.getMessage(),e);
            SnatchException exception = new SnatchException();
            exception.setExRank(exRank);
            exception.setExName(errMsg+e.getMessage());
            exception.setExUrl(href);
            exception.setExTime(sdf.format(new Date()));
            exception.setExClass(this.getClass().getName());//异常所在的类
            exception.setNoticeTitle(notice.getTitle());//异常信息标题
            exception.setNoticeOpendate(notice.getOpendate());//异常信息日期
            exception.setCatchType(notice.getCatchType());//异常信息所属的公告类型
            exception.setProvinceCode(notice.getProvinceCode());//省code
            exception.setCityCode(notice.getCityCode());//市code
            exception.setCountyCode(notice.getCountyCode());//县code
            exception.setType(notice.getType());
            exception.setSityClassify(notice.getSityClassify());
            noticeService.insertSnatchException(exception);
        }finally {
            if(Thread.currentThread().isInterrupted()){
                throw new Exception("检查到线程终止信号，终止线程。");
            }
            return isContinue;
        }

    }

    /**
     * 异常数据重抓
     */
    protected void disposeExpUrl()throws Exception{
        SnatchLogger.info("[重抓异常数据]---start");
        SnatchException se = new SnatchException();
        se.setExClass(this.getClass().getName());
        //查询该类异常数据
        List<SnatchException> list = noticeService.listSnatchExceptionByExClass(se.getExClass());
        int reCount = 0;
        if (list.size() > EX_NOTICE_RE_COUNTY) {
            reCount = EX_NOTICE_RE_COUNTY;
        }else {
            reCount = list.size();
        }
        for (int i = 0; i < reCount; i++) {
            Notice notice = new Notice();
            notice.setProvinceCode(list.get(i).getProvinceCode());//省code
            notice.setCityCode(list.get(i).getCityCode());//市code
            notice.setCountyCode(list.get(i).getCountyCode());//县code
            notice.setCatchType(list.get(i).getCatchType());//公告类型
            notice.setUrl(list.get(i).getExUrl());//url地址
            notice.setTitle(list.get(i).getNoticeTitle());//标题
            notice.setOpendate(list.get(i).getNoticeOpendate());//日期
            notice.setExpFlag(1);//异常数据标记

            noticeService.createNoticeByTableName(notice.getSource());
            int count = noticeService.getNoticeCountByOpenDateAndUrl(notice,notice.getSource());
            if (count < 1) {
                SnatchLogger.info("重抓异常数据-开始---------------"+list.get(i).getExUrl());
                standardDetail(list.get(i).getExUrl(), notice, list.get(i).getCatchType());
                SnatchLogger.info("重抓异常数据-结束---------------"+list.get(i).getExUrl());
            }else{
                //已抓取的就更新数据状态
                noticeService.updateSnatchExceptionStatus(list.get(i).getExUrl());
                SnatchLogger.info("重抓异常数据-已存在---------------"+list.get(i).getExUrl());
            }
        }
        SnatchLogger.info("[重抓异常数据]---end");
    }

    /**
     * 探测url中的内容,防止有些站点用URL.openStream方式获取信息不稳定，
     * 此时需要换Jsoup.connect方式探测站点。
     * @param url
     * @param pageEncoding
     * @param express
     * @return
     * @throws Exception
     */
    protected Document probeHttpDetail(String url, String pageEncoding, String express)throws Exception{
        Document docCount = Jsoup.parse(new URL(url).openStream(),pageEncoding,url);
        Elements targetEls = docCount.select(express);
        if(!targetEls.hasText()){
            SnatchLogger.warn(docCount.hasText()+"[url:"+url+"]Jsoup.parse text.length:\n"+targetEls.outerHtml().length());
            docCount = Jsoup.connect(url).userAgent("Mozilla").timeout(1000 * 60).ignoreHttpErrors(true).get();
            targetEls = docCount.select(express);
            SnatchLogger.warn(docCount.hasText()+"[url:"+url+"]Jsoup.connect text.size:\n"+targetEls.outerHtml().length());
        }
        return docCount;
    }

    /**
     * 统计每个站点抓取统计
     */
    protected void snatchStatistics(TbSnatchStatistics snatchStatistics) {
        int exceptionTotal = noticeService.getSnatchExceptionTotal(snatchStatistics.getSiteName());
        snatchStatistics.setExceptionTotal(exceptionTotal);
        String executeDate = snatchStatistics.getExecuteDate();
        String startCollectionDate = executeDate.substring(executeDate.indexOf("[startDate:") + 11, executeDate.indexOf("]["));
        String siteDomainName = snatchStatistics.getSiteDomainName();

        Map params = new HashMap<String, Object>();
        params.put("tableName", snatchStatistics.getSource());
        params.put("url", siteDomainName);
        params.put("catchDate", startCollectionDate);
        Map<String, Number> result = noticeService.getNoticeTotalBySnatchDate(params);
        snatchStatistics.setNoticeTotal(result.get("noticeTotal").intValue());
        snatchStatistics.setZhaobiaoTotal(result.get("zhaobiaoTotal").intValue());
        snatchStatistics.setZhongbiaoTotal(result.get("zhongbiaoTotal").intValue());
        snatchStatistics.setOtherTotal(result.get("otherTotal").intValue());

        noticeService.insertSnatchStatistics(snatchStatistics);
    }

}
