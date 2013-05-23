package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


public class SearchPartitionResponse extends PeerMessage
{
    Integer pendingSearchID;
    ArrayList<Entry> searchResult;

    public SearchPartitionResponse(PeerAddress source, PeerAddress destination, Integer pendingSearchID, ArrayList<Entry> searchResult) {
        super(source, destination);
        this.pendingSearchID = pendingSearchID;
        this.searchResult = searchResult;
    }

    public Integer getPendingSearchID() {
        return pendingSearchID;
    }

    public ArrayList<Entry> getSearchResult() {
        return searchResult;
    }
}
