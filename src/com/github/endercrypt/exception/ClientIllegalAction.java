package com.github.endercrypt.exception;

@SuppressWarnings("serial")
public class ClientIllegalAction extends RuntimeException
{

	public ClientIllegalAction()
	{
		
	}

	public ClientIllegalAction(String message)
	{
		super(message);
	}

	public ClientIllegalAction(Throwable cause)
	{
		super(cause);
	}

	public ClientIllegalAction(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ClientIllegalAction(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
