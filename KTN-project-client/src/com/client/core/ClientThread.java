package com.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.json.simple.JSONObject;

public class ClientThread extends Thread implements Runnable {
	
	private ObjectInputStream in;
	private Client client;
	
	public ClientThread(Client client, ObjectInputStream in) {
		this.client = client;
		this.in = in;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				JSONObject message = (JSONObject) in.readObject();
				client.getMessageHandler().handleMessage(message);
			} catch (ClassNotFoundException | IOException e) {
				client.quit();
				break;
			}
		}
	}

}
