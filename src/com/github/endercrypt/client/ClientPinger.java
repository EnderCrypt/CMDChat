package com.github.endercrypt.client;

import java.io.IOException;

public class ClientPinger implements Runnable
{
	public static final int CHECK_FREQUENCY = 20; // x/second
	public static final int TIMEOUT = 10; // seconds

	private int silenceTimer = 0;
	private int pingTimer = 0;

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1000 / CHECK_FREQUENCY);
				if ((Client.isConnected()) && (pingCheck() == false))
				{
					return;
				}
			}
			catch (IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean pingCheck() throws IOException
	{
		silenceTimer++;
		pingTimer++;
		if ((pingTimer / (double) CHECK_FREQUENCY) == TIMEOUT * 0.5)
		{
			pingTimer = 0;
			Client.sendPing();
		}
		if (silenceTimer / CHECK_FREQUENCY == TIMEOUT)
		{
			Client.frame.addMessage("Server timed out! please reconnect");
			Client.breakConnection();
			return false;
		}
		return true;
	}

	public void report()
	{
		silenceTimer = 0;
	}

}
