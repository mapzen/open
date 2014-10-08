package com.mapzen.open.widget;

import com.mapzen.open.util.Logger;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class EditIntPreference extends EditTextPreference {

    public EditIntPreference(Context context) {
        super(context);
    }

    public EditIntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditIntPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        int intValue;
        try {
            intValue = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            Logger.e("Unable to parse preference value: " + value);
            return true;
        }

        return persistInt(intValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        final String s = a.getString(index);
        // Workaround for Robolectric which loads integer resources as hex strings.
        if (s.startsWith("0x")) {
            return Integer.valueOf(s.substring(2), 16).toString();
        }

        return s;
    }
}
