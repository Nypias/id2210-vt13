package tman.system.peer.tman;

import se.sics.kompics.PortType;


/**
 * Port for TMan component to send out samples to other components.
 */
public final class TManSamplePort extends PortType {{
        positive(TManSample.class);
}}
