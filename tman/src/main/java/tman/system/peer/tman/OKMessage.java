package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;

public class OKMessage extends PeerMessage
{
    public OKMessage(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
