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
				//System.err.println("Lost connection to server!");
				//System.exit(-1);
			}
			catch (ClassNotFoundException | IOException e)
			{
				System.err.println("Severe error occured! (IOE)");
				System.exit(-1);
			}
		}
	}

	private static void process(NetworkMessage msg) throws IOException
	{
		clientPinger.report();
		switch (msg.getType())
		{
		case DISCONNECT:
			if (msg.getData() == null)
			{// disconnect me!
				System.out.println("You where disconnected by the server!");
				System.exit(0);
			}
			else
			{// someone else disconnected
				System.out.println('>'+String.valueOf(msg.getData())+" Disconnected from the server");
			}
		break;
		case PING:
			// do nothing
		break;
		case NAME_SET_REQUEST:
			throw new RuntimeException("Received illegal message from server! ");
		case CHAT_MESSAGE:
			System.out.println(">" + msg.getData());
			break;
		default:
			throw new ClientIllegalAction("sent unknown data type");
		}
	}

}
