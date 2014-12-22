package at.fhtw.partyradar.data;

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

    public LinkedList<String> getKeywords () {
        return keywords;
    }

    public boolean addKeyword(String keyword) {
        return keywords.add(keyword);
    }

    public boolean addKeywords(Collection<String> list) {
        return keywords.addAll(list);
    }

    public void clearKeywords() {
        keywords.clear();
    }
}
