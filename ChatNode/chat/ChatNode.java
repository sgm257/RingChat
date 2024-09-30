package chat;

import java.io.IOException;
import java.util.Properties;
import utils.NetworkUtilities;

import java.util.logging.Level;
import java.util.logging.Logger;


import utils.PropertyHandler;

/**
 * Chat client class
 * 
 * Reads configuration information and starts up the ChatClient
 * 
 * @author wolfdieterotte
 */
public class ChatNode implements Runnable
{
    // references to receiver/sender
    static Receiver receiver = null;
    static Sender sender = null;

    // this client connectivity information
    public static NodeInfo myNodeInfo = null;
    public static NodeInfo nextNode = null;

    // constructor
    public ChatNode(String propertiesFile)
    {
        // get properties from properties file
        Properties properties = null;
        try
        {
            properties = new PropertyHandler(propertiesFile);
        }
        catch(Exception e)
        {
            System.err.println("Error getting info from properties file");
            System.exit(1);
        }

        // get my reciver port number
        int myPort = 0;
        try
        {
            myPort = Integer.parseInt(properties.getProperty("MY_PORT"));
        }
        catch(Exception e)
        {
            System.err.println("Error parsing port from properties file");
        }

        // get my name
        String myName = properties.getProperty("MY_NAME");
        if(myName == null)
        {
            System.err.println("Could not read my name");
        }

        // create my own node info
        //myNodeInfo = new NodeInfo(NetworkUtilities.getMyIP(), myPort, myName);
        myNodeInfo = new NodeInfo(23.254.166.230, 12345, "Frodo");

        // get server default port
        int serverPort = 0;
        try
        {
            serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        }
        catch(Exception e)
        {
            System.err.println("Could not read server port");
        }

        // get server default IP
        String serverIP = null;
        serverIP = properties.getProperty("SERVER_IP");
        if(serverIP == null)
        {
            System.err.println("Could not read server IP");
        }

        // create server default connectivity information
        // TODO figure out how to do this...
        if(serverPort != 0 && serverIP != null)
        {
            nextNode = new NodeInfo(serverIP, serverPort);
        }
    }

    // code entry point, not used for threading
    @Override
    public void run()
    {
        // start receiver
        (receiver = new Receiver()).start();

        // new start the sender
        (sender = new Sender()).start();
    }

    // main()
    public static void main(String[] args)
    {
        String propertiesFile = null;

        try
        {
            propertiesFile = args[0];
        }
        catch(Exception e)
        {
            propertiesFile = "config/ChatNodeDefaults.properties";
        }

        // start ChatNode
        (new ChatNode(propertiesFile)).run();
    }
}
