package mtaxi.cumonywa.com;

import com.google.android.gms.maps.model.LatLng;

public class DriverStatus {

    private String driverId;
    private LatLng driverLocation;

    public DriverStatus(String driverId, LatLng driverLocation){
        this.driverId=driverId;
        this.driverLocation=driverLocation;
    }

    public String getDriverId() {
        return driverId;
    }

    public LatLng getDriverLocation() {
        return driverLocation;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setDriverLocation(LatLng driverLocation) {
        this.driverLocation = driverLocation;
    }
}
