package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


public class CoordinatorMessage extends PeerMessage
{
    private PeerAddress leader;
    private ArrayList<PeerAddress> electionGroup;

    public CoordinatorMessage(PeerAddress source, PeerAddress destination, ArrayList<PeerAddress> electionGroup) {
        super(source, destination);
        this.leader = source;
        this.electionGroup = electionGroup;
    }

    public PeerAddress getLeader() {
        return leader;
    }
    
    public ArrayList<PeerAddress> getElectionGroup() {
        return electionGroup;
    }
}
