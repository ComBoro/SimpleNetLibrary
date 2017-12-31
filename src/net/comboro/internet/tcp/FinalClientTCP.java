package net.comboro.internet.tcp;

import net.comboro.Client.ClientListener;
import net.comboro.SerializableMessage;

public class FinalClientTCP {
	
	private ClientTCP client;

	public FinalClientTCP(ClientTCP client) {
		this.client = client;
	}
	
	public void send(SerializableMessage<?> message){
		client.send(message);
	}
	
	public void addListener(ClientListener listener) {
		client.addListener(listener);
	}

}
