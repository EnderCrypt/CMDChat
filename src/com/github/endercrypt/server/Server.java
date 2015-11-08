package com.github.endercrypt.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.github.endercrypt.Main;
import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.exception.ClientIllegalAction;

public class Server
{
	private static ServerPinger serverPinger;
	private static ServerSocket serverSocket;
	public static final Set<ServerConnection> connections = Collections.synchronizedSet(new HashSet<>());
	public static final int CONNECTION_TIMEOUT = 10; // in seconds (when a connection gets timed out)

	public static void run() throws IOException
	{
		// start listener
		System.out.println("Starting server...");
		try
		{
			serverSocket = new ServerSocket(Main.port);
		}
		catch (BindException e)
		{
			System.err.println("The port seems busy (" + Main.port + "), server broke!");
			System.exit(-1);
		}
		// start overwatcher
		System.out.println("Starting server pinger...");
		serverPinger = new ServerPinger(connections);
		new Thread(serverPinger).start();
		// listen for connections
		System.out.println("(Server online!) Listening for connections...");
		while (true)
		{
			ServerConnection svc = null;
			try
			{
				svc = new ServerConnection(serverSocket.accept());
			}
			catch (IOException e)
			{
				connectionError(svc, e);
			}
			new Thread(svc).start();
			connections.add(svc);
		}
	}

	public static void connectionError(ServerConnection svc, Exception e)
	{
		System.err.println("Connection error occured for: " + svc);
		//e.printStackTrace();
		disconnectConnection(svc);
	}

	public static void connectionTimeout(ServerConnection svc, TimeoutException e)
	{
		System.out.print("Connection timed out! ");
		disconnectConnection(svc);
	}

	public static void illegalAction(ServerConnection svc, ClientIllegalAction e)
	{
		System.out.println("Warning: " + svc + " performed an illegal action: " + e.getMessage());
		disconnectConnection(svc);
	}

	public static void disconnectConnection(ServerConnection svc)
	{
		System.out.println("Connection " + svc + " was disconnected");
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

}
