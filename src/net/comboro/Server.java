package net.comboro;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final Object lock = new Object();

    protected ExecutorService serverExecutor;

    private boolean active = false;
    protected List<T> clientList = new ArrayList<>();
    private List<ServerListener<T>> serverListeners = new ArrayList<>();

    protected Server() {
        serverExecutor = Executors.newSingleThreadExecutor();
    }

    protected Executor getExecutor() {
        return serverExecutor;
    }

    public void sendAll(SerializableMessage message){
        clientList.forEach(e -> e.send(message));
    }

    protected boolean addClient(T client) {
        boolean result;
        synchronized (lock) {
            serverListeners.forEach(e -> e.onClientConnect(client));
            result = clientList.add(client);
            client.addListener(new Client.ClientListener.ClientAdapter(){
                @Override
                public void onReceive(SerializableMessage message) {
                    serverListeners.forEach(e -> e.onClientInput(client, message));
                }
            });
            client.fireConnectEvent();
            lock.notifyAll();
        }
        return result;
    }

    public boolean addLister(ServerListener<T> serverListener) {
        return serverListeners.add(serverListener);
    }

    public void fireClientInputEvent(T client, SerializableMessage message){
        serverListeners.forEach(e -> e.onClientInput(client, message));
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

    public boolean removeListener(ServerListener<T> listener) {
        return serverListeners.remove(listener);
    }

    public void startServer() throws Exception {
        active = true;
        serverExecutor.execute(() -> {
            try {
                start();
                serverListeners.forEach(e -> e.onServerStart());
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
        return active;
    }

    public List<T> getClientList() {
        return clientList;
    }

}
