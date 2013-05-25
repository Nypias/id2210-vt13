package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;

/**
 * Message sent by a node as a response to the SearchPartitionRequest message
 * returning the requested search operation results.
 */
public class SearchPartitionResponse extends PeerMessage
{
    /**
     * The ID of the search operation needed to keep track of multiple searches
     * initiated by the same peer.
     */
    Integer pendingSearchID;
    /**
     * A list of entries returned as a result to a search operation.
     */
    ArrayList<Entry> searchResult;

    /**
     * Create a new SearchPartitionResponse specifying the peer that sends
     * the message, the peer that will receive the message as well as the
     * search ID and the list with the results.
     * 
     * @param source The peer that sends the message.
     * @param destination The peer that will receive the message.
     * @param pendingSearchID The search ID to keep track of the search operation
     *                        by the nodes that performs the search operation.
     * @param searchResult A list of entries that contains the results of the
     *                     search operation.
     */
    public SearchPartitionResponse(PeerAddress source, PeerAddress destination, Integer pendingSearchID, ArrayList<Entry> searchResult) {
        super(source, destination);
        this.pendingSearchID = pendingSearchID;
        this.searchResult = searchResult;
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
     * Get the result of a search operation performed.
     * 
     * @return The list of results.
     */
    public ArrayList<Entry> getSearchResult() {
        return searchResult;
    }
}
