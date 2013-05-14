package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.PeerAddress;
import java.util.ArrayList;

import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import java.math.BigInteger;
import java.util.Collections;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import tman.simulator.snapshot.Snapshot;

public final class TMan extends ComponentDefinition {

    private int SIMILARITY_LIST_SIZE = 5;
    private int CONVERGENCE_CONSTANT = 5;
    
    Negative<TManSamplePort> tmanPartnersPort = negative(TManSamplePort.class);
    Positive<CyclonSamplePort> cyclonSamplePort = positive(CyclonSamplePort.class);
    Positive<Network> networkPort = positive(Network.class);
    Positive<Timer> timerPort = positive(Timer.class);
    private long period;
    private PeerAddress self;
    private ArrayList<PeerAddress> tmanPartners;
    private ArrayList<PeerAddress> tmanPrevPartners;
    private int convergenceCount = 0;
    private TManConfiguration tmanConfiguration;
    private boolean electing = false;

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
            ArrayList<PeerAddress> cyclonPartners = event.getSample();
            System.err.println("=====================================================================================");
            System.err.println("[TMAN::" + self.getPeerId() + "] Cyclon Partners:" + cyclonPartners);
            System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners:" + tmanPartners);
            if (!cyclonPartners.isEmpty()) {
                PeerAddress randomPeer = null;
                for (PeerAddress cyclonPeer : cyclonPartners) {
                    if (!tmanPartners.contains(cyclonPeer)) {
                        randomPeer = cyclonPeer;
                        break;
                    }
                }
                System.err.println("[TMAN::" + self.getPeerId() + "] Random Peer:" + randomPeer);
                if (randomPeer != null) {
                    if (tmanPartners.size() >= SIMILARITY_LIST_SIZE) {
                        // Sorting based on preference function to find the least prefered node.
                        UtilityComparator uc = new UtilityComparator(self);
                        Collections.sort(tmanPartners, uc);
                        System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners (sorted):" + tmanPartners);
                        if (uc.compare(tmanPartners.get(0), randomPeer) == -1) {
                            tmanPartners.set(0, randomPeer);
                        }
                        System.err.println("[TMAN::" + self.getPeerId() + "] TMan Partners (swapped):" + tmanPartners);
                    }
                    else {
                        tmanPartners.add(randomPeer);
                        System.err.println("[TMAN::" + self.getPeerId() + "] New Peer:" + tmanPartners.get(0));
                    }
                    if(compareList(tmanPartners, tmanPrevPartners)){
                        convergenceCount++;
                    }
                    else {
                        convergenceCount = 0;
                    }
                    
                    System.out.println("[" + self.getPeerId() + "] Convergence count is " + convergenceCount);
                    
                    if(!electing && convergenceCount == CONVERGENCE_CONSTANT && self.getPeerId().equals(self.getPeerId().min(minimumUtility(tmanPartners)))) {
                        electing = true;
                        System.err.println("[TMAN::" + self.getPeerId() + "] I am starting leader election!!");
                    }
                    
                    tmanPrevPartners.clear();
                    tmanPrevPartners.addAll(tmanPartners);
                }
            }
            System.err.println("=====================================================================================");
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

}
