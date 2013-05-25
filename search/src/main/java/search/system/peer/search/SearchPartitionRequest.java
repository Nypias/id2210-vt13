package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


/**
 * Message sent by a node to request a search operation by another node (usually
 * in a different partition).
 */
public class SearchPartitionRequest extends PeerMessage
{
    /**
     * The ID of the search operation needed to keep track of multiple searches
     * initiated by the same peer.
     */
    Integer pendingSearchID;
    /**
     * The search query to for the receiving node to perform the search
     * operation.
     */
    String searchQuery;

    /**
     * Create a new SearchPartitionRequest specifying the peer that sends the
     * message, the peer that will receive the message as well as the search ID
     * and the query the receiving node will perform the search with.
     *
     * @param source The peer that sends the message.
     * @param destination The peer that will receive the message.
     * @param pendingSearchID The search ID to keep track of the search
     * operation by the nodes that performs the search operation.
     * @param searchQuery The query string of the search operation.
     */
    public SearchPartitionRequest(PeerAddress source, PeerAddress destination, Integer pendingSearchID, String searchQuery) {
        super(source, destination);
        this.pendingSearchID = pendingSearchID;
        this.searchQuery = searchQuery;
    }

    /**
     * Get the pending search ID that the searching node uses to keep track of
     * multiple searches initiated by it.
     *
     * @return The pending search ID.
     */
    public Integer getPendingSearchID() {
        return pendingSearchID;
    }

    /**
     * Get the search query the searching node has sent.
     *
     * @return The search query string.
     */
    public String getSearchQuery() {
        return searchQuery;
    }
}
