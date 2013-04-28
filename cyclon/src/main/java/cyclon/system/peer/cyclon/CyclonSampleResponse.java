package cyclon.system.peer.cyclon;

import common.peer.PeerAddress;
import java.util.ArrayList;
import se.sics.kompics.Event;


public class CyclonSampleResponse extends Event {
	PeerAddress node;

//-------------------------------------------------------------------
	public CyclonSampleResponse(PeerAddress node) {
		this.node = node;
	}
        
	public CyclonSampleResponse() {
	}

//-------------------------------------------------------------------
	public PeerAddress getRandomPeer() {
		return this.node;
	}
}
