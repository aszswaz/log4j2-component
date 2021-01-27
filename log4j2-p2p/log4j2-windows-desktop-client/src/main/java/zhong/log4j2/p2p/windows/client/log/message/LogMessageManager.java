package zhong.log4j2.p2p.windows.client.log.message;

import java.util.List;
import java.util.Vector;

/**
 * log信息管理器
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 09:52
 */
public class LogMessageManager {
    /**
     * log信息容器
     */
    private static final List<LogMessageEntity> MESSAGE_ENTITIES = new Vector<>();

    /**
     * 获取最新的log信息
     */
    public static LogMessageEntity getRecentMessage() {
        return MESSAGE_ENTITIES.get(MESSAGE_ENTITIES.size() - 1);
    }

    /**
     * 添加一条消息到容器
     */
    public static void addLogMessage(LogMessageEntity messageEntity) {
        MESSAGE_ENTITIES.add(messageEntity);
    }
}
