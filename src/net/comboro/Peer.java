package net.comboro;

import com.sun.istack.internal.NotNull;

@FunctionalInterface
public interface Peer {
	public void send(@NotNull SerializableMessage<?> message);
}
