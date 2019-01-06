package code.the.fuck.com.tipsview.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * 2019/1/5 17:51
 * Description：
 */
public class TipsViewBuilder {

    private WeakReference<Context> mContextWeakReference;
    View targetView;
    View customTipsView;
    Bitmap tipsBitmap;
    private View.OnClickListener mOnClickListener;

    private TipsViewBuilder(Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    public static TipsViewBuilder with(Context context) {
        return new TipsViewBuilder(context);
    }


    public TipsViewBuilder target(View targetView) {
        this.targetView = targetView;
        return this;
    }

    public TipsViewBuilder customTips(View customTipsView) {
        this.customTipsView = customTipsView;
        return this;
    }

    public TipsViewBuilder bitmapTips(Bitmap tipsBitmap) {
        this.tipsBitmap = tipsBitmap;
        return this;
    }

    public TipsViewBuilder setOnTapListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        return this;
    }

    public TipsView build() {
        final TipsView mTipsView = new TipsView(getContext()).build(this);
        mTipsView.setOnTapListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                } else {
                    removeTips(mTipsView);
                }
            }
        });
        return mTipsView;
    }

    public TipsView show(Activity activity) {
        TipsView tipsView = build();
        tipsView.show(targetView);
        ((ViewGroup) activity.getWindow().getDecorView()).addView(tipsView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return tipsView;
    }

    private void removeTips(TipsView mTipsView) {
        ((ViewGroup) mTipsView.getParent()).removeView(mTipsView);
    }

    private Context getContext() {
        return mContextWeakReference == null ? null : mContextWeakReference.get();
    }
}
