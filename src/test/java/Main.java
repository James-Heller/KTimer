import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.jamestang.ktimer.message.ClientMetadata;
import space.jamestang.ktimer.message.KTimerMessage;
import space.jamestang.ktimer.message.MessageBuilder;
import space.jamestang.ktimer.message.enums.MessageType;
import space.jamestang.ktimer.message.enums.TimerPriority;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger("Main"); // 将 loggger 声明为 final

    public static void main(String[] args) throws IOException {

        var mapper = new ObjectMapper();
        var kotlinModule = new KotlinModule.Builder().build();
        mapper.registerModule(kotlinModule);

        try (var socket = new Socket("localhost", 8080); // 使用 try-with-resources 确保资源关闭
             var dis = new DataInputStream(socket.getInputStream());
             var dos = new DataOutputStream(socket.getOutputStream())) {

            var metadata = new ClientMetadata("TestClient", "localhost", 100, "1.0");
            var data = MessageBuilder.INSTANCE.createClientRegister("TestClient", "001", "order", "1.0", metadata);

            var bytes = mapper.writeValueAsBytes(data);
            dos.writeInt(bytes.length);
            dos.write(bytes);
            dos.flush();

            var length = dis.readInt();
            var responseBytes = new byte[length];
            dis.readFully(responseBytes);
            var response = mapper.readValue(responseBytes, KTimerMessage.class);

            if (response.getType() == MessageType.ACK){
                logger.info("注册成功: {}", response.getData());
            }

            // 发送一个心跳包
            var heartbeat = MessageBuilder.INSTANCE.createHeartbeat("TestClient", 0, 0, 0, null);
            var heartbeatBytes = mapper.writeValueAsBytes(heartbeat);
            dos.writeInt(heartbeatBytes.length);
            dos.write(heartbeatBytes);
            dos.flush();
            // 读取心跳包响应
            length = dis.readInt();
            var heartbeatResponseBytes = new byte[length];
            dis.readFully(heartbeatResponseBytes);
            var heartbeatResponse = mapper.readValue(heartbeatResponseBytes, KTimerMessage.class);
            if (heartbeatResponse.getType() == MessageType.ACK) {
                logger.info("心跳包响应成功: {}", heartbeatResponse.getData());
            } else {
                logger.error("心跳包响应失败: {}", heartbeatResponse.getData());
            }

            //定时器注册
            var timerData = MessageBuilder.INSTANCE.createTimerRegister("TestClient", "order-01", 30000, "order canceled", TimerPriority.HIGH, null);
            var timerBytes = mapper.writeValueAsBytes(timerData);
            dos.writeInt(timerBytes.length);
            dos.write(timerBytes);
            dos.flush();
            // 读取定时器注册响应
            length = dis.readInt();
            var timerResponseBytes = new byte[length];
            dis.readFully(timerResponseBytes);
            var timerResponse = mapper.readValue(timerResponseBytes, KTimerMessage.class);

            logger.info(timerResponse.getData().toString());


            length = dis.readInt();
            var callback = new byte[length];
            dis.readFully(callback);
            var callback_data = mapper.readValue(callback, KTimerMessage.class);
            logger.info(callback_data.getData().toString());
        }
    }
}
