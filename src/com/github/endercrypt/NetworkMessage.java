package com.github.endercrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NetworkMessage implements Serializable
{
	private static final long serialVersionUID = -6462324586680412542L;
	/**
	 * 
	 */
	private NetworkMessageType type;
	private Object data;

	public NetworkMessage(NetworkMessageType type, Object data)
	{
		this.type = type;
		this.data = data;
	}

	public NetworkMessageType getType()
	{
		return type;
	}

	public Object getData()
	{
		return data;
	}
	
	public int sizeof() throws IOException
	{
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
		
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
		objectOutputStream.close();
		
		return byteOutputStream.toByteArray().length;
	}
}
