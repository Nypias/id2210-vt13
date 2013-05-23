package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


public class SearchPartitionRequest extends PeerMessage
{
    Integer pendingSearchID;
    String searchQuery;

    public SearchPartitionRequest(PeerAddress source, PeerAddress destination, Integer pendingSearchID, String searchQuery) {
        super(source, destination);
        this.pendingSearchID = pendingSearchID;
        this.searchQuery = searchQuery;
    }

    public Integer getPendingSearchID() {
        return pendingSearchID;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
