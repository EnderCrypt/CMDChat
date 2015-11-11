package com.github.endercrypt.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.github.endercrypt.Main;
import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.NetworkMessageType;
import com.github.endercrypt.exception.ClientIllegalAction;

public class Server
{
	private static ServerPinger serverPinger;
	private static ServerSocket serverSocket;
	public static final Set<ServerConnection> connections = Collections.synchronizedSet(new HashSet<>());
	public static final int CONNECTION_TIMEOUT = 10; // in seconds (when a connection gets timed out)
	
	@SuppressWarnings({ "resource", "unused" })
	public static void run() throws IOException
	{
		// start listener
		Main.println(System.out, "Starting server...");
		try
		{
			serverSocket = new ServerSocket(Main.port);
		}
		catch (BindException e)
		{
			Main.println(System.err, "The port seems busy (" + Main.port + "), server broke!");
			System.exit(-1);
		}
		// start overwatcher
		Main.println(System.out, "Starting server ping service...");
		serverPinger = new ServerPinger(connections);
		new Thread(serverPinger).start();
		// listen for connections
		Main.println(System.out, "(Server online!) Listening for connections...");
		while (true)
		{
			ServerConnection svc = null;
			Socket socket = null;
			try
			{
				socket = serverSocket.accept();
				svc = new ServerConnection(socket);
			}
			catch (IOException e)
			{
				if (svc == null)
				{
					Main.println(System.err, "Received broken connection attempt from "+socket.getInetAddress().getHostAddress()+"!");
					continue;
				}
				connectionError(svc, e);
				continue;
			}
			new Thread(svc).start();
			connections.add(svc);
		}
	}

	public static void connectionError(ServerConnection svc, Exception e)
	{
		Main.println(System.err, "Connection error occured for: " + svc);
		//e.printStackTrace();
		disconnectConnection(svc);
	}

	public static void connectionTimeout(ServerConnection svc, TimeoutException e)
	{
		Main.println(System.err, "Connection timed out! ("+svc+")");
		disconnectConnection(svc);
	}

	public static void illegalAction(ServerConnection svc, ClientIllegalAction e)
	{
		Main.println(System.err, "Warning: " + svc + " performed an illegal action: " + e.getMessage());
		disconnectConnection(svc);
	}

	public static void disconnectConnection(ServerConnection svc)
	{
		Main.println(System.out, "Connection " + svc + " was disconnected");
		if (svc.getName() != null)
		{
			Server.broadcast(new NetworkMessage(NetworkMessageType.USER_LEFT, svc.getName()));
		}
		svc.close();
	}

	public static void broadcast(NetworkMessage msg)
	{
		Iterator<ServerConnection> svcItr = connections.iterator();
		while (svcItr.hasNext())
		{
			ServerConnection svc = svcItr.next();
			try
			{
				svc.send(msg);
			}
			catch (IOException e)
			{
				connectionError(svc, e);
				svcItr.remove();
			}
		}
	}

	public static String[] getUserNames()
	{
		Set<String> users = new HashSet<>();
		for (ServerConnection svc : connections)
		{
			String name = svc.getName();
			if (name != null)
			{
				users.add(name);
			}
		}
		return users.toArray(new String[users.size()]);
	}

}
