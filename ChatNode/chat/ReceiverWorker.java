package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import static message.MessageTypes.NOTE;
import static message.MessageTypes.SHUTDOWN;

/**
 * A thread processing a connection with the chat server
 * 
 * @author wolfdieterotte
 */
public class ReceiverWorker extends Thread
{
    Socket previousNodeConnection = null;
    ObjectInputStream readFromNet = null;
    Message message = null;
    Sender sender = null;
    ChatNode chatNode = null;

    /**
     * Constructor
     * 
     * @param serverConnection
     */
    public ReceiverWorker(ChatNode chatNode, Socket previousNodeConnection)
    {
        this.previousNodeConnection = previousNodeConnection;
        this.chatNode = chatNode;

        // open object streams
        try
        {
            readFromNet = new ObjectInputStream(previousNodeConnection.getInputStream());
        }
        catch(Exception e)
        {
            System.err.println("[ReceiverWorker.ReceiverWorker] Error opening input and output streams");
        }

    }

    // thread code entry point
    @Override
    public void run()
    {
        try
        {
            // read message
            message = (Message)readFromNet.readObject();
        }
        catch(Exception e)
        {
            System.err.println("[RecieverWorker].run Error reading message from input stream");

            // no use getting going
            System.exit(1);
        }

        // decide what to do depending on the type of message received
        switch(message.getType())
        {
            case JOIN:
                System.out.println("Received join message from %s, processing", message.getSender().getName());

                String test = new String("approved");

                if(test.equals(message.getContent()))
                {
                    System.out.println("Join approved! Closing ring...");

                    // set next node to sender, closing the ring
                    chatNode.setNextNodeInfo(message.getSender());
                }
                else if(!chatNode.nextNode.equals(message.getSender()))
                {
                    Message message = new Message(JOIN_APPROVED, myNodeInfo, chatNode.nextNode);

                    chatNode.setNextNodeInfo(message.getSender());

                    // call some function to set up new next node connection?
                    
                    sender.send_message(message);
                }

                break;

            case NOTE:
                System.out.println("Received note message from %s, processing", message.getSender().getName());

                System.out.println((String) message.getContent());
                
                if(!chatNode.nextNode.equals(message.getSender()))
                {
                    sender.send_message(message);
                }
                
                break;

            case LEAVE:
                System.out.println("Received leave message from %s, processing", message.getSender().getName());

                if(!chatNode.nextNode.equals(message.getSender()))
                {
                    sender.send_message(message);
                }
                else
                {
                    chatNode.setNextNodeInfo(message.getNextNode());
                }

                break;

            case SHUTDOWN_ALL:
                if(!chatNode.nextNode.equals(message.getSender()))
                {
                    sender.send_message(message);
                }

                break;            

            default:
                // cannot occur
        }
    }
}
