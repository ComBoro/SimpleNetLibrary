package net.comboro;

import com.sun.istack.internal.NotNull;

import java.io.*;

public class Serializer {

    public static final byte[] serialize(@NotNull SerializableMessage<?> message) {
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	ObjectOutputStream objectOutputStream = null;
        try {
        	objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
        byte[] arr = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        return arr;
    }

    public static final SerializableMessage<?> deserialize(@NotNull byte[] data){
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return (SerializableMessage<?>) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
    }

}
