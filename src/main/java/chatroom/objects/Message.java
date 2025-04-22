package chatroom.objects;

import chatroom.server.ClientConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

// Represents the Message object that is being used in the server
public class Message {
    public final String content;
    public final String sentTime;
    public final ClientConnection sender;
    public final ArrayList<ClientConnection> receivers;

    public Message (String content, ClientConnection sender) {
        this.content = content.trim();
        this.sentTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        this.sender = sender;
        this.receivers = new ArrayList<>();
    }
}