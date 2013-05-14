package common.simulation;

import java.io.Serializable;
import java.math.BigInteger;
import se.sics.kompics.Event;


public final class PeerJoin extends Event implements Serializable
{
    private final BigInteger peerId;
    private final int num;

//-------------------------------------------------------------------	
    public PeerJoin(BigInteger peerId, int num) {
        this.peerId = peerId;
        this.num = num;
    }

//-------------------------------------------------------------------	
    public BigInteger getPeerId() {
        return this.peerId;
    }

//-------------------------------------------------------------------	
    public int getNum() {
        return this.num;
    }
}
