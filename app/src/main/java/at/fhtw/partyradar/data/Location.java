package at.fhtw.partyradar.data;

public class Location {

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

    public void setGeoPosition(double longitude, double latitude) {
        this.lng = longitude;
        this.lat = latitude;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lng;
    }
}
