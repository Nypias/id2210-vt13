package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;

/**
 * Message sent by a peer during the Bully leader election when he receives
 * an election message by a peer with lower utility than his.
 */
public class OKMessage extends PeerMessage
{
    /** The list of peers that are part of the election */
    private ArrayList<PeerAddress> electionGroup;

    /**
     * Create a new OKMessage specifying the peer that sends the message,
     * the peer that will receive the messages and the list of peers that are
     * part of the ongoing election.
     * 
     * @param source The peer that sends the message.
     * @param destination The peer that will receive the message.
     * @param electionGroup The list of peers that are part of the ongoing election.
     */
    public OKMessage(PeerAddress source, PeerAddress destination, ArrayList<PeerAddress> electionGroup) {
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
