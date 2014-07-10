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
     * @return the resource ID of the turn icon to display.
     */
    public static int getRouteDrawable(Context context, int turnInstruction, String iconStyle) {
        int drawableId = context.getResources().getIdentifier("ic_route"  + iconStyle
                + turnInstruction, "drawable", context.getPackageName());

        if (drawableId == 0) {
            if (iconStyle.equals(IconStyle.GRAY)) {
                drawableId = R.drawable.ic_route_gr_1;
            }  else {
                drawableId = R.drawable.ic_route_1;
            }
        }

        return drawableId;
    }

    public static class IconStyle {
        public static final String STANDARD = "_";
        public static final String GRAY = "_gr_";
    }
}
