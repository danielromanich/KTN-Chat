package com.client.message;

import org.json.simple.JSONObject;

import com.client.core.Client;

public class MessageHandler {
	
	private Client client;
	public MessageHandler(Client client) {
		this.client = client;
	}
	
	public void handleMessage(JSONObject message) {
		System.out.println((String) message.get("response"));
		MessageType type = MessageType.getType((String) message.get("response"));
		String timestamp = (String) message.get("timestamp");
		switch (type) {
			case MESSAGE:
				client.getUI().pushMessage("[" + timestamp + ", " + message.get("sender") + "]: " + message.get("content"));
				break;
			case USER_UPDATE:
				String[] data = ((String) message.get("content")).split(",");
				if (Boolean.parseBoolean(data[1]))
					client.removeUser(data[0]);//
				else
					client.addUser(data[0]);
				client.getUI().updateUsers();
				break;
			case ACCESS_DENIED:
				client.getUI().pushMessage("");
				client.getUI().pushMessage("[" + message.get("timestamp") + ", " + message.get("sender") + "]: " + message.get("content"), true);
				client.quit();
				break;
			case PRIVATE_MESSAGE:
				client.getUI().pushMessage("    [PM from " + message.get("sender") + "]: " + message.get("content"));
				break;
			case HISTORY:
				JSONObject[] obj = (JSONObject[]) message.get("content");
				if(obj != null && obj.length > 0) {
					for (JSONObject o : obj) {
						handleMessage(o);
					}
				}
				break;
			case HELP:
				client.getUI().pushMessage((String) message.get("content")); 
				break;
			case CONNECTED:
				client.sendMessage("request", "history", "content", "None");
				break;
		}
	}

}
