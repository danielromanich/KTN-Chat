package com.server.core;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import org.json.simple.JSONObject;

public class ClientConnection extends Thread implements Runnable {
	
	private Socket socket;
	private String username, password;
	private Server server;
	private boolean isRunning = true;
	
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ClientConnection(Server server, Socket socket, ObjectOutputStream out, ObjectInputStream in, String username, String password) {
		this.server = server;
		this.socket = socket;
		this.username = username;
		this.password = password;
		this.in = in;
		this.out = out;
	}
	
	@Override
	public void run() {
		while (isRunning()) {
			try {
				JSONObject message = (JSONObject) in.readObject();
				String request = (String) message.get("request");
				switch (request) {
					case "logout":
						quit();
						break;
					case "msg":
						System.out.println(message);
						server.messageAll(this, (String) message.get("content"));
						server.addMessage(server.getMessageCreator().createMessage("timestamp", server.getTimestamp(), "sender", username, "response", "message", "content", (String) message.get("content")));
						break;
					case "pm":
						server.getUser((String) message.get("user")).sendMessage(server.getMessageCreator().createMessage("timestamp", server.getTimestamp(), "response", "private_message", "sender", username, "content", (String) message.get("content")));
						break;
					case "history":
						sendMessage(server.getMessageCreator().createOldMessages(server.getTimestamp(), server.getOldMessages()));
						break;
					case "help":
						sendMessage(server.getMessageCreator().createMessage("timestamp", server.getTimestamp(), "sender", "Server", "response", "help", "content", "Skriv og trykk enter for å sende en melding... <br>For å sende en PM dobbeltrykk på brukeren sitt navn."));
						break;
				}
			} catch (ClassNotFoundException | IOException e) {
				quit();
			}
		}
		System.out.println("The user: "+username+" has been disconnected");
		server.removeClient(this);
		server.updateUser(this, getUsername(), true);
		try {
			socket.close();
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(JSONObject message) {
		try {
			out.writeObject(message);
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public void quit() {
		this.isRunning = false;
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
