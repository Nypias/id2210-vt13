package search.system.peer.search;

import common.configuration.JRConfig;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.kompics.web.WebRequest;


public class PendingSearch
{
    private Map<Integer, ArrayList<Entry>> searchResults;
    private Map<Integer, Boolean> searchResultsChecklist;
    private Integer pendingSearchID;
    private WebRequest webRequest;
    private String searchQuery;
    private UUID queryTimeout;

    public PendingSearch(Integer pendingSearchID, WebRequest webRequest, String searchQuery) {
        searchResults = new ConcurrentHashMap<Integer, ArrayList<Entry>>();
        searchResultsChecklist = new ConcurrentHashMap<Integer, Boolean>();
        for (int i = 0; i < JRConfig.NUMBER_OF_PARTITIONS; i++) {
            searchResults.put(i, new ArrayList<Entry>());
            searchResultsChecklist.put(i, Boolean.FALSE);
        }

        this.pendingSearchID = pendingSearchID;
        this.webRequest = webRequest;
        this.searchQuery = searchQuery;
    }

    public void registerSearchResults(int partitionID, ArrayList<Entry> results) {
        this.searchResults.put(partitionID, results);
        searchResultsChecklist.put(partitionID, Boolean.TRUE);
    }

    public boolean isSearchComplete() {
        boolean complete = true;
        for (Integer key : this.searchResultsChecklist.keySet()) {
            if (searchResultsChecklist.get(key).equals(Boolean.FALSE)) {
                complete = false;
            }
        }
        return complete;
    }

    public ArrayList<Entry> getMergedSearchResults() {
        ArrayList<Entry> results = new ArrayList<Entry>();
        for (Integer key : this.searchResults.keySet()) {
            results.addAll(this.searchResults.get(key));
        }
        return results;
    }

    public ArrayList<Integer> getMissingResponses() {
        ArrayList<Integer> missingPartitionResponses = new ArrayList<Integer>();
        for (Integer key : this.searchResultsChecklist.keySet()) {
            if (searchResultsChecklist.get(key).equals(Boolean.FALSE)) {
                missingPartitionResponses.add(key);
            }
        }
        return missingPartitionResponses;
    }

    public WebRequest getWebRequest() {
        return webRequest;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public UUID getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(UUID queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}
