package com.mapzen.util;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;

public class Highlighter {
    private final String s;
    private final int color;

    private ArrayList<String> terms = new ArrayList<String>();

    public Highlighter(String s, int color) {
        this.s = s;
        this.color = color;
    }

    public Spanned highlight() {
        if (s == null) {
            return null;
        }

        return highlightInternal();
    }

    private Spanned highlightInternal() {
        final SpannableStringBuilder builder = new SpannableStringBuilder(s);
        int start, end;
        for (String term : terms) {
            start = s.toLowerCase().indexOf(term.toLowerCase());
            while (start > -1) {
                end = start + term.length();
                final ForegroundColorSpan span = new ForegroundColorSpan(color);
                builder.setSpan(span, start, end, 0);
                start = s.toLowerCase().indexOf(term.toLowerCase(), end);
            }
        }

        return builder;
    }

    public void addTerm(String term) {
        terms.add(term);
    }
}
