package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class ForwardEntryToLeader extends PeerMessage
{
    private int hops;
    private Entry newEntry;
    private PeerAddress requestSource;

    public ForwardEntryToLeader(PeerAddress source, PeerAddress destination, PeerAddress requestSource, Entry newEntry, int hops) {
        super(source, destination);
        this.newEntry = newEntry;
        this.requestSource = requestSource;
        this.hops = hops;
    }

    public Entry getNewEntry() {
        return newEntry;
    }

    public PeerAddress getRequestSource() {
        return requestSource;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }
}
