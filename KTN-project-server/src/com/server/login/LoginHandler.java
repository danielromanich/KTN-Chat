package com.server.login;

import java.util.HashMap;

import com.server.core.Server;

public class LoginHandler {
	
	private static HashMap<String, String> userList = new HashMap<>();
	
	static {
		userList.put("Daniel", "test");
	}
	
	private Server server;
	public LoginHandler(Server server) {
		this.server = server;
	}
	
	public boolean validate(String username, String password) {
		return userList.containsKey(username) ? server.getUser(username) == null && password.equals(userList.get(username)) : server.getUser(username) == null;
	}

}
