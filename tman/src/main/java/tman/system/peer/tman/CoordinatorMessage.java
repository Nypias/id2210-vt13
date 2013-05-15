package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class CoordinatorMessage extends PeerMessage
{
    public CoordinatorMessage(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
