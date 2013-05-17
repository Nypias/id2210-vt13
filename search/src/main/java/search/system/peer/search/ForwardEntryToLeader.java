package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class ForwardEntryToLeader extends PeerMessage
{
    Entry newEntry;
    PeerAddress requestSource;

    public ForwardEntryToLeader(PeerAddress source, PeerAddress destination, PeerAddress requestSource, Entry newEntry) {
        super(source, destination);
        this.newEntry = newEntry;
        this.requestSource = requestSource;
    }

    public Entry getNewEntry() {
        return newEntry;
    }

    public PeerAddress getRequestSource() {
        return requestSource;
    }
}
