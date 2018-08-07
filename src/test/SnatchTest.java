import com.silita.china.jilin.JiLinGongGongzyjyzx_webmagic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import us.codecraft.webmagic.Spider;

@ContextConfiguration(locations = {"classpath*:spring-test.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SnatchTest {

    @Autowired
    private JiLinGongGongzyjyzx_webmagic jiLinGongGongzyjyzx;

    @Test
    public void testSnatch() throws InterruptedException {
        String[] urls = {
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001001/1.html"
                /*"http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001003/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005001/005001004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002005/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002007/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005002/005002006/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005001/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005002/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005004/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005005/1.html",
                "http://www.ggzyzx.jl.gov.cn/jyxx/005005/005005006/1.html"*/
        };
        Spider.create(jiLinGongGongzyjyzx).addUrl(urls).thread(1).start();
        Thread.sleep(6000 * 1000);
    }


//    public static void main(String[] args) throws Exception {
////        String url = "http://bbs.tianya.cn/hotArticle.jsp";
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring-test.xml");
//        JiLinGongGongzyjyzx jiLinGongGongzyjyzx = applicationContext.getBean(JiLinGongGongzyjyzx.class);
//        System.out.println(jiLinGongGongzyjyzx);
//        jiLinGongGongzyjyzx.run();
//        Thread.sleep(1000);
//    }


//    public static void main(String[] args) {
//        String url = "http://bbs.tianya.cn/hotArticle.jsp";
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring-test.xml");
//        JiLinGongGongzyjyzx jiLinGongGongzyjyzx = applicationContext.getBean(JiLinGongGongzyjyzx.class);
//        System.out.println(jiLinGongGongzyjyzx);
//        Spider.create(jiLinGongGongzyjyzx).addUrl(url).thread(100).start();
//    }


//    public static void main(String[] args) {
//        String url = "http://bbs.tianya.cn/hotArticle.jsp";
//        JiLinGongGongzyjyzx jiLinGongGongzyjyzx = new JiLinGongGongzyjyzx();
//        System.out.println(jiLinGongGongzyjyzx);
//        Spider.create(jiLinGongGongzyjyzx).addUrl(url).thread(100).start();
//    }
}
