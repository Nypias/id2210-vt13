package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


/**
 * Message sent by a peer to request his missing index entries.
 */
public class IndexUpdateRequest extends PeerMessage
{
    /**
     * The list of missing index ranges.
     */
    ArrayList<Range> missingRanges;
    /**
     * The last available entry in the index of the requesting peer.
     */
    int lastExisting;

    /**
     * Create an IndexUpdateRequest specifying the source and destination peers
     * as well as the list of missing ranges and the last available entry.
     *
     * @param source The peer requesting missing ranges.
     * @param destination The peer how is going to provide the missing ranges
     * (whichever it has available).
     * @param missingRanges The list of missing ranges.
     * @param lastExisting The last available entry in the requesting node's
     * index.
     */
    public IndexUpdateRequest(PeerAddress source, PeerAddress destination, ArrayList<Range> missingRanges, int lastExisting) {
        super(source, destination);
        this.missingRanges = missingRanges;
        this.lastExisting = lastExisting;
    }

    /**
     * Get the missing ranges.
     *
     * @return The list of missing ranges.
     */
    public ArrayList<Range> getMissingRanges() {
        return missingRanges;
    }

    /**
     * Set the missing ranges to request.
     *
     * @param missingRanges The list of missing ranges.
     */
    public void setMissingRanges(ArrayList<Range> missingRanges) {
        this.missingRanges = missingRanges;
    }

    /**
     * Get the last entry (its ID) in the requesting node's index.
     *
     * @return The last entry ID.
     */
    public int getLastExisting() {
        return lastExisting;
    }

    /**
     * Set the last entry ID.
     *
     * @param lastExisting The last entry ID.
     */
    public void setLastExisting(int lastExisting) {
        this.lastExisting = lastExisting;
    }
}
