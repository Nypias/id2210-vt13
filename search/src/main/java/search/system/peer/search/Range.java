package search.system.peer.search;

public class Range
{
    private int left;
    private int right;

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getSize() {
        return Math.abs(right - left) + 1;
    }

    public Range(int left, int right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "Range{" + "left=" + left + ", right=" + right + '}';
    }
}
