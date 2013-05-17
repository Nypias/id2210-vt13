package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.PeerAddress;
import java.util.ArrayList;

import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Stop;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.CancelTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import tman.simulator.snapshot.Snapshot;

public final class TMan extends ComponentDefinition {

    private final int SIMILARITY_LIST_SIZE = 3;
    private final int CONVERGENCE_CONSTANT = 10;
    private final int BULLY_TIMEOUT = 2000;
    private final double SOFT_MAX_TEMPERATURE = 1.0;
    private final int HEARTBEAT_TIMEOUT = 5000;
    
    Negative<TManSamplePort> tmanPartnersPort = negative(TManSamplePort.class);
    Positive<CyclonSamplePort> cyclonSamplePort = positive(CyclonSamplePort.class);
    Positive<Network> networkPort = positive(Network.class);
    Positive<Timer> timerPort = positive(Timer.class);
    private long period;
    private PeerAddress self;
    private ArrayList<PeerAddress> tmanPartners;
    private ArrayList<PeerAddress> tmanPrevPartners;
    private int convergenceCount = 0;
    private UUID timeoutId;
    private UUID heartbeatTimeoutId;
    private TManConfiguration tmanConfiguration;
    private boolean electing = false;
    private PeerAddress leader = null;
    private long electionID = 0;
    private Random r = new Random();
    private UtilityComparator uc;
    private int roundCounter = 0;
    private ArrayList<PeerAddress> electionGroup;
    private boolean imDead = false;

    public class TManSchedule extends Timeout {

        public TManSchedule(SchedulePeriodicTimeout request) {
            super(request);
        }

//-------------------------------------------------------------------
        public TManSchedule(ScheduleTimeout request) {
            super(request);
        }
    }

//-------------------------------------------------------------------	
    public TMan() {
        tmanPartners = new ArrayList<PeerAddress>();
        tmanPrevPartners = new ArrayList<PeerAddress>();
        
        subscribe(handleInit, control);
        subscribe(handleRound, timerPort);
        subscribe(handleCyclonSample, cyclonSamplePort);
        subscribe(handleTManPartnersResponse, networkPort);
        subscribe(handleTManPartnersRequest, networkPort);
        subscribe(handleThinkLeaderMessage, networkPort);
        subscribe(handleElectionMessage, networkPort);
        subscribe(handleOKMessage, networkPort);
        subscribe(handleCoordinatorMessage, networkPort);
        subscribe(handleHeartbeatLeader, networkPort);
        subscribe(handleHeartbeatLeaderResponse, networkPort);
        
        subscribe(handleElectionTimeout, timerPort);
        subscribe(handleCoordinatorTimeout, timerPort);
        subscribe(handleTManGossipTimeout, timerPort);
        subscribe(handleHeartbeatTimeout, timerPort);
        subscribe(handleHeartbeatLeaderTimeout, timerPort);
        subscribe(handleLeaderSuicideTimeout, timerPort);
    }
//-------------------------------------------------------------------	
    Handler<TManInit> handleInit = new Handler<TManInit>() {
        @Override
        public void handle(TManInit init) {
            self = init.getSelf();
            tmanConfiguration = init.getConfiguration();
            period = tmanConfiguration.getPeriod();
            uc = new UtilityComparator(self);
            
            SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
            rst.setTimeoutEvent(new TManSchedule(rst));
            trigger(rst, timerPort);
            
            SchedulePeriodicTimeout grst = new SchedulePeriodicTimeout(period, period);
            grst.setTimeoutEvent(new TManGossipTimeout(grst));
            trigger(grst, timerPort);
        }
    };
//-------------------------------------------------------------------	
    Handler<TManSchedule> handleRound = new Handler<TManSchedule>() {
        @Override
        public void handle(TManSchedule event) {
            Snapshot.updateTManPartners(self, tmanPartners);

            // Publish sample to connected components
            trigger(new TManSample(tmanPartners), tmanPartnersPort);
        }
    };
//-------------------------------------------------------------------	
    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>()
    {
        @Override
        public void handle(CyclonSample event) {
            roundCounter++;
            ArrayList<PeerAddress> cyclonPartners = event.getSample();
            //            System.err.println("=====================================================================================");
//            System.err.println("[TMAN::" + self.getPeerId() + "] Cyclon Partners:" + cyclonPartners);
//            System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners:" + tmanPartners);
            // If provided list is empty don't do anything
            if (!cyclonPartners.isEmpty()) {
//                System.err.println("[TMAN::" + self.getPeerId() + "] Random Peer:" + randomPeer);
                // Loop through the provided partners and select the ones that we prefer

                tmanPartners.addAll(cyclonPartners);
                tmanPartners = removeDuplicates(tmanPartners);
                if (tmanPartners.contains(self)) {
                    tmanPartners.remove(self);
                }

                Collections.sort(tmanPartners, uc);
                if (tmanPartners.size() > SIMILARITY_LIST_SIZE) {
                    tmanPartners = new ArrayList<PeerAddress>(tmanPartners.subList(tmanPartners.size() - SIMILARITY_LIST_SIZE, tmanPartners.size()));
                }

//            for (PeerAddress cyclonPeer : partners) {
//                // If the peer is not already in our list
//                if (!tmanPartners.contains(cyclonPeer)) {
//                    // If our list is full we swap with an existing peer
//                    if (tmanPartners.size() >= SIMILARITY_LIST_SIZE) {
//                        // Sorting based on preference function to find the least prefered node (top node).
//                        Collections.sort(tmanPartners, uc);
////                        System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners (sorted):" + tmanPartners);
//                        // If we prefer the new peer more than our least prefered peer we swap
//                        if (uc.compare(tmanPartners.get(0), cyclonPeer) == -1) {
//                            tmanPartners.set(0, cyclonPeer);
//                        }
////                        System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners (swapped):" + tmanPartners);
//                    }
//                    else {
//                        // If the list is not yet full we just add the peer
//                        tmanPartners.add(cyclonPeer);
////                        System.err.println("[TMAN::" + self.getPeerId() + "] New Peer:" + tmanPartners.get(0));
//                    }
//                }
//            }

                // Keep track of consecutive same partner lists to detect convergence
                if (compareList(tmanPartners, tmanPrevPartners)) {
                    convergenceCount++;
                }
                else {
                    convergenceCount = 0;
                }

//                    System.out.println("[" + self.getPeerId() + "] Convergence count is " + convergenceCount);

                if (!electing && convergenceCount == CONVERGENCE_CONSTANT && self.getPeerId().equals(self.getPeerId().max(maximumUtility(tmanPartners)))) {
                    // Check if a leader exists first and if he is smaller than us we can start the election!

                    electing = true;
                    System.err.println("[ELECTION::" + self.getPeerId() + "] I think I am the leader!");
                    startLeaderElection();
                }

                tmanPrevPartners.clear();
                tmanPrevPartners.addAll(tmanPartners);
            }
//            System.err.println("=====================================================================================");
        }
    };
    
    private void startLeaderElection() {
        ArrayList<PeerAddress> initialElectionGroup = new ArrayList<PeerAddress>(tmanPartners);
        initialElectionGroup.add(self);
        System.err.println("[ELECTION::" + self.getPeerId() + "] The election group is " + initialElectionGroup);
        for(PeerAddress peer : tmanPartners) {
            trigger(new ThinkLeaderMessage(self, peer, initialElectionGroup), networkPort);
        }
    }
    
    Handler<ThinkLeaderMessage> handleThinkLeaderMessage = new Handler<ThinkLeaderMessage>() {
        @Override
        public void handle(ThinkLeaderMessage event) {
            electing = true;
            ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
            System.err.println("[ELECTION::" + self.getPeerId() + "] I got a THINK_LEADER message!");
            if(self.getPeerId().equals(minimumUtility(electionGroup))) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I am eligible to start the election! (" + minimumUtility(electionGroup) + ")");
                for(PeerAddress peer : electionGroup) {
                    if(!self.getPeerId().equals(peer.getPeerId())) {
                        trigger(new ElectionMessage(self, peer, electionGroup), networkPort);
                    }
                }
                
                ScheduleTimeout st = new ScheduleTimeout(BULLY_TIMEOUT);
                st.setTimeoutEvent(new ElectionTimeout(st, electionGroup));
                timeoutId = st.getTimeoutEvent().getTimeoutId();
                trigger(st, timerPort);
            }
        }
    };
    
    Handler<ElectionTimeout> handleElectionTimeout = new Handler<ElectionTimeout>() {
        @Override
        public void handle(ElectionTimeout event) {
            if(electing) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got an election timeout!");
                ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
                for(PeerAddress peer : electionGroup) {
                    trigger(new CoordinatorMessage(self, peer, electionGroup), networkPort);
                }
            }
        }
    };
    
    Handler<CoordinatorTimeout> handleCoordinatorTimeout = new Handler<CoordinatorTimeout>() {
        @Override
        public void handle(CoordinatorTimeout event) {
            if(electing) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got a coordinator timeout!");
                ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
                for (PeerAddress peer : electionGroup) {
                    if (self.getPeerId().compareTo(peer.getPeerId()) == -1) {
                        trigger(new ElectionMessage(self, peer, electionGroup), networkPort);

                        ScheduleTimeout st = new ScheduleTimeout(BULLY_TIMEOUT);
                        st.setTimeoutEvent(new ElectionTimeout(st, electionGroup));
                        timeoutId = st.getTimeoutEvent().getTimeoutId();
                        trigger(st, timerPort);
                    }
                }
            }
        }
    };
    
    Handler<ElectionMessage> handleElectionMessage = new Handler<ElectionMessage>() {
        @Override
        public void handle(ElectionMessage event) {
            if(electing) {
                boolean sentElectionMessage = false;
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got an election message from " + event.getPeerSource().getPeerId() + "!");
                BigInteger sender = event.getPeerSource().getPeerId();
                if(sender.compareTo(self.getPeerId()) == -1) {
                    trigger(new OKMessage(self, event.getPeerSource(), event.getElectionGroup()), networkPort);
                    ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
                    for(PeerAddress peer : electionGroup) {
                        if(self.getPeerId().compareTo(peer.getPeerId()) == -1) {
                            sentElectionMessage = true;
                            trigger(new ElectionMessage(self, peer, electionGroup), networkPort);

                            ScheduleTimeout st = new ScheduleTimeout(BULLY_TIMEOUT);
                            st.setTimeoutEvent(new ElectionTimeout(st, electionGroup));
                            timeoutId = st.getTimeoutEvent().getTimeoutId();
                            trigger(st, timerPort);
                        }
                    }

                    if (!sentElectionMessage) {
                        for (PeerAddress peer : electionGroup) {
                            trigger(new CoordinatorMessage(self, peer, electionGroup), networkPort);
                        }
                    }
                }
            }
        }
    };
    
    Handler<OKMessage> handleOKMessage = new Handler<OKMessage>() {
        @Override
        public void handle(OKMessage event) {
            if(electing) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got an OK message from " + event.getPeerSource().getPeerId() + "!");
                CancelTimeout ct = new CancelTimeout(timeoutId);
                trigger(ct, timerPort);

                ScheduleTimeout st = new ScheduleTimeout(2 * BULLY_TIMEOUT);
                st.setTimeoutEvent(new CoordinatorTimeout(st, event.getElectionGroup()));
                timeoutId = st.getTimeoutEvent().getTimeoutId();
                trigger(st, timerPort);
            }
        }
    };
    
    Handler<CoordinatorMessage> handleCoordinatorMessage = new Handler<CoordinatorMessage>() {
        @Override
        public void handle(CoordinatorMessage event) {
            System.err.println("[ELECTION::" + self.getPeerId() + "] I got a coordinator message from " + event.getPeerSource().getPeerId() + " the leader is " + event.getLeader().getPeerId() + "!");
            CancelTimeout ct = new CancelTimeout(timeoutId);
            trigger(ct, timerPort);
            
            leader = event.getLeader();
            electionGroup = event.getElectionGroup();
            electing = false;
            imDead = false;
            
            if (self.getPeerId().compareTo(leader.getPeerId()) != 0) {
                ScheduleTimeout heartbeatTimeout = new ScheduleTimeout(HEARTBEAT_TIMEOUT);
                heartbeatTimeout.setTimeoutEvent(new HeartbeatTimeout(heartbeatTimeout));
                trigger(heartbeatTimeout, timerPort);
            }
            
            if(self.getPeerId().equals(leader.getPeerId())) {
                ScheduleTimeout heartbeatTimeout = new ScheduleTimeout(20000);
                heartbeatTimeout.setTimeoutEvent(new LeaderSuicide(heartbeatTimeout));
                trigger(heartbeatTimeout, timerPort);
            }
        }
    };
    
    Handler<LeaderSuicide> handleLeaderSuicideTimeout = new Handler<LeaderSuicide>() {
        @Override
        public void handle(LeaderSuicide event) {
            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I am the leader and I'm killing myself gdwdgwwdtwndgntynndg...!! :( ");
            imDead = true;
        }
    };
    
    Handler<HeartbeatTimeout> handleHeartbeatTimeout = new Handler<HeartbeatTimeout>() {
        @Override
        public void handle(HeartbeatTimeout event) {
            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I am sending a heartbeat to the leader (" + leader.getPeerId() + ") ");
            trigger(new HeartbeatLeader(self, leader), networkPort);
            
            ScheduleTimeout st = new ScheduleTimeout(HEARTBEAT_TIMEOUT);
            st.setTimeoutEvent(new HeartbeatLeaderTimeout(st));
            heartbeatTimeoutId = st.getTimeoutEvent().getTimeoutId();
            trigger(st, timerPort);
        }
    };
    
    Handler<HeartbeatLeaderTimeout> handleHeartbeatLeaderTimeout = new Handler<HeartbeatLeaderTimeout>() {
        @Override
        public void handle(HeartbeatLeaderTimeout event) {
            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I got a leader heartbeat timeout!");
            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] Srating election with group " + electionGroup + "!");
            for (PeerAddress peer : electionGroup) {
                if (self.getPeerId().compareTo(peer.getPeerId()) == -1) {
                    trigger(new ElectionMessage(self, peer, electionGroup), networkPort);
                }
            }

            ScheduleTimeout st = new ScheduleTimeout(BULLY_TIMEOUT);
            st.setTimeoutEvent(new ElectionTimeout(st, electionGroup));
            timeoutId = st.getTimeoutEvent().getTimeoutId();
            trigger(st, timerPort);
        }
    };
    
    Handler<HeartbeatLeader> handleHeartbeatLeader = new Handler<HeartbeatLeader>() {
        @Override
        public void handle(HeartbeatLeader event) {
            if (!imDead || true) {
                System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I am the leader and I got a heartbeat from " + event.getPeerSource().getPeerId() + "!");
                trigger(new HeartbeatLeaderResponse(self, event.getPeerSource()), networkPort);
            }
        }
    };
    
    Handler<HeartbeatLeaderResponse> handleHeartbeatLeaderResponse = new Handler<HeartbeatLeaderResponse>() {
        @Override
        public void handle(HeartbeatLeaderResponse event) {
            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I got a leader heartbeat response!");
            CancelTimeout ct = new CancelTimeout(heartbeatTimeoutId);
            trigger(ct, timerPort);
            
            ScheduleTimeout heartbeatTimeout = new ScheduleTimeout(HEARTBEAT_TIMEOUT);
            heartbeatTimeout.setTimeoutEvent(new HeartbeatTimeout(heartbeatTimeout));
            trigger(heartbeatTimeout, timerPort);
        }
    };
    
    private boolean compareList(ArrayList<PeerAddress> a, ArrayList<PeerAddress> b) {
        if(a.size() == b.size()) {
            for(int i = 0; i < a.size(); i++) {
                if(!a.get(i).getPeerId().equals(b.get(i).getPeerId())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private BigInteger minimumUtility(ArrayList<PeerAddress> list) {
        BigInteger min = null;
        if (!list.isEmpty()) {
            min = list.get(0).getPeerId();
            
            for(PeerAddress peer : list) {
                if(peer.getPeerId().compareTo(min) == -1) {
                    min = peer.getPeerId();
                }
            }
        }
        return min;
    }
    
    private BigInteger maximumUtility(ArrayList<PeerAddress> list) {
        BigInteger max = null;
        if (!list.isEmpty()) {
            max = list.get(0).getPeerId();
            
            for(PeerAddress peer : list) {
                if(peer.getPeerId().compareTo(max) == 1) {
                    max = peer.getPeerId();
                }
            }
        }
        return max;
    }
//------------------------------------------------------------------
    Handler<TManGossipTimeout> handleTManGossipTimeout = new Handler<TManGossipTimeout>() {
        @Override
        public void handle(TManGossipTimeout event) {
            // If we have at least one peer in our similarity list
            if(tmanPartners.size() > 0) {
                // Select the most prefered node to exchange preference lists with
//                System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners are " + tmanPartners);
                PeerAddress shufflePeer = getSoftMaxAddress(tmanPartners);
                ArrayList<PeerAddress> shuffleView = prepareShuffleView(tmanPartners, shufflePeer);
//                System.err.println("[GOSSIP::" + self.getPeerId() + "] I am sending " + shuffleView + " to " + shufflePeer);
                trigger(new ExchangeMsg.Request(event.getTimeoutId(), shuffleView, self, shufflePeer), networkPort);
            }
        }
    };
    
    Handler<ExchangeMsg.Request> handleTManPartnersRequest = new Handler<ExchangeMsg.Request>() {
        @Override
        public void handle(ExchangeMsg.Request event) {
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners are " + tmanPartners);
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] Received " + event.getSimilaritySet() + " from " + event.getPeerSource());
//            merge(event.getSimilaritySet());
            
            tmanPartners.addAll(event.getSimilaritySet());
            tmanPartners = removeDuplicates(tmanPartners);
            if(tmanPartners.contains(self)) {
                tmanPartners.remove(self);
            }
            
            Collections.sort(tmanPartners, uc);
            if(tmanPartners.size() > SIMILARITY_LIST_SIZE) {
                tmanPartners = new ArrayList<PeerAddress>(tmanPartners.subList(tmanPartners.size() - SIMILARITY_LIST_SIZE, tmanPartners.size()));
            }

//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners now are " + tmanPartners);
            ArrayList<PeerAddress> shuffleView = prepareShuffleView(tmanPartners, event.getPeerSource());
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] I am sending " + shuffleView + " to " + event.getPeerSource());
            trigger(new ExchangeMsg.Response(UUID.randomUUID(), shuffleView, self, event.getPeerSource()), networkPort);
        }
    };
    
    Handler<ExchangeMsg.Response> handleTManPartnersResponse = new Handler<ExchangeMsg.Response>() {
        @Override
        public void handle(ExchangeMsg.Response event) {
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners are " + tmanPartners);
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] Received " + event.getSelectedBuffer() + " from " + event.getPeerSource());
//            merge(event.getSelectedBuffer());
            tmanPartners.addAll(event.getSimilaritySet());
            tmanPartners = removeDuplicates(tmanPartners);
            if(tmanPartners.contains(self)) {
                tmanPartners.remove(self);
            }
            
            Collections.sort(tmanPartners, uc);
            if(tmanPartners.size() > SIMILARITY_LIST_SIZE) {
                tmanPartners = new ArrayList<PeerAddress>(tmanPartners.subList(tmanPartners.size() - SIMILARITY_LIST_SIZE, tmanPartners.size()));
            }
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners now are " + tmanPartners);
        }
    };
    
    private ArrayList<PeerAddress> removeDuplicates(ArrayList<PeerAddress> partners) {
        HashSet hs = new HashSet();
        hs.addAll(partners);
        partners.clear();
        partners.addAll(hs);
        
        return partners;
    }
    
    private ArrayList<PeerAddress> prepareShuffleView(ArrayList<PeerAddress> partners, PeerAddress shufflePeer) {
        ArrayList<PeerAddress> shuffleView = new ArrayList<PeerAddress>(partners);
        shuffleView.add(self);
        shuffleView.remove(shufflePeer);
        
        return shuffleView;
    }

    // TODO - if you call this method with a list of entries, it will
    // return a single node, weighted towards the 'best' node (as defined by
    // ComparatorByID) with the temperature controlling the weighting.
    // A temperature of '1.0' will be greedy and always return the best node.
    // A temperature of '0.000001' will return a random node.
    // A temperature of '0.0' will throw a divide by zero exception :)
    // Reference:
    // http://webdocs.cs.ualberta.ca/~sutton/book/2/node4.html
    private PeerAddress getSoftMaxAddress(ArrayList<PeerAddress> entries) {
        Collections.sort(entries, new UtilityComparator(self));

        double rnd = r.nextDouble();
        double total = 0.0d;
        double[] values = new double[entries.size()];
        int j = entries.size() + 1;
        for (int i = 0; i < entries.size(); i++) {
            // get inverse of values - lowest have highest value.
            double val = j;
            j--;
            values[i] = Math.exp(val / SOFT_MAX_TEMPERATURE);
            total += values[i];
        }

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                values[i] += values[i - 1];
            }
            // normalise the probability for this entry
            double normalisedUtility = values[i] / total;
            if (normalisedUtility >= rnd) {
                return entries.get(i);
            }
        }
        return entries.get(entries.size() - 1);
    }
}
