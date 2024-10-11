// define package
package chat;

// import
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
    // member variables
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

        // open object stream
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
            // if a JOIN message is received
            case JOIN:
                // notify user
                System.out.println("Received join message from " + message.getSender().getName() + ", processing");

                // set own status to joined
                chatNode.hasJoined = true;

                // set string for testing
                String approved = new String("approved");

                // if string matches content, it means it is receiving an approval response
                if(approved.equals((String)message.getContent()))
                {
                    System.out.println("Join approved! Closing ring...");

                    // set next node to sender, closing the ring
                    chatNode.setNextNodeInfo(message.getNextNode());

                    // we are in!
                    chatNode.hasJoined = true;

                    System.out.println("Joined chat...");
                }
                // else we are receiving a request to join
                else
                {
                    // create message
                    Message mess = new Message(JOIN, "approved", chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    // send message
                    sender.send_message(mess, message.getSender());

                    // set the next node
                    chatNode.setNextNodeInfo(message.getSender());
                }

                break;

            // if a NOTE message is received
            case NOTE:
                // display the message
                System.out.println(message.getSender().getName() + ": " + (String) message.getContent());
                
                // if the next node is not the original sender
                if(!chatNode.getNextNode().equals(message.getSender()))
                {
                    // forward the message along the ring
                    sender.send_message(message);
                }
                
                break;

            // if a LEAVE message is received
            case LEAVE:
                // notify the user someone else has left the chat
                System.out.println(message.getSender().getName() + " has left the chat");
                
                // if the next node is not the original sender
                if(!chatNode.getNextNode().equals(message.getSender()))
                {                    
                    // forward the message along the ring
                    sender.send_message(message);
                }
                // else, close the ring
                else
                {
                    //set our next node to the sender's original next node
                    // this effectively removes the sender from the ring
                    chatNode.setNextNodeInfo(message.getNextNode());
                }

                break;

            // if a SHUTDOWN_ALL message is received
            case SHUTDOWN_ALL:

                // if the next node is not the original sender
                if(!chatNode.getNextNode().equals(message.getSender()))
                {
                    // forward message along the ring
                    // note: the send message function will handling shutting down
                    // the node after forwarding the message
                    sender.send_message(message);
                }
                // else, the message doesn't need to be forwarded
                // and you can simply shutdown the node
                else
                {
                    System.out.println("Exiting...\n");
                    System.exit(1);
                }

                break;            
        }
    }
}
