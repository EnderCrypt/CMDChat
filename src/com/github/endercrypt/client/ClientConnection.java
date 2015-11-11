package com.github.endercrypt.client;

import java.io.EOFException;
import java.io.IOException;

import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.exception.ClientIllegalAction;

public class ClientConnection implements Runnable
{
	static ClientPinger clientPinger;
	
	public ClientConnection()
	{
		clientPinger = new ClientPinger();
		new Thread(clientPinger).start();
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				process((NetworkMessage) Client.read());
			}
			catch (EOFException e)
			{
				continue;
			}
			catch (ClassNotFoundException | IOException e)
			{
				Client.frame.addMessage("Severe error occured! (IOE) please restart");
			}
		}
	}

	private static void process(NetworkMessage msg) throws IOException
	{
		clientPinger.report();
		switch (msg.getType())
		{
		case DISCONNECT_REQUEST:
			if (msg.getData() == null)
			{// disconnect me!
				//Client.frame.addMessage("You where disconnected by the server!");
				//Thread.currentThread().interrupt();
			}
			else
			{
				throw new RuntimeException("Received illegal message from server! ");
			}
		break;
		case PING:
			if (msg.getData() != null)
			{
				throw new RuntimeException("Received illegal message from server! ");
			}
		break;
		case NAME_REQUEST:
			if ((boolean) msg.getData())
			{
				Client.hasSetName = true;
				Client.frame.addMessage("Name accepted!");
			}
			else
			{
				Client.frame.addMessage("Name declined, Please enter a new name");
			}
		break;
		case CHAT_MESSAGE:
			if (Client.hasSetName)
			{
				Client.frame.addMessage((String) msg.getData());
			}
		break;
		case USERLIST:
			Client.frame.setUsers((String[])msg.getData());
		break;
		case USER_JOINED:
			if (Client.hasSetName)
			{
				Client.frame.joinUser((String)msg.getData());
			}
		break;
		case USER_LEFT:
			if (Client.hasSetName)
			{
				Client.frame.leaveUser((String)msg.getData());
			}
		break;
		default:
			throw new ClientIllegalAction("sent unknown data type");
		}
	}

}
