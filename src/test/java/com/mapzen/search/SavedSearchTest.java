package com.mapzen.search;

import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

import static com.mapzen.search.SavedSearch.MAX_ENTRIES;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class SavedSearchTest {
    @Before
    public void setUp() throws Exception {
        SavedSearch.clear();
    }

    @Test
    public void store_shouldStoreThingsAtTop() throws Exception {
        SavedSearch.store("search1");
        SavedSearch.store("search2");
        SavedSearch.store("expected");
        List<String> terms = SavedSearch.get(2);
        assertThat(terms.get(0)).isEqualTo("expected");
    }

    @Test
    public void store_shouldStoreMaxiumNumberOfElements() throws Exception {
        for (int i = 0; i < MAX_ENTRIES + 3; i++) {
            SavedSearch.store(String.valueOf(i));
        }
        assertThat(SavedSearch.get(MAX_ENTRIES + 10)).hasSize(MAX_ENTRIES);
    }

    @Test
    public void store_shouldEvictOldEntriesWhenMaxReached() throws Exception {
        SavedSearch.store("search1");
        for (int i = 0; i < MAX_ENTRIES; i++) {
            SavedSearch.store(String.valueOf(i));
        }
        assertThat(SavedSearch.get(MAX_ENTRIES)).doesNotContain("search1");
    }

    @Test
    public void get_shouldReturnDefaultNumberOfTerms() throws Exception {
        SavedSearch.store("search1");
        SavedSearch.store("search2");
        SavedSearch.store("search3");
        List<String> terms = SavedSearch.get();
        assertThat(terms).hasSize(SavedSearch.DEFAULT_SIZE);
    }

    @Test
    public void get_shouldReturnRequestedNumberOfTerms() throws Exception {
        SavedSearch.store("search1");
        SavedSearch.store("search2");
        SavedSearch.store("search3");
        List<String> terms = SavedSearch.get(1);
        assertThat(terms).hasSize(1);
    }

    @Test
    public void get_shouldReturnEmptyList() throws Exception {
        assertThat(SavedSearch.get()).hasSize(0);
    }

    @Test
    public void isEmtpy_shouldBeTrue() {
        assertThat(SavedSearch.isEmpty()).isTrue();
    }

    @Test
    public void isEmtpy_shouldBeFalse() {
        SavedSearch.store("search1");
        SavedSearch.store("search2");
        SavedSearch.store("search3");
        assertThat(SavedSearch.isEmpty()).isFalse();
    }

    @Test
    public void clearShouldEmptyCollection() throws Exception {
        SavedSearch.store("search1");
        SavedSearch.store("search2");
        SavedSearch.store("search3");
        SavedSearch.clear();
        assertThat(SavedSearch.get()).hasSize(0);
    }
}
