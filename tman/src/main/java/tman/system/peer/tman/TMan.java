package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.PeerAddress;
import java.util.ArrayList;

import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
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
    private TManConfiguration tmanConfiguration;
    private boolean electing = false;
    private PeerAddress leader = null;
    private long electionID = 0;
    private Random r;
    private int roundCounter = 0;

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
        subscribe(handleElectionTimeout, timerPort);
        subscribe(handleCoordinatorTimeout, timerPort);
    }
//-------------------------------------------------------------------	
    Handler<TManInit> handleInit = new Handler<TManInit>() {
        @Override
        public void handle(TManInit init) {
            self = init.getSelf();
            tmanConfiguration = init.getConfiguration();
            period = tmanConfiguration.getPeriod();

            SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
            rst.setTimeoutEvent(new TManSchedule(rst));
            trigger(rst, timerPort);
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
    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {
        @Override
        public void handle(CyclonSample event) {
            roundCounter++;
            ArrayList<PeerAddress> cyclonPartners = event.getSample();
            merge(cyclonPartners);
        }
    };
    
    private void merge(ArrayList<PeerAddress> partners) {
//            System.err.println("=====================================================================================");
//            System.err.println("[TMAN::" + self.getPeerId() + "] Cyclon Partners:" + cyclonPartners);
//            System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners:" + tmanPartners);
        if (!partners.isEmpty()) {
//                System.err.println("[TMAN::" + self.getPeerId() + "] Random Peer:" + randomPeer);
            for (PeerAddress cyclonPeer : partners) {
                if (!tmanPartners.contains(cyclonPeer)) {
                    if (tmanPartners.size() >= SIMILARITY_LIST_SIZE) {
                        // Sorting based on preference function to find the least prefered node.
                        UtilityComparator uc = new UtilityComparator(self);
                        Collections.sort(tmanPartners, uc);
//                        System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners (sorted):" + tmanPartners);
                        if (uc.compare(tmanPartners.get(0), cyclonPeer) == -1) {
                            tmanPartners.set(0, cyclonPeer);
                        }
//                        System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners (swapped):" + tmanPartners);
                    }
                    else {
                        tmanPartners.add(cyclonPeer);
//                        System.err.println("[TMAN::" + self.getPeerId() + "] New Peer:" + tmanPartners.get(0));
                    }
                }
            }

            // Keep track of consecutive same partner lists to detect convergence
            if (compareList(tmanPartners, tmanPrevPartners)) {
                convergenceCount++;
            }
            else {
                convergenceCount = 0;
            }

//                    System.out.println("[" + self.getPeerId() + "] Convergence count is " + convergenceCount);

            if (!electing && convergenceCount == CONVERGENCE_CONSTANT && self.getPeerId().equals(self.getPeerId().max(maximumUtility(tmanPartners)))) {
                electing = true;
                System.err.println("[ELECTION::" + self.getPeerId() + "] I think I am the leader!");
                startLeaderElection();
            }

            if (convergenceCount == CONVERGENCE_CONSTANT) {
                System.err.println("[TMAN::" + self.getPeerId() + "] I converged in " + (roundCounter - 10) + " rounds!");
            }

            tmanPrevPartners.clear();
            tmanPrevPartners.addAll(tmanPartners);
        }
//            System.err.println("=====================================================================================");
    }
    
    private void startLeaderElection() {
        ArrayList<PeerAddress> electionGroup = new ArrayList<PeerAddress>(tmanPartners);
        electionGroup.add(self);
        System.err.println("[ELECTION::" + self.getPeerId() + "] The election group is " + electionGroup);
        for(PeerAddress peer : tmanPartners) {
            trigger(new ThinkLeaderMessage(self, peer, electionGroup), networkPort);
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
                    trigger(new CoordinatorMessage(self, peer), networkPort);
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
                            trigger(new CoordinatorMessage(self, peer), networkPort);
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
            electing = false;
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
//-------------------------------------------------------------------	
    Handler<ExchangeMsg.Request> handleTManPartnersRequest = new Handler<ExchangeMsg.Request>() {
        @Override
        public void handle(ExchangeMsg.Request event) {

        }
    };
    
    Handler<ExchangeMsg.Response> handleTManPartnersResponse = new Handler<ExchangeMsg.Response>() {
        @Override
        public void handle(ExchangeMsg.Response event) {

        }
    };

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
