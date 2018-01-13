package net.comboro;

@FunctionalInterface
public interface Peer {
	void send(SerializableMessage<?> message);
}
