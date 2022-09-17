package com.carstendroesser.audiorecorder.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carstendrosser on 24.11.15.
 */
public class PaintView extends View {

    public static int MIN_STROKEWIDTH = 2;
    public static int MAX_STROKEWIDTH = 28;

    public OnDrawingListener mDrawListener;

    private final int LIST_LIMIT = 5;

    private List<Bitmap> mBitmapList;
    private Canvas mCanvas;
    private Path mPath;

    private boolean mIsBlurEnabled = false;
    private int mStrokeWidth = 5;
    private int mAlpha = 255;
    private int mColor = 0;

    public PaintView(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);

        mBitmapList = new ArrayList<Bitmap>();
        mPath = new Path();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas pCanvas) {
        super.onDraw(pCanvas);

        mCanvas = pCanvas;

        // draw the last added bitmap to the canvas
        if (!mBitmapList.isEmpty()) {
            mCanvas.drawBitmap(mBitmapList.get(mBitmapList.size() - 1), 0, 0, getBitmapPaint());
        }

        // and add the path we are currently drawing
        mCanvas.drawPath(mPath, getPathPaint());
    }

    @Override
    public boolean onTouchEvent(MotionEvent pEvent) {
        float touchX = pEvent.getX();
        float touchY = pEvent.getY();

        switch (pEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // move the path to the touched position
                mPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                // draw to the touched position
                mPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                // get the drawing
                setDrawingCacheEnabled(true);
                buildDrawingCache();
                // rearrange bitmaplist
                if (mBitmapList.size() == LIST_LIMIT) {
                    mBitmapList.get(0).recycle();
                    mBitmapList = mBitmapList.subList(1, LIST_LIMIT);
                }
                // add current state
                mBitmapList.add(Bitmap.createBitmap(getDrawingCache(false)));
                setDrawingCacheEnabled(false);
                // reset path
                mPath.rewind();
                break;
            default:
                break;
        }

        invalidate();

        if (pEvent.getAction() == MotionEvent.ACTION_UP && mDrawListener != null) {
            mDrawListener.onNewDrawn();
        }

        return true;
    }

    /**
     * Clears the canvas.
     */
    public void clear() {
        mBitmapList.clear();
        mPath.rewind();
        invalidate();
    }

    /**
     * Undos the last painting-action.
     */
    public void undo() {
        // we need at least one bitmap in the
        // list. Otherwise, if we'd delete it,
        // the canvas would be cleared.
        if (mBitmapList.isEmpty()) {
            // empty
        } else if (mBitmapList.size() == 1) {
            invalidate();
        } else if (mBitmapList.size() > 1) {
            mBitmapList.remove(mBitmapList.size() - 1);
            invalidate();
        }
    }

    public boolean canUndo() {
        if (mBitmapList.isEmpty()) {
            return false;
        } else if (mBitmapList.size() == 1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Saves the painting to a given path.
     *
     * @param pPath the path where to save the painting
     * @return true if success, otherwise false
     * @throws FileNotFoundException
     */
    public boolean save(String pPath) throws FileNotFoundException {
        setDrawingCacheEnabled(true);
        setDrawingCacheBackgroundColor(Color.WHITE);
        Bitmap savedCanvas = getDrawingCache();
        return savedCanvas.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(pPath));
    }

    /**
     * Sets the transparency.
     *
     * @param pAlpha the new alpha-value
     */
    public void setTransparency(int pAlpha) {
        mAlpha = pAlpha;
    }

    /**
     * Sets the strokewidth.
     *
     * @param pStrokeWidth the new strokewidth
     */
    public void setStrokeWidth(int pStrokeWidth) {
        mStrokeWidth = pStrokeWidth;
    }

    /**
     * Sets if blur is enabled or not.
     *
     * @param pBlurIsEnabled the new blurstate
     */
    public void setBlurIsEnabled(boolean pBlurIsEnabled) {
        mIsBlurEnabled = pBlurIsEnabled;
    }

    public void setOnDrawnListener(OnDrawingListener pListener) {
        mDrawListener = pListener;
    }

    /**
     * Sets a color.
     *
     * @param pColor the new color
     */
    public void setColor(int pColor) {
        mColor = pColor;
    }

    /**
     * Creates a new Paint used for the path.
     *
     * @return the new Paint
     */
    public Paint getPathPaint() {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setColor(mColor);
        paint.setAlpha(mAlpha);
        if (mIsBlurEnabled) {
            paint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.SOLID));
        }
        paint.setAntiAlias(true);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    /**
     * Creates a new Paint, used for drawing the bitmaps.
     *
     * @return the new Paint
     */
    public Paint getBitmapPaint() {
        Paint paint = new Paint();
        paint.setColor(0xFFFFFFFF);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    /**
     * Returns the currently selected color.
     *
     * @return the selected color
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Returns the currently set strokewidth.
     *
     * @return the selected strokewidth
     */
    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    /**
     * Returns the state of blur.
     *
     * @return true if enabled, otherwise false
     */
    public boolean isBlurEnabled() {
        return mIsBlurEnabled;
    }

    @Override
    protected void onMeasure(int pWidthMeasureSpec, int pWeightMeasureSpec) {
        super.onMeasure(pWidthMeasureSpec, pWidthMeasureSpec);
    }

    public interface OnDrawingListener {
        void onNewDrawn();
    }

}
