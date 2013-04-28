package search.simulator.core;

import common.simulation.SimulatorPort;
import java.math.BigInteger;
import java.util.HashMap;

import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;
import se.sics.kompics.network.Network;
import se.sics.kompics.p2p.bootstrap.BootstrapConfiguration;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

import search.system.peer.SearchPeer;
import search.system.peer.SearchPeerInit;
import common.peer.PeerAddress;
import search.simulator.snapshot.Snapshot;
import common.configuration.SearchConfiguration;
import common.configuration.Configuration;
import common.configuration.CyclonConfiguration;
import common.simulation.AddIndexEntry;
import common.simulation.ConsistentHashtable;
import common.simulation.GenerateReport;
import common.simulation.PeerFail;
import common.simulation.PeerJoin;
import common.simulation.SimulatorInit;
import java.net.InetAddress;
import java.util.Random;
import se.sics.ipasdistances.AsIpGenerator;
import se.sics.kompics.Negative;
import se.sics.kompics.web.Web;
import search.system.peer.AddIndexText;
import search.system.peer.IndexPort;

public final class SearchSimulator extends ComponentDefinition {

    Positive<SimulatorPort> simulator = positive(SimulatorPort.class);
    Positive<Network> network = positive(Network.class);
    Positive<Timer> timer = positive(Timer.class);
    Negative<Web> webIncoming = negative(Web.class);
    private final HashMap<BigInteger, Component> peers;
    private final HashMap<BigInteger, PeerAddress> peersAddress;
    private BootstrapConfiguration bootstrapConfiguration;
    private CyclonConfiguration cyclonConfiguration;
    private SearchConfiguration searchConfiguration;
    private int peerIdSequence;
    private int monotonicID = 0;
    private BigInteger identifierSpaceSize;
    private ConsistentHashtable<BigInteger> ringNodes;
    private AsIpGenerator ipGenerator = AsIpGenerator.getInstance(125);

    static String[] articles = {" ", "The ", "A "};
    static String[] verbs = {"fires ", "walks ", "talks ", "types ", "programs "};
    static String[] subjects = {"computer ", "Lucene ", "torrent "};
    static String[] objects = {"computer", "java", "video"};
    static String[] magnetLink = {"b03c8641415d3a0fc7077f5bf567634442989a74", "a896f7155237fb27e2eaa06033b5796d7ae84a1d",
                                   "3ebb7aa97076cac0ac1b0812f5e16cf46d5daf41", "9823e8059552f26bc72f301d5417a2eb8c7db20b",
                                   "bfc022fc0b977fbc6c59e54ab9d206a66f3eea68", "165c14e9aa637b2157d9ee3fc3050d9afc652e40",
                                   "4c316fd383a7ef1fbc389cf0755cbf9434a39f7c", "a2196ef379c39e524ff7868a72ad1d8fa54a7a4b",
                                   "29bb0c30991e4490acdec056045dd19acb34b194", "f502f11df1c29b5ca8e5c2fa50abcbf59b1d274f"};
    Random r = new Random(System.currentTimeMillis());
    
    
//-------------------------------------------------------------------	
    public SearchSimulator() {
        peers = new HashMap<BigInteger, Component>();
        peersAddress = new HashMap<BigInteger, PeerAddress>();
        ringNodes = new ConsistentHashtable<BigInteger>();

        subscribe(handleInit, control);
        subscribe(handleGenerateReport, timer);
        subscribe(handlePeerJoin, simulator);
        subscribe(handlePeerFail, simulator);
        subscribe(handleAddIndexEntry, simulator);
    }
//-------------------------------------------------------------------	
    Handler<SimulatorInit> handleInit = new Handler<SimulatorInit>() {
        public void handle(SimulatorInit init) {
            peers.clear();
            peerIdSequence = 0;

            bootstrapConfiguration = init.getBootstrapConfiguration();
            cyclonConfiguration = init.getCyclonConfiguration();
            searchConfiguration = init.getAggregationConfiguration();

            identifierSpaceSize = cyclonConfiguration.getIdentifierSpaceSize();

            // generate periodic report
            int snapshotPeriod = Configuration.SNAPSHOT_PERIOD;
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(snapshotPeriod, snapshotPeriod);
            spt.setTimeoutEvent(new GenerateReport(spt));
            trigger(spt, timer);

        }
    };
    
    String randomText() {
        StringBuilder sb = new StringBuilder();
        int clauses = Math.max(1, r.nextInt(3));
        for (int i = 0; i < clauses; i++) {
                sb.append(articles[r.nextInt(articles.length)]);
                sb.append(subjects[r.nextInt(subjects.length)]);
                sb.append(verbs[r.nextInt(verbs.length)]);
                sb.append(objects[r.nextInt(objects.length)]);
                sb.append(". ");
        }
        return sb.toString();
    }
    
//-------------------------------------------------------------------	
    Handler<AddIndexEntry> handleAddIndexEntry = new Handler<AddIndexEntry>() {
        @Override
        public void handle(AddIndexEntry event) {
            BigInteger successor = ringNodes.getNode(event.getId());
            Component peer = peers.get(successor);
            
            trigger(new AddIndexText(monotonicID++, randomText(), magnetLink[r.nextInt(10)]), peer.getNegative(IndexPort.class));
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerJoin> handlePeerJoin = new Handler<PeerJoin>() {
        public void handle(PeerJoin event) {
            int num = event.getNum();
            BigInteger id = event.getPeerId();

            // join with the next id if this id is taken
            BigInteger successor = ringNodes.getNode(id);

            while (successor != null && successor.equals(id)) {
                id = id.add(BigInteger.ONE).mod(identifierSpaceSize);
                successor = ringNodes.getNode(id);
            }

            createAndStartNewPeer(id, num);
            ringNodes.addNode(id);
        }
    };
//-------------------------------------------------------------------	
    Handler<PeerFail> handlePeerFail = new Handler<PeerFail>() {
        public void handle(PeerFail event) {
            BigInteger id = ringNodes.getNode(event.getCyclonId());

            if (ringNodes.size() == 0) {
                System.err.println("Empty network");
                return;
            }

            ringNodes.removeNode(id);
            stopAndDestroyPeer(id);
        }
    };
//-------------------------------------------------------------------	
    Handler<GenerateReport> handleGenerateReport = new Handler<GenerateReport>() {
        public void handle(GenerateReport event) {
            Snapshot.report();
        }
    };

//-------------------------------------------------------------------	
    private final void createAndStartNewPeer(BigInteger id, int num) {
        Component peer = create(SearchPeer.class);
        int peerId = ++peerIdSequence;
        InetAddress ip = ipGenerator.generateIP();
        Address address = new Address(ip, 8058, peerId);
        PeerAddress peerAddress = new PeerAddress(address, id);

        connect(network, peer.getNegative(Network.class), new MessageDestinationFilter(address));
        connect(timer, peer.getNegative(Timer.class));
        connect(peer.getPositive(Web.class), webIncoming); //, new WebDestinationFilter(peerId));

        trigger(new SearchPeerInit(peerAddress, num, bootstrapConfiguration, cyclonConfiguration, searchConfiguration), peer.getControl());

        trigger(new Start(), peer.getControl());
        peers.put(id, peer);
        peersAddress.put(id, peerAddress);

    }

//-------------------------------------------------------------------	
    private final void stopAndDestroyPeer(BigInteger id) {
        Component peer = peers.get(id);

        trigger(new Stop(), peer.getControl());

        disconnect(network, peer.getNegative(Network.class));
        disconnect(timer, peer.getNegative(Timer.class));

        Snapshot.removePeer(peersAddress.get(id));

        peers.remove(id);
        peersAddress.remove(id);

        destroy(peer);
    }

//-------------------------------------------------------------------	
    private final static class MessageDestinationFilter extends ChannelFilter<Message, Address> {

        public MessageDestinationFilter(Address address) {
            super(Message.class, address, true);
        }

        public Address getValue(Message event) {
            return event.getDestination();
        }
    }
}
