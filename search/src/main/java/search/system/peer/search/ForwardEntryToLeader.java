package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


/**
 * Message sent among peers so that it will eventually be forwarded to the
 * leader to add and replicate a new entry to the partition index.
 */
public class ForwardEntryToLeader extends PeerMessage
{
    /**
     * Counter to keep track of the number of hops the message had before it
     * reached the leader.
     */
    private int hops;
    /**
     * The new entry the leader has to replicate and acknowledge.
     */
    private Entry newEntry;
    /**
     * The peer that initiated the addition.
     */
    private PeerAddress requestSource;

    /**
     * Create a new ForwardEntryToLeader message specifying the source, the
     * destination, the initiating node the new entry and the number of hops.
     *
     * @param source The sender address.
     * @param destination The destination address.
     * @param requestSource The initiating node address.
     * @param newEntry
     * @param hops
     */
    public ForwardEntryToLeader(PeerAddress source, PeerAddress destination, PeerAddress requestSource, Entry newEntry, int hops) {
        super(source, destination);
        this.newEntry = newEntry;
        this.requestSource = requestSource;
        this.hops = hops;
    }

    /**
     * Get the new entry to be added.
     *
     * @return The new entry.
     */
    public Entry getNewEntry() {
        return newEntry;
    }

    /**
     * Get the address of the node that initiated the new entry addition.
     *
     * @return The address of the initiating node.
     */
    public PeerAddress getRequestSource() {
        return requestSource;
    }

    /**
     * Get the number of hops the message made till it reached the leader.
     *
     * @return The number of hops.
     */
    public int getHops() {
        return hops;
    }

    /**
     * Set the number of hops for the message.
     *
     * @param hops The number of hops.
     */
    public void setHops(int hops) {
        this.hops = hops;
    }
}
