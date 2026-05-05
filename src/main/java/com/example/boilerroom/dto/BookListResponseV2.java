package com.example.boilerroom.dto;

import java.util.List;

// Wrappas svaret från GET /api/v2/books med "data" (boklistan) och "version" (alltid "v2").
public class BookListResponseV2 {
    private String version;
    private List<BookResponseV2> data;

    // Getters
    public String getVersion() {return version;}
    public List<BookResponseV2> getData() {return data;}
    // Setters
    public void setVersion(String version) {this.version = version;}
    public void setData(List<BookResponseV2> data) {this.data = data;}
}
