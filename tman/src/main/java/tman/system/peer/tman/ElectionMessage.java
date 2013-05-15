package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


public class ElectionMessage extends PeerMessage
{
    public ElectionMessage(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
