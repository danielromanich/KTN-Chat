package com.server.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONObject;

import com.server.login.LoginHandler;
import com.server.message.MessageCreator;

public class Server extends Thread implements Runnable {
	
	private ArrayList<ClientConnection> clientConnections = new ArrayList<>();
	private boolean isRunning = true;
	private ServerSocket serverSocket;
	private MessageCreator messageCreator;
	private LoginHandler loginHandler;
	
	private ArrayList<JSONObject> messages = new ArrayList<JSONObject>();
	
	public Server(int port) {
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Starting server ("+port+")");
		this.messageCreator = new MessageCreator();
		this.loginHandler = new LoginHandler(this);
		this.start();
	}

	@Override
	public void run() {
		while (isRunning()) {
			try {
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				JSONObject message = (JSONObject) in.readObject();
				String[] details = ((String)message.get("content")).split(",");
				ClientConnection c = new ClientConnection(this, clientSocket, out, in, details[0], details[1]);
				if (loginHandler.validate(details[0], details[1])) {
					clientConnections.add(c);
					c.start();
					System.out.println("Received a connection from: "+details[0]);
					for (ClientConnection connection : getClientConnections()) {
						if (connection != null)
							c.sendMessage(getMessageCreator().createMessage("timestamp", getTimestamp(), "sender", "Server", "response", "user_update", "content", connection.getUsername()+","+false));
					}
					updateUser(c, c.getUsername(), false);
					c.sendMessage(getMessageCreator().createMessage("timestamp", getTimestamp(), "response", "connected", "sender", "Server", "content", "Successfully connected"));
				} else {
					c.sendMessage(getMessageCreator().createMessage("timestamp",  getTimestamp(), "response", "access_denied", "sender", "Server", "content", "Access denied: Invalid username or password."));
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (ClientConnection c : clientConnections) {
			c.quit();
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Disconnecting");
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public synchronized void messageAll(ClientConnection source, String message) {
		for (ClientConnection c : clientConnections) {
			if (c != null && !c.equals(source)) {
				c.sendMessage(getMessageCreator().createMessage("timestamp", getTimestamp(), "sender", source.getUsername(), "response", "message", "content", message));
			}
		}
	}
	
	public synchronized void updateUser(ClientConnection source, String name, boolean disconnected) {
		for (ClientConnection c : clientConnections) {
			if (c != null && !c.equals(source))
				c.sendMessage(getMessageCreator().createMessage("timestamp", getTimestamp(), "sender", "Server", "response", "user_update", "content", name+","+disconnected));
		}
	}
	
	public void removeClient(ClientConnection c) {
		if (clientConnections.contains(c))
			clientConnections.remove(c);
	}
	
	public ClientConnection getUser(String username) {
		for (ClientConnection c : clientConnections)
			if (c.getUsername().equals(username))
				return c;
		return null;
	}

	public MessageCreator getMessageCreator() {
		return messageCreator;
	}
	
	public JSONObject[] getOldMessages() {
		return messages.isEmpty() ? new JSONObject[]{} : messages.toArray(new JSONObject[messages.size()]);
	}
	
	public void addMessage(JSONObject message) {
		messages.add(message);
	}
	
	public String getTimestamp() {
		return new Timestamp(new Date().getTime()).toString().substring(0, 19);
	}
	
	public ArrayList<ClientConnection> getClientConnections() {
		return this.clientConnections;
	}
	
	public static void main(String...args) {
		new Server(5000);
	}

}
