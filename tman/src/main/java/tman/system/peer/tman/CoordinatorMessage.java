package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class CoordinatorMessage extends PeerMessage
{
    private PeerAddress leader;

    public CoordinatorMessage(PeerAddress source, PeerAddress destination) {
        super(source, destination);
        this.leader = source;
    }

    public PeerAddress getLeader() {
        return leader;
    }
}
