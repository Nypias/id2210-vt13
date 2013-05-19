package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


/**
 * Message sent by a peer to start a Bully election when someone thinks it is
 * the leader or when the leader is suspected to have died.
 */
public class ElectionMessage extends PeerMessage
{
    /**
     * The list of peers that are part of the election.
     */
    private ArrayList<PeerAddress> electionGroup;

    /**
     * Create a new ElectionMessage specifying the peer that sends the message,
     * the peer that will receive the messages and the list of peers that are
     * part of the (re)starting election.
     *
     * @param source The peer that sends the message.
     * @param destination The peer that will receive the message.
     * @param electionGroup The list of peers that are part of the ongoing
     * election.
     */
    public ElectionMessage(PeerAddress source, PeerAddress destination, ArrayList<PeerAddress> electionGroup) {
        super(source, destination);
        this.electionGroup = electionGroup;
    }

    /**
     * Get the list of peers that are part of the election.
     *
     * @return The list of peers that are part of the election.
     */
    public ArrayList<PeerAddress> getElectionGroup() {
        return electionGroup;
    }
}
