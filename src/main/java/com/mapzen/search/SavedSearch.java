package com.mapzen.search;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SavedSearch {
    private static List<String> store = new ArrayList<String>();
    public static final int DEFAULT_SIZE = 3;
    public static final int MAX_ENTRIES = 10;

    public static int store(String term) {
        if (store.size() >= MAX_ENTRIES) {
            store.remove(0);
        }
        store.add(term);
        return 0;
    }

    public static Iterator<String> get() {
        return SavedSearch.get(DEFAULT_SIZE);
    }

    public static Iterator<String> get(int size) {
        if (store.size() == 0) {
            return store.iterator();
        }
        if (store.size() < size) {
            size = store.size();
        }
        return Lists.reverse(store.subList(store.size() - size, store.size())).iterator();
    }

    public static void clear() {
        store.clear();
    }

    public static boolean isEmpty() {
        return store.size() == 0;
    }
}
