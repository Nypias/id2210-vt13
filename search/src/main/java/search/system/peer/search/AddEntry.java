package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


/**
 * Message sent by the leader to its neighbors to request a new entry
 * replication. Neighboring nodes will ACK this upon successful replication.
 */
public final class AddEntry extends PeerMessage
{
    /**
     * The new entry to be replicated.
     */
    Entry newEntry;

    /**
     * Create an AddEntry message specifying the source, the destination and the
     * entry set for replication.
     *
     * @param source The peer (leader) requesting the replication.
     * @param destination The neighbor asked to replicate a new entry.
     * @param newEntry The new entry to be replicated.
     */
    public AddEntry(PeerAddress source, PeerAddress destination, Entry newEntry) {
        super(source, destination);
        this.newEntry = newEntry;
    }

    /**
     * Get the new entry to replicate.
     *
     * @return The entry set for replication.
     */
    public Entry getNewEntry() {
        return newEntry;
    }
}
