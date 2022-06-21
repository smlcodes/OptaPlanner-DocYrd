
package com.dpworld.domain;

public class DocYard {

    private String name;
    private int capacity;


    public DocYard(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public DocYard( ) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
