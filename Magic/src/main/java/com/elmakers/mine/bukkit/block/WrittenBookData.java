package com.elmakers.mine.bukkit.block;

import java.util.List;

import org.bukkit.inventory.meta.BookMeta;

public class WrittenBookData extends MaterialExtraData {
    protected String author;
    protected String title;
    protected List<String> pages;

    public WrittenBookData(BookMeta meta) {
        this.author = meta.getAuthor();
        this.title = meta.getTitle();
        this.pages = meta.getPages();
    }

    private WrittenBookData(String author, String title, List<String> pages) {
        this.author = author;
        this.title = title;
        this.pages = pages;
    }

    public void applyTo(BookMeta meta) {
        meta.setAuthor(this.author);
        meta.setTitle(this.title);
        meta.setPages(this.pages);
    }

    @Override
    public MaterialExtraData clone() {
        return new WrittenBookData(author, title, pages);
    }
}
