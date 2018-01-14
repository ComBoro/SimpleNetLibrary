package net.comboro.internet.tcp;

import net.comboro.Client;
import net.comboro.SerializableMessage;
import net.comboro.encryption.rsa.RSAInformation;
import net.comboro.encryption.rsa.RSASecurePeer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public class ClientTCP extends Client{

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Thread thread;

    public ClientTCP(boolean serverSide, Socket socket){
    	super(serverSide);
        this.socket = socket;
        try{
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());

            // Send RSA Information
            this.<RSAInformation>send(getRSAInformation());

            this.receive();
        } catch (IOException io){
            this.trouble = true;
        }
    }
    
    public ClientTCP(Socket socket) {
    	this(false, socket);
    }

    @Override
    public void send(SerializableMessage message){
        try {
            if(Objects.nonNull(outputStream))
                outputStream.writeObject(message);
        } catch (IOException e) {
            fireConnectionError(e);
        }
    }

    private void receive(){
        thread = new Thread(() -> {
            while(!(thread.isInterrupted() || hasTrouble() || socket.isInputShutdown() || socket.isClosed())){
                try {
                    SerializableMessage<?> message = (SerializableMessage<?>) inputStream.readObject();
                    fireReceiveEvent(message);
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    trouble = true;
                    if(e instanceof IOException) fireConnectionError((IOException)e);
                }
            }
        });
        thread.setName(socket.getInetAddress().getHostAddress());
        thread.start();
    }

    public void preRemoval(){
        try {
            socket.shutdownInput();
            socket.close();
        } catch (IOException | NullPointerException e) {

        }

        socket = null;
        inputStream = null;
        outputStream = null;

        thread.interrupt();

        listeners.clear();
    }
    
    public FinalClientTCP getStatic() {
    	if(!serverSide) throw new IllegalArgumentException("Not a server-sided client");
    	return new FinalClientTCP(this);
    }

    public static ClientTCP create(InetAddress address, int port) throws IOException{
        Socket socket = new Socket(address, port);
        return new ClientTCP(socket);
    }
     
    public String getThreadName() {
    	return thread.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != ClientTCP.class))
            return false;
        ClientTCP other = (ClientTCP) obj;
        return this.uuid.equals(other.uuid);
    }
}
