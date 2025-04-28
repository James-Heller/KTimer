import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KTimerMessage {
    MessageType type;
    String taskId;
    Object context;
}
