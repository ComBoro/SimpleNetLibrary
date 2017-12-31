package net.comboro.encryption;

import java.math.BigInteger;

import net.comboro.SerializableMessage;

abstract public class EncryptedMessage extends SerializableMessage<String> {
	private static final long serialVersionUID = -7503286664312507506L;
	
	protected static transient BigInteger number;
	
	protected EncryptedMessage() {
		
	}
	
	public EncryptedMessage(BigInteger bi) {
		number = new BigInteger(bi.toByteArray());
		data = number.toString();
	}
	
	public byte[] toByteArray() {
		return new BigInteger(data).toByteArray();
	}
}
