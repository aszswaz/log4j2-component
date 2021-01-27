package zhong.log4j2.server.commpent;


/**
 * @author aszswaz
 * @date 2021-01-26 星期二 14:57
 */
public class P2pServerTest {
    public static void main(String[] args) {
        P2pServer.start(8080, "test");
    }
}