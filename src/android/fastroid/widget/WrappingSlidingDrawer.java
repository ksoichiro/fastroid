/*
 * Copyright (c) 2011 Soichiro Kashima
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package android.fastroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SlidingDrawer;

/**
 * SlidingDrawer which adjusts to the visible area.
 * 
 * @author Soichiro Kashima
 * @since 2011/05/05
 */
public final class WrappingSlidingDrawer extends SlidingDrawer implements
        SlidingDrawer.OnDrawerOpenListener,
        SlidingDrawer.OnDrawerCloseListener {

    /** True if the orientation is vertical. */
    private boolean mVertical;

    /** Vertical offset from the top. */
    private int mTopOffset;

    /** Listener which receives the drawer opened event. */
    private OnDrawerOpenListener mOnDrawerOpenListener;

    /** Listener which receives the drawer closed event. */
    private OnDrawerCloseListener mOnDrawerCloseListener;

    /** This is the listener which receives the drawer opened event. */
    public interface OnDrawerOpenListener {
        /** Called when the drawer has opened. */
        void onDrawerOpened();
    }

    /** This is the listener which receives the drawer closed event. */
    public interface OnDrawerCloseListener {
        /** Called when the drawer has closed. */
        void onDrawerClosed();
    }

    /**
     * Creates the drawer.
     * 
     * @param context context
     * @param attrs attributes
     * @param defStyle style
     */
    public WrappingSlidingDrawer(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);

        final int orientation =
                attrs.getAttributeIntValue("android", "orientation", ORIENTATION_VERTICAL);
        mTopOffset = attrs.getAttributeIntValue("android", "topOffset", 0);
        mVertical = orientation == SlidingDrawer.ORIENTATION_VERTICAL;

        setOnDrawerOpenListener(this);
        setOnDrawerCloseListener(this);
    }

    /**
     * Creates the drawer.
     * 
     * @param context context
     * @param attrs attributes
     */
    public WrappingSlidingDrawer(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final int orientation =
                attrs.getAttributeIntValue("android", "orientation", ORIENTATION_VERTICAL);
        mTopOffset = attrs.getAttributeIntValue("android", "topOffset", 0);
        mVertical = orientation == SlidingDrawer.ORIENTATION_VERTICAL;

        setOnDrawerOpenListener(this);
        setOnDrawerCloseListener(this);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (!isOpened()) {
            setMeasuredDimension(0, 0);
            return;
        }

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        final View handle = getHandle();
        final View content = getContent();
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        if (mVertical) {
            final int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
            content.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, heightSpecMode));
            heightSpecSize = handle.getMeasuredHeight() + mTopOffset + content.getMeasuredHeight();
            widthSpecSize = content.getMeasuredWidth();
            if (handle.getMeasuredWidth() > widthSpecSize) {
                widthSpecSize = handle.getMeasuredWidth();
            }
        } else {
            final int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
            getContent().measure(
                    MeasureSpec.makeMeasureSpec(width, widthSpecMode),
                    heightMeasureSpec);
            widthSpecSize = handle.getMeasuredWidth() + mTopOffset + content.getMeasuredWidth();
            heightSpecSize = content.getMeasuredHeight();
            if (handle.getMeasuredHeight() > heightSpecSize) {
                heightSpecSize = handle.getMeasuredHeight();
            }
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    public void onDrawerOpened() {
        invalidate();
        requestLayout();
        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    @Override
    public void onDrawerClosed() {
        invalidate();
        requestLayout();
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    /**
     * Sets the event listener for drawer-closed event.
     * 
     * @param onDrawerCloseListener close event listener
     */
    public void setOnDrawerCloseListener(final OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * Sets the event listener for drawer-opened event.
     * 
     * @param onDrawerOpenListener open event listener
     */
    public void setOnDrawerOpenListener(final OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }
}
