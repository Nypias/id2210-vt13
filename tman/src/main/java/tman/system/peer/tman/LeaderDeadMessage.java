package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;


/**
 * Message sent by a peer, that thinks it is the leader, to its neighbors in
 * order to start an election.
 */
public class LeaderDeadMessage extends PeerMessage
{
    /**
     * The peers that will be part of the election
     */
    private ArrayList<PeerAddress> electionGroup;

    /**
     * Create a new ThinkLeaderMessage specifying the election group
     *
     * @param source Peer that sends the ThinkLeaderMessage message.
     * @param destination Peer that will receive the ThinkLeaderMessage message.
     * @param electionGroup The election group that will be part of the
     * election.
     */
    public LeaderDeadMessage(PeerAddress source, PeerAddress destination, ArrayList<PeerAddress> electionGroup) {
        super(source, destination);
        this.electionGroup = electionGroup;
    }

    /**
     * Get the list of the peers that will be part of the election.
     *
     * @return The list of peers that are part of the election group.
     */
    public ArrayList<PeerAddress> getElectionGroup() {
        return electionGroup;
    }
}
