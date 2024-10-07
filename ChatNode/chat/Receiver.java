package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// included to make server socket work
import java.net.ServerSocket;

import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;

public class Receiver extends Thread
{
    static ServerSocket receiverSocket = null;
    static String userName = null;
    static ChatNode chatNode = null;

    // Constructor
    public Receiver(ChatNode chatNode)
    {
        this.chatNode = chatNode;
        
        try
        {
            //receiverSocket = new ServerSocket(chatNode.getMyNodeInfo().getAddress(), chatNode.getMyNodeInfo().getPort());
            receiverSocket = new ServerSocket(chatNode.getMyNodeInfo().getPort());
            System.out.println("[Receiver.Receiver] receiver socket created, listening on port " + chatNode.getMyNodeInfo().getPort());
        }
        catch(Exception e)
        {
            System.err.println("Failed to create recevier");
        }

        System.out.println(chatNode.getMyNodeInfo().getName() + " listening on " + chatNode.getMyNodeInfo().getAddress() + ":" + chatNode.getMyNodeInfo().getPort());
    }

    // thread entry point
    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                // previously, this wouldn't run until it got a connection...
                // I made receiverSocket a ServerSocket type so that I can run accept()
                // which will return a socket type of the previous connection
                // after someone requests to send a message
                (new ReceiverWorker(chatNode, receiverSocket.accept())).start();
            }
            catch(Exception e)
            {
                System.err.println("[Receiver.run] Warning: Error accepting client");
            }
        }
    }
}
