package net.comboro;

import java.io.Serializable;

public class TaggedMessage<D extends Serializable> extends SerializableMessage<D>{

    private final String[] tags;

    public TaggedMessage(SerializableMessage<D> message, String... tags){
        super(message.getData());
        this.tags = tags;
    }

    public String[] getTags(){
        return tags;
    }
}
