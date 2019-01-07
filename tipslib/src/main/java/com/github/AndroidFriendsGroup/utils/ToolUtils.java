package com.github.AndroidFriendsGroup.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by 大灯泡 on 2019/1/7.
 */
public class ToolUtils {
    public static Activity scanForActivity(Context from, final int limit) {
        Context result = from;
        if (result instanceof Activity) {
            return (Activity) result;
        }
        int tryCount = 0;
        while (result instanceof ContextWrapper) {
            if (result instanceof Activity) {
                return (Activity) result;
            }
            if (tryCount > limit) {
                //break endless loop
                return null;
            }
            result = ((ContextWrapper) result).getBaseContext();
            tryCount++;
        }
        return null;
    }
}
