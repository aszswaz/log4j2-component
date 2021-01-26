package zhong.log4j2.server.commpent;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 配置信息
 *
 * @author aszswaz
 * @date 2021-01-26 星期二 18:37
 */
class Config {
    /**
     * 保持连接的口令
     */
    public static final String PING = "ping" + System.lineSeparator();
    /**
     * 客户端建立连接时发送欢迎消息
     */
    public static final String WELCOME = "status: 200" + System.lineSeparator() + "welcome to log4j2 p2p server" + System.lineSeparator();
    /**
     * 日期格式化
     */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
}
