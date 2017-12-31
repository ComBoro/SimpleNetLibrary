package net.comboro.encryption.rsa;

import net.comboro.SerializableMessage;

public interface RSASecurePeer{
		
	RSAInformation getRSAInformation();
	
	public SerializableMessage<?> decrypt(RSASecureMessage message);

}
