package search.system.peer.search;

import common.configuration.SearchConfiguration;
import common.peer.PeerAddress;
import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.CancelTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;
import se.sics.kompics.web.WebResponse;
import search.simulator.snapshot.Snapshot;
import search.system.peer.AddIndexText;
import search.system.peer.IndexPort;
import tman.system.peer.tman.TManSample;
import tman.system.peer.tman.TManSamplePort;
import tman.system.peer.tman.UtilityComparator;

/**
 * Should have some comments here.
 * @author jdowling
 */
public final class Search extends ComponentDefinition {

    private final double SOFT_MAX_TEMPERATURE = 1.0;
    private final int NEW_ENTRY_ACK_TIMEOUT = 3000;
    private final int NEW_ENTRY_ADD_RETRIES = 5;
    
    private static final Logger logger = LoggerFactory.getLogger(Search.class);
    Positive<IndexPort> indexPort = positive(IndexPort.class);
    Positive<Network> networkPort = positive(Network.class);
    Positive<Timer> timerPort = positive(Timer.class);
    Negative<Web> webPort = negative(Web.class);
    Positive<CyclonSamplePort> cyclonSamplePort = positive(CyclonSamplePort.class);
    Negative<CyclonSamplePort> cyclonSamplePortRequest = negative(CyclonSamplePort.class);
    Positive<TManSamplePort> tmanSamplePort = positive(TManSamplePort.class);

    private HashMap<String, PendingACK> pendingResponses = new HashMap<String, PendingACK>();
    private HashMap<String, PendingEntry> pendingEntries = new HashMap<String, PendingEntry>();
    private ArrayList<PeerAddress> tmanPartners = new ArrayList<PeerAddress>();
    private PeerAddress leader = null;
    private ArrayList<Integer> indexStore = new ArrayList<Integer>();
    private PeerAddress self;
    private double num;
    private Random r = new Random();
    private long period;
    private SearchConfiguration searchConfiguration;
        // Apache Lucene used for searching
    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
    Directory index = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);

    private int latestMissingIndexValue = 0;
    private int nextIndexEntryID = 0;
    private int pendingEntryID = 0;
    
//-------------------------------------------------------------------	
    public Search() {
        subscribe(handleIndexUpdateRequest, networkPort);
        subscribe(handleIndexUpdateResponse, networkPort);
        subscribe(handleAddEntryToLeader, networkPort);
        subscribe(handleAddEntry, networkPort);
        subscribe(handleInit, control);
        subscribe(handleWebRequest, webPort);
        subscribe(handleCyclonSample, cyclonSamplePort);
        subscribe(handleTManSample, tmanSamplePort);
        subscribe(handleAddIndexText, indexPort);
        subscribe(handleAddNewEntryTimeout, timerPort);
        subscribe(handleAddEntryACK, networkPort);
        subscribe(handleNewEntryACK, networkPort);
        subscribe(handleNewEntryNACK, networkPort);
    }
//-------------------------------------------------------------------	
    Handler<SearchInit> handleInit = new Handler<SearchInit>() {
        @Override
        public void handle(SearchInit init) {
            self = init.getSelf();
            num = init.getNum();
            searchConfiguration = init.getConfiguration();
            period = searchConfiguration.getPeriod();

            Snapshot.updateNum(self, num);
        }
    };
    
    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {
        @Override
        public void handle(CyclonSample event) {
//            System.err.println("[INDEX::" + self.getPeerId() + "] CyclonSample (" + event.getSample() + ")");
            if(!event.getSample().isEmpty()) {
                PeerAddress peer = event.getSample().get(0); // TODO PICK A PEER!!!!
                ArrayList<Range> missingRanges = getMissingRanges();
                Integer lastExisting = (indexStore.isEmpty())?-1:indexStore.get(indexStore.size() - 1);
//                System.out.println("============================================================");
//                System.out.println("[INDEX::" + self.getPeerAddress().getId() + "->" + peer.getPeerAddress().getId() + "] I need " + missingRanges.size() + " ranges and my maximum ID is " + lastExisting + "!");
//                System.out.println(missingRanges);
//                System.out.println("============================================================");
                IndexUpdateRequest iur = new IndexUpdateRequest(self, peer, missingRanges, lastExisting);
                trigger(iur, networkPort);
            }
        }
    };
    
    Handler<IndexUpdateRequest> handleIndexUpdateRequest = new Handler<IndexUpdateRequest>() {
        @Override
        public void handle(IndexUpdateRequest request) {
            try {
//                System.err.println("[INDEX::" + self.getPeerId() + "] IndexUpdateRequest");
                if(!indexStore.isEmpty()) {
                    ArrayList<Entry> missingEntries = getMissingEntries(request.getMissingRanges(), request.getLastExisting());
//                    System.out.println("============================================================");
//                    System.out.println("[INDEX::" + self.getPeerAddress().getId() + "->" + request.getPeerSource().getPeerAddress().getId() + "] I have these entries:");
//                    System.out.println(missingEntries);
//                    System.out.println("============================================================");
                    IndexUpdateResponse iur = new IndexUpdateResponse(self, request.getPeerSource(), missingEntries);
                    trigger(iur, networkPort);
                }
            }
            catch (IOException ex) {
                java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    };
    Handler<IndexUpdateResponse> handleIndexUpdateResponse = new Handler<IndexUpdateResponse>()
    {
        @Override
        public void handle(IndexUpdateResponse response) {
            try {
                for (Entry entry : response.getEntries()) {
                    addEntry(entry.getId(), entry.getTitle(), entry.getMagnetLink());
                }
            }
            catch (IOException ex) {
                java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            }
//            System.err.println("[" + self.getPeerAddress().getId() + "] IndexUpdateResponse (" + indexStore.size() + ")");
        }
    };

    Handler<WebRequest> handleWebRequest = new Handler<WebRequest>() {
        public void handle(WebRequest event) {
            if (event.getDestination() != self.getPeerAddress().getId()) {
                return;
            }
            
            String[] args = event.getTarget().split("-");

            logger.debug("Handling Webpage Request");
            WebResponse response;
            if (args[0].compareToIgnoreCase("search") == 0) {
                response = new WebResponse(searchPageHtml(args[1]), event, 1, 1);
                trigger(response, webPort);
            } else if (args[0].compareToIgnoreCase("add") == 0) {
                Entry newEntry = new Entry(String.valueOf(pendingEntryID), args[1], args[2]);
                pendingEntries.put(String.valueOf(pendingEntryID), new PendingEntry(String.valueOf(pendingEntryID++), event, newEntry));
                handleNewEntry(newEntry);
            } else if (args[0].compareToIgnoreCase("jollyroger") == 0) {
                response = new WebResponse(jollyRogerHTML(), event, 1, 1);
                trigger(response, webPort);
            } else if (args[0].compareToIgnoreCase("jrsearch") == 0) {
                response = new WebResponse(jrSearchPageHtml(args[1]), event, 1, 1);
                trigger(response, webPort);
            } else {
                response = new WebResponse(searchPageHtml(event.getTarget()), event, 1, 1);
                trigger(response, webPort);
            }
        }
    };
    
    private void handleNewEntry(Entry entry) {
        PeerAddress targetPeer;
        if(leader != null) {
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Sending new entry to the leader (" + leader.getPeerId() + ")");
            targetPeer = leader;
        }
        else {
            targetPeer = getSoftMaxAddress(tmanPartners);
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Forwarding new entry to the leader through " + targetPeer.getPeerId());
        }
        trigger(new ForwardEntryToLeader(self, targetPeer, self, entry), networkPort);
    }
    
    Handler<ForwardEntryToLeader> handleAddEntryToLeader = new Handler<ForwardEntryToLeader>() {
        @Override
        public void handle(ForwardEntryToLeader event) {
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Got forward message from " + event.getPeerSource().getPeerId());
            if(leader != null) {
                if(self.getPeerId().equals(leader.getPeerId())) {
                    System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] I am the leader and I received a new entry!");
                    System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Sending new entry to " + tmanPartners);
                    Entry newEntry = event.getNewEntry();
                    String tempID = newEntry.getId();
                    try {
                        newEntry.setId((nextIndexEntryID++) + "");
                        
                        // Add to ourselves
                        addEntry(newEntry);

                        for (PeerAddress peer : tmanPartners) {
                            trigger(new AddEntry(self, peer, newEntry), networkPort);
                        }
                        
                        ScheduleTimeout st = new ScheduleTimeout(NEW_ENTRY_ACK_TIMEOUT);
                        st.setTimeoutEvent(new AddNewEntryTimeout(st, newEntry.getId()));
                        UUID timeoutID = st.getTimeoutEvent().getTimeoutId();
                        trigger(st, timerPort);

                        pendingResponses.put(newEntry.getId(), new PendingACK(timeoutID, event.getRequestSource(), tempID));
                        System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Created PendingACK for " + newEntry.getId() + " with timeout " + timeoutID);
                    }
                    catch (IOException ex) {
                        nextIndexEntryID--;
                        trigger(new NewEntryNACK(self, event.getRequestSource(), tempID), networkPort);
                    }
                }
                else {
                    trigger(new ForwardEntryToLeader(self, leader, event.getRequestSource(), event.getNewEntry()), networkPort);
                }
            }
            else {
                trigger(new ForwardEntryToLeader(self, getSoftMaxAddress(tmanPartners), event.getRequestSource(), event.getNewEntry()), networkPort);
            }
        }
    };
    
    Handler<AddEntryACK> handleAddEntryACK = new Handler<AddEntryACK>() {
        @Override
        public void handle(AddEntryACK event) {
            PendingACK pack = pendingResponses.get(event.getEntryID());
            // Pack can be null because we can reach quorum before all nodes ACK their adds.
            if(pack != null) {
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] I got an acknowledgment back from " + event.getPeerSource() + " for " + event.getEntryID());
                pack.incReceivedACKs();
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] " + pack.getReceivedACKs() + " / " + ((tmanPartners.size() / 2) + 1) + " tmanPartners " + tmanPartners);
                if(pack.getReceivedACKs() >= ((tmanPartners.size() / 2) + 1)) {
                    System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] We have reached quorum of ACKs (" + pack.getReceivedACKs() + "/" + tmanPartners.size() + ")");
                    trigger(new NewEntryACK(self, pack.getRequestSource(), pack.getNewEntryTempID()), networkPort);
                    CancelTimeout ct = new CancelTimeout(pack.getTimeoutID());
                    trigger(ct, timerPort);
                    pendingResponses.remove(event.getEntryID());
                }
            }
        }
    };
    
    Handler<AddNewEntryTimeout> handleAddNewEntryTimeout = new Handler<AddNewEntryTimeout>() {
        @Override
        public void handle(AddNewEntryTimeout event) {
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] AddEntry for (" + event.getEntryID() + ") timed out with timer " + event.getTimeoutId());
            PendingACK pack = pendingResponses.get(event.getEntryID());
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] " + pack.getReceivedACKs() + " / " + ((tmanPartners.size() / 2) + 1));
            if(pack.getReceivedACKs() < ((tmanPartners.size() / 2) + 1)) {
                // TODO Remove added entry from index and uncomment the line below!
                // nextIndexEntryID--;
                trigger(new NewEntryNACK(self, pack.getRequestSource(), pack.getNewEntryTempID()), networkPort);
            }
        }
    };
    
    Handler<NewEntryACK> handleNewEntryACK = new Handler<NewEntryACK>() {
        @Override
        public void handle(NewEntryACK event) {
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Got ACK for " + event.getNewEntryTempID());
            PendingEntry pen = pendingEntries.get(event.getNewEntryTempID());
            if(pen.getWebRequest() == null) {
                // If the add request came from the Simulator don't send web content back
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] New entry (" + pen.getPendingEntryID() + ") successfully added and replicated.");
            } else {
                // If the add request came from the browser respond through Jetty
                WebResponse response = new WebResponse("New entry (" + pen.getPendingEntryID() + ") successfully added and replicated.", pen.getWebRequest(), 1, 1);
                trigger(response, webPort);
            }
            pendingEntries.remove(event.getNewEntryTempID());
        }
    };
    
    Handler<NewEntryNACK> handleNewEntryNACK = new Handler<NewEntryNACK>() {
        @Override
        public void handle(NewEntryNACK event) {
            PendingEntry pen = pendingEntries.get(event.getNewEntryTempID());
            pen.incAddEntryTriesCounter();
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] New entry failed attempt (" + pen.getAddEntryTriesCounter() + " / " + NEW_ENTRY_ADD_RETRIES + ")");
            if (pen.getAddEntryTriesCounter() > NEW_ENTRY_ADD_RETRIES) {
                pendingEntries.remove(event.getNewEntryTempID());
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Removed " + event.getNewEntryTempID() + " from pending entries list!");
                if (pen.getWebRequest() == null) {
                    // If the add request came from the Simulator don't send web content back
                    System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] New entry (" + pen.getPendingEntryID() + ") NOT added after " + NEW_ENTRY_ADD_RETRIES + " tries!");
                } else {
                    // If the add request came from the browser respond through Jetty
                    WebResponse response = new WebResponse("New entry (" + pen.getPendingEntryID() + ") NOT added after " + NEW_ENTRY_ADD_RETRIES + " tries!", pen.getWebRequest(), 1, 1);
                    trigger(response, webPort);
                }
            }
            else {
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Retrying " + event.getNewEntryTempID());
                handleNewEntry(pen.getEntry());
            }
        }
    };
    
    private ArrayList<Range> getMissingRanges() {
        ArrayList<Range> missing = new ArrayList<Range>();
        int lowLimit = -1;
        for(int i = 0; i < indexStore.size(); i++) {
            if(indexStore.get(i) > (lowLimit + 1)) {
                missing.add(new Range(lowLimit + 1, indexStore.get(i) - 1));
            }
            lowLimit = indexStore.get(i);
        }
        return missing;
    }
    
    private ArrayList<Entry> getMissingEntries(ArrayList<Range> missingRanges, int lastExisting) throws IOException {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        IndexSearcher searcher = null;
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);
        }
        catch (IOException ex) {
            java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Add a range to cover for the missing entries after the last existing entry.
        missingRanges.add(new Range(lastExisting + 1, indexStore.get(indexStore.size() - 1)));

        // Get the entries of the missing ranges.
        for (Range range : missingRanges) {
            Query query = NumericRangeQuery.newIntRange("id", range.getLeft(), range.getRight(), true, true);
            TopScoreDocCollector collector = TopScoreDocCollector.create(range.getSize(), true);
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                entries.add(new Entry(d.get("id"), d.get("title"), d.get("magnet")));                
            }
        }
        reader.close();
        return entries;
    }
    
    private String jollyRogerHTML() {
        String jollyRoger = "<!DOCTYPE html><html><head>	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />	<title>Jolly Roger</title>		<!-- CSS Styles -->	<link rel=\"stylesheet\" href=\"http://service.yummycode.com/JollyRoger/css/base.css\" type=\"text/css\"/>	<link rel=\"stylesheet\" href=\"http://service.yummycode.com/JollyRoger/css/layout.css\" type=\"text/css\"/>	<link rel=\"stylesheet\" href=\"http://service.yummycode.com/JollyRoger/css/module.css\" type=\"text/css\"/>    	<!-- JavaScript Libraries -->    <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\" type=\"text/javascript\"></script>    <script type=\"text/javascript\" src=\"http://service.yummycode.com/JollyRoger/js/jolly-lib.js\"></script>		<script>        var SEARCH_LINK = \"http://192.168.56.1:9999/\";		$(document).ready(function(e) {			$(\"#searchSubmit\").click(function(event) {                if($(\"#searchText\").val().length > 0) {                    var searchUrl = SEARCH_LINK + $(\"#searchPeer\").val() + \"/jrsearch-\" + $(\"#searchText\").val();                    searchAndRender(searchUrl, $(\".searchResults\"));                }                else {                    alert(\"Arrrrgh! Enter a search term!\");                }			});		});	</script></head><body>	<div class=\"website\">        <header>            <section class=\"headerContent\">            	<div class=\"logo\" />            </section>        </header>                <section class=\"search\">            <section class=\"searchBar\">            	<form action=\"#\">            		<input id=\"searchText\" type=\"text\" />                    <input id=\"searchPeer\" type=\"text\" />            		<input id=\"searchSubmit\" type=\"submit\" value=\"Search\" />            	</form>            </section>                        <section class=\"searchResultsContainer\">                <ul class=\"searchResults\">                	<img src=\"http://service.yummycode.com/JollyRoger/img/no-torrents.png\" class=\"noresult\" alt=\"No results\" />                </ul>            </section>	        </section>                <!-- <footer>        	<div class=\"team\">	        	<div class=\"participant\">	            	<span>Thomas Fattal</span><br />	            	<span>tfattal@kth.se</span>	            </div>	            <div class=\"participant\">	            	<span>George Kallergis</span><br />	            	<span>geokal@kth.se</span>	            </div>	        </div>        </footer> -->    </div></body></html>";
        return jollyRoger;
    }

    private String jrSearchPageHtml(String title) {
        StringBuilder sb = new StringBuilder();
        try {
            jrQuery(sb, title);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            sb.append(ex.getMessage());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            sb.append(ex.getMessage());
        }
        return sb.toString();
    }
    
    private String jrQuery(StringBuilder sb, String querystr) throws ParseException, IOException {
        // Check if index has any entries first to avoid exception from Lucene when creating the reader.
        if (index.listAll().length > 0) {
            // the "title" arg specifies the default field to use when no field is explicitly specified in the query.
            Query q = new QueryParser(Version.LUCENE_42, "title", analyzer).parse(querystr);
            IndexSearcher searcher = null;
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(index);
                searcher = new IndexSearcher(reader);
            }
            catch (IOException ex) {
                java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }

            int hitsPerPage = 1000;
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // display results
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                sb.append("<li class=\"searchResultItem\"><div class=\"srIcon\"></div>")
                        .append("<div class=\"srTitle\"><a href=\"magnet:?xt=urn:btih:")
                        .append(d.get("magnet")).append("\">[").append(d.get("id")).append("] ")
                        .append(d.get("title")).append("</a></div></li>");
            }

            // reader can only be closed when there
            // is no need to access the documents any more.
            reader.close();
        }
        else {
            sb.append("Found 0 entries!");
        }
        return sb.toString();
    }
    
    private String searchPageHtml(String title) {
        StringBuilder sb = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C");
        sb.append("//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR");
        sb.append("/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http:");
        sb.append("//www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Conten");
        sb.append("t-Type\" content=\"text/html; charset=utf-8\" />");
        sb.append("<title>Kompics P2P Bootstrap Server</title>");
        sb.append("<style type=\"text/css\"><!--.style2 {font-family: ");
        sb.append("Arial, Helvetica, sans-serif; color: #0099FF;}--></style>");
        sb.append("</head><body><h2 align=\"center\" class=\"style2\">");
        sb.append("ID2210 (Decentralized Search for Piratebay)</h2><br>");
        try {
            query(sb, title);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            sb.append(ex.getMessage());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            sb.append(ex.getMessage());
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String addEntryHtml(String id, String title, String magnetLink) {
        StringBuilder sb = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C");
        sb.append("//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR");
        sb.append("/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http:");
        sb.append("//www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Conten");
        sb.append("t-Type\" content=\"text/html; charset=utf-8\" />");
        sb.append("<title>Adding an Entry</title>");
        sb.append("<style type=\"text/css\"><!--.style2 {font-family: ");
        sb.append("Arial, Helvetica, sans-serif; color: #0099FF;}--></style>");
        sb.append("</head><body><h2 align=\"center\" class=\"style2\">");
        sb.append("ID2210 Uploaded Entry</h2><br>");
        try {
            addEntry(id, title, magnetLink);
            sb.append("Entry: ").append(title).append(" - ").append(id);
        } catch (IOException ex) {
            sb.append(ex.getMessage());
            java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
        sb.append("</body></html>");
        return sb.toString();
    }
    
    private void addEntry(Entry newEntry) throws IOException {
        addEntry(newEntry.getId(), newEntry.getTitle(), newEntry.getMagnetLink());
    }

    private void addEntry(String id, String title, String magnetLink) throws IOException {
        if (!indexStore.contains(Integer.parseInt(id))) {
            IndexWriter w = new IndexWriter(index, config);
            Document doc = new Document();
            // You may need to make the StringField searchable by NumericRangeQuery. See:
            // http://stackoverflow.com/questions/13958431/lucene-4-0-indexwriter-updatedocument-for-numeric-term
            // http://lucene.apache.org/core/4_2_0/core/org/apache/lucene/document/IntField.html
            doc.add(new IntField("id", Integer.parseInt(id), Field.Store.YES));
            doc.add(new TextField("title", title, Field.Store.YES));
            doc.add(new StringField("magnet", magnetLink, Field.Store.YES));
            w.addDocument(doc);
            w.close();


            int idVal = Integer.parseInt(id);
            indexStore.add(idVal);
            Collections.sort(indexStore);


            if (idVal == latestMissingIndexValue + 1) {
                latestMissingIndexValue++;
            }
        }
    }
    
    private String query(StringBuilder sb, String querystr) throws ParseException, IOException {
        // Check if index has any entries first to avoid exception from Lucene when creating the reader.
        if(index.listAll().length > 0) {
            // the "title" arg specifies the default field to use when no field is explicitly specified in the query.
            Query q = new QueryParser(Version.LUCENE_42, "title", analyzer).parse(querystr);
            IndexSearcher searcher = null;
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(index);
                searcher = new IndexSearcher(reader);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }

            int hitsPerPage = 1000;
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // display results
            sb.append("Found ").append(hits.length).append(" entries.<ul>");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                sb.append("<li>").append(i + 1).append(". [")
                                 .append(d.get("id")).append("] ")
                                 .append(d.get("title")).append(" (<a href=\"magnet:?xt=urn:btih:")
                                 .append(d.get("magnet")).append("\">Download!</a>)</li>");
            }
            sb.append("</ul>");

            // reader can only be closed when there
            // is no need to access the documents any more.
            reader.close();
        }
        else {
            sb.append("Found 0 entries!");
        }
        return sb.toString();
    }
    
    Handler<TManSample> handleTManSample = new Handler<TManSample>() {
        @Override
        public void handle(TManSample event) {
            // receive a new list of TMan partners
            ArrayList<PeerAddress> sample = new ArrayList<PeerAddress>(event.getSample());
//            System.err.println("[SERMAN::" + self.getPeerId() + "] Have " + tmanPartners);
//            System.err.println("[SERMAN::" + self.getPeerId() + "] TMan sent " + sample);
            tmanPartners = sample;
//            System.err.println("[SERMAN::" + self.getPeerId() + "] Got " + tmanPartners + " from TMan component");
            leader = event.getLeader();
        }
    };
    
    Handler<AddEntry> handleAddEntry = new Handler<AddEntry>() {
        @Override
        public void handle(AddEntry event) {
            System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Received AddEntry (" + event.getNewEntry() + ") from " + event.getPeerSource().getPeerId());
            try {
                addEntry(event.getNewEntry());
                trigger(new AddEntryACK(self, event.getPeerSource(), event.getNewEntry().getId()), networkPort);
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Acknowledged AddEntry!");
            }
            catch (IOException ex) {
                System.err.println("[NEW_ENTRY::" + self.getPeerId() + "] Failed in adding new entry!");
            }
        }
    };
    
//-------------------------------------------------------------------	
    Handler<AddIndexText> handleAddIndexText = new Handler<AddIndexText>() {
        @Override
        public void handle(AddIndexText event) {
            String id = String.valueOf(event.getID());
            System.err.println("[" + self.getPeerAddress().getId() + "] Adding index entry " + id + "::" + event.getText() + " (" + event.getMagnetLink() + ")!");
            Entry newEntry = new Entry(String.valueOf(pendingEntryID), event.getText(), event.getMagnetLink());
            System.err.println("[" + self.getPeerAddress().getId() + "] Added pending entry " + pendingEntryID);
            pendingEntries.put(String.valueOf(pendingEntryID), new PendingEntry(String.valueOf(pendingEntryID++), null, newEntry));
            handleNewEntry(newEntry);
        }
    };
    
    // If you call this method with a list of entries, it will
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
