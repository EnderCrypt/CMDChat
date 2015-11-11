package com.github.endercrypt.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import com.github.endercrypt.Main;
import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.NetworkMessageType;
import com.github.endercrypt.client.gui.Frame;

public class Client
{
	private static Socket socket;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static ClientConnection clientConnection;
	public static final Frame frame = new Frame();
	public static boolean hasSetName = false;
	private static boolean connectionReady = false;

	public static void run()
	{
		frame.addMessage("Using port: " + Main.port);
		frame.addMessage("Connecting to server server...");
		try
		{
			socket = new Socket(Main.connectTo, Main.port);
			socket.setTcpNoDelay(true);
			// prepare streams
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			connectionReady();
		}
		catch (ConnectException e)
		{
			frame.addMessage("Failed to connect to server!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			frame.addMessage("Error occured!");
		}
		if (connectionReady)
		{
			// start message sender
			clientConnection = new ClientConnection();
			new Thread(clientConnection).start();
			// start chat
			frame.addMessage("Welcome to the chat!");
			frame.addMessage("Please enter a name");
		}
	}

	public static Object read() throws ClassNotFoundException, IOException
	{
		return ois.readObject();
	}
	
	private static void connectionReady()
	{
		connectionReady = true;
		frame.enableInput(true);
	}
	
	public static void breakConnection()
	{
		connectionReady = false;
		frame.enableInput(false);
	}
	
	public static boolean isConnected()
	{
		return connectionReady;
	}

	public static synchronized void send(NetworkMessage msg) throws IOException
	{
		if (connectionReady)
		{
			oos.writeObject(msg);
			oos.flush();
		}
		else
		{
			Client.frame.addMessage("Connection offline/not ready!");
		}
	}

	public static void sendPing() throws IOException
	{
		send(new NetworkMessage(NetworkMessageType.PING, null));
	}
	
	public static void sendDisconnect() throws IOException
	{
		send(new NetworkMessage(NetworkMessageType.DISCONNECT_REQUEST, null));
	}
	
	public static void sendChatMessage(String message) throws IOException
	{
		Client.send(new NetworkMessage(NetworkMessageType.CHAT_MESSAGE, message));
	}

	public void close()
	{
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			frame.addMessage("Failed to properly close connection (this could lead to resource leaks)");
			e.printStackTrace();
		}
	}

}
