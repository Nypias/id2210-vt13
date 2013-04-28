package search.system.peer;


import se.sics.kompics.Event;

public final class AddIndexText extends Event {

    private final int id;
    private final String text;
    private final String magnetLink;

    public AddIndexText(int id, String text, String magnetLink) {
        this.id = id;
        this.text = text;
        this.magnetLink = magnetLink;
    }
    
    public int getID() {
        return id;
    }

    public String getText() {
        return text;
    }
    
    public String getMagnetLink() {
        return magnetLink;
    }
}
