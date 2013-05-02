package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


public class IndexUpdateResponse extends PeerMessage
{
    private ArrayList<Entry> entries;

    public IndexUpdateResponse(PeerAddress source, PeerAddress destination, ArrayList<Entry> entries) {
        super(source, destination);
        this.entries = entries;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
