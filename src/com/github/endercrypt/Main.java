package com.github.endercrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import com.github.endercrypt.client.Client;
import com.github.endercrypt.server.Server;

public final class Main
{
	public static boolean runAsServer = false;
	public static int port = 8080;

	public static InetAddress connectTo;

	public static void main(String[] args) throws IOException
	{
		parameters(args);
		System.out.println("Using port: " + port);
		if (runAsServer)
		{
			Server.run();
		}
		else
		{
			if (connectTo != null)
			{
				Client.run();
			}
			else
			{
				System.err.println("No IP specified (use -client <IP>)");
				System.exit(-1);
			}
		}
		System.out.println("The application has reached an (unexpected) end");
	}

	public static void parameters(String[] args)
	{
		// load all args
		List<String> list = Arrays.asList(args);
		Iterator<String> itr = list.iterator();
		while (itr.hasNext())
		{
			String input = itr.next().toLowerCase();
			switch (input)
			{
			case "port": // change port
			case "-port":
			case "--port":
				String portString = itr.next();
				try
				{
					port = Integer.parseInt(portString);
				}
				catch (NumberFormatException e)
				{
					System.err.println("Port number not a number! Please restart");
					System.exit(-1);
				}
				break;
			case "client": // run as a client
			case "-client":
			case "--client":
				try
				{
					if (itr.hasNext())
						connectTo = InetAddress.getByName(itr.next());
					else
						connectTo = InetAddress.getByName(JOptionPane.showInputDialog("Choose target IP"));
				}
				catch (UnknownHostException e)
				{
					System.err.println("Unkown host! Please restart");
					System.exit(-1);
				}
				break;
			case "server": // run as a server not client
			case "-server":
			case "--server":
				runAsServer = true;
				break;
			case "-h": // get help
			case "--h":
			case "-help":
			case "--help":
			default:
				System.out.println("-h help");
				System.out.println("	lists all the help\n");
				System.exit(0);
				break;
			}
		}
	}
}
