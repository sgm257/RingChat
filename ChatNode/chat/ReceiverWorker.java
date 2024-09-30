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
    Socket serverConnection = null;

    ObjectInputStream readFromNet = null;
    ObjectOutputStream writeToNet = null;

    Message message = null;

    /**
     * Constructor
     * 
     * @param serverConnection
     */
    public ReceiverWorker(Socket serverConnection)
    {
        this.serverConnection = serverConnection;

        // open object streams
        try
        {
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
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
            case SHUTDOWN:
                System.out.println("Received shutdown message from server, exiting");

                try
                {
                    // close server connection
                    serverConnection.close();
                }
                catch(Exception e)
                {
                    System.err.println("Couldn't close connection");
                }

                // exit system
                System.exit(1);

                break;

            case NOTE:
                System.out.println((String) message.getContent());

                try
                {
                    serverConnection.close();
                }
                catch(Exception e)
                {
                    System.err.println("Error closing server connection");
                }

                break;

            default:
                // cannot occur
        }
    }
}
