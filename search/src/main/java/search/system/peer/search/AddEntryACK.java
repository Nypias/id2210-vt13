package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class AddEntryACK extends PeerMessage
{
    private String entryID;

    public AddEntryACK(PeerAddress source, PeerAddress destination, String entryID) {
        super(source, destination);
        this.entryID = entryID;
    }

    public String getEntryID() {
        return entryID;
    }
}
