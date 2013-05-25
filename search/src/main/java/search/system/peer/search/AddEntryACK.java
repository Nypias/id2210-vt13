package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


/**
 * Message sent to the leader as a response to an AddEntry message to notify it
 * of successful replication of a newly added entry.
 */
public class AddEntryACK extends PeerMessage
{
    /**
     * The ID of the newly added entry.
     */
    private String entryID;

    /**
     * Create an AddEntryACK specifying the source, the destination and the new
     * entry ID.
     *
     * @param source The leader neighbor acknowledging replication.
     * @param destination The leader receiving the ACK.
     * @param entryID The ID of the successfully replicated entry.
     */
    public AddEntryACK(PeerAddress source, PeerAddress destination, String entryID) {
        super(source, destination);
        this.entryID = entryID;
    }

    /**
     * Get the replicated entry ID.
     *
     * @return The entry ID.
     */
    public String getEntryID() {
        return entryID;
    }
}
