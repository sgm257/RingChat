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
    Socket serverConnection = null;
    Scanner userInput = new Scanner(System.in);
    String inputLine = null;
    boolean hasJoined;  // flag indicating if we have joined chat

    // Constructor
    public Sender()
    {
        userInput = new Scanner(System.in);
        hasJoined = false;
    }

    // implementation interface runnable not needed for threading
    @Override
    public void run()
    {
        ObjectOutputStream writeToNet;
        ObjectInputStream readFromNet;

        // until forever, unless the user enters SHUTDOWN or SHUTDOWN_ALL
        while(true)
        {
            // get user input
            inputLine = userInput.nextLine();

            if(inputLine.startsWith("JOIN"))
            {
                // ignore if we have already joined a chat
                if(hasJoined)
                {
                    System.err.println("You have already joined a chat");
                    continue;
                }

                // read server information user provided with join command
                String[] connectivityInfo = inputLine.split("[ ]+");

                // if there is information that may override the connectivity information
                // that was provided through the parameters
                ChatClient.serverNodeInfo = new NodeInfo(connectivityInfo[1], Integer.parseInt(connectivityInfo[2]));

                // check if we have valid server connectivity information
                if(ChatClient.serverNodeInfo == null)
                {
                    System.err.println("[Sender].run No server connectivity informtion provided!");
                    continue;
                }

                // server information was provided, so send join request
                try
                {
                    // open connection to server
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    // open object streams
                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    // send join request
                    writeToNet.writeObject(new Message(JOIN, ChatClient.myNodeInfo));

                    // close connection
                    serverConnection.close();
                }
                catch(Exception e)
                {
                    System.err.println("Error connecting to server, opening streams, or closing connection");
                    continue;
                }

                // we are in!
                hasJoined = true;

                System.out.println("Joined chat...");
            }
            else if(inputLine.startsWith("LEAVE"))
            {
                if(!hasJoined)
                {
                    System.err.println("You have not joined a chat...");
                    continue;
                }

                // send leave request
                try
                {
                    // open connection to server
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    // open object streams
                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    // send join request                    
                    writeToNet.writeObject(new Message(LEAVE, ChatClient.myNodeInfo));

                    // close connection
                    serverConnection.close();
                }
                catch(Exception e)
                {
                    System.err.println("Error connecting to the server, opening the streams, or closing the connection");
                    continue;
                }

                // we are out!
                hasJoined = false;

                System.out.println("Left chat...");
            }
            else if(inputLine.startsWith("SHUTDOWN_ALL"))
            {
                // check if we are in the chat
                if(!hasJoined)
                {
                    System.err.println("To shutdown the whole chat, you must join it first...");
                    continue;
                }

                // we are a participant, send out a SHUTDOWN_ALL message
                try
                {
                    // open connection to server
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    // open object streams
                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    // send shutdown all request
                    writeToNet.writeObject(new Message(SHUTDOWN_ALL, ChatClient.myNodeInfo));

                    // close connection
                    serverConnection.close();
                }
                catch(Exception e)
                {
                    System.err.println("Error opening or writing to object streams, or closing connection");
                }

                System.out.println("Sent shutdown all request...\n");
            }
            else if(inputLine.startsWith("SHUTDOWN"))
            {
                // if we are a participant, leave the chat first
                if(hasJoined)
                {
                    // send leave request
                    try
                    {
                        // open connection to server
                        serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                        // open object streams
                        readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                        writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                        // send leave request
                        writeToNet.writeObject(new Message(SHUTDOWN, ChatClient.myNodeInfo));

                        // close connection
                        serverConnection.close();

                        System.out.println("Left chat...");
                    }
                    catch(Exception e)
                    {
                        System.err.println("Error opening or writing to object streams, or closing connection");
                    }
                }

                System.out.println("Exiting...\n");
                System.exit(1);
            }
            else // sending a note
            {
                if(!hasJoined)
                {
                    System.err.println("You need to join a chat first!");
                    continue;
                }

                // send note
                try
                {
                    // open connection to server
                    serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

                    // open object streams
                    readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                    writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

                    // send note
                    writeToNet.writeObject(new Message(NOTE, ChatClient.myNodeInfo.getName() + ": " + inputLine));

                    // close connection
                    serverConnection.close();

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
}
