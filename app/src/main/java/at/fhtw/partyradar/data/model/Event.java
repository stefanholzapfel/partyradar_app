package at.fhtw.partyradar.data.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class Event {

    public String eventID;
    public String title;
    public String description;
    public String website;
    public Date start;
    public Date end;
    public String locationID;
    private LinkedList<String> keywords = new LinkedList<String>();
    public int attendeeCount;

    /**
     * returns a list of all keywords of the event
     * @return
     */
    public LinkedList<String> getKeywords() {
        return keywords;
    }

    /**
     * adds a keyword to the event
     * @param keyword
     * @return
     */
    public boolean addKeyword(String keyword) {
        return keywords.add(keyword);
    }

    /**
     * adds a collection of keywords to the event
     * @param list
     * @return
     */
    public boolean addKeywords(Collection<String> list) {
        return keywords.addAll(list);
    }

    /**
     * deletes / clears all keywords of the event
     */
    public void clearKeywords() {
        keywords.clear();
    }
}
