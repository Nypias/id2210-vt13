package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class NewEntryNACK extends PeerMessage
{
    public NewEntryNACK(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
