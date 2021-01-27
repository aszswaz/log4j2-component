package zhong.log4j2.p2p.windows.client.log.message;

import org.apache.commons.lang3.StringUtils;

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
     * 添加一条消息到容器
     */
    public static void addLogMessage(LogMessageEntity messageEntity) {
        MESSAGE_ENTITIES.add(messageEntity);
    }

    /**
     * 清理消息
     */
    public static void clear() {
        MESSAGE_ENTITIES.clear();
    }

    /**
     * 输出所有消息
     */
    public static String getAllMessageByString() {
        StringBuilder builder = new StringBuilder();
        for (int i = MESSAGE_ENTITIES.size() - 1; i >= 0; i--) {
            LogMessageEntity messageEntity = MESSAGE_ENTITIES.get(i);
            if (StringUtils.isNoneBlank(messageEntity.getSummary())) {
                builder.append(messageEntity.getSummary()).append(System.lineSeparator());
            }
            if (StringUtils.isNoneBlank(messageEntity.getBody())) {
                builder.append(messageEntity.getBody()).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
