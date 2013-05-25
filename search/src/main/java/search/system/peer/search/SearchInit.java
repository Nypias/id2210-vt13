package search.system.peer.search;

import common.configuration.SearchConfiguration;
import common.peer.PeerAddress;
import se.sics.kompics.Init;


/**
 * Initialize message to initialize the search component.
 */
public final class SearchInit extends Init
{
    /**
     * The address of the SearchPeer containing the Search component.
     */
    private final PeerAddress peerSelf;
    /**
     * !
     */
    private final int num;
    /**
     * The configuration of the search component.
     */
    private final SearchConfiguration configuration;

    /**
     * Create a SearchInit initialization event.
     *
     * @param peerSelf The address of the SearchPeer containing the Search
     * component.
     * @param num !
     * @param configuration The configuration of the search component.
     */
    public SearchInit(PeerAddress peerSelf, int num, SearchConfiguration configuration) {
        super();
        this.peerSelf = peerSelf;
        this.num = num;
        this.configuration = configuration;
    }

    /**
     * Get the peer address of the SearchPeer containing the current Search
     * component.
     *
     * @return The peer address of the node containing the Search component.
     */
    public PeerAddress getSelf() {
        return this.peerSelf;
    }

    /**
     * !
     *
     * @return !
     */
    public int getNum() {
        return this.num;
    }

    /**
     * Get the configuration options for the Search component.
     *
     * @return The Search component configuration options.
     */
    public SearchConfiguration getConfiguration() {
        return this.configuration;
    }
}