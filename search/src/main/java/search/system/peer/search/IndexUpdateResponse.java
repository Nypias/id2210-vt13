package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;

/**
 * Message sent by a peer as a response to a IndexUpdateRequest returning any
 * index entries it might have (and that the requesting node is missing).
 */
public class IndexUpdateResponse extends PeerMessage
{
    /**
     * The list of returned entries.
     */
    private ArrayList<Entry> entries;

    /**
     * Create an IndexpdateRespnse specifying the source and destination peers
     * as well as the list of entries to send.
     * 
     * @param source The peer sending missing index entries.
     * @param destination The peer requesting the missing index entries.
     * @param entries The missing index entries.
     */
    public IndexUpdateResponse(PeerAddress source, PeerAddress destination, ArrayList<Entry> entries) {
        super(source, destination);
        this.entries = entries;
    }

    /**
     * Get the list of entries.
     * 
     * @return The list of missing entries.
     */
    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
