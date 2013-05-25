package search.system.peer.search;

/**
 * Class that represents an entry index.
 */
public class Entry
{
    /**
     * The id of the index entry.
     */
    private String id;
    /**
     * The title of the index entry.
     */
    private String title;
    /**
     * The magnet link of the index entry.
     */
    private String magnetLink;

    /**
     * Create a new entry object specifying the id, the title and the magnet
     * link of it.
     *
     * @param id The id of the index entry.
     * @param title The title of the index entry.
     * @param magnetLink The magnet link of the index entry.
     */
    public Entry(String id, String title, String magnetLink) {
        this.id = id;
        this.title = title;
        this.magnetLink = magnetLink;
    }

    /**
     * Create a new entry object specifying the title and the magnet link of it.
     *
     * @param title The title of the index entry.
     * @param magnetLink The magnet link of the index entry.
     */
    public Entry(String title, String magnetLink) {
        this.title = title;
        this.magnetLink = magnetLink;
    }

    /**
     * Get the ID of the entry.
     *
     * @return The ID of the entry.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the ID of the entry.
     *
     * @param id The ID of the entry.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the title of the entry.
     *
     * @return The title of the entry.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the magnet link of the entry.
     *
     * @return The magnet link of the entry.
     */
    public String getMagnetLink() {
        return magnetLink;
    }

    /**
     * Override toString method to print an entry in a human readable format.
     *
     * @return
     */
    @Override
    public String toString() {
        return "Entry{" + "id=" + id + ", title=" + title + ", magnetLink=" + magnetLink + '}';
    }
}
