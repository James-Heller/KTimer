import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KTimerMessage {
    String clientId;
    MessageType type;
    String taskId;
    KTimerTaskContext context;
}
