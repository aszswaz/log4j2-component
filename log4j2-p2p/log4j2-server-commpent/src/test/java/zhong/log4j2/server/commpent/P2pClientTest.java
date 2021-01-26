package zhong.log4j2.server.commpent;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author aszswaz
 * @date 2021-01-26 星期二 15:06
 */
public class P2pClientTest {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8080);
        InputStream inputStream = socket.getInputStream();
        while (true) {
            int i = inputStream.read();
            if (i == -1) break;
            System.out.print((char) i);
            if (i == '\n') break;
        }
        /*while (true) {
            socket.getOutputStream().write(("ping" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            while (true) {
                int i = inputStream.read();
                if (i == -1) break;
                System.out.print((char) i);
                if (i == '\n') break;
            }
            Thread.sleep(1000);
        }*/
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        while (true) {
            String line = reader.readLine();
            if (Objects.isNull(line)) break;
            System.out.println(line);
        }
    }
}
