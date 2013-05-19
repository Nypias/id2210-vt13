package search.system.peer.search;

import common.peer.PeerAddress;
import java.util.UUID;


public class PendingACK
{
    private int receivedACKs;
    private PeerAddress requestSource;
    private UUID timeoutID;
    private String newEntryTempID;

    public PendingACK(UUID timeoutID, PeerAddress requestSource, String newEntryTempID) {
        this.receivedACKs = 0;
        this.timeoutID = timeoutID;
        this.requestSource = requestSource;
        this.newEntryTempID = newEntryTempID;
    }

    public int getReceivedACKs() {
        return receivedACKs;
    }

    public void incReceivedACKs() {
        this.receivedACKs++;
    }

    public void decReceivedACKs() {
        this.receivedACKs--;
    }

    public PeerAddress getRequestSource() {
        return requestSource;
    }

    public UUID getTimeoutID() {
        return timeoutID;
    }

    public String getNewEntryTempID() {
        return newEntryTempID;
    }
}
