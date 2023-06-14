package com.bugspointer.entity;


import lombok.Data;

@Data
public class Link implements Comparable<Link>{
    private int index;
    private String url;
    private int responseCode;
    private LinkType type;

    public Link(String url, int responseCode, LinkType type) {
        this.url = url;
        this.responseCode = responseCode;
        this.type = type;
    }

    @Override
    public int compareTo(Link other) {
        return this.getType().compareTo(other.getType());
    }
}


