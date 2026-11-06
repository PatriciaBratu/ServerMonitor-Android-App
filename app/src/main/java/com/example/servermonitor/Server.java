package com.example.servermonitor;

import java.util.ArrayList;

public class Server {
    private String name;
    private String address;
    private String utils;
    private int freq;
    private int timeout_s;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUtils() {
        return utils;
    }

    public void setUtils(String utils) {
        this.utils = utils;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }
    public boolean validateAddress(Server server) {
        String address = server.getAddress();
        if(Character.isDigit(address.charAt(0))) {   //cazul pt adresa ip
            int i = 0;
            int number = 0;
            while (i < address.length() && address.charAt(i) != ':') {
                if (address.charAt(i) == '.')
                    number++;
                if (address.charAt(i) != '.' && Character.isDigit(address.charAt(0)) &&
                        !Character.isDigit(address.charAt(i))) {
                    System.out.println("nu sunt de acelasi tip");
                    return false;
                }
                i++;
            }
            if (i < address.length()) {
                i++;
                while (i < address.length()) {
                    if (!Character.isDigit(address.charAt(i))) {
                        System.out.println("portul nu e format doar din digits");
                        return false;
                    }
                    i++;
                }
            }
            if (Character.isDigit(address.charAt(0)) && number != 3) {
                System.out.println("nu sunt 4 nr");
                return false;
            }
        }
        return true; //cazul pentru domain name
    }
    public boolean validateUtils(Server server) {
        String utils = server.getUtils();
        ArrayList<String> accepted = new ArrayList<>();
        accepted.add("ping");
        accepted.add("http_request");
        accepted.add("telnet");
        accepted.add("curl");
        if(!accepted.contains(utils)) {
            System.out.println("un utilitar gresit");
            return false;
        }
        return true;
    }
    public boolean validate(){
        return validateAddress(this) && validateUtils(this);
    }


    public int getTimeout_s() {
        return timeout_s;
    }

    public void setTimeout_s(int timeout_s) {
        this.timeout_s = timeout_s;
    }
}
