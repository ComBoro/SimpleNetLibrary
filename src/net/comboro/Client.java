package net.comboro;

import com.sun.istack.internal.NotNull;
import net.comboro.SerializableMessage;

import java.util.ArrayList;
import java.util.List;

abstract public class Client {

    public interface ClientListener {

        class ClientAdapter implements ClientListener{

            @Override public void onReceive(SerializableMessage message) {}

            @Override public void onDisconnect() {}

            @Override public void onConnect() {}
        }

        void onReceive(SerializableMessage message);
        void onConnect();
        void onDisconnect();
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

    public void fireReceiveEvent(@NotNull SerializableMessage message) {
        listeners.forEach(e -> e.onReceive(message));
    }

    public abstract void send(@NotNull SerializableMessage message);
}
