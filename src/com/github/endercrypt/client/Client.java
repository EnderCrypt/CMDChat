package com.github.endercrypt.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

import com.github.endercrypt.Main;
import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.NetworkMessageType;

public class Client
{
	private static Socket socket;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;

	private static ClientConnection clientConnection;

	private static Scanner scanner = new Scanner(System.in);

	public static void run()
	{
		System.out.println("Connecting to server server...");
		try
		{
			socket = new Socket(Main.connectTo, Main.port);
			socket.setTcpNoDelay(true);
		}
		catch (ConnectException e)
		{
			System.err.println("Failed to connect to server!");
			System.exit(-1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		// prepare streams
		try
		{
//			oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//			ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// start message sender
		clientConnection = new ClientConnection();
		new Thread(clientConnection).start();
		// start chat
		System.out.println("Welcome to the chat!");
		System.out.print("Please enter a name: ");
		String name = scanner.nextLine();
		try
		{
			Client.send(new NetworkMessage(NetworkMessageType.NAME_SET_REQUEST, name));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		while (true)
		{
			String message = scanner.nextLine();
			try
			{
				if (message.equalsIgnoreCase("/exit"))
				{
					send(new NetworkMessage(NetworkMessageType.DISCONNECT, null));
				}
				else
				{
					Client.send(new NetworkMessage(NetworkMessageType.CHAT_MESSAGE, message));
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static Object read() throws ClassNotFoundException, IOException
	{
		return ois.readObject();
	}

	public static synchronized void send(NetworkMessage msg) throws IOException
	{
		//oos.reset();
		oos.writeObject(msg);
		oos.flush();
	}

	public static void sendPing() throws IOException
	{
		send(new NetworkMessage(NetworkMessageType.PING, null));
	}

	public void close()
	{
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			System.err.println("Failed to properly close connection (this could lead to resource leaks)");
			e.printStackTrace();
		}
	}

}
