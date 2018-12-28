package code.the.fuck.com.tipsview.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import code.the.fuck.com.tipsview.R;


/**
 * Created by 大灯泡 on 2017/9/13.
 * <p>
 * 用于View的高亮指引
 */

public class TipsView extends FrameLayout {
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

    private Bitmap tipsBitmap;

    private OnClickListener mOnClickListener;

    public TipsView(@NonNull Context context) {
        this(context, null);
    }

    public TipsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipsView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        try {
            BitmapDrawable d = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.ic_test_share);
            if (d != null) {
                tipsBitmap = d.getBitmap();
            }
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
                destroy();
            }
        });
    }

    public void show(final View targetView) {
        if (targetView == null) {
            isPrepared = false;
            return;
        }

        if (globalOffset == null) {
            globalOffset = new Point();
        }
        if (targetViewRect == null) {
            targetViewRect = new Rect();
        }
        globalOffset.set(0, 0);
        targetViewRect.setEmpty();
        targetView.getGlobalVisibleRect(targetViewRect);

        if (!targetViewRect.isEmpty()) {
            //statusbar的高度
            targetViewRect.offset(0, -globalOffset.y);
            isPrepared = true;
        } else {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    targetView.getGlobalVisibleRect(targetViewRect, globalOffset);
                    if (!targetViewRect.isEmpty()) {
                        //statusbar的高度
                        targetViewRect.offset(0, -globalOffset.y);
                        isPrepared = true;
                    }
                    if (isPrepared) {
                        setVisibility(VISIBLE);
                        postInvalidate();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        targetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        targetView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }

        if (isPrepared) {
            setVisibility(VISIBLE);
            postInvalidate();
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
        if (tipsBitmap != null) {
            int left = targetViewRect.centerX() - tipsBitmap.getWidth();
            int top = targetViewRect.bottom + 5;
            drawCanvas.drawBitmap(tipsBitmap, left, top, mPaint);
        }
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
