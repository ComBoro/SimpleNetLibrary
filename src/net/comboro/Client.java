package net.comboro;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import net.comboro.encryption.aes.AES;
import net.comboro.encryption.aes.AESInformation;
import net.comboro.encryption.rsa.RSA;
import net.comboro.encryption.rsa.RSAInformation;
import net.comboro.encryption.rsa.RSASecureMessage;
import net.comboro.encryption.rsa.RSASecurePeer;

abstract public class Client implements RSASecurePeer{

	private RSA rsa = null;
	private AES aes = null;
	public RSAInformation rsaInformation = null;
	
    protected RSAInformation serverRSA = null;
    
    protected boolean trouble = false, serverSide;
    protected Exception lastException = null;

    public interface ClientListener {

        class ClientAdapter implements ClientListener{

            @Override public void onReceive(SerializableMessage<?> message) {}

            @Override public void onDisconnect() {}

            @Override public void onConnect() {}
            
            @Override public void onConnectionError(@Nullable IOException io) {}
        }

        void onReceive(SerializableMessage<?> message);
        void onConnect();
        void onDisconnect();
        void onConnectionError(@Nullable IOException io);
    }
    
    public Client() {
    	this(false);
    }
    
    public Client(boolean serverSide) {
    	this.serverSide = serverSide;
    	if(!serverSide) {
    		rsa = new RSA();
    		setRSAInfomation(rsa.getInformation());
    	}
    }

    protected List<ClientListener> listeners = new ArrayList<>();

    public boolean addListener(ClientListener listener){
        return listeners.add(listener);
    }

    public boolean removeListener(ClientListener listener){
        return listeners.remove(listener);
    }

    public void fireDisconnectEvent(){
        listeners.forEach(e -> e.onDisconnect());
    }

    public void fireConnectEvent(){
        listeners.forEach(e -> e.onConnect());
    }
    
    public void fireConnectionError(@Nullable IOException io){
    	 listeners.forEach(e -> e.onConnectionError(io));
    }

    public void fireReceiveEvent(@NotNull SerializableMessage<?> message) {
            	
    	if(!serverSide) {
    		// Decrypt
    		
        	if(message instanceof RSASecureMessage) {
        		RSASecureMessage sm = (RSASecureMessage) message;
        		message = Client.this.decrypt(sm);
        	}
    		
    		if(message.getData() instanceof RSAInformation) {
    			System.out.println("Client : Server RSA Information received");
        		serverRSA = (RSAInformation) message.getData();
        		RSAInformation clientRSA = ((RSASecurePeer) this).getRSAInformation();
        		        		
        		SerializableMessage<RSAInformation> respond = new SerializableMessage<>(clientRSA);
        		send(respond);
        		System.out.println("Client : Client RSA Send");
        		return;
    		} else if (message.getData() instanceof AESInformation) {
    			String key = ((AESInformation) message.getData()).getKey();
    			System.out.println("Received AES key : " + key);
    			aes = new AES(key);
    			return;
    		}
    	}
    	
    	for(ClientListener cl : listeners) {
    		cl.onReceive(message);
    	}    	
    }
    
    public boolean hasTrouble() {
    	return new Boolean(trouble);
    }
    
    public Exception getLastException() {
    	return new Exception(lastException);
    }
    
    @Override
    public SerializableMessage<?> decrypt(RSASecureMessage message) {
    	BigInteger en = message.getNumber();
    	BigInteger de = rsa.decrypt(en);
    	de = de.negate();
    	byte[] data = de.toByteArray();
    	return Serializer.deserialize(data);
    }
    
    @Override
    public RSAInformation getRSAInformation() {
    	return rsaInformation;
    }
    
    public void setRSAInfomation(RSAInformation rsa) {
    	this.rsaInformation = rsa;
    }

    public abstract void send(@NotNull SerializableMessage<?> message);
}
