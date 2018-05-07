package net.comboro.internet.tcp;

import net.comboro.Client;
import net.comboro.SerializableMessage;
import net.comboro.encryption.aes.AESInformation;
import net.comboro.encryption.rsa.RSA;
import net.comboro.encryption.rsa.RSAInformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Queue;

public class ClientTCP extends Client{

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Thread thread;

    private Deque<SerializableMessage> sendQueue = new ArrayDeque<>();
    private boolean autoFlush;

    public ClientTCP(boolean serverSide, Socket socket, boolean autoFlush){
    	super(serverSide);
        this.socket = socket;
        this.autoFlush = autoFlush;
        try{
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());

            this.receive();
        } catch (IOException io){
            this.trouble = true;
        }
        flush();
    }
    
    public ClientTCP(Socket socket) {
    	this(false, socket);
    }

    public ClientTCP(boolean serverSide, Socket socket){
        this(serverSide, socket, true);
    }

    @Override
    public void send(SerializableMessage<?> message){
        if(sendQueue == null){
            sendQueue = new ArrayDeque<>();
        }
        sendQueue.offerFirst(message);
        if(autoFlush) flush();

    }

    public void flush(){
        try {
            if(Objects.nonNull(outputStream)){
                while(!sendQueue.isEmpty()){
                    outputStream.writeObject(sendQueue.pop());
                }
            }
        } catch (IOException e) {
            fireConnectionError(e);
        }
    }

    private void receive(){
        thread = new Thread(() -> {
            this.thread.setName(socket.getInetAddress().getHostAddress());

            while(!(thread.isInterrupted() || hasTrouble() || socket.isInputShutdown() || socket.isClosed())){
                try {
                    SerializableMessage<?> message = (SerializableMessage<?>) inputStream.readObject();
                    fireReceiveEvent(message);
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    trouble = true;
                    lastException = e;
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

    public static ClientTCP create(InetAddress address, int port) throws IOException{
        Socket socket = new Socket(address, port);
        return new ClientTCP(socket);
    }
     
    public String getDisplayName() {
        if(thread == null) return "<NULL>";
    	return thread.getName() + ":" + socket.getPort();
    }

    @Override
    protected void onConnectionTermination() {
        if(thread!=null)
            thread.interrupt();
        preRemoval();
    }

    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != ClientTCP.class))
            return false;
        Client other = (ClientTCP) obj;
        return this.rsaInformation.equals(other.rsaInformation);
    }
}
