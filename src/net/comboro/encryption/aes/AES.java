package net.comboro.encryption.aes;

import net.comboro.SerializableMessage;
import net.comboro.Serializer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AES {

	private Cipher cipher;
	
	public final String algorithm = this.getClass().getSimpleName();
	
	private boolean trouble = false;

	public AES(String keyVal) {
		Key key = new SecretKeySpec(keyVal.getBytes(), algorithm);
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			trouble = true;
		}
	}
	
	public byte[] doFinal(byte[] input) {
		try {
			return cipher.doFinal(input);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			trouble = true;
			return input;
		}
	}

	public AESSecureMessage encrypt(SerializableMessage<?> serializableMessage){
	    return new AESSecureMessage(this,serializableMessage);
    }
    public SerializableMessage<?> decrypt(AESSecureMessage message){
        byte[] dataEn = message.toByteArray();
        byte[] data = doFinal(dataEn);
        return Serializer.deserialize(data);
    }
	
	public boolean hasTrouble() {
		return trouble;
	}

}
