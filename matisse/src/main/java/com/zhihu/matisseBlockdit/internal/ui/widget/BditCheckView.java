/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisseBlockdit.internal.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.zhihu.matisse.R;

public class BditCheckView extends View {

    public static final int UNCHECKED = Integer.MIN_VALUE;
    private static final float STROKE_WIDTH = 1.0f; // dp
    private static final float SHADOW_WIDTH = 6.0f; // dp
    private static final int SIZE = 16; // dp
    private static final float STROKE_RADIUS = 7.5f; // dp
    private static final float BG_RADIUS = 8f; // dp
    private static final int CONTENT_SIZE = 16; // dp
    private boolean mCountable;
    private boolean mChecked;
    private int mCheckedNum;
    private Paint mStrokePaint;
    private Paint mCheckedBackgroundPaint;
    private Paint mUnCheckBackgroundPaint;
    private TextPaint mTextPaint;
    private Paint mShadowPaint;
    private Drawable mCheckDrawable;
    private float mDensity;
    private Rect mCheckRect;
    private boolean mEnabled = true;

    public BditCheckView(Context context) {
        super(context);
        init(context);
    }

    public BditCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BditCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // fixed size 48dp x 48dp
        int sizeSpec = MeasureSpec.makeMeasureSpec((int) (SIZE * mDensity), MeasureSpec.EXACTLY);
        super.onMeasure(sizeSpec, sizeSpec);
    }

    private void init(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;

        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        mStrokePaint.setStrokeWidth(STROKE_WIDTH * mDensity);
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.item_checkCircle_borderColor});
        int defaultColor = ResourcesCompat.getColor(
                getResources(), R.color.zhihu_item_checkCircle_borderColor,
                getContext().getTheme());
        int color = ta.getColor(0, defaultColor);
        ta.recycle();
        mStrokePaint.setColor(color);

        mCheckDrawable = ResourcesCompat.getDrawable(context.getResources(),
                R.drawable.ic_check_white_18dp, context.getTheme());
    }

    public void setChecked(boolean checked) {
        if (mCountable) {
            throw new IllegalStateException("CheckView is countable, call setCheckedNum() instead.");
        }
        mChecked = checked;
        invalidate();
    }

    public void setCountable(boolean countable) {
        mCountable = countable;
    }

    public void setCheckedNum(int checkedNum) {
        if (!mCountable) {
            throw new IllegalStateException("CheckView is not countable, call setChecked() instead.");
        }
        if (checkedNum != UNCHECKED && checkedNum <= 0) {
            throw new IllegalArgumentException("checked num can't be negative.");
        }
        mCheckedNum = checkedNum;
        invalidate();
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initBackgroundPaint();
        canvas.drawCircle((float) ((SIZE * mDensity) / 2), (float) ((SIZE * mDensity) / 2),
                BG_RADIUS * mDensity, mUnCheckBackgroundPaint);

        // draw content
        if (mCountable) {
            if (mCheckedNum != UNCHECKED) {
                initCheckedBackgroundPaint();

                canvas.drawCircle((float) ((SIZE * mDensity) / 2), (float) ((SIZE * mDensity) / 2),
                        BG_RADIUS * mDensity, mCheckedBackgroundPaint);

                canvas.drawCircle((float) ((SIZE * mDensity) / 2), (float) ((SIZE * mDensity) / 2),
                        STROKE_RADIUS * mDensity, mStrokePaint);

                initTextPaint();
                String text = String.valueOf(mCheckedNum);
                int baseX = (int) (canvas.getWidth() - mTextPaint.measureText(text)) / 2;
                int baseY = (int) (canvas.getHeight() - mTextPaint.descent() - mTextPaint.ascent()) / 2;
                canvas.drawText(text, baseX, baseY, mTextPaint);
            }
        } else {
            if (mChecked) {
                initCheckedBackgroundPaint();
                canvas.drawCircle((float) SIZE * mDensity / 2, (float) SIZE * mDensity / 2,
                        BG_RADIUS * mDensity, mCheckedBackgroundPaint);

                mCheckDrawable.setBounds(getCheckRect());
                mCheckDrawable.draw(canvas);
            }
        }

        // enable hint
        setAlpha(mEnabled ? 1.0f : 0.5f);
    }

    private void initShadowPaint() {
        if (mShadowPaint == null) {
            mShadowPaint = new Paint();
            mShadowPaint.setAntiAlias(true);
            // all in dp
            float outerRadius = STROKE_RADIUS + STROKE_WIDTH / 2;
            float innerRadius = outerRadius - STROKE_WIDTH;
            float gradientRadius = outerRadius + SHADOW_WIDTH;
            float stop0 = (innerRadius - SHADOW_WIDTH) / gradientRadius;
            float stop1 = innerRadius / gradientRadius;
            float stop2 = outerRadius / gradientRadius;
            float stop3 = 1.0f;
            mShadowPaint.setShader(
                    new RadialGradient((float) SIZE * mDensity / 2,
                            (float) SIZE * mDensity / 2,
                            gradientRadius * mDensity,
                            new int[]{Color.parseColor("#00000000"), Color.parseColor("#0D000000"),
                                    Color.parseColor("#0D000000"), Color.parseColor("#00000000")},
                            new float[]{stop0, stop1, stop2, stop3},
                            Shader.TileMode.CLAMP));
        }
    }

    private void initCheckedBackgroundPaint() {
        if (mCheckedBackgroundPaint == null) {
            mCheckedBackgroundPaint = new Paint();
            mCheckedBackgroundPaint.setAntiAlias(true);
            mCheckedBackgroundPaint.setStyle(Paint.Style.FILL);
            TypedArray ta = getContext().getTheme()
                    .obtainStyledAttributes(new int[]{R.attr.item_checkCircle_backgroundColor});
            int defaultColor = ResourcesCompat.getColor(
                    getResources(), R.color.zhihu_item_checkCircle_backgroundColor,
                    getContext().getTheme());
            int color = ta.getColor(0, defaultColor);
            ta.recycle();
            mCheckedBackgroundPaint.setColor(color);
        }
    }

    private void initBackgroundPaint() {
        if (mUnCheckBackgroundPaint == null) {
            mUnCheckBackgroundPaint = new Paint();
            mUnCheckBackgroundPaint.setAntiAlias(true);
            mUnCheckBackgroundPaint.setStyle(Paint.Style.FILL);
            TypedArray ta = getContext().getTheme()
                    .obtainStyledAttributes(new int[]{R.attr.item_uncheckCircle_backgroundColor});
            int defaultColor = ResourcesCompat.getColor(
                    getResources(), R.color.colorTransparent,
                    getContext().getTheme());
            int color = ta.getColor(0, defaultColor);
            ta.recycle();
            mUnCheckBackgroundPaint.setColor(color);
        }
    }

    private void initTextPaint() {
        if (mTextPaint == null) {
            mTextPaint = new TextPaint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            mTextPaint.setTextSize(12.0f * mDensity);
        }
    }

    // rect for drawing checked number or mark
    private Rect getCheckRect() {
        if (mCheckRect == null) {
            int rectPadding = (int) (SIZE * mDensity / 2 - CONTENT_SIZE * mDensity / 2);
            mCheckRect = new Rect(rectPadding, rectPadding,
                    (int) (SIZE * mDensity - rectPadding), (int) (SIZE * mDensity - rectPadding));
        }

        return mCheckRect;
    }
}