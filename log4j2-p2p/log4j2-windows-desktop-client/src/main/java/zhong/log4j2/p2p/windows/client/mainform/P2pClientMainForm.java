package zhong.log4j2.p2p.windows.client.mainform;

import zhong.log4j2.p2p.windows.client.log.message.LogMessageManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 客户端主窗口
 *
 * @author aszswaz
 * @date 2021-01-27 星期三 19:58
 */
public class P2pClientMainForm extends JFrame {
    private static JTextArea jTextArea;

    public P2pClientMainForm(String title) throws HeadlessException {
        super(title);
    }

    /**
     * 创建主窗口
     */
    public static void createMainForm() {
        P2pClientMainForm p2pClientMainForm = new P2pClientMainForm("log4j2-p2p-client");
        p2pClientMainForm.setLayout(null);// 去除绝对布局
        p2pClientMainForm.setSize(1400, 1000);
        p2pClientMainForm.setLocationRelativeTo(null);// 设置窗口居中
        p2pClientMainForm.setVisible(true);// 显示窗口

        jTextArea = new JTextArea();
        jTextArea.setText(LogMessageManager.getAllMessageByString());
        jTextArea.setLayout(null);
        jTextArea.setBackground(new Color(243, 236, 236, 250));
        // jTextArea.setEditable(false);
        jTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(jTextArea);

        Insets insets = p2pClientMainForm.getInsets();
        scrollPane.setBounds(10, 10, p2pClientMainForm.getWidth() - insets.right - 30, p2pClientMainForm.getHeight() - insets.top - 30);
        p2pClientMainForm.add(scrollPane);

        p2pClientMainForm.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        p2pClientMainForm.setResizable(false);// 设置不能使用鼠标改变窗口大小
    }

    public static void clear() {
        if (Objects.nonNull(jTextArea)) jTextArea.setText(null);// 清理文本
    }
}
