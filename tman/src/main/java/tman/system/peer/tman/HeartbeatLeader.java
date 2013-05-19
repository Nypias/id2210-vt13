package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;


/**
 * Message sent by a peer, from the election group, to the leader in order to
 * check if it is still alive.
 */
public class HeartbeatLeader extends PeerMessage
{
    /**
     * Create a new HeartbeatLeader message specifying the peer that sends the
     * message and the peer that will receive the message.
     *
     * @param source The peer that sends the message.
     * @param destination The peer that will receive the message.
     */
    public HeartbeatLeader(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
