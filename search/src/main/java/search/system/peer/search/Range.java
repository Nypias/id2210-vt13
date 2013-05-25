package search.system.peer.search;

/**
 * Class to represent a numerical range. This is used to represent missing
 * ranges in the index when peers gossip to disseminate index updates.
 */
public class Range
{
    /**
     * The left limit of the numerical range.
     */
    private int left;
    /*
     * The right limit of the numerical range.
     */
    private int right;

    /**
     * Create a range specifying the left and right limits.
     *
     * @param left The left limit of the numerical range.
     * @param right The right limit of the numerical range.
     */
    public Range(int left, int right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Get the left limit of the numerical range.
     *
     * @return The left limit of the numerical range.
     */
    public int getLeft() {
        return left;
    }

    /**
     * Get the right limit of the numerical range.
     *
     * @return The right limit of the numerical range.
     */
    public int getRight() {
        return right;
    }

    /**
     * Get the size of the range.
     *
     * @return The size of the range (inclusive).
     */
    public int getSize() {
        return Math.abs(right - left) + 1;
    }

    /**
     * Override method toString to facilitate easier debugging.
     *
     * @return A human readable string representation of the range.
     */
    @Override
    public String toString() {
        return "Range{" + "left=" + left + ", right=" + right + '}';
    }
}
