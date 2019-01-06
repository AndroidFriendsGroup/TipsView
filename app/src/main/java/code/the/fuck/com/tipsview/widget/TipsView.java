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
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
    private View customTipsView;
    private View targetView;

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

    public TipsView build(TipsViewBuilder builder) {
        this.customTipsView = builder.customTipsView;
        this.tipsBitmap = builder.tipsBitmap;
        this.targetView = builder.targetView;
        return this;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (customTipsView != null) {
            layoutCustomView(left, top, right, bottom);
        } else if (tipsBitmap != null){
            layoutBitmap(left, top, right, bottom);
        }
    }

    void layoutBitmap(int left, int top, int right, int bottom) {
        if (tipsBitmap != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(tipsBitmap);
            addView(imageView);

            final int width = 400;
            final int height = 100;

            int childLeft;
            int childTop;

            childLeft = targetViewRect.left - width/2;
            childTop = targetViewRect.bottom;

            imageView.layout(childLeft, childTop, childLeft+width, childTop+height);
        }
    }

    void layoutCustomView(int left, int top, int right, int bottom) {
        if (customTipsView != null) {
            if (customTipsView.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) customTipsView.getLayoutParams();

                final int width2 = customTipsView.getMeasuredWidth();
//                final int height = customTipsView.getMeasuredHeight();
                final int width = 150;
                final int height = 150;

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }

                int layoutDirection = LayoutDirection.LTR;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    layoutDirection = getLayoutDirection();
                }
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = targetViewRect.left - width - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = targetViewRect.right + lp.leftMargin;
                        break;
                    case Gravity.LEFT:
                        childLeft = targetViewRect.left - width - lp.rightMargin;
                    default:
                        childLeft = targetViewRect.right + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = targetViewRect.top + height + lp.bottomMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = targetViewRect.centerY() + height / 2 + lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = targetViewRect.bottom + lp.topMargin;
                        break;
                    default:
                        childTop = targetViewRect.bottom + lp.topMargin;
                }

                customTipsView.layout(childLeft, childTop, childLeft + width2, childTop + height);
            }
        }
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
            }
        });
    }

    public TipsView show(final View targetView) {
        if (targetView == null) {
            isPrepared = false;
            return this;
        }
        if (customTipsView != null) {
            addView(customTipsView);
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
        return this;
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

    public static Bitmap convertViewToBitmap(View view, int bitmapWidth, int bitmapHeight) {
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public Bitmap getViewBitmap(View comBitmap, int width, int height) {
        Bitmap bitmap = null;
        if (comBitmap != null) {
            comBitmap.clearFocus();
            comBitmap.setPressed(false);

            boolean willNotCache = comBitmap.willNotCacheDrawing();
            comBitmap.setWillNotCacheDrawing(false);

            // Reset the drawing cache background color to fully transparent
            // for the duration of this operation
            int color = comBitmap.getDrawingCacheBackgroundColor();
            comBitmap.setDrawingCacheBackgroundColor(0);
            float alpha = comBitmap.getAlpha();
            comBitmap.setAlpha(1.0f);

            if (color != 0) {
                comBitmap.destroyDrawingCache();
            }

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            comBitmap.measure(widthSpec, heightSpec);
            comBitmap.layout(0, 0, width, height);

            comBitmap.buildDrawingCache();
            Bitmap cacheBitmap = comBitmap.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e("view.ProcessImageToBlur", "failed getViewBitmap(" + comBitmap + ")",
                        new RuntimeException());
                return null;
            }
            bitmap = Bitmap.createBitmap(cacheBitmap);
            // Restore the view
            comBitmap.setAlpha(alpha);
            comBitmap.destroyDrawingCache();
            comBitmap.setWillNotCacheDrawing(willNotCache);
            comBitmap.setDrawingCacheBackgroundColor(color);
        }
        return bitmap;
    }
}
