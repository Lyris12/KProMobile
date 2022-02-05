package cn.garymb.ygomobile.utils;

import android.content.Context;

public class DensityUtils {
    /**
     * dp转px
     */
    public static int dp2px(Context ctx, float dp) {
        if(dp == 0){
            return 0;
        }
        float density = ctx.getResources().getDisplayMetrics().density;
        int px = Math.round(dp * density);// 4.9->5 4.4->4
        return px;
    }

    public static float px2dp(Context ctx, int px) {
        float density = ctx.getResources().getDisplayMetrics().density;
        float dp = px / density;
        return dp;
    }
}

