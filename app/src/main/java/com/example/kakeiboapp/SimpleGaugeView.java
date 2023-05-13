package com.example.kakeiboapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class SimpleGaugeView extends View {

    private int mBarColor;
    private int mFillColor;
    private int mFillColorStart;
    private int mFillColorEnd;
    private int mStrokeCap;
    private float mStartAngle;
    private float mSweepAngle;
    private int mValue;
    private int mMinValue;
    private int mMaxValue;
    private boolean mShowValue;
    private float mTextSize;
    private int mTextColor;
    private float mTextOffset;

    private float mLabelSize;
    private int mLabelColor;
    private String mLabelText;

    private Typeface typeface;

    public SimpleGaugeView(Context context) {
        super(context);
        init(null, 0);
    }

    public SimpleGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SimpleGaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleGaugeView, defStyle, 0);

        mBarColor = a.getColor(R.styleable.SimpleGaugeView_gaugeView_barColor, Color.parseColor("#8F8C8C"));
        mFillColor = a.getColor(R.styleable.SimpleGaugeView_gaugeView_fillColor, 0);
        mFillColorStart = a.getColor(R.styleable.SimpleGaugeView_gaugeView_fillColorStart, Color.WHITE);
        mFillColorEnd = a.getColor(R.styleable.SimpleGaugeView_gaugeView_fillColorEnd, Color.BLACK);
        mStrokeCap = a.getInt(R.styleable.SimpleGaugeView_gaugeView_strokeCap, Paint.Cap.ROUND.ordinal());
        mStartAngle = a.getFloat(R.styleable.SimpleGaugeView_gaugeView_startAngle, 135.0f);
        mSweepAngle = a.getFloat(R.styleable.SimpleGaugeView_gaugeView_sweepAngle, 270.0f);
        mValue = a.getInt(R.styleable.SimpleGaugeView_gaugeView_value, 90);
        mMinValue = a.getInt(R.styleable.SimpleGaugeView_gaugeView_minValue, 0);
        mMaxValue = a.getInt(R.styleable.SimpleGaugeView_gaugeView_maxValue, 100);
        mShowValue = a.getBoolean(R.styleable.SimpleGaugeView_gaugeView_showValue, true);
        mTextSize = a.getDimension(R.styleable.SimpleGaugeView_gaugeView_textSize, 30.0f);
        mTextColor = a.getColor(R.styleable.SimpleGaugeView_gaugeView_textColor, Color.BLACK);
        mTextOffset = a.getDimension(R.styleable.SimpleGaugeView_gaugeView_textOffset, 0.0f);
        mLabelSize = a.getDimension(R.styleable.SimpleGaugeView_gaugeView_labelSize, 200.0f);
        mLabelColor = a.getColor(R.styleable.SimpleGaugeView_gaugeView_labelColor, Color.parseColor("#FF018786"));
        mLabelText = a.getString(R.styleable.SimpleGaugeView_gaugeView_labelText);
        typeface = a.getFont(R.styleable.SimpleGaugeView_gaugeView_FontFamily);

            typeface = Typeface.create("monospace", Typeface.BOLD_ITALIC);



        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Calculate the dimensions and positions of the gauge components
        int width = getWidth() ;
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY) - 20;
        float strokeWidth = radius / 6.0f;
        float valueFraction = (float) (mValue - mMinValue) / (float) (mMaxValue - mMinValue);
        float startAngle = mStartAngle;
        float sweepAngle = mSweepAngle * valueFraction;

        // Draw the background arc
        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeCap(Paint.Cap.values()[mStrokeCap]);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(mBarColor);
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, startAngle, mSweepAngle , false, backgroundPaint);

        // Draw the filled arc
        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStrokeCap(Paint.Cap.values()[mStrokeCap]);
        fillPaint.setStrokeWidth(strokeWidth + strokeWidth/2); // Set a thicker stroke width for the filled arc
        if (mFillColorStart != 0 && mFillColorEnd != 0) {
            fillPaint.setShader(new SweepGradient(centerX, centerY, mFillColorStart, mFillColorEnd));
            Matrix matrix = new Matrix();
            matrix.postRotate(60, centerX, centerY);
            fillPaint.getShader().setLocalMatrix(matrix);
        } else {
            fillPaint.setColor(mFillColor);
        }
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, startAngle, sweepAngle, false, fillPaint);

        // Draw the value text
        if (mShowValue) {
            Paint textPaint = new Paint();
            textPaint.setTypeface(typeface);
            textPaint.setTextSize(mTextSize);
            textPaint.setColor(mTextColor);
            textPaint.setTextAlign(Paint.Align.CENTER);
            float textHeight = textPaint.descent() - textPaint.ascent();
            float textOffset = mTextOffset + textHeight / 3.0f;
            canvas.drawText((mValue)+"%", centerX -10, centerY + textOffset, textPaint);
        }

        // Draw the label text
        if (mLabelText != null) {
            Paint labelPaint = new Paint();
            labelPaint.setTypeface(typeface);
            labelPaint.setTextSize(mLabelSize);
            labelPaint.setColor(mLabelColor);
            labelPaint.setTextAlign(Paint.Align.CENTER);
            float labelHeight = labelPaint.descent() - labelPaint.ascent() ;
            canvas.drawText(mLabelText, centerX, centerY + 150 - radius + labelHeight + strokeWidth, labelPaint);
        }
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
        invalidate();
    }


    public void setmValue(int mValue) {
        this.mValue = mValue;
        invalidate();
    }

    public void setmFillColorEnd(int mFillColorEnd) {
        this.mFillColorEnd = mFillColorEnd;
        invalidate();
    }
}