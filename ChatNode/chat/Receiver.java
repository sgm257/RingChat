// define package
package chat;

// imports
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.net.ServerSocket;

import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;

// class definition
public class Receiver extends Thread
{
    // member variables
    static ServerSocket receiverSocket = null;
    static String userName = null;
    static ChatNode chatNode = null;

    // Constructor
    public Receiver(ChatNode chatNode)
    {
        // set chat node
        this.chatNode = chatNode;
        
        try
        {
            // create receiver socket
            receiverSocket = new ServerSocket(chatNode.getMyNodeInfo().getPort());
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
                // start the receiver worker
                (new ReceiverWorker(chatNode, receiverSocket.accept())).start();
            }
            catch(Exception e)
            {
                System.err.println("[Receiver.run] Warning: Error accepting client");
            }
        }
    }
}
