package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class IndexUpdateResponse extends PeerMessage
{
    public IndexUpdateResponse(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
