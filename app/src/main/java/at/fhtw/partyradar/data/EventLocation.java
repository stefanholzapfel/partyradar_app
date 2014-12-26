package at.fhtw.partyradar.data;

public class EventLocation {

    public String locationID;
    private double lng;
    private double lat;
    public String locationName;
    public String address;
    public String addressAdditions;
    public String zipCode;
    public String city;
    public String country;
    public int maxAttends;

    /**
     * sets the position of the location
     * @param longitude
     * @param latitude
     */
    public void setGeoPosition(double longitude, double latitude) {
        this.lng = longitude;
        this.lat = latitude;
    }

    public double getLatitude() {
        return this.lat;
    }

    public double getLongitude() {
        return this.lng;
    }
}
