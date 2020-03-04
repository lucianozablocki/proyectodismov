package com.proyfinaldismov;

public class User {
    String uid, deviceid;

    public User(String uid, String deviceid){
        this.uid = uid;
        this.deviceid = deviceid;
    }

    public String getUid(){return uid;}

    public String getDeviceid(){return deviceid;}

    public User(){}
}
