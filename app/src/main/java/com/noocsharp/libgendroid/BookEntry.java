package com.noocsharp.libgendroid;

import java.util.ArrayList;

public class BookEntry {
    private String author, title, publisher, language, extension, size;
    private ArrayList<String> mirrors;
    private int id, pages, year;

    BookEntry() {
        mirrors = new ArrayList<>();
    }

    public void addMirror(String mirror) {
        mirrors.add(mirror);
    }

    public ArrayList<String> getMirrors() {
        return mirrors;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String toString() {
        return "id: " + id +
            ", author: " + author +
            ", title: " + title +
            ", publisher: " + publisher +
            ", language: " + language +
            ", extension: " + extension +
            ", pages: " + pages +
            ", size: " + size;
    }
}
