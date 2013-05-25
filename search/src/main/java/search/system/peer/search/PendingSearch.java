package search.system.peer.search;

import common.configuration.JRConfig;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import se.sics.kompics.web.WebRequest;

/**
 * Class to represent a pending search operation initiated by a peer.
 * When a peer sends search messages to other partitions it keeps track
 * of the responses using a PendingSearch.
 */
public class PendingSearch
{
    /**
     * The search results returned by each partition.
     */
    private Map<Integer, ArrayList<Entry>> searchResults;
    /**
     * A checklist to check which partitions have answered in case we want
     * to resend the search to some of them after a specific timeout.
     */
    private Map<Integer, Boolean> searchResultsChecklist;
    /**
     * A search ID to keep track of the specific search operation.
     */
    private Integer pendingSearchID;
    /**
     * The WebRequest to which we sent back the response to the user that
     * operates using a browser.
     */
    private WebRequest webRequest;
    /**
     * The search query string of the search operation.
     */
    private String searchQuery;
    /**
     * The ID of the timeout of a search operation, so that we can cancel it
     * when we get all our responses back.
     */
    private UUID queryTimeout;

    /**
     * Create a new PendingSearch object specifying the search ID, the WebResponse
     * object to respond to and the search query string.
     * 
     * @param pendingSearchID The search ID to keep track of the specific
     *                        search operation.
     * @param webRequest The WebRequest to which we sent back the response
     *                   to the user that operates using a browser.
     * @param searchQuery The search query string of the search operation.
     */
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

    /**
     * Register the results returned by each partition.
     * 
     * @param partitionID The ID of the partition to which the responding node
     *                    belongs.
     * @param results The results returned by that specific partition.
     */
    public void registerSearchResults(int partitionID, ArrayList<Entry> results) {
        this.searchResults.put(partitionID, results);
        searchResultsChecklist.put(partitionID, Boolean.TRUE);
    }

    /**
     * Method to check if we got the responses from all the partitions.
     * 
     * @return True if we have responses from all the partitions, false otherwise.
     */
    public boolean isSearchComplete() {
        boolean complete = true;
        for (Integer key : this.searchResultsChecklist.keySet()) {
            if (searchResultsChecklist.get(key).equals(Boolean.FALSE)) {
                complete = false;
            }
        }
        return complete;
    }

    /**
     * Get a list with the results of all the partitions.
     * 
     * @return A list of all the entries returned by the different partitions.
     */
    public ArrayList<Entry> getMergedSearchResults() {
        ArrayList<Entry> results = new ArrayList<Entry>();
        for (Integer key : this.searchResults.keySet()) {
            results.addAll(this.searchResults.get(key));
        }
        return results;
    }

    /**
     * Get a list of partition IDs specifying which partitions haven't
     * answered yet to a search request.
     * 
     * @return A list of the partition IDs.
     */
    public ArrayList<Integer> getMissingResponses() {
        ArrayList<Integer> missingPartitionResponses = new ArrayList<Integer>();
        for (Integer key : this.searchResultsChecklist.keySet()) {
            if (searchResultsChecklist.get(key).equals(Boolean.FALSE)) {
                missingPartitionResponses.add(key);
            }
        }
        return missingPartitionResponses;
    }

    /**
     * Get the WebRequest object to respond to.
     * 
     * @return The WebRequest object.
     */
    public WebRequest getWebRequest() {
        return webRequest;
    }

    /**
     * Get the search query string.
     * 
     * @return The search query string.
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Get the timeout ID associated with the specific pending search operation.
     * 
     * @return The timeout ID.
     */
    public UUID getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Set the timeout ID associated with the specific pending search operation.
     * 
     * @param queryTimeout The timeout ID.
     */
    public void setQueryTimeout(UUID queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}
