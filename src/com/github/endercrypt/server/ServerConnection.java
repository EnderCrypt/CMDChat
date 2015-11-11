package com.github.endercrypt.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import com.github.endercrypt.Main;
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
		Main.println(System.out, "Receiving connection from " + getHostAddress() + "...");
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
				return;//TODO: questionable
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
			Main.println(System.err, "Failed to properly close connection (this could lead to resource leaks)");
			e.printStackTrace();
		}
	}
	
	public String getName()
	{
		return name;
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
		case DISCONNECT_REQUEST:
			if (msg.getData() != null)
			{
				throw new ClientIllegalAction("Sent disallowed object through DISCONNECT");
			}
			send(new NetworkMessage(NetworkMessageType.DISCONNECT_REQUEST, null));
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
		case NAME_REQUEST:
			if (name == null)
			{
				String getName = (String) msg.getData();
				for (ServerConnection svc : Server.connections)
				{
					String name = svc.getName();
					if ((name != null) && (name.equalsIgnoreCase(getName)))
					{
						send(new NetworkMessage(NetworkMessageType.NAME_REQUEST, false));
						return;
					}
				}
				send(new NetworkMessage(NetworkMessageType.USERLIST, Server.getUserNames()));
				name = getName;
				send(new NetworkMessage(NetworkMessageType.NAME_REQUEST, true));
				Main.println(System.out, getHostAddress() + " joined (" + name + ")");
				Server.broadcast(new NetworkMessage(NetworkMessageType.USER_JOINED, name));
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
			if (((String)msg.getData()).length() > 1)
			Server.broadcast(new NetworkMessage(NetworkMessageType.CHAT_MESSAGE, name + ": " + msg.getData())); // send same message to everyone
		break;
		case USER_JOINED:
			throw new ClientIllegalAction("sent a USER_JOINED (not allowed)");
		case USER_LEFT:
			throw new ClientIllegalAction("sent a USER_LEFT (not allowed)");
		default:
			throw new ClientIllegalAction("sent unknown data type");
		}
	}
}
