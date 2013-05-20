package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.PeerAddress;
import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import se.sics.kompics.timer.Timer;
import tman.simulator.snapshot.Snapshot;
import tman.simulator.snapshot.Stats;


/**
 * TMan component. It handles the creation of the gradient and the election of
 * the leader.
 */
public final class TMan extends ComponentDefinition
{
    private static final Object tmanPartnersLock = new Object();
    
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
//    private boolean imDead = false;

    /**
     * Create a TMan component and subscribe the handlers to the appropriate
     * ports of the component.
     */
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
    
    /**
     * Handle the TManInit event.
     *
     * Retrieve the SearchPeer address information, retrieve the TMan
     * configuration details, setup the timeouts for publishing the
     * partners list to other components and to initiate gossiping with
     * other gradient peers.
     */
    Handler<TManInit> handleInit = new Handler<TManInit>()
    {
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
    
    /**
     * Handle the TManSchedule event.
     * 
     * A TManSample is triggered for other component to get the similarity
     * list of a peer in the gradient.
     */
    Handler<TManSchedule> handleRound = new Handler<TManSchedule>()
    {
        @Override
        public void handle(TManSchedule event) {
            Snapshot.updateTManPartners(self, tmanPartners);

            // Publish sample to connected components
            synchronized (tmanPartnersLock) {
                trigger(new TManSample(tmanPartners, leader), tmanPartnersPort);
            }
//            System.err.println("[TMAN::" + self.getPeerId() + "] Sent out " + tmanPartners + " to Search component");
        }
    };
    
    /**
     * Handle the CyclonSample event.
     * 
     * When Cyclon publishes a new membership list, it is merged to the
     * similarity list, duplicates are remover and the most preferred
     * nodes are kept. If we have a stable similarity list for CONVERGENCE_CONSTANT
     * rounds and the peer has highest utility value than any peer in its
     * similarity list it can "think" it is the leader and start an election.
     */
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

                synchronized (tmanPartnersLock) {
                    tmanPartners.addAll(cyclonPartners);
                    tmanPartners = removeDuplicates(tmanPartners);
                    if (tmanPartners.contains(self)) {
                        tmanPartners.remove(self);
                    }

                    Collections.sort(tmanPartners, uc);
                    if (tmanPartners.size() > SIMILARITY_LIST_SIZE) {
                        tmanPartners = new ArrayList<PeerAddress>(tmanPartners.subList(tmanPartners.size() - SIMILARITY_LIST_SIZE, tmanPartners.size()));
                    }
                }

                // Keep track of consecutive same partner lists to detect convergence
                if (compareList(tmanPartners, tmanPrevPartners)) {
                    convergenceCount++;
                }
                else {
                    convergenceCount = 0;
                }

                if (!electing && convergenceCount == CONVERGENCE_CONSTANT && self.getPeerId().equals(self.getPeerId().max(maximumUtility(tmanPartners)))) {
                    // Check if a leader exists first and if he is smaller than us we can start the election!

                    electing = true;
                    System.err.println("[ELECTION::" + self.getPeerId() + "] I think I am the leader (" + (roundCounter - CONVERGENCE_CONSTANT) + ")!");
                    startLeaderElection();
                }

                tmanPrevPartners.clear();
                tmanPrevPartners.addAll(tmanPartners);
            }
//            System.err.println("=====================================================================================");
        }
    };
    
    /**
     * Handle the ThinkLeaderMessage event.
     * 
     * This message is sent by a peer that "thinks" it is the leader. Then the
     * election group will initiate an election using the bully algorithm and
     * will eventually select a leader.
     */
    Handler<ThinkLeaderMessage> handleThinkLeaderMessage = new Handler<ThinkLeaderMessage>()
    {
        @Override
        public void handle(ThinkLeaderMessage event) {
            electing = true;
            leader = null;
            ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
            System.err.println("[ELECTION::" + self.getPeerId() + "] I got a THINK_LEADER (or LEADER_DEAD) message!");
            if (self.getPeerId().equals(minimumUtility(electionGroup))) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I am eligible to start the election! (" + minimumUtility(electionGroup) + ")");
                for (PeerAddress peer : electionGroup) {
                    if (!self.getPeerId().equals(peer.getPeerId())) {
                        trigger(new ElectionMessage(self, peer, electionGroup), networkPort);
                    }
                }
                Stats.registerElectionMessages(electionGroup.size());

                ScheduleTimeout st = new ScheduleTimeout(BULLY_TIMEOUT);
                st.setTimeoutEvent(new ElectionTimeout(st, electionGroup));
                timeoutId = st.getTimeoutEvent().getTimeoutId();
                trigger(st, timerPort);
            }
        }
    };
    
    /**
     * Handle the ElectionTimeout event.
     * 
     * If a specific amount of time (BULLY_TIMEOUT) goes by and the peer that
     * initiated the election doesn't get an OKMessage or a CoordinatorMessage
     * it will declare itself a leader and let all the peers in the election
     * group know.
     */
    Handler<ElectionTimeout> handleElectionTimeout = new Handler<ElectionTimeout>()
    {
        @Override
        public void handle(ElectionTimeout event) {
            if (electing) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got an election timeout!");
                ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
                for (PeerAddress peer : electionGroup) {
                    trigger(new CoordinatorMessage(self, peer, electionGroup), networkPort);
                }
                Stats.registerElectionMessages(electionGroup.size());
            }
        }
    };
    
    /**
     * Handle the CoordinatorTimeout event.
     * 
     * If a specific amount of time (2 * BULLY_TIMEOUT) goes by after receiving
     * an OKMessage without receiving a CoordinatorMessage the peer will try to
     * restart the election process.
     */
    Handler<CoordinatorTimeout> handleCoordinatorTimeout = new Handler<CoordinatorTimeout>()
    {
        @Override
        public void handle(CoordinatorTimeout event) {
            if (electing) {
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got a coordinator timeout!");
                ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
                for (PeerAddress peer : electionGroup) {
                    if (self.getPeerId().compareTo(peer.getPeerId()) == -1) {
                        trigger(new ElectionMessage(self, peer, electionGroup), networkPort);
                        Stats.registerElectionMessage();

                        ScheduleTimeout st = new ScheduleTimeout(BULLY_TIMEOUT);
                        st.setTimeoutEvent(new ElectionTimeout(st, electionGroup));
                        timeoutId = st.getTimeoutEvent().getTimeoutId();
                        trigger(st, timerPort);
                    }
                }
            }
        }
    };
    
    /**
     * Handle the ElectionMessage event.
     * 
     * When a peer receives an ElectionMessage it checks if it was sent by
     * a peer with lower utility value and if so, it sends an OKMessage to it.
     * After sending the OKMessage it sends an ElectionMessage to all nodes with
     * higher utility than him.
     */
    Handler<ElectionMessage> handleElectionMessage = new Handler<ElectionMessage>()
    {
        @Override
        public void handle(ElectionMessage event) {
            if (electing) {
                boolean sentElectionMessage = false;
                System.err.println("[ELECTION::" + self.getPeerId() + "] I got an election message from " + event.getPeerSource().getPeerId() + "!");
                BigInteger sender = event.getPeerSource().getPeerId();
                if (sender.compareTo(self.getPeerId()) == -1) {
                    trigger(new OKMessage(self, event.getPeerSource(), event.getElectionGroup()), networkPort);
                    Stats.registerElectionMessage();
                    ArrayList<PeerAddress> electionGroup = event.getElectionGroup();
                    for (PeerAddress peer : electionGroup) {
                        if (self.getPeerId().compareTo(peer.getPeerId()) == -1) {
                            sentElectionMessage = true;
                            trigger(new ElectionMessage(self, peer, electionGroup), networkPort);
                            Stats.registerElectionMessage();

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
                        Stats.registerElectionMessages(electionGroup.size());
                    }
                }
            }
        }
    };
    
    /**
     * Handle the OKMessage event.
     * 
     * When a peer received an OKMessage it waits for (2 * BULLY_TIMEOUT) time
     * for a CoordinatorMessage.
     */
    Handler<OKMessage> handleOKMessage = new Handler<OKMessage>()
    {
        @Override
        public void handle(OKMessage event) {
            if (electing) {
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
    
    /**
     * Handle the CoordinatorMessage event.
     * 
     * When a peer receives a CoordinatorMessage it declares the election as
     * completed and he registers the new selected leader. It also sets the
     * timeout to periodically sent a heartbeat to the leader (if it is not
     * the leader).
     */
    Handler<CoordinatorMessage> handleCoordinatorMessage = new Handler<CoordinatorMessage>()
    {
        @Override
        public void handle(CoordinatorMessage event) {
            System.err.println("[ELECTION::" + self.getPeerId() + "] I got a coordinator message from " + event.getPeerSource().getPeerId() + " the leader is " + event.getLeader().getPeerId() + "!");
            CancelTimeout ct = new CancelTimeout(timeoutId);
            trigger(ct, timerPort);

            leader = event.getLeader();
            electionGroup = event.getElectionGroup();
            electing = false;
//            imDead = false;

            if (self.getPeerId().compareTo(leader.getPeerId()) != 0) {
                ScheduleTimeout heartbeatTimeout = new ScheduleTimeout(HEARTBEAT_TIMEOUT);
                heartbeatTimeout.setTimeoutEvent(new HeartbeatTimeout(heartbeatTimeout));
                trigger(heartbeatTimeout, timerPort);
            }
            
            Stats.reportElectionMessages();

            // SUICIDE
//            if(self.getPeerId().equals(leader.getPeerId())) {
//                ScheduleTimeout heartbeatTimeout = new ScheduleTimeout(20000);
//                heartbeatTimeout.setTimeoutEvent(new LeaderSuicide(heartbeatTimeout));
//                trigger(heartbeatTimeout, timerPort);
//            }
        }
    };
    
    /**
     * Handle the HeartbeatTimeout event.
     * 
     * After a specific amount of time (HEARTBEAT_TIMEOUT) each peer in the 
     * election group sends a HeartbeatLeader message to the leader to check
     * if it is still alive.
     */
    Handler<HeartbeatTimeout> handleHeartbeatTimeout = new Handler<HeartbeatTimeout>()
    {
        @Override
        public void handle(HeartbeatTimeout event) {
            if (leader != null) {
//                System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I am sending a heartbeat to the leader (" + leader.getPeerId() + ") ");
                trigger(new HeartbeatLeader(self, leader), networkPort);

                ScheduleTimeout st = new ScheduleTimeout(HEARTBEAT_TIMEOUT);
                st.setTimeoutEvent(new HeartbeatLeaderTimeout(st));
                heartbeatTimeoutId = st.getTimeoutEvent().getTimeoutId();
                trigger(st, timerPort);
            }
        }
    };
    
    /**
     * Handle the HeartbeatLeaderTimeout event.
     * 
     * If the leader fails to respond to a HeartbeatLeader message in time
     * (HEARTBEAT_TIMEOUT) the peer will start the election to select another
     * (or the same) leader.
     */
    Handler<HeartbeatLeaderTimeout> handleHeartbeatLeaderTimeout = new Handler<HeartbeatLeaderTimeout>()
    {
        @Override
        public void handle(HeartbeatLeaderTimeout event) {
            leader = null;
//            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I got a leader heartbeat timeout!");
//            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] Srating election with group " + electionGroup + "!");
            startLeaderReelection();
        }
    };
    
    /**
     * Handle the HeartbeatLeader event.
     * 
     * When a peer receives a HeartbeatLeader message (that happens if it is
     * the leader) it responds with a HeartbeatLeaderResponse message.
     */
    Handler<HeartbeatLeader> handleHeartbeatLeader = new Handler<HeartbeatLeader>()
    {
        @Override
        public void handle(HeartbeatLeader event) {
//            if (!imDead) {
//                System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I am the leader and I got a heartbeat from " + event.getPeerSource().getPeerId() + "!");
            trigger(new HeartbeatLeaderResponse(self, event.getPeerSource()), networkPort);
//            }
        }
    };
    
    /**
     * Handle the HeartbeatLeaderResponse event.
     * 
     * When a peer receives a HeartbeatLeaderResponse message as a response to
     * its HeartbeatLeader message sent it cancels the timeout (HeartbeatTimeout)
     * and reschedules a new heartbeat to be send in HEARTBEAT_TIMEOUT time.
     */
    Handler<HeartbeatLeaderResponse> handleHeartbeatLeaderResponse = new Handler<HeartbeatLeaderResponse>()
    {
        @Override
        public void handle(HeartbeatLeaderResponse event) {
            if (leader != null) {
//                System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I got a leader heartbeat response!");
                CancelTimeout ct = new CancelTimeout(heartbeatTimeoutId);
                trigger(ct, timerPort);

                ScheduleTimeout heartbeatTimeout = new ScheduleTimeout(HEARTBEAT_TIMEOUT);
                heartbeatTimeout.setTimeoutEvent(new HeartbeatTimeout(heartbeatTimeout));
                trigger(heartbeatTimeout, timerPort);
            }
        }
    };
    
    /**
     * Handle the LeaderSuicide event.
     * 
     * Test event to remove the leader and restart the election by the
     * election group.
     */
    Handler<LeaderSuicide> handleLeaderSuicideTimeout = new Handler<LeaderSuicide>()
    {
        @Override
        public void handle(LeaderSuicide event) {
//            System.err.println("[HEARTBEAT::" + self.getPeerId() + "] I am the leader and I'm killing myself gdwdgwwdtwndgntynndg...!! :( ");
//            imDead = true;
        }
    };
    
    /**
     * Handle the TManGossipTimeout event.
     * 
     * Periodically each peer gossips with other peers in the gradient to
     * exchange similarity lists. Each peer picks its similarity list,
     * add itself in it and removes the peer that was selected to shuffle with.
     */
    Handler<TManGossipTimeout> handleTManGossipTimeout = new Handler<TManGossipTimeout>()
    {
        @Override
        public void handle(TManGossipTimeout event) {
            // If we have at least one peer in our similarity list
            if (tmanPartners.size() > 0) {
                // Select the most prefered node to exchange preference lists with
//                System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners are " + tmanPartners);
                PeerAddress shufflePeer = getSoftMaxAddress(tmanPartners);
                ArrayList<PeerAddress> shuffleView = prepareShuffleView(tmanPartners, shufflePeer);
//                System.err.println("[GOSSIP::" + self.getPeerId() + "] I am sending " + shuffleView + " to " + shufflePeer);
                trigger(new ExchangeMsg.Request(event.getTimeoutId(), shuffleView, self, shufflePeer), networkPort);
            }
        }
    };
    
    /**
     * Handle the ExchangeMsg.Request event.
     * 
     * When a peer receives a ExchangeMsg.Request message it merges the
     * received peers with its similarity list and responds with his own list
     * (removing the shuffle peer and adding itself in).
     */
    Handler<ExchangeMsg.Request> handleTManPartnersRequest = new Handler<ExchangeMsg.Request>()
    {
        @Override
        public void handle(ExchangeMsg.Request event) {
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners are " + tmanPartners);
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] Received " + event.getSimilaritySet() + " from " + event.getPeerSource());
//            merge(event.getSimilaritySet());

            synchronized (tmanPartnersLock) {
                tmanPartners.addAll(event.getSimilaritySet());
                tmanPartners = removeDuplicates(tmanPartners);
                if (tmanPartners.contains(self)) {
                    tmanPartners.remove(self);
                }

                Collections.sort(tmanPartners, uc);
                if (tmanPartners.size() > SIMILARITY_LIST_SIZE) {
                    tmanPartners = new ArrayList<PeerAddress>(tmanPartners.subList(tmanPartners.size() - SIMILARITY_LIST_SIZE, tmanPartners.size()));
                }

//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners now are " + tmanPartners);
                ArrayList<PeerAddress> shuffleView = prepareShuffleView(tmanPartners, event.getPeerSource());
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] I am sending " + shuffleView + " to " + event.getPeerSource());
                trigger(new ExchangeMsg.Response(UUID.randomUUID(), shuffleView, self, event.getPeerSource()), networkPort);
            }
        }
    };
    
    /**
     * Handle the ExchangeMsg.Response event.
     * 
     * When a peer receives a ExchangeMsg.Response message it merges the
     * received peers with its similarity list.
     */
    Handler<ExchangeMsg.Response> handleTManPartnersResponse = new Handler<ExchangeMsg.Response>()
    {
        @Override
        public void handle(ExchangeMsg.Response event) {
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners are " + tmanPartners);
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] Received " + event.getSelectedBuffer() + " from " + event.getPeerSource());
//            merge(event.getSelectedBuffer());
            synchronized (tmanPartnersLock) {
                tmanPartners.addAll(event.getSimilaritySet());
                tmanPartners = removeDuplicates(tmanPartners);
                if (tmanPartners.contains(self)) {
                    tmanPartners.remove(self);
                }

                Collections.sort(tmanPartners, uc);
                if (tmanPartners.size() > SIMILARITY_LIST_SIZE) {
                    tmanPartners = new ArrayList<PeerAddress>(tmanPartners.subList(tmanPartners.size() - SIMILARITY_LIST_SIZE, tmanPartners.size()));
                }
            }
//            System.err.println("[GOSSIP::" + self.getPeerId() + "] My partners now are " + tmanPartners);
        }
    };

    /**
     * Starts an election.
     * 
     * The election group is specified and a ThinkLeaderMessage is sent to
     * every peer in that group so they can start the bully algorithm.
     */
    private void startLeaderElection() {
        ArrayList<PeerAddress> initialElectionGroup = new ArrayList<PeerAddress>(tmanPartners);
        initialElectionGroup.add(self);
        System.err.println("[ELECTION::" + self.getPeerId() + "] The election group is " + initialElectionGroup);
        for (PeerAddress peer : tmanPartners) {
            trigger(new ThinkLeaderMessage(self, peer, initialElectionGroup), networkPort);
        }
        
        Stats.clearElectionMessages();
        Stats.registerElectionMessages(tmanPartners.size());
    }

    /**
     * Restarts an election.
     * 
     * The election group is kept the same and the ThinkLeaderMessage is sent
     * again so that a new election can start to select a new leader.
     */
    private void startLeaderReelection() {
        System.err.println("[ELECTION::" + self.getPeerId() + "] The re-election group is " + electionGroup);
        for (PeerAddress peer : tmanPartners) {
            trigger(new ThinkLeaderMessage(self, peer, electionGroup), networkPort);
        }
    }

    /**
     * Compares two partner lists to see if they are the same.
     * 
     * @param a First list to compare.
     * @param b Second list to compare.
     * @return True if lists are the same or False if lists differ at least
     *         in one entry.
     */
    private boolean compareList(ArrayList<PeerAddress> a, ArrayList<PeerAddress> b) {
        if (a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).getPeerId().equals(b.get(i).getPeerId())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Find the peer with the minimum utility in the list.
     * 
     * @param list List to search into for the peer with the minimum utility.
     * @return The minimum utility found in the list.
     */
    private BigInteger minimumUtility(ArrayList<PeerAddress> list) {
        BigInteger min = null;
        if (!list.isEmpty()) {
            min = list.get(0).getPeerId();

            for (PeerAddress peer : list) {
                if (peer.getPeerId().compareTo(min) == -1) {
                    min = peer.getPeerId();
                }
            }
        }
        return min;
    }

    /**
     * Find the peer with the maximum utility in the list.
     * 
     * @param list List to search into for the peer with the maximum utility.
     * @return The maximum utility found in the list.
     */
    private BigInteger maximumUtility(ArrayList<PeerAddress> list) {
        BigInteger max = null;
        if (!list.isEmpty()) {
            max = list.get(0).getPeerId();

            for (PeerAddress peer : list) {
                if (peer.getPeerId().compareTo(max) == 1) {
                    max = peer.getPeerId();
                }
            }
        }
        return max;
    }

    /**
     * Remove duplicates from a list of partners.
     * 
     * This case might appear when merging lists from Cyclon and other peers
     * through gossiping.
     * 
     * The solution is more of a hack. The contents of the ArrayList (which
     * allows for duplicate entries) are transfered over to a HashSet (which
     * does not allow for duplicates) and then back to the ArryList!
     * 
     * @param partners
     * @return 
     */
    private ArrayList<PeerAddress> removeDuplicates(ArrayList<PeerAddress> partners) {
        HashSet hs = new HashSet();
        hs.addAll(partners);
        partners.clear();
        partners.addAll(hs);

        return partners;
    }

    /**
     * Prepare the partner list for shuffling.
     * 
     * This is pretty much what Cyclon paper does (without taking ages under
     * consideration). We remove the peer, with whom we are shuffling, from the
     * list and we add ourselves.
     * 
     * @param partners
     * @param shufflePeer
     * @return 
     */
    private ArrayList<PeerAddress> prepareShuffleView(ArrayList<PeerAddress> partners, PeerAddress shufflePeer) {
        ArrayList<PeerAddress> shuffleView = new ArrayList<PeerAddress>(partners);
        shuffleView.add(self);
        shuffleView.remove(shufflePeer);

        return shuffleView;
    }
    
    /**
     * SoftMax selection of peer to shuffle with.
     * 
     * It returns a peer weighted towards the 'best' node (as defined by 
     * UtilityComparator) with the temperature controlling the weighting.
     * A temperature of '1.0' will be greedy and always return the best node.
     * A temperature of '0.000001' will return a random node.
     * A temperature of '0.0' will throw a divide by zero exception
     * 
     * http://webdocs.cs.ualberta.ca/~sutton/book/2/node4.html
     * 
     * @param entries List of peers from which we select one based on the 
     *        SOFT_MAX_TEMPERATURE value.
     * @return One peer from the list.
     */
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
