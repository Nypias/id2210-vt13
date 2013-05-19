package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;

/**
 * Message sent by the leader as a response to a HeartbeatLeader message
 * sent by a peer in the election group.
 */
public class HeartbeatLeaderResponse extends PeerMessage
{
    /**
     * Create a new HeartbeatLeaderResponse message specifying the peer 
     * that sends the message and the peer that will receive the message.
     * 
     * @param source The peer that sends the message.
     * @param destination The peer that will receive the message.
     */
    public HeartbeatLeaderResponse(PeerAddress source, PeerAddress destination) {
        super(source, destination);
    }
}
