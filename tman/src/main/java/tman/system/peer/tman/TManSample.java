package tman.system.peer.tman;

import java.util.ArrayList;

import common.peer.PeerAddress;

import se.sics.kompics.Event;


public class TManSample extends Event
{
    ArrayList<PeerAddress> partners = new ArrayList<PeerAddress>();
    PeerAddress leader;

    public TManSample() {
    }

    public TManSample(ArrayList<PeerAddress> partners, PeerAddress leader) {
        this.partners.addAll(partners);
        this.leader = leader;
    }

    public ArrayList<PeerAddress> getSample() {
        return this.partners;
    }

    public PeerAddress getLeader() {
        return leader;
    }
}
