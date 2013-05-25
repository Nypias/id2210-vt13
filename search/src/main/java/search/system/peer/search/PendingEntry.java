package search.system.peer.search;

import se.sics.kompics.web.WebRequest;


/**
 * Class to represent a pending entry addition operation.
 */
public class PendingEntry
{
    /**
     * Pending entry ID, to keep track of multiple entries being added by the
     * same node.
     */
    private String pendingEntryID;
    /**
     * WebRequest object to send a response back to the user's browser.
     */
    private WebRequest webRequest;
    /**
     * The new entry being added.
     */
    private Entry entry;
    /**
     * The number of failed attempts to add that specific entry.
     */
    private int addEntryTriesCounter;

    /**
     * Create a new PendingEntry specifying the pending entry ID, the WebRequest
     * object and the new entry to be added.
     *
     * @param pendingEntryID The pending entry ID.
     * @param webRequest The WebRequest object.
     * @param entry The new entry to be added.
     */
    public PendingEntry(String pendingEntryID, WebRequest webRequest, Entry entry) {
        this.pendingEntryID = pendingEntryID;
        this.webRequest = webRequest;
        this.entry = entry;
    }

    /**
     * Get the pending entry ID.
     *
     * @return The pending entry ID.
     */
    public String getPendingEntryID() {
        return pendingEntryID;
    }

    /**
     * Get the WebRequest object.
     *
     * @return The WebRequest object.
     */
    public WebRequest getWebRequest() {
        return webRequest;
    }

    /**
     * Get the new entry to be added.
     *
     * @return The new entry.
     */
    public Entry getEntry() {
        return entry;
    }

    /**
     * Increment the failed attempts counter by one.
     */
    public void incAddEntryTriesCounter() {
        this.addEntryTriesCounter++;
    }

    /**
     * Decrement the failed attempts counter by one.
     */
    public void decAddEntryTriesCounter() {
        this.addEntryTriesCounter--;
    }

    /**
     * Get the number of failed attempts to add the new entry.
     *
     * @return The number of failed attempts.
     */
    public int getAddEntryTriesCounter() {
        return addEntryTriesCounter;
    }
}
