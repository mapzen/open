package com.mapzen.open.search;

import com.mapzen.open.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Parcel;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;

import static android.provider.BaseColumns._ID;

public final class SavedSearch {
    public static final int DEFAULT_SIZE = 3;
    public static final int MAX_ENTRIES = 10;
    public static final String TAG = SavedSearch.class.getSimpleName();

    public static final String SEARCH_TERM = "search_term";
    public static final String PAYLOAD = "payload";
    public static final String[] COLUMNS = {
            _ID, SEARCH_TERM
    };

    public class Member {
        private String term;
        private Parcel payload = null;

        public Member(String term, Parcel payload) {
            this.term = term;
            this.payload = payload;
        }

        public Member(String term) {
            this.term = term;
        }

        public String getTerm() {
            return term;
        }

        public Parcel getPayload() {
            return payload;
        }

        public JSONObject toJson() {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put(SEARCH_TERM, getTerm());
                Parcel payload = getPayload();
                if (payload != null) {
                    jsonObject.put(PAYLOAD, new String(payload.marshall(), "ISO-8859-1"));
                }
            } catch (UnsupportedEncodingException e) {
                Logger.e(e.getMessage());
            } catch (JSONException e) {
                Logger.e(e.getMessage());
            }
            return jsonObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Member member = (Member) o;

            return term.equals(member.term);
        }

        @Override
        public int hashCode() {
            int result = term.hashCode();
            if (payload != null) {
                result = 31 * result + payload.hashCode();
            }
            return result;
        }
    }

    private LinkedList<Member> store = new LinkedList<Member>();

    public int store(String term, Parcel payload) {
        truncate();
        Member member = new Member(term, payload);
        store.remove(member);
        store.addFirst(member);
        return 0;
    }

    public int store(String term) {
        truncate();
        Member member = new Member(term);
        store.remove(member);
        store.addFirst(member);
        return 0;
    }

    public Member get(int i) {
        return store.get(i);
    }

    public Iterator<Member> getIterator() {
        return getSubIterator(DEFAULT_SIZE);
    }

    public Iterator<Member> getSubIterator(int size) {
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
        JSONArray jsonArray = new JSONArray();
        for (Member member : store) {
            jsonArray.put(member.toJson());
        }
        return jsonArray.toString();
    }

    public void deserialize(String serializedSavedSearch) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(serializedSavedSearch);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String term = jsonObject.getString(SEARCH_TERM);
                Parcel payload = null;
                if (jsonObject.has(PAYLOAD)) {
                    payload = Parcel.obtain();
                    String rawPayload = jsonObject.getString(PAYLOAD);
                    payload.unmarshall(rawPayload.getBytes("ISO-8859-1"), 0,
                            rawPayload.getBytes("ISO-8859-1").length);
                    payload.setDataPosition(0);
                }
                store.add(new Member(term, payload));
            }
        } catch (UnsupportedEncodingException e) {
            Logger.e(e.getMessage());
        } catch (JSONException e) {
            Logger.e(e.getMessage());
        }
    }

    public Cursor getCursor() {
        final MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for (int i = 0; i < store.size(); i++) {
            cursor.addRow(new Object[]{ i, store.get(i).getTerm() });
        }

        return cursor;
    }

    private void truncate() {
        if (store.size() >= MAX_ENTRIES) {
            store.removeLast();
        }
    }
}
