package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class HeartbeatLeader extends PeerMessage
{
    public HeartbeatLeader(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
