package com.github.endercrypt;

public enum NetworkMessageType
{
	DISCONNECT_REQUEST, //client sends to server when it wants to disconnect, server sends to client to tell it, its been disconnected
	PING, // ping object
	NAME_REQUEST, //client sends to server once to inform about its name of choice, server sends to client to ask for a new name
	CHAT_MESSAGE, // normal chat message
	USERLIST, //client gets when it joins, containing a list of names in the chat
	USER_JOINED, // client gets when anyone joins chat
	USER_LEFT, // client gets when anyone leaves chat
	;
}
