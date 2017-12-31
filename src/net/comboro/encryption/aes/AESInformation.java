package net.comboro.encryption.aes;

import java.io.Serializable;

public class AESInformation implements Serializable{
	private static final long serialVersionUID = 4436379720962924182L;
	
	private String key;

	public AESInformation(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
