package net.comboro.encryption.rsa;

import java.math.BigInteger;
import java.security.SecureRandom;

import static java.math.BigInteger.*;

public class RSA {

	private static final SecureRandom secureRandom = new SecureRandom();
	public static final int defaultBytes = 2048; //1536
	
	private final BigInteger p,q; //primes
	private final BigInteger modulus;
	private final BigInteger toitent; // Eucler's function
	private final BigInteger e = new BigInteger("65537"); // public key (2^16 + 1)
	private final BigInteger d; // private key

	public RSA() {
		this(defaultBytes);
	}
	
	public RSA(int bytes){
		p = probablePrime(bytes/2, secureRandom);
		q = probablePrime(bytes/2, secureRandom);
		modulus = p.multiply(q);
		toitent = p.subtract(ONE).multiply(q.subtract(ONE));
		d = e.modInverse(toitent);
	}
	
	public BigInteger encrypt(BigInteger toEncrypt){
		return toEncrypt.modPow(e, modulus);
	}
	
	public BigInteger decrypt(BigInteger toDecrypt) {
		return toDecrypt.modPow(d, modulus);
	}
	
	public RSAInformation getInformation() {
		return new RSAInformation(e, modulus);
	}
	
	static public BigInteger encrypt(BigInteger toEncrypt, BigInteger e, BigInteger modulus) {
		return toEncrypt.modPow(e, modulus);
	}
	
	static public BigInteger encrypt(BigInteger toEncrypt, RSAInformation information) {
		return encrypt(toEncrypt, information.getPublicKey(), information.getModulus());
	}
	
}
