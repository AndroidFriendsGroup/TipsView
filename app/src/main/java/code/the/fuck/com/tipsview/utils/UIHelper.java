package code.the.fuck.com.tipsview.utils;

import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.widget.Toast;

/**
 * Created by 大灯泡 on 2018/12/28.
 */
public class UIHelper {


    public static void toast(@StringRes int textResId) {
        toast(AppContext.getResources().getString(textResId));
    }

    public static void toast(String text) {
        toast(text, Toast.LENGTH_SHORT);
    }

    public static void toast(@StringRes int textResId, int duration) {
        toast(AppContext.getResources().getString(textResId), duration);
    }

    public static void toast(String text, int duration) {
        Toast.makeText(AppContext.getAppContext(), text, duration).show();
    }

    public static int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spValue, Resources.getSystem().getDisplayMetrics());
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

}
