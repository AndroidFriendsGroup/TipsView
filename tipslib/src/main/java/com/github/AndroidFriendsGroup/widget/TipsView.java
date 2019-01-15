package com.github.AndroidFriendsGroup.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.AndroidFriendsGroup.utils.ToolUtils;

import java.util.List;


/**
 * Created by 大灯泡 on 2017/9/13.
 * <p>
 * 用于View的高亮指引
 * <p>
 * 使用请看{@link TipsViewBuilder}
 */

public class TipsView extends FrameLayout {
    private static final String TAG = "TipsView";
    private static final int DEFAULT_RADIUS = -5;


    private Paint mPaint;
    private Paint mEraser;

    //用来绘画的bitmap
    private Bitmap drawingBitmap;
    private Canvas drawCanvas;

    private ImageView imageView = null;
    private Bitmap tipsBitmap;
    private View customTipsView;
    private View targetView;
    private TipsViewBuilder mViewBuilder;

    private OnClickListener mOnClickListener;

    TipsView(@NonNull Context context, TipsViewBuilder builder) {
        this(context, null, builder);
    }

    TipsView(@NonNull Context context, @Nullable AttributeSet attrs, TipsViewBuilder builder) {
        this(context, attrs, 0, builder);
    }

    TipsView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, TipsViewBuilder builder) {
        super(context, attrs, defStyleAttr);
        mViewBuilder = builder;
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initView();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            TipsViewBuilder.Params mTipsParams = p.mTipsParams;
            Rect targetRect = mTipsParams.getTargetRect();
            int gravity = mTipsParams.gravity;
            //默认定位到target的左下方
            left = targetRect.left;
            top = targetRect.bottom;


            switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.LEFT:
                case Gravity.START:
                    left += -child.getMeasuredWidth() + p.leftMargin - p.rightMargin;
                    break;
                case Gravity.RIGHT:
                case Gravity.END:
                    left += targetRect.width() + p.leftMargin - p.rightMargin;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    left += targetRect.centerX() - (left + (child.getMeasuredWidth() >> 1));
                    break;
                default:
                    break;
            }
            switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.TOP:
                    top = targetRect.top - child.getMeasuredHeight() + p.topMargin - p.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    //default
                    break;
                case Gravity.CENTER_VERTICAL:
                    top = targetRect.centerY() - (top + (child.getMeasuredHeight() >> 1));
                    break;
                default:
                    break;
            }
            right = left + child.getMeasuredWidth();
            bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);
        }
    }

    private void initView() {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);
        List<TipsViewBuilder.Params> paramsList = mViewBuilder.getParams();
        if (!ToolUtils.isListEmpty(paramsList)) {
            applyViewParams(paramsList);
        }

        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setDither(true);
            mPaint.setStyle(Paint.Style.FILL);
        }

        if (mEraser == null) {
            mEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEraser.setColor(Color.WHITE);
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                }
            }
        });
    }

    private void applyViewParams(List<TipsViewBuilder.Params> paramsList) {
        for (TipsViewBuilder.Params params : paramsList) {
            setUpAndAddView(params);
        }
    }

    private void setUpAndAddView(TipsViewBuilder.Params params) {
        View tips = params.getTips();
        if (tips != null) {
            tips.setVisibility(INVISIBLE);
            LayoutParams p = new LayoutParams(params.width, params.height);
            p.setTipsParams(params);
            addViewInLayout(tips, -1, p, true);
        }
    }

    void onShow() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setVisibility(View.VISIBLE);
        }
        setVisibility(View.VISIBLE);
        postInvalidate();
    }


    public static class LayoutParams extends FrameLayout.LayoutParams {
        private TipsViewBuilder.Params mTipsParams;

        public TipsViewBuilder.Params getTipsParams() {
            return mTipsParams;
        }

        public LayoutParams setTipsParams(TipsViewBuilder.Params tipsParams) {
            mTipsParams = tipsParams;
            return this;
        }

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull ViewGroup.MarginLayoutParams source) {
            super(source);
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getChildCount() <= 0 || mViewBuilder == null) return;

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        if (width <= 0 || height <= 0) return;

        if (drawingBitmap == null || drawCanvas == null) {

            if (drawingBitmap != null) drawingBitmap.recycle();

            drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            if (drawCanvas == null) {
                drawCanvas = new Canvas(drawingBitmap);
            }
        }

        //清除画布
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //蒙层颜色
        drawCanvas.drawColor(mViewBuilder.getMaskColor());

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            TipsViewBuilder.Params mTipsParams = p.mTipsParams;
            Rect targetRect = mTipsParams.getTargetRect();
            drawCanvas.drawCircle(targetRect.centerX(), targetRect.centerY(), DEFAULT_RADIUS + targetRect.width() / 2, mEraser);
        }
        canvas.drawBitmap(drawingBitmap, 0, 0, null);
    }

    public OnClickListener getOnTapListener() {
        return mOnClickListener;
    }

    public void setOnTapListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private void destroy() {
        this.mOnClickListener = null;
        drawCanvas = null;
        if (drawingBitmap != null) {
            drawingBitmap.recycle();
            drawingBitmap = null;
        }
        if (tipsBitmap != null) {
            tipsBitmap.recycle();
            tipsBitmap = null;
        }
        if (mViewBuilder != null) {
            mViewBuilder.destroy();
            mViewBuilder = null;
        }
    }
}
