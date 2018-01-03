package net.comboro.encryption.rsa;

import net.comboro.SerializableMessage;

public interface RSASecurePeer{
		
	RSAInformation getRSAInformation();
	
	SerializableMessage<?> decryptRSA(RSASecureMessage message);

}
