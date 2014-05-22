package com.mapzen.search;

import com.mapzen.util.Logger;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class SavedSearch {
    private static LinkedList<String> store = new LinkedList<String>();
    public static final int DEFAULT_SIZE = 3;
    public static final int MAX_ENTRIES = 10;
    public static final String TAG = SavedSearch.class.getSimpleName();

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

    public static String serialize() {
        String serialized = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(store);
            os.close();
            serialized = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        } catch (IOException ioException) {
            Logger.e("Serializing SavedSearch failed");
        }
        return serialized;
    }

    public static void deserialize(String serializedSavedSearch) {
        try {
            ByteArrayInputStream bis =
                    new ByteArrayInputStream(Base64.decode(serializedSavedSearch, Base64.DEFAULT));
            ObjectInputStream oInputStream = new ObjectInputStream(bis);
            store = (LinkedList<String>) oInputStream.readObject();
            oInputStream.close();
        } catch (IOException ioException) {
            Logger.e("Deserializing SavedSearch failed");
        } catch (ClassNotFoundException classNotFound) {
            Logger.e("Deserializing SavedSearch failed");
        }
    }
}
