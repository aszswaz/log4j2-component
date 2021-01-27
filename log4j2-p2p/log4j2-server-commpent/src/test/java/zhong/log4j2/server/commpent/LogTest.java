package zhong.log4j2.server.commpent;

import lombok.extern.log4j.Log4j2;

/**
 * @author aszswaz
 * @date 2021-01-26 星期二 19:11
 */
@Log4j2
public class LogTest {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
            Thread.sleep(10000);
            log.error("{}", "测试", new Exception("测试"));
        }
    }
}
