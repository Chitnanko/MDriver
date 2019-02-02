package mtaxi.cumonywa.com.model;

public class DriverLocation {
    private double driverLat,driverLng;

    public  DriverLocation(){

    }

    public DriverLocation(double driverLat, double driverLng) {
        this.driverLat = driverLat;
        this.driverLng = driverLng;
    }

    public double getDriverLat() {
        return driverLat;
    }

    public void setDriverLat(double driverLat) {
        this.driverLat = driverLat;
    }

    public double getDriverLng() {
        return driverLng;
    }

    public void setDriverLng(double driverLng) {
        this.driverLng = driverLng;
    }
}
