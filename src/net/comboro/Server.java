package net.comboro;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.comboro.encryption.aes.AESInformation;
import net.comboro.encryption.rsa.RSA;
import net.comboro.encryption.rsa.RSAInformation;
import net.comboro.encryption.rsa.RSASecureMessage;
import net.comboro.encryption.rsa.RSASecurePeer;

public abstract class Server<T extends Client> {

    public interface ServerListener<T extends Client> {

        class ServerAdapter<T extends Client> implements ServerListener<T>{

            @Override public void onServerStart() {}

            @Override public void onServerStartError(Exception e) {}

            @Override public void onServerStop() {}

            @Override public void onClientConnect(T client) {}

            @Override public void onClientInput(T client, SerializableMessage message) {}

            @Override public void onClientDisconnect(T client) {}
        }

        void onServerStart();

        void onServerStartError(Exception e);

        void onServerStop();

        void onClientConnect(T client);

        void onClientInput(T client, SerializableMessage message);

        void onClientDisconnect(T client);
    }
    
    protected RSASecurePeer securePeer = null;
    protected RSA rsa = null;
    
    private final Object lock = new Object();

    protected ExecutorService serverExecutor;

    private boolean active = false, secure = false;
    protected List<T> clientList = new ArrayList<>();
    protected List<ServerListener<T>> serverListeners = new ArrayList<>();

    protected Server() {
        serverExecutor = Executors.newSingleThreadExecutor();
    }

    @Deprecated protected Executor getExecutor() {
        return serverExecutor;
    }

    public void sendAll(SerializableMessage<Serializable> message){
        clientList.forEach(e -> e.send(message));
    }

    protected boolean addClient(T client) {
        boolean result;
        synchronized (lock) {
            serverListeners.forEach(e -> e.onClientConnect(client));
            result = clientList.add(client);
            client.addListener(new Client.ClientListener.ClientAdapter(){
                @Override
                public void onReceive(SerializableMessage<?> message) {
                    fireClientInputEvent(client, message);
                }
                @Override
                public void onConnectionError(java.io.IOException io) {
                	removeClient(client);
                };
            });
            client.fireConnectEvent();
            
            if(secure) {
            	RSAInformation rsaInf = securePeer.getRSAInformation();
            	SerializableMessage<RSAInformation> rsaMessage = new SerializableMessage<>(rsaInf);
            	client.send(rsaMessage);
            	System.out.println("RSA Information send");
            } 
            
            lock.notifyAll();
        }
        return result;
    }

    public boolean addLister(ServerListener<T> serverListener) {
        return serverListeners.add(serverListener);
    }

    public void fireClientInputEvent(T client, SerializableMessage<?> message){
    	
    	if(message.getData() instanceof RSAInformation) {
    		System.out.println("Received client RSA. Generating AES key");
    		RSAInformation clientRSA = (RSAInformation) message.getData();
    		RSAInformation serverRSA = securePeer.getRSAInformation();
    		String key = UUID.randomUUID().toString();
    		AESInformation aesInf = new AESInformation(key);
    		RSASecureMessage sm = new RSASecureMessage(clientRSA, serverRSA, new SerializableMessage<>(aesInf));
    		client.send(sm);
    		System.out.println("AES information send. Key = " + key);
    		return;
    	} 
    	
        serverListeners.forEach(e -> e.onClientInput(client, message));
    }

    public void fireClientDisconnectEvent(T client){
        serverListeners.forEach(e -> e.onClientDisconnect(client));
    }

    public boolean removeClient(T client) {
        boolean result;
        synchronized (lock) {
            client.fireDisconnectEvent();
            result = clientList.remove(client);
            serverListeners.forEach( e -> e.onClientDisconnect(client));
            lock.notifyAll();
        }
        return result;
    }

    abstract protected void notifyClientRemoval(T client);

    public boolean removeListener(ServerListener<T> listener) {
        return serverListeners.remove(listener);
    }
    
    public boolean isSecure() {
    	return secure;
    }
    
    public void setSecure(boolean secure) {
    	if(isActive()) 
    		throw new IllegalArgumentException("Server is active");
    	makeSecure();
    	this.secure = secure;
    }
    
    private void makeSecure() {
    	rsa = new RSA();
    	securePeer = new RSASecurePeer() {
			
			@Override
			public RSAInformation getRSAInformation() {
				return Server.this.rsa.getInformation();
			}
			
			@Override
		    public SerializableMessage<?> decrypt(RSASecureMessage message) {
		    	BigInteger en = message.getNumber();
		    	BigInteger de = rsa.decrypt(en);
		    	byte[] data = de.toByteArray();
		    	return Serializer.deserialize(data);
		    }
		};
    }

    public void startServer() {
        active = true;
        serverExecutor.execute(() -> {
            try {
                serverListeners.forEach(e -> e.onServerStart());
                start();
            } catch (Exception e) {
                serverListeners.forEach(e1 -> e1.onServerStartError(e));
            }
        });
    }

    public void stopServer() {
        active = false;
        serverExecutor.execute(() -> stop());
        serverExecutor.shutdown();
        serverListeners.forEach(e -> e.onServerStop());
        serverListeners.clear();
    }

    abstract protected void start() throws Exception;

    abstract protected void stop();

    public boolean isActive() {
        return new Boolean(active);
    }

    public List<T> getClientList() {
        return new ArrayList<T>(clientList);
    }

}
