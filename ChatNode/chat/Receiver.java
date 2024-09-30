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

public class Receiver extends Thread
{
    static Socket receiverSocket = null;
    static String userName = null;

    // Constructor
    public Receiver()
    {
        try
        {
            receiverSocket = new Socket(ChatClient.myNodeInfo.getPort());
            System.out.println("[Receiver.Receiver] receiver socket created, listening on port " + ChatClient.myNodeInfo.getPort());
        }
        catch(Exception e)
        {
            System.err.println("Failed to create recevier");
        }

        System.out.println(ChatClient.myNodeInfo.getName() + " listening on " + ChatClient.myNodeInfo.getAddress() + ":" + ChatClient.myNodeInfo.getPort());
    }

    // thread entry point
    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                (new ReceiverWorker(receiverSocket.accept())).start();
            }
            catch(Exception e)
            {
                System.err.println("[Receiver.run] Warning: Error accepting client");
            }
        }
    }
}
