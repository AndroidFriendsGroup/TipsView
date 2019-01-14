package com.github.AndroidFriendsGroup.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

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

    private TipsViewBuilder(Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    public static TipsViewBuilder with(Context context) {
        return new TipsViewBuilder(context);
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

    public TipsView show(Activity activity) {
        TipsView tipsView = build();
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
        public static final int TYPE_VIEW = 1;
        public static final int TYPE_BITMAP = 1 << 1;


        TipsViewBuilder mViewBuilder;
        View target;
        int type;
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.BOTTOM | Gravity.LEFT;
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
            final Point globalOffset = new Point();
            if (target != null) {
                mTargetRect = new Rect();
                target.getGlobalVisibleRect(mTargetRect, globalOffset);
                if (!mTargetRect.isEmpty()) {
                    mTargetRect.offset(0, -globalOffset.y);
                } else {
                    target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            target.getGlobalVisibleRect(mTargetRect, globalOffset);
                            if (!mTargetRect.isEmpty()) {
                                mTargetRect.offset(0, -globalOffset.y);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                target.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                target.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
                }
            }
            return this;
        }

        public ViewParams append(View v) {
            return mViewBuilder.append(v);
        }

        public BitmapParams append(Bitmap bitmap) {
            return mViewBuilder.append(bitmap);
        }

        View getTips(){
            return null;
        }

        B self() {
            return (B) this;
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
            imageView = null;
        }
    }

}
