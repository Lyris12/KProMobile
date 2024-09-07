package cn.garymb.ygomobile.utils.glide;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

/**
 * Glide工具类
 * 注意对缓存的处理，后端更新图片后，要有合理机制更新缓存：
 * https://bumptech.github.io/glide/doc/caching.html
 */
public class GlideCompat {
    public static RequestManager with(Context context) {
        return Glide.with(context);
    }
    @Deprecated
    public static RequestManager with(android.app.Fragment context) {
        return Glide.with(context);
    }

    public static RequestManager with(Fragment context) {
        return Glide.with(context);
    }

    public static RequestManager with(View context) {
        return Glide.with(context);
    }

    public static RequestManager with(FragmentActivity context) {
        return Glide.with(context);
    }

    public static RequestManager with(Activity activity) {
        return Glide.with(activity);
    }
}
