package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class NewEntryACK extends PeerMessage
{
    private String newEntryTempID;

    public NewEntryACK(PeerAddress source, PeerAddress destination, String newEntryTempID) {
        super(source, destination);
        this.newEntryTempID = newEntryTempID;
    }

    public String getNewEntryTempID() {
        return newEntryTempID;
    }
}
