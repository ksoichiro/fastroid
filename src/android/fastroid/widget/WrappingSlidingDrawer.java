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
import android.util.Log;
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

    /** 表示方向が縦向きであるかどうかを示します。 */
    private boolean mVertical;

    /** 上部からのオフセット値です。 */
    private int mTopOffset;

    /** ドロワーが開いたときの通知を受け取るリスナーです。 */
    private OnDrawerOpenListener mOnDrawerOpenListener;

    /** ドロワーが閉じたときの通知を受け取るリスナーです。 */
    private OnDrawerCloseListener mOnDrawerCloseListener;

    /** ドロワーが開いたときの通知を受け取るリスナーです。 */
    public interface OnDrawerOpenListener {
        /** ドロワーが開いたときに呼び出されます。 */
        void onDrawerOpened();
    }

    /** ドロワーが閉じたときの通知を受け取るリスナーです。 */
    public interface OnDrawerCloseListener {
        /** ドロワーが閉じたときに呼び出されます。 */
        void onDrawerClosed();
    }

    /**
     * コンストラクタです。
     * 
     * @param context コンテキスト
     * @param attrs 設定属性
     * @param defStyle スタイル
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
     * コンストラクタです。
     * 
     * @param context コンテキスト
     * @param attrs 設定属性
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
            int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
            content.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, heightSpecMode));
            heightSpecSize = handle.getMeasuredHeight() + mTopOffset + content.getMeasuredHeight();
            widthSpecSize = content.getMeasuredWidth();
            if (handle.getMeasuredWidth() > widthSpecSize) {
                widthSpecSize = handle.getMeasuredWidth();
            }
        } else {
            int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
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
        Log.v("tag", "requested layout at onDrawerOpened");
        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    @Override
    public void onDrawerClosed() {
        invalidate();
        requestLayout();
        Log.v("tag", "requested layout at onDrawerClosed");
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    /**
     * ドロワーが閉じたときのリスナーを設定します。
     * 
     * @param onDrawerCloseListener リスナー
     */
    public void setOnDrawerCloseListener(final OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * ドロワーが開いたときのリスナーを設定します。
     * 
     * @param onDrawerOpenListener リスナー
     */
    public void setOnDrawerOpenListener(final OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }
}
