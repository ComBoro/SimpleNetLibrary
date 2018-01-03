package net.comboro.internet.udp;

import com.sun.istack.internal.NotNull;
import net.comboro.Client;
import net.comboro.SerializableMessage;
import net.comboro.Serializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by ComBoro on 6/12/2017.
 */
@Deprecated public class ClientUDP extends Client{

    protected DatagramSocket datagramSocket;
    protected InetAddress inetAddress;
    protected int port;

    public ClientUDP(InetAddress inetAddress, int port){
        this.inetAddress = inetAddress;
        this.port = port;
        this.receive();
        try {
            this.datagramSocket = new DatagramSocket(port,inetAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(@NotNull SerializableMessage message) {
        try {
            byte[] data = Serializer.serialize(message);
            DatagramPacket packet = new DatagramPacket(data, data.length, inetAddress, port);
            datagramSocket.send(packet);
            System.out.println("send");
        } catch (IOException e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),new IOException("Error serializing message",e));
        }
    }

    private void receive(){
        Thread thread = new Thread(() -> {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[65_535], 65_535);
                datagramSocket.receive(datagramPacket);
                SerializableMessage message = Serializer.deserialize(datagramPacket.getData());
                fireReceiveEvent(message);
            } catch (IOException e) {

            }
        });
        thread.start();
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        ClientUDP other = (ClientUDP) obj;
        return inetAddress.toString().equals(other.inetAddress.toString()) && this.port == other.port;
    }
}
