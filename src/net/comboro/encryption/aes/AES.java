package net.comboro.encryption.aes;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	
	private final Key key;
	private Cipher cipher;
	
	public final String algorithm = this.getClass().getSimpleName();
	
	private boolean trouble = false;

	public AES(String keyVal) {
		this.key = new SecretKeySpec(keyVal.getBytes(), algorithm);
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
	
	public boolean hasTrouble() {
		return trouble;
	}

}
