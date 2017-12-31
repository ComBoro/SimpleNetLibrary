package net.comboro.encryption.rsa;

import java.io.Serializable;
import java.math.BigInteger;

public class RSAInformation implements Serializable{
	private static final long serialVersionUID = -8796156021935623938L;
	private BigInteger publicKey, modulus;
	
	public RSAInformation(BigInteger publicKey, BigInteger modulus) {
		this.publicKey = publicKey;
		this.modulus = modulus;
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}

	public BigInteger getModulus() {
		return modulus;
	}

}
