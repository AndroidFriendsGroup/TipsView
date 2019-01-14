package com.github.AndroidFriendsGroup.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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

    private int maskColor = 0xCC000000;

    private Paint mPaint;
    private Paint mEraser;

    private boolean isPrepared = false;

    private Rect targetViewRect;
    private Point globalOffset;
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
        initView();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initView() {
        List<TipsViewBuilder.Params> paramsList = mViewBuilder.getParams();
        if (!ToolUtils.isListEmpty(paramsList)) {
            applyViewParams(paramsList);
        }

        try {
//            tipsBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_test_share);
            /*BitmapDrawable d = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.ic_test_share);
            if (d != null) {
                tipsBitmap = d.getBitmap()h;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        setWillNotDraw(false);
        setVisibility(GONE);
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
                setVisibility(GONE);
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

        if (!isPrepared) return;

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
        drawCanvas.drawColor(maskColor);
        //掏空一个圈O
        drawCanvas.drawCircle(targetViewRect.centerX(), targetViewRect.centerY(), DEFAULT_RADIUS + targetViewRect.width() / 2, mEraser);
        /*if (tipsBitmap != null && !tipsBitmap.isRecycled()) {
            int left = targetViewRect.centerX() - tipsBitmap.getWidth();
            int top = targetViewRect.bottom + 5;
            drawCanvas.drawBitmap(tipsBitmap, left, top, mPaint);
        } else if(true){//添加自定义View

        }*/
        //画好bitmap后，把新画布应用到当前画布中
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
    }
}
