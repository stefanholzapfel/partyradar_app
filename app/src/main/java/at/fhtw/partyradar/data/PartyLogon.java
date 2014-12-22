package at.fhtw.partyradar.data;

import java.util.Date;

public class PartyLogon {

    private String eventID;
    private Date logonTime;

    public String getEventID() {
        return this.eventID;
    }

    public Date getLogonTime() {
        return logonTime;
    }

    public void LogOnParty(String eventID, Date dateTime) {
        this.eventID = eventID;
        this.logonTime = dateTime;
    }
}
