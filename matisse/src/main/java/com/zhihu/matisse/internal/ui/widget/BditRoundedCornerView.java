package com.zhihu.matisse.internal.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.zhihu.matisse.R;

public class BditRoundedCornerView extends FrameLayout {

    public BditRoundedCornerView(Context context) {
        super(context);
    }

    public BditRoundedCornerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BditRoundedCornerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final float cornerRadius = getContext().getResources().getDisplayMetrics().density * 16;
    private final Path path1 = new Path();

    public void dispatchDraw(Canvas canvas) {
        int count = canvas.save();
        drawBackground(canvas);
        canvas.clipPath(this.path1);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }

    public void drawBackground(Canvas canvas) {
        Paint paintBackground = new Paint();
        paintBackground.setColor(ContextCompat.getColor(getContext(), R.color.black));
        canvas.drawPath(this.path1, paintBackground);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.path1.reset();
        float padding = 0f;
        this.path1.addRoundRect(new RectF(padding, padding, (float) w - padding, (float) h - padding), this.cornerRadius, this.cornerRadius, Path.Direction.CW);
        this.path1.close();
    }

}
