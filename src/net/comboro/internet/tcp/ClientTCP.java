package net.comboro.internet.tcp;

import net.comboro.Client;
import net.comboro.SerializableMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by ComBoro on 6/11/2017.
 */
public class ClientTCP extends Client {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Thread thread;

    private boolean trouble = false;

    public ClientTCP(Socket socket){
        this.socket = socket;
        try{
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.receive();
        } catch (IOException io){
            this.trouble = true;
        }
    }

    @Override public void send(SerializableMessage message){
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            trouble = true;
        }
    }

    private void receive(){
        thread = new Thread(() -> {
            while(!(thread.isInterrupted() || hasTrouble() || socket.isInputShutdown() || socket.isClosed())){
                try {
                    SerializableMessage message = (SerializableMessage) inputStream.readObject();
                    fireReceiveEvent(message);
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    trouble = true;
                }
            }
        });
        thread.setName(socket.getInetAddress().getHostAddress());
        thread.start();
    }

    public boolean hasTrouble(){
        return trouble;
    }

    public static ClientTCP create(InetAddress address, int port) throws IOException{
        Socket socket = new Socket(address, port);
        return new ClientTCP(socket);
    }

}
