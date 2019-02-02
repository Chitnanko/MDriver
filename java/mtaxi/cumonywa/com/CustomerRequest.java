package mtaxi.cumonywa.com;

public class CustomerRequest {

    private String customerId,startLat,startLng,
            stopLat,stopLng,startAddress,stopAddress;
    private String distance,totalCost;

    public CustomerRequest(){

    }


    public CustomerRequest(String customerId, String startLat, String startLng, String stopLat, String stopLng, String startAddress, String stopAddress,String distance,String totalCost) {
        this.customerId = customerId;
        this.startLat = startLat;
        this.startLng = startLng;
        this.stopLat = stopLat;
        this.stopLng = stopLng;
        this.startAddress = startAddress;
        this.stopAddress = stopAddress;
        this.distance=distance;
        this.totalCost=totalCost;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getStartLat() {
        return startLat;

    }

    public void setStartLat(String startLat) {
        this.startLat = startLat;
    }

    public String getStartLng() {
        return startLng;
    }

    public void setStartLng(String startLng) {
        this.startLng = startLng;
    }

    public String getStopLat() {
        return stopLat;
    }

    public void setStopLat(String stopLat) {
        this.stopLat = stopLat;
    }

    public String getStopLng() {
        return stopLng;
    }

    public void setStopLng(String stopLng) {
        this.stopLng = stopLng;
    }

    public String getDistance(){
        return distance;
    }

    public String getTotalCost(){
        return totalCost;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getStopAddress() {
        return stopAddress;
    }

    public void setStopAddress(String stopAddress) {
        this.stopAddress = stopAddress;
    }
}
