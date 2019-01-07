package com.github.AndroidFriendsGroup.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
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


    public abstract class Params<B extends Params> {
        TipsViewBuilder mViewBuilder;
        int width;
        int height;
        int gravity = Gravity.BOTTOM | Gravity.LEFT;

        private Params(TipsViewBuilder viewBuilder) {
            mViewBuilder = viewBuilder;
        }

        public B setWidth(int width) {
            this.width = width;
            return self();
        }

        public B setHeight(int height) {
            this.height = height;
            return self();
        }

        public B setGravity(int gravity) {
            this.gravity = gravity;
            return self();
        }

        public B self() {
            return (B) this;
        }

        abstract void destroy();
    }

    public class ViewParams extends Params<ViewParams> {
        View customTipsView;

        private ViewParams(TipsViewBuilder viewBuilder) {
            super(viewBuilder);
        }

        public ViewParams addView(View customTipsView) {
            this.customTipsView = customTipsView;
            return self();
        }

        @Override
        void destroy() {
            customTipsView = null;
        }
    }

}
