package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class NewEntryACK extends PeerMessage
{
    public NewEntryACK(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
