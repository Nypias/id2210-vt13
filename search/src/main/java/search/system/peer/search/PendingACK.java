package search.system.peer.search;

import common.peer.PeerAddress;
import java.util.UUID;

/**
 * Class to represent pending ACKs a leader waits to ensure proper replication
 * to its neighborhood. Since a leader might receive multiple requests to add
 * new entries, it needs a way to match the ACKs it get to the specific entry
 * that was replicated.
 */
public class PendingACK
{
    /**
     * The number of ACKs received for that specific entry.
     */
    private int receivedACKs;
    /**
     * The node to which the leader has to finally acknowledge the successful
     * entry addition.
     */
    private PeerAddress requestSource;
    /**
     * The ID of the timeout to check for a failed new entry replication.
     */
    private UUID timeoutID;
    /**
     * The temporary ID assigned to the new entry by the node that initiated
     * the addition.
     */
    private String newEntryTempID;

    /**
     * Create a PendingACK object specifying the timeout ID, the initiating
     * node address and the temporary ID assigned to this entry.
     * 
     * @param timeoutID The timeout ID of this pending add operation.
     * @param requestSource The node that initiated the addition to the index.
     * @param newEntryTempID The temporary ID assigned to the new entry by the
     *                       initiating node.
     */
    public PendingACK(UUID timeoutID, PeerAddress requestSource, String newEntryTempID) {
        this.receivedACKs = 0;
        this.timeoutID = timeoutID;
        this.requestSource = requestSource;
        this.newEntryTempID = newEntryTempID;
    }

    /**
     * Get the number of received ACKs for a specific new entry.
     * 
     * @return The number of ACKs received so far.
     */
    public int getReceivedACKs() {
        return receivedACKs;
    }

    /**
     * Increment the number of received ACKs by one.
     */
    public void incReceivedACKs() {
        this.receivedACKs++;
    }

    /**
     * Decrement the number of received ACKs by one.
     */
    public void decReceivedACKs() {
        this.receivedACKs--;
    }

    /**
     * Get the address of the node that initiated the addition of the new entry.
     * 
     * @return The address of the initiating node.
     */
    public PeerAddress getRequestSource() {
        return requestSource;
    }

    /**
     * Get the timeout ID of the new entry addition operation.
     * 
     * @return The timeout ID.
     */
    public UUID getTimeoutID() {
        return timeoutID;
    }

    /**
     * Get the temporary ID of the new entry being added and replicated.
     * 
     * @return The new entry temporary ID.
     */
    public String getNewEntryTempID() {
        return newEntryTempID;
    }
}
