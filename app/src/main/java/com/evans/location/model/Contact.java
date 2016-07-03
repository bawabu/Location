package com.evans.location.model;

import java.io.Serializable;

/**
 * Created by evans on 10/18/15.
 */
public class Contact implements Serializable {

    private int id;
    private String contactPhoneNumber;
    private String contactName;

    public Contact() {

    }

    public Contact(String contactPhoneNumber, String contactName) {
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactName = contactName;
    }

    public Contact(int id, String contactPhoneNumber, String contactName) {
        this.id = id;
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactName = contactName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
