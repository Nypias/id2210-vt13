package search.system.peer.search;

import se.sics.kompics.web.WebRequest;


public class PendingEntry
{
    private String pendingEntryID;
    private WebRequest webRequest;
    private Entry entry;
    private int addEntryTriesCounter;

    public PendingEntry(String pendingEntryID, WebRequest webRequest, Entry entry) {
        this.pendingEntryID = pendingEntryID;
        this.webRequest = webRequest;
        this.entry = entry;
    }

    public String getPendingEntryID() {
        return pendingEntryID;
    }

    public WebRequest getWebRequest() {
        return webRequest;
    }

    public Entry getEntry() {
        return entry;
    }

    public void incAddEntryTriesCounter() {
        this.addEntryTriesCounter++;
    }

    public void decAddEntryTriesCounter() {
        this.addEntryTriesCounter--;
    }

    public int getAddEntryTriesCounter() {
        return addEntryTriesCounter;
    }
}
