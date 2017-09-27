package com.client.message;

public enum MessageType {
	
	MESSAGE, HELP, USER_UPDATE, ACCESS_DENIED, HISTORY, PRIVATE_MESSAGE, CONNECTED;
	
	@Override
	public String toString() {
		return name();
	}
	
	public static MessageType getType(String name) {
		for (MessageType type : MessageType.values())
			if (type.name().toLowerCase().equals(name.toLowerCase()))
				return type;
		return null;
	}

}
