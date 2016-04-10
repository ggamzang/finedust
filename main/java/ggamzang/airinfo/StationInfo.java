package ggamzang.airinfo;

/**
 * Created by user on 2016-01-06.
 */
public class StationInfo {
    String stationName = "";
    String addr = "";

    public StationInfo(String stationName, String addr) {
        this.stationName = stationName;
        this.addr = addr;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
