package search.system.peer.search;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


public class IndexUpdateRequest extends PeerMessage
{
    ArrayList<Range> missingRanges;
    int lastExisting;

    public IndexUpdateRequest(PeerAddress source, PeerAddress destination, ArrayList<Range> missingRanges, int lastExisting) {
        super(source, destination);
        this.missingRanges = missingRanges;
        this.lastExisting = lastExisting;
    }

    public ArrayList<Range> getMissingRanges() {
        return missingRanges;
    }

    public void setMissingRanges(ArrayList<Range> missingRanges) {
        this.missingRanges = missingRanges;
    }

    public int getLastExisting() {
        return lastExisting;
    }

    public void setLastExisting(int lastExisting) {
        this.lastExisting = lastExisting;
    }
}
