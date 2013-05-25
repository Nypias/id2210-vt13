package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


/**
 * Message sent by the leader to a node that initiated an operation to add a new
 * entry to the index.
 */
public class NewEntryACK extends PeerMessage
{
    /**
     * The temporary ID of the new entry assigned by the initiating node.
     */
    private String newEntryTempID;

    /**
     * Create a NewEntryACK specifying the source and destination of the message
     * as well as the temporary ID of the new entry which the leader is
     * acknowledging.
     *
     * @param source The peer (the leader) that acknowledges the new entry to
     * the initiating node.
     * @param destination The initiating node.
     * @param newEntryTempID The temporary ID of the new entry.
     */
    public NewEntryACK(PeerAddress source, PeerAddress destination, String newEntryTempID) {
        super(source, destination);
        this.newEntryTempID = newEntryTempID;
    }

    /**
     * Get the temporary ID of the new entry.
     *
     * @return The new entry temporary ID.
     */
    public String getNewEntryTempID() {
        return newEntryTempID;
    }
}
