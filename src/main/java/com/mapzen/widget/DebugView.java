package com.mapzen.widget;

import com.mapzen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DebugView extends RelativeLayout {
    private TextView textView;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.debug, this);
        textView = (TextView) findViewById(R.id.text);
    }

    public void setText(String msg) {
        textView.setText(msg);
    }

    public String getText() {
        return textView.getText().toString();
    }
}
