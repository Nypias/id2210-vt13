package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public final class AddEntry extends PeerMessage
{
    Entry newEntry;

    public AddEntry(PeerAddress source, PeerAddress destination, Entry newEntry) {
        super(source, destination);
        this.newEntry = newEntry;
    }

    public Entry getNewEntry() {
        return newEntry;
    }
}
