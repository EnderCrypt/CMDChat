package com.github.endercrypt.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.NetworkMessageType;
import com.github.endercrypt.exception.ClientIllegalAction;

public class ServerConnection implements Runnable
{
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private volatile int pingTimer = 0;

	private String name = null;

	public ServerConnection(Socket socket) throws IOException
	{
		this.socket = socket;
		socket.setTcpNoDelay(true);
		System.out.println("Receiving connection from " + getHostAddress() + "...");
//		oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//		ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				process((NetworkMessage) ois.readObject());
			}
			catch (SocketException e)
			{
				return;
			}
			catch (IOException | ClassNotFoundException | ClientIllegalAction e)
			{
				Server.connectionError(this, e);
				Server.connections.remove(this);
				return;
			}
		}
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

	@Override
	public String toString()
	{
		String nameString = " (no name)";
		if (name != null)
		{
			nameString = ": \"" + name + "\"";
		}
		return getHostAddress() + nameString;
	}

	public String getHostAddress()
	{
		return socket.getInetAddress().getHostAddress();
	}

	public synchronized void send(NetworkMessage msg) throws IOException
	{
		//oos.reset();
		oos.writeObject(msg);
		oos.flush();
	}

	public void sendPing()
	{
		try
		{
			send(new NetworkMessage(NetworkMessageType.PING, null));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void pingCheck() throws IOException, TimeoutException
	{
		pingTimer++;
//		if (pingTimer > 100)
//		System.out.println(this+": "+pingTimer);
		if (pingTimer / ServerPinger.CHECK_FREQUENCY == ServerPinger.TIMEOUT)
		{
			throw new TimeoutException();
		}
	}

	private void process(NetworkMessage msg) throws IOException
	{
		pingTimer = 0;
		switch (msg.getType())
		{
		case DISCONNECT:
			if (msg.getData() != null)
			{
				throw new ClientIllegalAction("Sent disallowed object through DISCONNECT");
			}
			send(new NetworkMessage(NetworkMessageType.DISCONNECT, null));
			Server.disconnectConnection(this);
			Server.connections.remove(this);
			break;
		case PING:
			if (msg.getData() != null)
			{
				throw new ClientIllegalAction("Sent disallowed object through PING");
			}
			sendPing();
			break;
		case NAME_SET_REQUEST:
			if (name == null)
			{
				name = (String) msg.getData();
				System.out.println(getHostAddress() + " joined (" + name + ")");
				Server.broadcast(new NetworkMessage(NetworkMessageType.CHAT_MESSAGE, name + " Joined the chat!"));
			}
			else
			{
				throw new ClientIllegalAction("tried to set name twice");
			}
			break;
		case CHAT_MESSAGE:
			if (name == null)
			{
				throw new ClientIllegalAction("sent a chat message whitout setting a username");
			}
			Server.broadcast(new NetworkMessage(NetworkMessageType.CHAT_MESSAGE, name + ": " + msg.getData())); // send same message to everyone
			break;
		default:
			throw new ClientIllegalAction("sent unknown data type");
		}
	}
}
