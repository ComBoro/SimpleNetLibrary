package net.comboro.internet.udp;

import net.comboro.SerializableMessage;
import net.comboro.Server;
import net.comboro.internet.InternetServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ComBoro on 6/12/2017.
 */
@Deprecated public class ServerUDP extends InternetServer<ClientUDP> {

    DatagramSocket datagramSocket;
    static public final int PACKET_SIZE = 1024;

    public ServerUDP(int port) {
        super(port);
    }

    @Override
    protected void start() throws Exception {
        datagramSocket = new DatagramSocket(port);
        acceptClients();
    }

    private void acceptClients() throws IOException, ClassNotFoundException {
        while(!datagramSocket.isClosed()){
            DatagramPacket datagramPacket = new DatagramPacket(new byte[PACKET_SIZE],PACKET_SIZE);
            datagramSocket.receive(datagramPacket);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            SerializableMessage serializableMessage = (SerializableMessage) objectInputStream.readObject();

            InetAddress inetAddress = datagramPacket.getAddress();
            int port = datagramPacket.getPort();

            ClientUDP client = null;

            for(ClientUDP clientUDP : getClientList()){
                if(clientUDP.getInetAddress().toString().equals(inetAddress.toString()) && clientUDP.port == port) {
                    client = clientUDP;
                }
            }

            if(client == null){
                client = new ClientUDP(inetAddress, port);
                addClient(client);
            }

            fireClientInputEvent(client, serializableMessage);
        }
    }

    @Override
    protected void stop() {
        datagramSocket.close();
    }

}
