package com.mapzen.open.search;

import com.mapzen.open.util.Logger;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import static android.provider.BaseColumns._ID;

public final class SavedSearch {
    public static final int DEFAULT_SIZE = 3;
    public static final int MAX_ENTRIES = 10;
    public static final String TAG = SavedSearch.class.getSimpleName();

    public static final String SEARCH_TERM = "search_term";
    public static final String[] COLUMNS = {
            _ID, SEARCH_TERM
    };

    private static SavedSearch instance;
    private LinkedList<String> store = new LinkedList<String>();

    static {
        instance = new SavedSearch();
    }

    public static SavedSearch getSavedSearch() {
        return instance;
    }

    public int store(String term) {
        if (store.size() >= MAX_ENTRIES) {
            store.removeLast();
        }

        store.remove(term);
        store.addFirst(term);
        return 0;
    }

    public Iterator<String> get() {
        return get(DEFAULT_SIZE);
    }

    public Iterator<String> get(int size) {
        if (store.size() == 0 || store.size() < size) {
            return store.iterator();
        }
        return store.subList(0, size).iterator();
    }

    public void clear() {
        store.clear();
    }

    public boolean isEmpty() {
        return store.size() == 0;
    }

    public String serialize() {
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

    public void deserialize(String serializedSavedSearch) {
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

    public Cursor getCursor() {
        final MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for (int i = 0; i < store.size(); i++) {
            cursor.addRow(new Object[]{i, store.get(i)});
        }

        return cursor;
    }
}
