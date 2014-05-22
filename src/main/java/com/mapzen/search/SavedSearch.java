package com.mapzen.search;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class SavedSearch {
    private static LinkedList<String> store = new LinkedList<String>();
    public static final int DEFAULT_SIZE = 3;
    public static final int MAX_ENTRIES = 10;

    public static int store(String term) {
        if (store.size() >= MAX_ENTRIES) {
            store.removeLast();
        }
        store.addFirst(term);
        return 0;
    }

    public static Iterator<String> get() {
        return SavedSearch.get(DEFAULT_SIZE);
    }

    public static Iterator<String> get(int size) {
        if (store.size() == 0 || store.size() < size) {
            return store.iterator();
        }
        return store.subList(0, size).iterator();
    }

    public static void clear() {
        store.clear();
    }

    public static boolean isEmpty() {
        return store.size() == 0;
    }
}
