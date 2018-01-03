package net.comboro.encryption.aes;

import net.comboro.SerializableMessage;
import net.comboro.Serializer;
import net.comboro.encryption.EncryptedMessage;

import java.math.BigInteger;

public class AESSecureMessage extends EncryptedMessage {

    public AESSecureMessage(AES aes, SerializableMessage<?> message){
        byte[] data = Serializer.serialize(message);
        byte[] encryptedData = aes.doFinal(data);
        number = new BigInteger(encryptedData);
        this.data = number.toString();
    }

}
