package chat;

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
    Socket connection = null;
    ObjectOutputStream writeToNet;
    Scanner userInput = new Scanner(System.in);
    String inputLine = null;
    ChatNode chatNode = null;
    //boolean hasJoined;  // flag indicating if we have joined chat

    // Constructor
    public Sender(ChatNode chatNode)
    {
        this.chatNode = chatNode;
        userInput = new Scanner(System.in);
        //hasJoined = false;
    }


    // make a sender function that can take input from either the command line
    // or the reciever worker as a parameter

    /*
    Name: run
    Process: monitors the command line input and takes appropriate action based on the requested command
             JOIN: 
             LEAVE: 
             SHUTDOWN:
             SHUTDOWN_ALL:
             NOTE:
    Function Input/Parameters: 
    Function Output/Parameters: 
    Function Output/Returned: 
    Device Input/device: 
    Device Output/device: 
    Dependencies: 
    */

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
                    message = new Message(JOIN, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    send_message(message);
                }
                catch(Exception e)
                {
                    System.err.println("Error connecting to server, opening streams, or closing connection");
                    continue;
                }

                // we are in!
                chatNode.hasJoined = true;

                System.out.println("Joined chat...");
            }
            else if(inputLine.startsWith("LEAVE"))
            {
                if(!chatNode.hasJoined)
                {
                    System.err.println("You have not joined a chat...");
                    continue;
                }

                // send leave request
                try
                {
                    message = new Message(LEAVE, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
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
            else if(inputLine.startsWith("SHUTDOWN"))
            {
                if(!chatNode.getNextNode().equals(chatNode.getMyNodeInfo()))
                {
                    // we are a participant, send out a SHUTDOWN_ALL message
                    try
                    {
                        message = new Message(LEAVE, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                        
                        send_message(message);                    
                    }
                    catch(Exception e)
                    {
                        System.err.println("Error opening or writing to object streams, or closing connection");
                    }

                    System.out.println("Sent shutdown notice...\n");
                }

                System.out.println("Exiting...\n");
                System.exit(1);
            }
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
                    message = new Message(SHUTDOWN_ALL, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    send_message(message);
                }
                catch(Exception e)
                {
                    System.err.println("Error opening or writing to object streams, or closing connection");
                }

                // NOTE: should handle shutdown in send_message
            }
            else // sending a note
            {
                if(!chatNode.hasJoined)
                {
                    System.err.println("You need to join a chat first!");
                    continue;
                }

                // send note
                try
                {
                    message = new Message(NOTE, chatNode.getMyNodeInfo(), chatNode.getNextNode());
                    
                    send_message(message);

                    System.out.println("Message sent...");
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

        try
        {
            // open connection to server
            connection = new Socket(message.getNextNode().getAddress(), message.getNextNode().getPort());

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

        System.out.println("Message sent...");

        if(message.getType() == SHUTDOWN_ALL)
        {
            System.out.println("Sent shutdown all notice...\n");
            
            System.out.println("Exiting...\n");
            System.exit(1);
        }
    }
}
