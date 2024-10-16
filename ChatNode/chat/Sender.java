// define package
package chat;

// imports
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;

/**
 * Sender is processing user input, translates user input into messages
 * and send them to the chat server
 * 
 * @author wolfdieterotte
 */
public class Sender extends Thread implements MessageTypes
{
    // member variables
    Socket connection = null;
    ObjectOutputStream writeToNet;
    Scanner userInput = new Scanner(System.in);
    String inputLine = null;
    ChatNode chatNode = null;

    // Constructor
    public Sender(ChatNode chatNode)
    {
        this.chatNode = chatNode;
        userInput = new Scanner(System.in);
    }

    // implementation interface runnable not needed for threading
    @Override
    public void run()
    {
        Message message;
        
        // until forever, unless the user enters SHUTDOWN or SHUTDOWN_ALL
        while(true)
        {
            // get user input
            inputLine = userInput.nextLine();

            // if the input starts with JOIN
            if(inputLine.startsWith("JOIN"))
            {
                // ignore if we have already joined a chat
                if(chatNode.hasJoined)
                {
                    System.err.println("You have already joined a chat");
                    continue;
                }

                // read server information user provided with join command
                String[] connectivityInfo = inputLine.split("[ ]+");

                // if there is information that may override the connectivity information
                // that was provided through the parameters
                chatNode.setNextNodeInfo(new NodeInfo(connectivityInfo[1], Integer.parseInt(connectivityInfo[2])));

                // check if we have valid server connectivity information
                if(chatNode.getNextNode() == null)
                {
                    System.err.println("[Sender].run No server connectivity informtion provided!");
                    continue;
                }

                // server information was provided, so send join request
                try
                {
                    // create message
                    message = new Message(JOIN, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    // send message
                    send_message(message);
                }
                catch(Exception e)
                {
                    System.err.println("Error connecting to server, opening streams, or closing connection");
                    continue;
                }
            }
            // else if the input starts with LEAVE
            else if(inputLine.startsWith("LEAVE"))
            {
                // ignore if the node isn't joined to a chat
                if(!chatNode.hasJoined)
                {
                    System.err.println("You have not joined a chat...");
                    continue;
                }

                // send leave request
                try
                {
                    // create message
                    message = new Message(LEAVE, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    // send message
                    send_message(message);
                }
                catch(Exception e)
                {
                    System.err.println("Error connecting to the server, opening the streams, or closing the connection");
                    continue;
                }

                // we are out!
                chatNode.hasJoined = false;

                System.out.println("Left chat...");
            }
            // else if the input starts with SHUTDOWN_ALL
            else if(inputLine.startsWith("SHUTDOWN_ALL"))
            {
                // check if we are in the chat
                if(!chatNode.hasJoined)
                {
                    System.err.println("To shutdown the whole chat, you must join it first...");
                    continue;
                }

                // we are a participant, send out a SHUTDOWN_ALL message
                try
                {
                    // create message
                    message = new Message(SHUTDOWN_ALL, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    // send message
                    send_message(message);
                }
                catch(Exception e)
                {
                    System.err.println("Error opening or writing to object streams, or closing connection");
                }

                // NOTE: shutdown is handled in send_message
            }
            // else if the line starts with SHUTDOWN
            else if(inputLine.startsWith("SHUTDOWN"))
            {
                // if the next node isn't me (we aren't the only one in the chat)
                if(!chatNode.getNextNode().equals(chatNode.getMyNodeInfo()))
                {
                    // we are a participant, send out a SHUTDOWN_ALL message
                    try
                    {
                        // create a LEAVE message
                        message = new Message(LEAVE, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                        
                        // send message
                        send_message(message);                    
                    }
                    catch(Exception e)
                    {
                        System.err.println("Error opening or writing to object streams, or closing connection");
                    }
                }

                System.out.println("Exiting...\n");
                System.exit(1);
            }
            // else the person wants to send a note
            else
            {
                // check if the node is joined
                if(!chatNode.hasJoined)
                {
                    System.err.println("You need to join a chat first!");
                    continue;
                }

                // send note
                try
                {
                    // create message
                    message = new Message(NOTE, inputLine, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    // send message
                    send_message(message);
                }
                catch(Exception e)
                {
                    System.err.println("Sending message failed!");
                    continue;
                }
            }
        }
    }


    // sender function that gets called by the 
    void send_message(Message message)
    {
        // variables
        Socket connection;    

        // if next node is not sender
        if(!chatNode.getNextNode().equals(message.getSender()))
        {
            try
            {
                // open connection to server
                connection = new Socket(chatNode.getNextNode().getAddress(), chatNode.getNextNode().getPort());

                // open object streams
                writeToNet = new ObjectOutputStream(connection.getOutputStream());

                // send note
                writeToNet.writeObject(message);

                // close connection
                connection.close();
            }
            catch(Exception e)
            {
                System.err.println("Error connecting to server, opening streams, or closing connection");
            }
        }

        // if the message is SHUTDOWN_ALL, shutdown the node after sending
        if(message.getType() == SHUTDOWN_ALL)
        {
            System.out.println("Sent shutdown all notice...\n");
            
            System.out.println("Exiting...\n");
            System.exit(1);
        }
    }

    // function overload for join case
    void send_message(Message message, NodeInfo sendTo)
    {
        // variables
        Socket connection;

        try
        {
            // open connection to server
            connection = new Socket(sendTo.getAddress(), sendTo.getPort());

            // open object streams
            //readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(connection.getOutputStream());

            // send note
            writeToNet.writeObject(message);

            // close connection
            connection.close();
        }
        catch(Exception e)
        {
            System.err.println("Error connecting to server, opening streams, or closing connection");
        }

        // if the message is SHUTDOWN_ALL, shutdown the node after sending
        if(message.getType() == SHUTDOWN_ALL)
        {
            System.out.println("Sent shutdown all notice...\n");
            
            System.out.println("Exiting...\n");
            System.exit(1);
        }
    }
}
