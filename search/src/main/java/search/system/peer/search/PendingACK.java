package search.system.peer.search;

import common.peer.PeerAddress;
import java.util.UUID;


public class PendingACK
{
    private int receivedACKs;
    private PeerAddress requestSource;
    private UUID timeoutID;

    public PendingACK(UUID timeoutID, PeerAddress requestSource) {
        this.receivedACKs = 0;
        this.timeoutID = timeoutID;
        this.requestSource = requestSource;
    }

    public int getReceivedACKs() {
        return receivedACKs;
    }

    public void setReceivedACKs(int receivedACKs) {
        this.receivedACKs = receivedACKs;
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
}
