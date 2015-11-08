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
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			try
			{
				pingCheck();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void pingCheck() throws IOException
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
			System.err.println("Server timed out!");
			System.exit(-1);
		}
	}

	public void report()
	{
		silenceTimer = 0;
	}

}
