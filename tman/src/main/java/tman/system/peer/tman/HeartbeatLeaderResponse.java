package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class HeartbeatLeaderResponse extends PeerMessage
{
    public HeartbeatLeaderResponse(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
