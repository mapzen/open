package com.mapzen.util;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;

public class Highlighter {
    private String s;
    private int color;
    private ArrayList<String> terms = new ArrayList<String>();

    public void setString(String s) {
        this.s = s;
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

    public void setColor(int color) {
        this.color = color;
    }

    public void addTerm(String term) {
        terms.add(term);
    }
}
