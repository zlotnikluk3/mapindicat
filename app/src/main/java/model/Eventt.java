package model;

public class Eventt {

    public Eventt(int id, String opis, String lat, String lng) {
        this.id = id;
        this.opis = opis;
        this.lat = lat;
        this.lng = lng;
    }

    private int id;
    private String opis,lat,lng;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
