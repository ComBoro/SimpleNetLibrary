package net.comboro;

import java.io.*;

public class SerializableMessage<T extends Serializable> implements Serializable {

    private T data;

    public SerializableMessage(T data){
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
