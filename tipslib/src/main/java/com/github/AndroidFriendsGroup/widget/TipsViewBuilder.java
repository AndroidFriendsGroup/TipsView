package com.github.AndroidFriendsGroup.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.github.AndroidFriendsGroup.utils.ToolUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 2019/1/5 17:51
 * Description：
 */
public class TipsViewBuilder {

    private WeakReference<Context> mContextWeakReference;
    private List<Params> mParams = new ArrayList<>();
    private View.OnClickListener mOnClickListener;
    private int maskColor = 0xCC000000;

    private TipsViewBuilder(Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    public static TipsViewBuilder with(Context context) {
        return new TipsViewBuilder(context);
    }

    public TipsViewBuilder setMaskColor(int maskColor) {
        this.maskColor = maskColor;
        return this;
    }

    int getMaskColor() {
        return maskColor;
    }

    public ViewParams append(View v) {
        ViewParams params = new ViewParams(this);
        params.setView(v);
        mParams.add(params);
        return params;
    }

    public BitmapParams append(Bitmap bitmap) {
        BitmapParams params = new BitmapParams(this);
        params.bitmap(bitmap);
        mParams.add(params);
        return params;
    }

    public TipsViewBuilder setOnTapListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        return this;
    }

    public TipsView build() {
        final TipsView mTipsView = new TipsView(getContext(), this);
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

    List<Params> getParams() {
        return mParams;
    }

    public final TipsView show(Activity activity) {
        TipsView tipsView = build();
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final int childCount = decorView.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View v = decorView.getChildAt(i);
            if (v instanceof TipsView) {
                decorView.removeViewInLayout(v);
            }
        }
        tipsView.onShow();
        ((ViewGroup) activity.getWindow().getDecorView()).addView(tipsView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return tipsView;
    }

    private void removeTips(TipsView mTipsView) {
        ((ViewGroup) mTipsView.getParent()).removeView(mTipsView);
    }

    private Context getContext() {
        return mContextWeakReference == null ? null : mContextWeakReference.get();
    }

    void destroy() {
        if (!ToolUtils.isListEmpty(mParams)) {
            for (Params param : mParams) {
                param.destroy();
            }
            mParams = null;
        }
        if (mContextWeakReference != null) {
            mContextWeakReference.clear();
            mContextWeakReference = null;
        }
        mOnClickListener = null;
    }


    public abstract class Params<B extends Params> {
        public static final int TYPE_VIEW = 1;
        public static final int TYPE_BITMAP = 1 << 1;


        TipsViewBuilder mViewBuilder;
        View target;
        int type;
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.BOTTOM ;
        Rect mTargetRect;

        private Params(TipsViewBuilder viewBuilder, int type) {
            mViewBuilder = viewBuilder;
            this.type = type;
        }

        public B width(int width) {
            this.width = width;
            return self();
        }

        public B height(int height) {
            this.height = height;
            return self();
        }

        public B gravity(int gravity) {
            this.gravity = gravity;
            return self();
        }

        public Params<B> target(final View target) {
            this.target = target;
            findTargetVisibleRect(target);
            return this;
        }

        private void findTargetVisibleRect(final View target) {
            if (target != null) {
                mTargetRect = new Rect();
                target.getGlobalVisibleRect(mTargetRect);
                if (mTargetRect.isEmpty()) {
                    target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            target.getGlobalVisibleRect(mTargetRect);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                target.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                target.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
                }
            } else {
                mTargetRect = new Rect();
            }
        }

        public final Rect getTargetRect() {
            if (mTargetRect == null) {
                findTargetVisibleRect(target);
            }
            return mTargetRect;
        }

        public ViewParams append(View v) {
            return mViewBuilder.append(v);
        }

        public BitmapParams append(Bitmap bitmap) {
            return mViewBuilder.append(bitmap);
        }

        View getTips() {
            return null;
        }

        B self() {
            return (B) this;
        }

        public TipsViewBuilder build() {
            return mViewBuilder;
        }

        public TipsView show(Activity activity) {
            return build().show(activity);
        }

        abstract void destroy();
    }

    public class ViewParams extends Params<ViewParams> {
        View customTipsView;

        private ViewParams(TipsViewBuilder viewBuilder) {
            super(viewBuilder, TYPE_VIEW);
        }

        public ViewParams setView(View customTipsView) {
            this.customTipsView = customTipsView;
            return self();
        }

        @Override
        View getTips() {
            return customTipsView;
        }

        @Override
        void destroy() {
            customTipsView = null;
        }
    }

    public class BitmapParams extends Params<BitmapParams> {
        ImageView imageView;

        private BitmapParams(TipsViewBuilder viewBuilder) {
            super(viewBuilder, TYPE_BITMAP);
        }

        public BitmapParams bitmap(Bitmap bitmap) {
            imageView = new ImageView(getContext());
            imageView.setImageBitmap(bitmap);
            return this;
        }

        @Override
        View getTips() {
            return imageView;
        }

        @Override
        void destroy() {
            imageView.setImageBitmap(null);
        }
    }

}
