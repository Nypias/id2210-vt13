package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class IndexUpdateRequest extends PeerMessage
{
    
    public IndexUpdateRequest(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
