package net.comboro;

import net.comboro.encryption.aes.AES;
import net.comboro.encryption.aes.AESInformation;
import net.comboro.encryption.aes.AESSecureMessage;
import net.comboro.encryption.rsa.RSA;
import net.comboro.encryption.rsa.RSAInformation;
import net.comboro.encryption.rsa.RSASecureMessage;
import net.comboro.encryption.rsa.RSASecurePeer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

abstract public class Client implements RSASecurePeer{

	private RSA rsa = null;
	private AES aes = null;
	public RSAInformation rsaInformation = null;
	public AESInformation aesInformation = null;
	public UUID uuid;
	
    protected RSAInformation serverRSA = null;
    
    protected boolean trouble = false, serverSide;
    protected Exception lastException = null;

    public interface ClientListener {

        class ClientAdapter implements ClientListener{

            @Override public void onReceive(SerializableMessage<?> message) {}

            @Override public void onDisconnect() {}

            @Override public void onConnect() {}
            
            @Override public void onConnectionError(IOException io) {}
        }

        void onReceive(SerializableMessage<?> message);
        void onConnect();
        void onDisconnect();
        void onConnectionError(IOException io);
    }
    
    public Client() {
    	this(false);
    }
    
    public Client(boolean serverSide) {
    	this.serverSide = serverSide;
    	if(!serverSide) {
    		rsa = new RSA();
    		setRSAInfomation(rsa.getInformation());
    		uuid = UUID.randomUUID();
    		aes = new AES(uuid.toString());
    		aesInformation = new AESInformation(uuid.toString());
    	}
    }

    public void sendEncrypted(SerializableMessage<?> message){
        send(aes.encrypt(message));
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
    
    public void fireConnectionError(IOException io){
    	 listeners.forEach(e -> e.onConnectionError(io));
    }

    public void fireReceiveEvent(SerializableMessage<?> message) {
            	
    	if(!serverSide) {
    		// Decrypt
    		
        	if(message instanceof RSASecureMessage) {
        		RSASecureMessage sm = (RSASecureMessage) message;
        		message = Client.this.decryptRSA(sm);
            } else if (message instanceof AESSecureMessage) {
        	    AESSecureMessage sm = (AESSecureMessage) message;
        	    message = aes.decrypt(sm);
            }

            if(message.getData() instanceof RSAInformation) {
        		serverRSA = (RSAInformation) message.getData();
        		RSAInformation clientRSA = this.getRSAInformation();
        		        		
        		SerializableMessage<RSAInformation> respond = new SerializableMessage<>(clientRSA);
        		send(respond);
        		return;
    		} else if (message.getData() instanceof AESInformation) {
    			String key = ((AESInformation) message.getData()).getKey();
    			aes = new AES(key);
    			aesInformation = new AESInformation(key);
    			return;
    		}
    	}
    	
    	for(ClientListener cl : listeners) {
    		cl.onReceive(message);
    	}    	
    }
    
    public boolean hasTrouble() {
    	return Boolean.valueOf(trouble);
    }
    
    public Exception getLastException() {
    	return new Exception(lastException);
    }
    
    @Override
    public SerializableMessage<?> decryptRSA(RSASecureMessage message) {
    	BigInteger en = message.getNumber();
    	BigInteger de = rsa.decrypt(en);
    	de = de.negate();
    	byte[] data = de.toByteArray();
    	return Serializer.deserialize(data);
    }
    
    public AES getAES() {
    	return aes;
    }
    
    public UUID getUUID() {
    	return uuid;
    }
    
    public void setUUID(UUID uuid) {
    	this.uuid = uuid;
    }
    
    @Override
    public RSAInformation getRSAInformation() {
    	return rsaInformation;
    }
    
    public void setRSAInfomation(RSAInformation rsa) {
    	this.rsaInformation = rsa;
    }

    public abstract void send(SerializableMessage<?> message);

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != Client.class))
            return false;
        Client other = (Client) obj;
        return this.uuid.equals(other.uuid);
    }
}
