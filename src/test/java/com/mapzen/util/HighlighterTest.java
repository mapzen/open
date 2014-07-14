package com.mapzen.util;

import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import static com.mapzen.support.TestHelper.assertSpan;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class HighlighterTest {
    private Highlighter highlighter;

    @Before
    public void setUp() throws Exception {
        highlighter = new Highlighter();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(highlighter).isNotNull();
    }

    @Test
    public void nullInputString_shouldReturnNull() throws Exception {
        highlighter.setString(null);
        assertThat(highlighter.highlight()).isNull();
    }

    @Test
    public void emptyInputString_shouldReturnEmptyString() throws Exception {
        highlighter.setString("");
        assertThat(highlighter.highlight().toString()).isEmpty();
    }

    @Test
    public void stringWithNoTermsToHighlight_shouldReturnSameString() throws Exception {
        highlighter.setString("One Two Three");
        assertThat(highlighter.highlight().toString()).isEqualTo("One Two Three");
    }

    @Test
    public void stringWithOneTermToHighlight_shouldHighlightTerm() throws Exception {
        highlighter.setString("One Two Three");
        highlighter.setColor(0xff0000);
        highlighter.addTerm("One");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);

        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
    }

    @Test
    public void stringWithTwoTermsToHighlight_shouldHighlightBothTerms() throws Exception {
        highlighter.setString("One Two Three");
        highlighter.setColor(0xff0000);
        highlighter.addTerm("One");
        highlighter.addTerm("Two");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);

        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
        assertSpan(spanned, foregroundColorSpans[1], 4, 7, 0xff0000);
    }

    @Test
    public void stringWithMultipleCopiesOfSameTerm_shouldHighlightAllInstances() throws Exception {
        highlighter.setString("One Two One Three");
        highlighter.setColor(0xff0000);
        highlighter.addTerm("One");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);

        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
        assertSpan(spanned, foregroundColorSpans[1], 8, 11, 0xff0000);
    }

    @Test
    public void multipleCopiesOfMultipleTerms_shouldHighlightAllInstances() throws Exception {
        highlighter.setString("One Two Three One Two");
        highlighter.setColor(0xff0000);
        highlighter.addTerm("One");
        highlighter.addTerm("Two");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);

        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
        assertSpan(spanned, foregroundColorSpans[1], 14, 17, 0xff0000);
        assertSpan(spanned, foregroundColorSpans[2], 4, 7, 0xff0000);
        assertSpan(spanned, foregroundColorSpans[3], 18, 21, 0xff0000);
    }

    @Test
    public void shouldIgnoreCase() throws Exception {
        highlighter.setString("One Two Three One Two");
        highlighter.setColor(0xff0000);
        highlighter.addTerm("one");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);
        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
    }
}
