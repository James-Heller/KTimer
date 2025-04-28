import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Client {

    private static DataInputStream input;
    private static DataOutputStream output;
    private static Socket socket;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try {
            socket = new Socket("localhost", 4396) ;
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

           String clientId = doRegistry();
           if (clientId != null) {
               log.info(clientId);
           }


            sendMessage(clientId);

            while (true){
                int size = input.readInt();
                byte[] bytes = new byte[size];
                input.readFully(bytes);
                KTimerMessage payload = mapper.readValue(bytes, KTimerMessage.class);

                log.info(payload.toString());
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            input.close();
            output.close();
            socket.close();
        }
    }

    public static String doRegistry(){
        KTimerMessage registerMsg = new KTimerMessage(null, MessageType.CLIENT_REGISTER, "L", null);
        try {
            byte[] registryBytes = mapper.writeValueAsBytes(registerMsg);
            output.writeInt(registryBytes.length);
            output.write(registryBytes);
            output.flush();


            int responseLength = input.readInt();
            byte[] responseBytes = new byte[responseLength];
            input.readFully(responseBytes);
            KTimerMessage response = mapper.readValue(responseBytes, KTimerMessage.class);
            return response.getClientId();

        }catch (IOException e){
            e.printStackTrace();
        }


        return null;
    }

    public static void sendMessage(String clientId) {
        KTimerMessage msg = new KTimerMessage(
                clientId,
            MessageType.SCHEDULE_TASK,
            "task-123",
           new KTimerTaskContext(1L,"HELLO")
        );
        try {
            byte[] bytes = mapper.writeValueAsBytes(msg);
            output.writeInt(bytes.length);
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
