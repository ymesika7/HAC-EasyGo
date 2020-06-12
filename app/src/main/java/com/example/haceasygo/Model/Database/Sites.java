package com.example.haceasygo.Model.Database;


public class Sites {
    public String type, number, description;

    /** Constructor
     * @param type site type
     * @param number site id number
     */
    public Sites(String type, String number){
        this.type = type;
        this.number = number;
    }

    public Sites(){}

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setType(String type) {
        this.type = type;
    }
}
