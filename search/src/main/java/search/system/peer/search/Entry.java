package search.system.peer.search;

public class Entry
{
    private String id;
    private String title;
    private String magnetLink;

    public Entry(String id, String title, String magnetLink) {
        this.id = id;
        this.title = title;
        this.magnetLink = magnetLink;
    }
    
    public Entry(String title, String magnetLink) {
        this.title = title;
        this.magnetLink = magnetLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getMagnetLink() {
        return magnetLink;
    }

    @Override
    public String toString() {
        return "Entry{" + "id=" + id + ", title=" + title + ", magnetLink=" + magnetLink + '}';
    }
}
