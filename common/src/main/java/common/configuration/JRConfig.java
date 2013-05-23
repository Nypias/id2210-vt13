package common.configuration;

/**
 * Jolly Roger System Parameters
 */
public class JRConfig
{
    /**
     * The number of nodes in the system.
     */
    public static final int NUMBER_OF_NODES = 200;
    /**
     * The number of entries to add to the system after all nodes have started
     * up.
     */
    public static final int NUMBER_OF_INDEX_ENTRIES = 500;
    /**
     * The size of the similarity list in TMan component.
     */
    public static final int SIMILARITY_LIST_SIZE = 5;
    /**
     * The number of partitions to maintain.
     */
    public static final int NUMBER_OF_PARTITIONS = 5;
    /**
     * The number of cross-partition links.
     */
    public static final int NUMBER_OF_PARTITION_LINKS = 3;
    /**
     * Timeout when searching different partitions
     */
    public static final int PARTITION_QUERY_TIMEOUT = 5000;
    /**
     * The number of consecutive rounds we need to have a stable similarity list
     * in order to start the election.
     */
    public static final int CONVERGENCE_CONSTANT = 10;
    /**
     * The Bully algorithm timeout.
     */
    public static final int BULLY_TIMEOUT = 2000;
    /**
     * The Soft-Max selection temperature parameter.
     */
    public static final double SOFT_MAX_TEMPERATURE = 1.0;
    /**
     * The leader heartbeat timeout period.
     */
    public static final int HEARTBEAT_TIMEOUT = 5000;
    /**
     * The time to wait for an acknowledgment when adding a new entry.
     */
    public static final int NEW_ENTRY_ACK_TIMEOUT = 3000;
    /**
     * The number of retries to attempt when a new entry failed to add.
     */
    public static final int NEW_ENTRY_ADD_RETRIES = 5;
}
