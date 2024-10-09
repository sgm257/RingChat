package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import static message.MessageTypes.NOTE;
import static message.MessageTypes.LEAVE;
import static message.MessageTypes.JOIN;
import static message.MessageTypes.SHUTDOWN;
import static message.MessageTypes.SHUTDOWN_ALL;


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

        sender = new Sender(chatNode);

    }

    // thread code entry point
    @Override
    public void run()
    {
        System.err.println("[RecieverWorker].run in function");
        
        try
        {
            // read message
            message = (Message)readFromNet.readObject();

            System.err.println("[RecieverWorker].run read object");
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
                System.out.println("Received join message from " + message.getSender().getName() + ", processing");

                chatNode.hasJoined = true;

                String approved = new String("approved");

                System.out.println("Content: " + message.getContent());

                if(approved.equals((String)message.getContent()))
                {
                    System.out.println("Join approved! Closing ring...");

                    System.out.println("OG Next node: " + chatNode.getNextNode().getName());

                    // set next node to sender, closing the ring
                    chatNode.setNextNodeInfo(message.getNextNode());

                    System.out.println("New Next node: " + chatNode.getNextNode().getName());

                    // we are in!
                    chatNode.hasJoined = true;

                    System.out.println("Joined chat...");
                }
                //else if(!chatNode.getNextNode().equals(message.getSender()))
                else
                {
                    // call some function to set up new next node connection?

                    Message mess = new Message(JOIN, "approved", chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    sender.send_message(mess, message.getSender());

                    chatNode.setNextNodeInfo(message.getSender());

                    System.out.println("Next node is: " + chatNode.getNextNode().getName());
                }

                break;

            case NOTE:
                System.out.println("Received note message from " + message.getSender().getName() + ", processing");

                System.out.println("Next node is: " + chatNode.getNextNode().getAddress());

                System.out.println(message.getSender().getName() + ": " + (String) message.getContent());
                
                if(!chatNode.getNextNode().equals(message.getSender()))
                {
                    sender.send_message(message);
                }
                
                break;

            case LEAVE:
                System.out.println("Received leave message from " + message.getSender().getName() + ", processing");

                if(!chatNode.getNextNode().equals(message.getSender()))
                {
                    sender.send_message(message);
                }
                else
                {
                    chatNode.setNextNodeInfo(message.getNextNode());
                }

                break;

            case SHUTDOWN_ALL:
                System.out.println("Received shutdown all message from " + message.getSender().getName() + ", processing");
                
                if(!chatNode.getNextNode().equals(message.getSender()))
                {
                    sender.send_message(message);
                }
                else
                {
                    System.out.println("Exiting...\n");
                    System.exit(1);
                }

                break;            

            default:
                // cannot occur
        }
    }
}
