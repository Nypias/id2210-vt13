package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.PeerAddress;
import se.sics.kompics.Init;


/**
 * Event sent to TMan component to initialize it.
 */
public final class TManInit extends Init
{
    /**
     * The address of that specific peer
     */
    private final PeerAddress peerSelf;
    /**
     * The configuration information for TMan component as specified in the
     * common package.
     */
    private final TManConfiguration configuration;

    /**
     * Create a new TManInit event specifying the peer address and the
     * configuration details for the TMan component.
     *
     * @param peerSelf The peer address for the specific peer.
     * @param configuration The configuration details for TMan.
     */
    public TManInit(PeerAddress peerSelf, TManConfiguration configuration) {
        super();
        this.peerSelf = peerSelf;
        this.configuration = configuration;
    }

    /**
     * Get the peer address.
     *
     * @return The peer address.
     */
    public PeerAddress getSelf() {
        return this.peerSelf;
    }

    /**
     * Get the TMan component configuration details.
     *
     * @return The TMan component configuration details.
     */
    public TManConfiguration getConfiguration() {
        return this.configuration;
    }
}