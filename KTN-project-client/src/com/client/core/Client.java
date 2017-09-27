package com.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.client.core.user.User;
import com.client.message.MessageCreator;
import com.client.message.MessageHandler;
import com.client.ui.ClientUI;

public class Client {
	
	private ArrayList<String> users = new ArrayList<String>();
	
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ClientThread clientThread;
	private String ip;
	private int port;
	
	private MessageCreator messageCreator;
	private MessageHandler messageHandler;

	private User user;
	
	private ClientUI ui;
	
	public Client(ClientUI ui, String username, String password, String ip, int port) {
		this.ui = ui;
		this.ip = ip;
		this.port = port;
		this.messageCreator = new MessageCreator();
		this.user = new User(username, password);
		this.messageHandler = new MessageHandler(this);
	}
	
	public MessageHandler getMessageHandler() {
		return this.messageHandler;
	}
	
	public MessageCreator getMessageCreator() {
		return this.messageCreator;
	}
	
	public void sendMessage(String...message) {
		try {
			out.writeObject(getMessageCreator().createMessage(message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendLoginRequest(String username, String password) {
		try {
			out.writeObject(getMessageCreator().createMessage("request", "login", "content", (user.getUsername()+","+(user.getPassword().equals("") ? "none" : user.getPassword()))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean connect(String ip, int port) {
		if (this.socket == null || !this.socket.isConnected()) {
			try {
				this.socket = new Socket(ip, port);
			} catch (IOException e) {
				ui.pushMessage("Connecting to server failed.", true);
				return false;
			}
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			sendLoginRequest(user.getPassword(), user.getPassword());
			clientThread = new ClientThread(this, in);
			clientThread.start();
			getUI().pushMessage("Connected to server.", true);
			return true;
		}
		return false;
	}
	
	public void quit() {
		try {
			if (this.socket != null) {
				this.socket.close();
				this.in.close();
				this.out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		users.clear();
		ui.updateUsers();
		ui.setClient(null);
	}
	
	public ClientUI getUI() {
		return this.ui;
	}
	
	public String getIP() {
		return ip;
	}

	public int getPort() {
		return port;
	}
	
	public String[] getUsers() {
		return this.users.toArray(new String[users.size()]);
	}
	
	public void removeUser(String name) {
		if (users.contains(name))
			users.remove(name);
	}
	
	public void addUser(String name) {
		if (!users.contains(name))
			users.add(name);
	}
	
	public String getTimestamp() {
		return new Timestamp(new Date().getTime()).toString().substring(0, 19);
	}

}
