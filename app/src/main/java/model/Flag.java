package model;

public class Flag {

    public Flag(int id, int map, int dial) {
        this.id = id;
        this.map = map;
        this.dial = dial;
    }

    private int id, map, dial;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMap() {
        return map;
    }

    public void setMap(int map) {
        this.map = map;
    }

    public int getDial() {
        return dial;
    }

    public void setDial(int dial) {
        this.dial = dial;
    }
}
