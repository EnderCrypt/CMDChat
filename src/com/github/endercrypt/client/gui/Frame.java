package com.github.endercrypt.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.endercrypt.Main;
import com.github.endercrypt.NetworkMessage;
import com.github.endercrypt.NetworkMessageType;
import com.github.endercrypt.client.Client;

@SuppressWarnings("serial")
public class Frame extends JFrame
{
	
	private JTextArea textArea;
	private JTextField textField;
	
	private int lines = 0;
	private int characters = 0;
	
	private DefaultListModel<String> userList = new DefaultListModel<>();

	public Frame()
	{
		super();
		setTitle("Chat: "+Main.connectTo.getHostAddress());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(750, 500));
		prepareLayout();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void prepareLayout()
	{
		// layout
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		// create text area
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		// create userlist label
		JLabel label = new JLabel("     Connected users     ", JLabel.CENTER);
		
		// create userlist
		JList<String> jList = new JList<>(userList);
		jList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		jList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				jList.clearSelection();
			}
		});
		jList.setLayoutOrientation(JList.VERTICAL);
		jList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		// create quit button
		JButton quitButton = new JButton("Disconnect");
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Client.sendDisconnect();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				quitButton.setEnabled(false);
				Timer t = new Timer();
				t.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						System.exit(0);
					}
				}, 2000);
			}
		});
		
		// create userlist panel
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.PAGE_START);
		panel.add(jList, BorderLayout.CENTER);
		panel.add(quitButton, BorderLayout.PAGE_END);
		contentPane.add(panel, BorderLayout.LINE_END);
		
		// create text input
		textField = new JTextField();
		textField.setEditable(false);
		textField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Client.frame.sendChatMessage();
			}
		});
		contentPane.add(textField, BorderLayout.PAGE_END);
	}
	
	public void enableInput(boolean enable)
	{
		textField.setEditable(enable);
	}
	
	public void setUsers(String[] names)
	{
		userList.clear();
		for (String name : names)
		{
			userList.addElement(name);
		}
	}
	
	public void joinUser(String name)
	{
		Client.frame.addMessage("<"+name+"> Joined the server");
		userList.addElement(name);
	}
	
	public void leaveUser(String name)
	{
		Client.frame.addMessage("<"+name+"> Disconnected from the server");
		userList.removeElement(name);
	}
	
	
	public void sendChatMessage()
	{
		String message = textField.getText();
		textField.setText("");
		try
		{
			if (Client.hasSetName == false)
			{
				Client.send(new NetworkMessage(NetworkMessageType.NAME_REQUEST, message));
				return;
			}
			if (message.equalsIgnoreCase("/exit"))
			{
				Client.sendDisconnect();
				return;
			}
			Client.sendChatMessage(message);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void addMessage(String message)
	{
		String out = message;
		if (lines > 0)
		{
			out = '\n'+out;
		}
		characters += out.length();
		textArea.append(out);
		textArea.select(characters,characters);
		
		lines++;
	}

}
