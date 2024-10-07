package chat;

import java.io.IOException;
import java.util.Properties;

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
    private static NodeInfo myNodeInfo = null;
    private static NodeInfo nextNode = null;

    // constructor
    public ChatNode(String propertiesFile)
    {
        // get properties from properties file
        Properties properties = null;
        String myIP;
        int myPort = 0;
        String myName;

        try
        {
            // parse properties file
            properties = new PropertyHandler(propertiesFile);
        }
        catch(Exception e)
        {
            System.err.println("Error getting info from properties file");
            System.exit(1);
        }

        // get my IP
        myIP = properties.getProperty("MY_IP");

        // get my port
        myPort = Integer.parseInt(properties.getProperty("MY_PORT"));

        // get my name
        myName = properties.getProperty("MY_NAME");
        
        // create my own node info
        myNodeInfo = new NodeInfo(myIP, myPort, myName);
    }

    // getters
    NodeInfo getMyNodeInfo()
    {
        return myNodeInfo;
    }

    NodeInfo getNextNode()
    {
        return nextNode;
    }

    // setters
    void setMyNodeInfo(NodeInfo info)
    {
        this.myNodeInfo = info;
    }

    void setNextNodeInfo(NodeInfo info)
    {
        this.nextNode = info;
    }

    // code entry point, not used for threading
    @Override
    public void run()
    {
        // start receiver
        (receiver = new Receiver(this)).start();

        // new start the sender
        (sender = new Sender(this)).start();
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
            propertiesFile = "config/base.properties";
        }

        // start ChatNode
        (new ChatNode(propertiesFile)).run();
    }
}
