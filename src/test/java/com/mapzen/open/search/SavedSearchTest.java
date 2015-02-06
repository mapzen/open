package com.mapzen.open.search;

import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.os.Parcel;

import java.util.Iterator;

import static com.mapzen.open.entity.SimpleFeature.TEXT;
import static com.mapzen.open.search.SavedSearch.DEFAULT_SIZE;
import static com.mapzen.open.search.SavedSearch.MAX_ENTRIES;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class SavedSearchTest {
    SavedSearch savedSearch;
    Parcel payload;

    @Before
    public void setUp() throws Exception {
        savedSearch = new SavedSearch();
        savedSearch.clear();
        payload = Parcel.obtain();
        TestHelper.getTestSimpleFeature().writeToParcel(payload, 0);
        payload.setDataPosition(0);
    }

    @Test
    public void store_shouldHaveNullPayload() throws Exception {
        savedSearch.store("term");
        assertThat(savedSearch.get(0).getPayload()).isNull();
    }

    @Test
    public void store_shouldHaveNullPayloadAfterSerialize() throws Exception {
        savedSearch.store("term");
        String serialized = savedSearch.serialize();
        savedSearch.clear();
        savedSearch.deserialize(serialized);
        assertThat(savedSearch.get(0).getPayload()).isNull();
    }

    @Test
    public void payloadShouldBeHealthy() throws Exception {
        savedSearch.store("term", payload);
        String serialized = savedSearch.serialize();
        savedSearch.clear();
        savedSearch.deserialize(serialized);
        SimpleFeature feature = SimpleFeature.readFromParcel(savedSearch.get(0).getPayload());
        assertThat(feature.getProperty(SimpleFeature.ID)).isNotNull();
    }

    @Test
    public void store_updateRecordWithPayload() throws Exception {
        savedSearch.store("term");
        savedSearch.store("term", payload);
        assertThat(savedSearch.get(0).getPayload()).isNotNull();
        assertThat(countTerms(savedSearch.getIterator())).isEqualTo(1);
    }

    @Test
    public void store_shouldStoreThingsAtTop() throws Exception {
        savedSearch.store("search1");
        savedSearch.store("search2", payload);
        savedSearch.store("expected");
        assertThat(savedSearch.getSubIterator(2).next().getTerm()).isEqualTo("expected");
    }

    @Test
    public void store_shouldStoreMaximumNumberOfElements() throws Exception {
        for (int i = 0; i < MAX_ENTRIES + 3; i++) {
            savedSearch.store(String.valueOf(i));
        }
        assertThat(countTerms(savedSearch.getSubIterator(MAX_ENTRIES + 10))).isEqualTo(MAX_ENTRIES);
    }

    @Test
    public void store_shouldEvictOldEntriesWhenMaxReached() throws Exception {
        savedSearch.store("search1");
        for (int i = 0; i < MAX_ENTRIES; i++) {
            savedSearch.store(String.valueOf(i));
        }
        Iterator<SavedSearch.Member> it = savedSearch.getSubIterator(MAX_ENTRIES);
        while (it.hasNext()) {
            assertThat(it.next().getTerm()).isNotEqualTo("search1");
        }
    }

    @Test
    public void store_shouldNotStoreExistingTerms() throws Exception {
        savedSearch.store("expected");
        savedSearch.store("search1", payload);
        savedSearch.store("search2");
        savedSearch.store("expected");
        assertThat(countTerms(savedSearch.getSubIterator(MAX_ENTRIES))).isEqualTo(3);
    }

    @Test
    public void store_shouldUpdateEntriresWithPayload() throws Exception {
        Parcel newPayload = Parcel.obtain();
        SimpleFeature expectedFeature = TestHelper.getTestSimpleFeature();
        expectedFeature.setProperty(TEXT, "new property");
        expectedFeature.writeToParcel(newPayload, 0);
        newPayload.setDataPosition(0);
        savedSearch.store("expected", payload);
        savedSearch.store("expected", newPayload);
        SavedSearch.Member member = savedSearch.getIterator().next();
        SimpleFeature simpleFeature = SimpleFeature.readFromParcel(member.getPayload());
        assertThat(simpleFeature.getProperty(TEXT)).isEqualTo(expectedFeature.getProperty(TEXT));
        assertThat(countTerms(savedSearch.getSubIterator(MAX_ENTRIES))).isEqualTo(1);
    }

    @Test
    public void store_shouldPutExistingTermsAtTheTop() throws Exception {
        savedSearch.store("expected");
        savedSearch.store("search1");
        savedSearch.store("search2");
        savedSearch.store("expected", payload);
        assertThat(savedSearch.getSubIterator(1).next().getTerm()).isEqualTo("expected");
    }

    @Test
    public void get_shouldReturnDefaultNumberOfTerms() throws Exception {
        savedSearch.store("search1");
        savedSearch.store("search2", payload);
        savedSearch.store("search3");
        savedSearch.store("search4");
        assertThat(countTerms(savedSearch.getIterator())).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    public void get_shouldReturnRequestedNumberOfTerms() throws Exception {
        savedSearch.store("search1");
        savedSearch.store("search2", payload);
        savedSearch.store("search3");
        assertThat(countTerms(savedSearch.getSubIterator(1))).isEqualTo(1);
    }

    @Test
    public void get_shouldReturnEmptyList() throws Exception {
        assertThat(savedSearch.getIterator().hasNext()).isFalse();
    }

    @Test
    public void isEmpty_shouldBeTrue() {
        assertThat(savedSearch.isEmpty()).isTrue();
    }

    @Test
    public void isEmpty_shouldBeFalse() {
        savedSearch.store("search1");
        savedSearch.store("search2", payload);
        savedSearch.store("search3");
        assertThat(savedSearch.isEmpty()).isFalse();
    }

    @Test
    public void clearShouldEmptyCollection() throws Exception {
        savedSearch.store("search1");
        savedSearch.store("search2", payload);
        savedSearch.store("search3");
        savedSearch.clear();
        assertThat(savedSearch.getIterator().hasNext()).isFalse();
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        savedSearch.store("search1");
        savedSearch.store("search2", payload);
        savedSearch.store("expected");
        String serialized = savedSearch.serialize();
        savedSearch.clear();
        assertThat(savedSearch.getIterator().hasNext()).isFalse();
        savedSearch.deserialize(serialized);
        Iterator<SavedSearch.Member> it = savedSearch.getIterator();
        assertThat(it.next().getTerm()).isEqualTo("expected");
        assertThat(it.next().getTerm()).isEqualTo("search2");
        assertThat(it.next().getTerm()).isEqualTo("search1");
    }

    @Test
    public void deserialize_shouldHandleEmptyString() throws Exception {
        String serialized = savedSearch.serialize();
        savedSearch.clear();
        assertThat(savedSearch.getIterator().hasNext()).isFalse();
        savedSearch.deserialize(serialized);
        Iterator<SavedSearch.Member> it = savedSearch.getIterator();
        assertThat(it.hasNext()).isFalse();
    }

    @Test
    public void getCursor_shouldReturnCursorWithSavedSearchTerms() throws Exception {
        savedSearch.store("saved query 1");
        savedSearch.store("saved query 2", payload);
        savedSearch.store("saved query 3");
        Cursor cursor = savedSearch.getCursor();
        assertThat(cursor).hasCount(3);
        cursor.moveToFirst();
        assertThat(cursor.getString(1)).isEqualTo("saved query 3");
        cursor.moveToNext();
        assertThat(cursor.getString(1)).isEqualTo("saved query 2");
        cursor.moveToNext();
        assertThat(cursor.getString(1)).isEqualTo("saved query 1");
    }

    @Test
    public void deserialize_shouldClearStoreBeforeLoadingTerms() throws Exception {
        savedSearch.store("saved query");
        String serialized = savedSearch.serialize();
        savedSearch.deserialize(serialized);
        assertThat(savedSearch.size()).isEqualTo(1);
    }

    private int countTerms(Iterator<SavedSearch.Member> results) {
        int count = 0;
        while (results.hasNext()) {
            results.next();
            count++;
        }
        return count;
    }
}
