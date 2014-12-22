package at.fhtw.partyradar.data;

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
    public LinkedList<String> keywords;
    public int attendeeCount;

}
