package com.mapzen.util;

import android.content.Context;

import com.mapzen.R;

public final class DisplayHelper {

    private DisplayHelper() {
    }

    /**
     * Fetch the resource drawable ID of the turn icon for the given instruction and style.
     *
     * @param context         current context in which to display the icon.
     * @param turnInstruction the integer value representing this turn instruction.
     * @param iconStyle       use {@link IconStyle#WHITE} or {@link IconStyle#BLACK}.
     * @return the resource ID of the turn icon to display.
     */
    public static int getRouteDrawable(Context context, int turnInstruction, String iconStyle) {
        int drawableId = context.getResources().getIdentifier("ic_route_" + iconStyle + "_"
                + turnInstruction, "drawable", context.getPackageName());

        if (drawableId == 0) {
            if (IconStyle.WHITE.equals(iconStyle)) {
                drawableId = R.drawable.ic_route_wh_10;
            } else {
                drawableId = R.drawable.ic_route_bl_10;
            }
        }

        return drawableId;
    }

    public static class IconStyle {
        public static final String WHITE = "wh";
        public static final String BLACK = "bl";
    }
}
