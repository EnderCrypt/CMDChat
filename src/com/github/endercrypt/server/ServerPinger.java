package com.github.endercrypt.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class ServerPinger implements Runnable
{
	public static final int CHECK_FREQUENCY = 20; // x/second
	public static final int TIMEOUT = 10; // seconds

	private final Set<ServerConnection> connections;

	public ServerPinger(Set<ServerConnection> connections)
	{
		this.connections = connections;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1000 / CHECK_FREQUENCY);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			Iterator<ServerConnection> svcItr = connections.iterator();
			while (svcItr.hasNext())
			{
				ServerConnection svc = svcItr.next();
				try
				{
					svc.pingCheck();
				}
				catch (IOException e)
				{
					Server.connectionError(svc, e);
					Server.connections.remove(svc);
				}
				catch (TimeoutException e)
				{
					Server.connectionTimeout(svc, e);
					Server.connections.remove(svc);
					break;
				}
			}
		}
	}

}
