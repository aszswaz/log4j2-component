package zhong.log4j2.server.commpent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * p2p服务端
 *
 * @author aszswaz
 * @date 2021-01-26 星期二 10:58
 */
@Log4j2
@Plugin(name = "P2pServerAppender", category = "Core", elementType = "appender", printObject = true)
public class P2pServerAppender extends AbstractAppender {
    private final String project;
    private final P2pServer p2pServer;
    private static final List<P2pServer> servers = new ArrayList<>();

    protected P2pServerAppender(
            String name, String project, Filter filter,
            Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties,
            P2pServer p2pServer
    ) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.project = project;
        this.p2pServer = p2pServer;
    }

    @Override
    public void append(LogEvent event) {
        // 不发送来自同包的日志, 防止出现递归调用
        if (event.getLoggerName().contains(P2pServerAppender.class.getPackage().getName()) && !event.getLoggerName().contains("Test")) {
            return;
        }
        // 群发消息
        String header = "project: " + this.project + "\r\n"
                + "level: " + event.getLevel().name() + "\r\n"
                + "time: " + Config.DATE_FORMAT.format(event.getTimeMillis()) + "\r\n"
                + "logger: " + event.getLoggerName() + "\r\n\r\n";
        this.p2pServer.sendGroup(header, super.getLayout().toByteArray(event));
    }

    /**
     * 构造一个组件
     */
    @SuppressWarnings("unused")
    @NotNull
    @PluginFactory
    public static P2pServerAppender creatAppender(
            @PluginAttribute(value = "name") String name,
            @PluginAttribute(value = "project") String project,
            @PluginAttribute(value = "port") String port,
            @PluginElement("PatternLayout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter
    ) {
        if (StringUtils.isBlank(name)) throw new P2pServerException("Attribute name cannot be empty or null");
        if (StringUtils.isBlank(project)) throw new P2pServerException("Attribute project cannot be empty or null");
        if (!StringUtils.isNumeric(port))
            throw new P2pServerException("Attribute port cannot be empty or null. Attribute port is number");
        if (Objects.isNull(layout)) {
            layout = PatternLayout.createDefaultLayout();
        }
        // 启动服务
        P2pServer p2pServer = P2pServer.start(Integer.parseInt(port));
        servers.add(p2pServer);
        return new P2pServerAppender(name, project, filter, layout, false, Property.EMPTY_ARRAY, p2pServer);
    }

    public static void close() {
        servers.forEach(P2pServer::close);
    }
}
