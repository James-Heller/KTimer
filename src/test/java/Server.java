import com.fasterxml.jackson.databind.ObjectMapper;
import space.jamestang.ktimer.core.KTimer;
import space.jamestang.ktimer.message.KTimerMessage;

import java.io.IOException;

public class Server {


    public static void main(String[] args) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        KTimer timer = new KTimer();
        timer.messageDecoder = (msg) -> {
            try {
                return mapper.readValue(msg, KTimerMessage.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        timer.messageEncoder = (msg) -> {
            try {
                return mapper.writeValueAsBytes(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        timer.strat();
    }
}
