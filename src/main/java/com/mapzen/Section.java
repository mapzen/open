package com.mapzen;

import java.util.ArrayList;
import java.util.List;

public class Section {

    private String title;
    private List<SectionItem> sectionItems = new ArrayList<SectionItem>();

    public Section(String title) {
        this.title = title;
    }

    public void addSectionItem(long id, String title, String icon) {
        this.sectionItems.add( new SectionItem(id, title, icon));
    }

    public String getTitle() {
        return title;
    }

    public List<SectionItem> getSectionItems() {
        return sectionItems;
    }
}