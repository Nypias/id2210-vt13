package tman.system.peer.tman;

import java.util.ArrayList;

import common.peer.PeerAddress;

import se.sics.kompics.Event;


/**
 * Event sent from the TMan component to notify other components of the current
 * state of the gradient partners and the leader.
 */
public class TManSample extends Event
{
    /**
     * The list of partners the peer currently has on the gradient
     */
    ArrayList<PeerAddress> partners = new ArrayList<PeerAddress>();
    /**
     * The leader (if any) that has been selected.
     */
    PeerAddress leader;

    /**
     * Create a TManSample event specifying the gradient partners and leader.
     *
     * @param partners The gradient partners the peer has.
     * @param leader The leader the peer knows about (if any).
     */
    public TManSample(ArrayList<PeerAddress> partners, PeerAddress leader) {
        this.partners.addAll(partners);
        this.leader = leader;
    }

    /**
     * Get the gradient partners list.
     *
     * @return The list of gradient partners.
     */
    public ArrayList<PeerAddress> getSample() {
        return this.partners;
    }

    /**
     * Get the leader.
     *
     * @return The leader that was selected (if any).
     */
    public PeerAddress getLeader() {
        return leader;
    }
}
