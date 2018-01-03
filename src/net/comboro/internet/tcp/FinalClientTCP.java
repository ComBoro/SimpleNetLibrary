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

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != FinalClientTCP.class))
            return false;
        FinalClientTCP other = (FinalClientTCP) obj;
        return client.equals(other.client);
    }
}
