package search.system.peer.search;

import common.configuration.SearchConfiguration;
import common.peer.PeerAddress;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import cyclon.system.peer.cyclon.CyclonSampleRequest;
import cyclon.system.peer.cyclon.CyclonSampleResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
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
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.web.Web;
import se.sics.kompics.web.WebRequest;
import se.sics.kompics.web.WebResponse;
import search.simulator.snapshot.Snapshot;
import search.system.peer.AddIndexText;
import search.system.peer.IndexPort;
import tman.system.peer.tman.TManSample;
import tman.system.peer.tman.TManSamplePort;

/**
 * Should have some comments here.
 * @author jdowling
 */
public final class Search extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(Search.class);
    Positive<IndexPort> indexPort = positive(IndexPort.class);
    Positive<Network> networkPort = positive(Network.class);
    Positive<Timer> timerPort = positive(Timer.class);
    Negative<Web> webPort = negative(Web.class);
    Positive<CyclonSamplePort> cyclonSamplePort = positive(CyclonSamplePort.class);
    Negative<CyclonSamplePort> cyclonSamplePortRequest = negative(CyclonSamplePort.class);
    Positive<TManSamplePort> tmanSamplePort = positive(TManSamplePort.class);

    ArrayList<PeerAddress> neighbours = new ArrayList<PeerAddress>();
    Random randomGenerator = new Random();
    ArrayList<Integer> indexStore = new ArrayList<Integer>();
    private PeerAddress self;
    private long period;
    private double num;
    private SearchConfiguration searchConfiguration;
    // Apache Lucene used for searching
    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
    Directory index = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);

    private int latestMissingIndexValue =0;
    
//-------------------------------------------------------------------	
    public Search() {
        subscribe(handleUpdateIndexTimeout, timerPort);
        subscribe(handleIndexUpdateRequest, networkPort);
        subscribe(handleIndexUpdateResponse, networkPort);
        subscribe(handleInit, control);
        subscribe(handleWebRequest, webPort);
        subscribe(handleCyclonSample, cyclonSamplePort);
        subscribe(handleTManSample, tmanSamplePort);
        subscribe(handleAddIndexText, indexPort);
    }
//-------------------------------------------------------------------	
    Handler<SearchInit> handleInit = new Handler<SearchInit>() {
        public void handle(SearchInit init) {
            self = init.getSelf();
            num = init.getNum();
            searchConfiguration = init.getConfiguration();
            period = searchConfiguration.getPeriod();
            
            System.err.println("[wdijviuwhbu ] " + searchConfiguration);
            
//            SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
//            rst.setTimeoutEvent(new UpdateIndexTimeout(rst));
//            trigger(rst, timerPort);

            Snapshot.updateNum(self, num);
//            try {
//                String id = "100";
//                String title = "The Art of Computer Science";
//                String magnet = "5f601f38e6bd666763da8ebad157879b230f2d5c";
//                addEntry(id, title, magnet);
//            } catch (IOException ex) {
//                java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
//                System.exit(-1);
//            }
        }
    };

    Handler<UpdateIndexTimeout> handleUpdateIndexTimeout = new Handler<UpdateIndexTimeout>() {
        @Override
        public void handle(UpdateIndexTimeout event) {
            System.err.println("[" + self.getPeerAddress().getId() + "] UpdateIndexTimeout");
            trigger(new CyclonSampleRequest(), cyclonSamplePortRequest);
        }
    };
    
    Handler<CyclonSampleResponse> handleCyclonSample = new Handler<CyclonSampleResponse>() {
        @Override
        public void handle(CyclonSampleResponse event) {
            System.err.println("[" + self.getPeerAddress().getId() + "] CyclonSampleResponse (" + indexStore.size() + ")");
            PeerAddress peer = event.getRandomPeer();
            ArrayList<Range> missingRanges = getMissingRanges();
            Integer lastExisting = (indexStore.isEmpty())?-1:indexStore.get(indexStore.size() - 1);
            System.out.println("============================================================");
            System.out.println("[DEBUG::" + self.getPeerAddress().getId() + "->" + peer.getPeerAddress().getId() + "] I need " + missingRanges.size() + " ranges and my maximum ID is " + lastExisting + "!");
            System.out.println(missingRanges);
            System.out.println("============================================================");
            IndexUpdateRequest iur = new IndexUpdateRequest(self, peer, missingRanges, lastExisting);
            trigger(iur, networkPort);
        }
    };
    
    Handler<IndexUpdateRequest> handleIndexUpdateRequest = new Handler<IndexUpdateRequest>() {
        @Override
        public void handle(IndexUpdateRequest request) {
            try {
                System.err.println("[" + self.getPeerAddress().getId() + "] IndexUpdateRequest");
                if(!indexStore.isEmpty()) {
                    ArrayList<Entry> missingEntries = getMissingEntries(request.getMissingRanges(), request.getLastExisting());
                    System.out.println("============================================================");
                    System.out.println("[DEBUG::" + self.getPeerAddress().getId() + "->" + request.getPeerSource().getPeerAddress().getId() + "] I have these entries:");
                    System.out.println(missingEntries);
                    System.out.println("============================================================");
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
            System.err.println("[" + self.getPeerAddress().getId() + "] IndexUpdateResponse (" + indexStore.size() + ")");
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
            } else if (args[0].compareToIgnoreCase("add") == 0) {
                response = new WebResponse(addEntryHtml(args[1], args[2], args[3]), event, 1, 1);
            } else if (args[0].compareToIgnoreCase("jollyroger") == 0) {
                response = new WebResponse(jollyRogerHTML(), event, 1, 1);
            } else if (args[0].compareToIgnoreCase("jrsearch") == 0) {
                response = new WebResponse(jrSearchPageHtml(args[1]), event, 1, 1);
            } else {
                response = new WebResponse(searchPageHtml(event
                        .getTarget()), event, 1, 1);
            }
            trigger(response, webPort);
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
        String jollyRoger = "<!DOCTYPE html><html><head>	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />	<title>Jolly Roger</title>		<!-- CSS Styles -->	<link rel=\"stylesheet\" href=\"http://127.0.0.1/JollyRoger/css/base.css\" type=\"text/css\"/>	<link rel=\"stylesheet\" href=\"http://127.0.0.1/JollyRoger/css/layout.css\" type=\"text/css\"/>	<link rel=\"stylesheet\" href=\"http://127.0.0.1/JollyRoger/css/module.css\" type=\"text/css\"/>    	<!-- JavaScript Libraries -->    <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\" type=\"text/javascript\"></script>    <script type=\"text/javascript\" src=\"http://127.0.0.1/JollyRoger/js/jolly-lib.js\"></script>		<script>        var SEARCH_LINK = \"http://192.168.56.1:9999/\";		$(document).ready(function(e) {			$(\"#searchSubmit\").click(function(event) {                if($(\"#searchText\").val().length > 0) {                    var searchUrl = SEARCH_LINK + $(\"#searchPeer\").val() + \"/jrsearch-\" + $(\"#searchText\").val();                    searchAndRender(searchUrl, $(\".searchResults\"));                }                else {                    alert(\"Arrrrgh! Enter a search term!\");                }			});		});	</script></head><body>	<div class=\"website\">        <header>            <section class=\"headerContent\">            	<div class=\"logo\" />            </section>        </header>                <section class=\"search\">            <section class=\"searchBar\">            	<form action=\"#\">            		<input id=\"searchText\" type=\"text\" />                    <input id=\"searchPeer\" type=\"text\" />            		<input id=\"searchSubmit\" type=\"submit\" value=\"Search\" />            	</form>            </section>                        <section class=\"searchResultsContainer\">                <ul class=\"searchResults\">                	<img src=\"http://127.0.0.1/JollyRoger/img/no-torrents.png\" class=\"noresult\" alt=\"No results\" />                </ul>            </section>	        </section>                <!-- <footer>        	<div class=\"team\">	        	<div class=\"participant\">	            	<span>Thomas Fattal</span><br />	            	<span>tfattal@kth.se</span>	            </div>	            <div class=\"participant\">	            	<span>George Kallergis</span><br />	            	<span>geokal@kth.se</span>	            </div>	        </div>        </footer> -->    </div></body></html>";
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
        return sb.toString();
    }
    
    Handler<TManSample> handleTManSample = new Handler<TManSample>() {
        @Override
        public void handle(TManSample event) {
            // receive a new list of neighbours
            ArrayList<PeerAddress> sampleNodes = event.getSample();
            // Pick a node or more, and exchange index with them
        }
    };
    
//-------------------------------------------------------------------	
    Handler<AddIndexText> handleAddIndexText = new Handler<AddIndexText>() {
        @Override
        public void handle(AddIndexText event) {
            String id = String.valueOf(event.getID());
            logger.info("[" + self.getPeerAddress().getId() + "] Adding index entry " + id + "::" + event.getText() + " (" + event.getMagnetLink() + ")!");
            try {
                addEntry(id, event.getText(), event.getMagnetLink());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
    };
    
}
