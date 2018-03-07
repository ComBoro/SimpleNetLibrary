package net.comboro.internet;

import net.comboro.Client;
import net.comboro.Server;

public abstract class InternetServer<T extends Client> extends Server<T>{

    protected int port;

    public InternetServer(int port){
    	super();
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    protected void setPort(int port){
        if(super.isActive())
            return;
        this.port = port;
    }

}
