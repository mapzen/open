package com.mapzen.search;

import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.database.Cursor;

import java.util.Iterator;

import static com.mapzen.search.SavedSearch.DEFAULT_SIZE;
import static com.mapzen.search.SavedSearch.MAX_ENTRIES;
import static com.mapzen.search.SavedSearch.getSavedSearch;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class SavedSearchTest {

    @Before
    public void setUp() throws Exception {
        getSavedSearch().clear();
    }

    @Test
    public void store_shouldStoreThingsAtTop() throws Exception {
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("expected");
        assertThat(getSavedSearch().get(2).next()).isEqualTo("expected");
    }

    @Test
    public void store_shouldStoreMaximumNumberOfElements() throws Exception {
        for (int i = 0; i < MAX_ENTRIES + 3; i++) {
            getSavedSearch().store(String.valueOf(i));
        }
        assertThat(countTerms(getSavedSearch().get(MAX_ENTRIES + 10))).isEqualTo(MAX_ENTRIES);
    }

    @Test
    public void store_shouldEvictOldEntriesWhenMaxReached() throws Exception {
        getSavedSearch().store("search1");
        for (int i = 0; i < MAX_ENTRIES; i++) {
            getSavedSearch().store(String.valueOf(i));
        }
        Iterator<String> it = getSavedSearch().get(MAX_ENTRIES);
        while (it.hasNext()) {
            assertThat(it.next()).isNotEqualTo("search1");
        }
    }

    @Test
    public void store_shouldNotStoreExistingTerms() throws Exception {
        getSavedSearch().store("expected");
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("expected");
        assertThat(countTerms(getSavedSearch().get(MAX_ENTRIES))).isEqualTo(3);
    }

    @Test
    public void store_shouldPutExistingTermsAtTheTop() throws Exception {
        getSavedSearch().store("expected");
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("expected");
        assertThat(getSavedSearch().get(1).next()).isEqualTo("expected");
    }

    @Test
    public void get_shouldReturnDefaultNumberOfTerms() throws Exception {
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("search3");
        getSavedSearch().store("search4");
        assertThat(countTerms(getSavedSearch().get())).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    public void get_shouldReturnRequestedNumberOfTerms() throws Exception {
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("search3");
        assertThat(countTerms(getSavedSearch().get(1))).isEqualTo(1);
    }

    @Test
    public void get_shouldReturnEmptyList() throws Exception {
        assertThat(getSavedSearch().get().hasNext()).isFalse();
    }

    @Test
    public void isEmpty_shouldBeTrue() {
        assertThat(getSavedSearch().isEmpty()).isTrue();
    }

    @Test
    public void isEmpty_shouldBeFalse() {
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("search3");
        assertThat(getSavedSearch().isEmpty()).isFalse();
    }

    @Test
    public void clearShouldEmptyCollection() throws Exception {
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("search3");
        getSavedSearch().clear();
        assertThat(getSavedSearch().get().hasNext()).isFalse();
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        getSavedSearch().store("search1");
        getSavedSearch().store("search2");
        getSavedSearch().store("expected");
        String serialized = getSavedSearch().serialize();
        getSavedSearch().clear();
        assertThat(getSavedSearch().get().hasNext()).isFalse();
        getSavedSearch().deserialize(serialized);
        Iterator<String> it = getSavedSearch().get();
        assertThat(it.next()).isEqualTo("expected");
        assertThat(it.next()).isEqualTo("search2");
        assertThat(it.next()).isEqualTo("search1");
    }

    @Test
    public void deserialize_shouldHandleEmptyString() throws Exception {
        String serialized = getSavedSearch().serialize();
        getSavedSearch().clear();
        assertThat(getSavedSearch().get().hasNext()).isFalse();
        getSavedSearch().deserialize(serialized);
        Iterator<String> it = getSavedSearch().get();
        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void getCursor_shouldReturnCursorWithSavedSearchTerms() throws Exception {
        getSavedSearch().store("saved query 1");
        getSavedSearch().store("saved query 2");
        getSavedSearch().store("saved query 3");
        Cursor cursor = getSavedSearch().getCursor();
        assertThat(cursor).hasCount(3);
        cursor.moveToFirst();
        assertThat(cursor.getString(1)).isEqualTo("saved query 3");
        cursor.moveToNext();
        assertThat(cursor.getString(1)).isEqualTo("saved query 2");
        cursor.moveToNext();
        assertThat(cursor.getString(1)).isEqualTo("saved query 1");
    }

    private int countTerms(Iterator<String> results) {
        int count = 0;
        while (results.hasNext()) {
            results.next();
            count++;
        }
        return count;
    }

}