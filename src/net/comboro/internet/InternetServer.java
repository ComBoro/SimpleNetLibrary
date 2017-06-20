package net.comboro.internet;

import net.comboro.Client;
import net.comboro.Server;

public abstract class InternetServer<T extends Client> extends Server<T>{

    protected final int port;

    public InternetServer(int port){
        this.port = port;
    }

    public final int getPort(){
        return port;
    }

}
