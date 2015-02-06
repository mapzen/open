package com.mapzen.open.util;

import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import static com.mapzen.open.support.TestHelper.assertSpan;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class HighlighterTest {
    @Test
    public void shouldNotBeNull() throws Exception {
        Highlighter highlighter = new Highlighter(null, 0xff0000);
        assertThat(highlighter).isNotNull();
    }

    @Test
    public void nullInputString_shouldReturnNull() throws Exception {
        Highlighter highlighter = new Highlighter(null, 0xff0000);
        assertThat(highlighter.highlight()).isNull();
    }

    @Test
    public void emptyInputString_shouldReturnEmptyString() throws Exception {
        Highlighter highlighter = new Highlighter("", 0xff0000);
        assertThat(highlighter.highlight().toString()).isEmpty();
    }

    @Test
    public void stringWithNoTermsToHighlight_shouldReturnSameString() throws Exception {
        Highlighter highlighter = new Highlighter("One Two Three", 0xff0000);
        assertThat(highlighter.highlight().toString()).isEqualTo("One Two Three");
    }

    @Test
    public void stringWithOneTermToHighlight_shouldHighlightTerm() throws Exception {
        Highlighter highlighter = new Highlighter("One Two Three", 0xff0000);
        highlighter.addTerm("One");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);

        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
    }

    @Test
    public void stringWithTwoTermsToHighlight_shouldHighlightBothTerms() throws Exception {
        Highlighter highlighter = new Highlighter("One Two Three", 0xff0000);
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
        Highlighter highlighter = new Highlighter("One Two One Three", 0xff0000);
        highlighter.addTerm("One");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);

        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
        assertSpan(spanned, foregroundColorSpans[1], 8, 11, 0xff0000);
    }

    @Test
    public void multipleCopiesOfMultipleTerms_shouldHighlightAllInstances() throws Exception {
        Highlighter highlighter = new Highlighter("One Two Three One Two", 0xff0000);
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
        Highlighter highlighter = new Highlighter("One Two Three One Two", 0xff0000);
        highlighter.addTerm("one");
        Spanned spanned = highlighter.highlight();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);
        assertSpan(spanned, foregroundColorSpans[0], 0, 3, 0xff0000);
    }
}
