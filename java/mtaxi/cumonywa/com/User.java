package mtaxi.cumonywa.com;

public class User {

    String user_name,gmail,phone;

    public User(){

    }
    public User(String phone, String user_name, String gmail){
        this.user_name=user_name;
        this.gmail=gmail;
        this.phone=phone;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
