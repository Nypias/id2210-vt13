package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class NewEntryNACK extends PeerMessage
{
    private String newEntryTempID;

    public NewEntryNACK(PeerAddress source, PeerAddress destination, String newEntryTempID) {
        super(source, destination);
        this.newEntryTempID = newEntryTempID;
    }

    public String getNewEntryTempID() {
        return newEntryTempID;
    }
}
