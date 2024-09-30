package message;

import java.io.Serializable;
import chat.NodeInfo;

/**
 * Class [Message] Defines a generic Message that has a message type and content.
 * Instances of this class can be sent over a network, using object streams.
 * Message types are defined in MessageTypes
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Message implements MessageTypes, Serializable {
    
    // type of message, types are defined in interface MessageTypes
    int type;

    // content of a note
    String content;

    // sending ip info
    NodeInfo sender;

    // next node ip info
    NodeInfo nextNode;

    
    // constructor
    public Message(int type, String content, NodeInfo sender, NodeInfo nextNode) {
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.nextNode = nextNode;
    }
    
    // getters
    public int getType() 
    {
        return type;
    }

    public String getContent()
    {
        return content;
    }

    public NodeInfo getSender()
    {
        return sender;
    }

    public NodeInfo getNextNode()
    {
        return nextNode;
    }
}
