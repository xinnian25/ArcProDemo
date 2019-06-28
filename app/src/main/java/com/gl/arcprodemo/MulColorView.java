package com.gl.arcprodemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class MulColorView extends View {

    private int viewWidth;
    private int viewHeight;

    private boolean mHasData = false;
    private ArrayList<Integer> mAngles;
    private ArrayList<Integer> mLevelStartAngles;//每段圆弧的起始角度值
    private RectF mRingRect;
    private RectF mInnerRect;
    private Paint mNoAssetsPaint;
    private Paint mInnerCirclePaint;
    private ArrayList<Paint> mPaints;

    private int mRingStrokeWidth = 20;//圆环的宽度

    private int mMoveAngle;//圆弧移动的角度
    private static final int CIRCLE_ANGLE = 360;//圆环的角度
    private int mRingStartAngle = -90;//圆环的起始角度

    private ValueAnimator mMoveAnimator;

    private int mNoDataPaintColor = Color.parseColor("#cccccc");//没有数据的paint的颜色
    private int mInnerCirclePaintColor = Color.parseColor("#ffffff");//内圆的paint的颜色

    public MulColorView(Context context) {
        this(context,null);
    }

    public MulColorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MulColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaints = new ArrayList<Paint>();
        mAngles = new ArrayList<Integer>();
        mLevelStartAngles = new ArrayList<Integer>();

        mNoAssetsPaint = new Paint();
        mNoAssetsPaint.setAntiAlias(true);
        mNoAssetsPaint.setStyle(Paint.Style.FILL);
        mNoAssetsPaint.setColor(mNoDataPaintColor);

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setAntiAlias(true);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setColor(mInnerCirclePaintColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode){
            case MeasureSpec.EXACTLY:
                viewWidth = viewHeight = Math.min(viewWidth,viewHeight);
                break;
            case MeasureSpec.AT_MOST:
                float desity = getResources().getDisplayMetrics().density;
                viewWidth = viewHeight = (int) (100 * desity);
                break;
        }
        setMeasuredDimension(viewWidth,viewHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRingRect = new RectF(0, 0, w, h);
        float desity = getResources().getDisplayMetrics().density;
        mRingStrokeWidth = (int) (mRingStrokeWidth * desity);
        mInnerRect = new RectF(mRingStrokeWidth, mRingStrokeWidth, w - mRingStrokeWidth, h - mRingStrokeWidth);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mHasData) {//没有数据
            mMoveAngle = CIRCLE_ANGLE;
            drawRingView(canvas, mRingStartAngle, mMoveAngle, mNoAssetsPaint);
        }else {
            int level = 0;//圆弧的段数
            for (int i = 0; i < mAngles.size(); i++) {//计算需要画几段圆弧
                if (mMoveAngle < mLevelStartAngles.get(1)) {
                    level = 1;
                } else if (mMoveAngle > mLevelStartAngles.get(i) && mMoveAngle <= mLevelStartAngles.get(i + 1)) {
                    level = i + 1;
                }
            }
            drawRing(level, canvas);
        }

        canvas.drawArc(mInnerRect, mRingStartAngle, CIRCLE_ANGLE, true, mInnerCirclePaint);//画内部的圆

    }

    private void drawRing(int level, Canvas canvas) {
        if (level <= 0) {
            drawRingView(canvas, mRingStartAngle, CIRCLE_ANGLE, mNoAssetsPaint);
            return;
        }
        if (mAngles.size() > mPaints.size()) {
            int temp = mAngles.size() - mPaints.size();
            for (int i = 0; i < temp; i++) {
                mPaints.add(mNoAssetsPaint);
            }
        }
        for (int i = 0; i < level; i++) {
            if (i == level - 1) {
                drawRingView(canvas, mRingStartAngle + mLevelStartAngles.get(i),
                        mMoveAngle - mLevelStartAngles.get(i), mPaints.get(i));
            } else {
                drawRingView(canvas, mRingStartAngle + mLevelStartAngles.get(i), mAngles.get(i), mPaints.get(i));
            }
        }
    }

    private void drawRingView(Canvas canvas, int startAngle, int sweepAngle, Paint paint) {
        if (sweepAngle != 0) {
            canvas.drawArc(mRingRect, startAngle, sweepAngle, true, paint);
        }
    }


    public void setData(String... data){

        int size = data.length;
        //String转big
        BigDecimal[] bigDecimals = new BigDecimal[size];
        for (int i = 0; i < size; i++) {
            bigDecimals[i] = new BigDecimal(TextUtils.isEmpty(data[i]) ? "0" : data[i]);
        }

        //格式化获取总和
        BigDecimal total = new BigDecimal("0.00");
        for (int i = 0; i < size ; i++) {
            total = total.add(bigDecimals[i]);
        }

        if (total.compareTo(BigDecimal.valueOf(0)) == 0){
            mHasData = false;
            return;
        }

        BigDecimal[] bigDecimalsLast = new BigDecimal[size];
        for (int i = 0; i < size; i++) {
            bigDecimalsLast[i] = bigDecimals[i].divide(total,10,ROUND_HALF_UP).multiply(BigDecimal.valueOf(360));
        }

        //数值小于1且大于0的，就直接定1，否则转int类型，确保小数据也能出现在圆环上
        int[] intData = new int[size];
        for (int i = 0; i < size; i++) {
            intData[i] = bigDecimalsLast[i].compareTo(BigDecimal.valueOf(1.0)) < 0 && bigDecimalsLast[i].compareTo(BigDecimal.valueOf(0)) > 0
                    ? 1 : bigDecimalsLast[i].intValue();
        }

        //所有数据加起来可能会不满360也可能会超出360，由于精度的问题
        //处理方案是把缺少的度数（有正也有负）加在最大的值上，这样图形出现的误差会不明显
        int remind = 360;//剩余的角度
        int maxPosition = -1;
        int max = intData[0];
        for (int i = 0; i < intData.length; i++) {
            remind = remind - intData[i];
            if (max <= intData[i]) {
                maxPosition = i;
            }
        }
        intData[maxPosition] += remind;//将缺少的度数加载最大值上

        mAngles.clear();
        for (int i = 0; i < intData.length; i++) {
            mAngles.add(intData[i]);
        }

        mLevelStartAngles.clear();
        mLevelStartAngles.add(0);
        int _angle = 0;
        for (int _i = 0; _i < mAngles.size(); _i++) {
            _angle += mAngles.get(_i);
            mLevelStartAngles.add(_angle);
            if (mAngles.get(_i) > 0) {
                mHasData = true;
            }
        }

    }

    public void setMColor(String... colors) {

        ArrayList<Integer> colorsTemp = new ArrayList<Integer>();
        for (int i = 0; i < colors.length; i++) {
            colorsTemp.add(Color.parseColor(colors[i]));
        }

        mPaints.clear();
        for (int i = 0; i < colorsTemp.size(); i++) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colorsTemp.get(i));
            mPaints.add(paint);
        }
    }

    public void startAnim(){
        mMoveAnimator = ValueAnimator.ofFloat(0,1);
        mMoveAnimator.setDuration(1000);
        mMoveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                mMoveAngle = (int)( v  * CIRCLE_ANGLE) ;
                invalidate();
            }
        });
        mMoveAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mMoveAnimator!=null){
            mMoveAnimator.cancel();
            mMoveAnimator.removeAllUpdateListeners();
            mMoveAnimator = null;
        }
        super.onDetachedFromWindow();
    }
}
