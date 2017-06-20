package net.comboro.internet.tcp;

import net.comboro.internet.InternetServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP extends InternetServer<ClientTCP> {

    private ServerSocket serverSocket;

    public ServerTCP(int port) {
        super(port);
    }

    @Override
    protected void start() throws IOException{
        serverSocket = new ServerSocket(port, 50, null);
        acceptClients();
    }

    private void acceptClients() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                ClientTCP clientTCP = new ClientTCP(socket);
                addClient(clientTCP);
            } catch (IOException e) {
                continue;
            }
        }
    }

    @Override
    protected void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {

        }
    }

}
