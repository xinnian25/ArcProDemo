package com.gl.arcprodemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.math.BigDecimal;

public class CircleView extends View {

    private int viewWidth;
    private int viewHeight;

    private float startAngle = 135;
    private float sweepAngle = 270;
    private float currentAngle = 0;
    private float maxValues = 100;

    private RectF bgRect;
    private Paint mOutPaint;
    private int mOutPaintWidth = dipToPx(2);
    private String outArcColor = "#111111";
    private String outLineColor = "#FF3333";

    private Paint progressPaint;
    private int mProgressWidth = dipToPx(6);
    private float per = 0;
    private float curValues = 0;

    private Paint textValuePaint;
    private Paint textUnitPaint;
    private int textSizeValue = dipToPx(30);
    private int textSizeUnit = dipToPx(15);
    private int textSizePadding = dipToPx(5);
    private int[] colors = new int[]{Color.GREEN, Color.YELLOW, Color.RED, Color.RED};

    private Paint linePaint;
    private int mLineWidth = dipToPx(2);
    private int lineWidth = dipToPx(8);
    private int linePadding = dipToPx(2);

    private SweepGradient sweepGradient;
    private Matrix rotateMatrix;
    private ValueAnimator progressAnimator;
    private ValueAnimator mDarkWaveAnimator;
    private ValueAnimator mLightWaveAnimator;


    //水波路径
    private Path mWaveLimitPath;
    private Path mWavePath;
    //水波高度
    private float mWaveHeight = 40;
    //水波数量
    private int mWaveNum = 1;
    //深色水波
    private Paint mWavePaint;
    //深色水波颜色
    private int mDarkWaveColor = Color.parseColor("#803cbcb7");;
    //浅色水波颜色
    private int mLightWaveColor = Color.parseColor("#800de6e8");
    //深色水波贝塞尔曲线上的起始点、控制点
    private Point[] mDarkPoints;
    //浅色水波贝塞尔曲线上的起始点、控制点
    private Point[] mLightPoints;
    //贝塞尔曲线点的总个数
    private int mAllPointCount;
    private int mHalfPointCount;
    //半径
    private float mRadius;
    //圆心
    private Point mCenterPoint;
    //深色波浪移动距离
    private float mDarkWaveOffset;
    //浅色波浪移动距离
    private float mLightWaveOffset;
    //浅色波浪方向
    private boolean isR2L = true;
    //是否锁定波浪不随进度移动
    private boolean lockWave;




    public CircleView(Context context) {
        this(context,null);
    }

    public CircleView(Context context,@Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleView(Context context,@Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        switch (widthMode){
            case MeasureSpec.EXACTLY:
                viewWidth = viewHeight = Math.min(viewHeight,viewWidth);
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
        mRadius = w / 2 - mProgressWidth * 3 / 2  - lineWidth - linePadding;
        mCenterPoint = new Point();
        mCenterPoint.x = w/2 ;
        mCenterPoint.y = w/2 ;
        init();
        initWavePoints();
    }

    private void init() {

        //外环
        bgRect = new RectF();
        bgRect.top = mProgressWidth + lineWidth + linePadding;
        bgRect.left = mProgressWidth  + lineWidth + linePadding;
        bgRect.right = viewWidth - mProgressWidth  - lineWidth - linePadding;
        bgRect.bottom = viewWidth - mProgressWidth  - lineWidth - linePadding;

        //外环
        mOutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutPaint.setAntiAlias(true);
        mOutPaint.setStyle(Paint.Style.STROKE);
        mOutPaint.setStrokeWidth(mOutPaintWidth);
        mOutPaint.setColor(Color.parseColor(outArcColor));
        mOutPaint.setStrokeCap(Paint.Cap.ROUND);

        //当前进度的弧形
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(mProgressWidth);
        progressPaint.setColor(Color.GREEN);

        //文本
        textValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textValuePaint.setTextSize(textSizeValue);
        textValuePaint.setColor(Color.BLACK);
        textValuePaint.setTextAlign(Paint.Align.CENTER);

        //文本单位
        textUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textUnitPaint.setTextSize(textSizeUnit);
        textUnitPaint.setColor(Color.BLACK);
        textUnitPaint.setTextAlign(Paint.Align.CENTER);

        //最外环刻度
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(mLineWidth);
        linePaint.setColor(Color.BLACK);

        sweepGradient = new SweepGradient(viewWidth / 2, viewHeight / 2, colors, null);
        rotateMatrix = new Matrix();

    }

    //波浪
    private void initWavePoints() {

        mWaveLimitPath = new Path();
        mWavePath = new Path();

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mLightWaveColor);

        float waveWidth = (mRadius * 2) / mWaveNum;
        mAllPointCount = 8 * mWaveNum + 1;
        mHalfPointCount = mAllPointCount / 2;
        mDarkPoints = getPoint(false, waveWidth);
        mLightPoints = getPoint(isR2L, waveWidth);
    }

    //从左往右或者从右往左获取贝塞尔点
    private Point[] getPoint(boolean isR2L, float waveWidth) {
        Point[] points = new Point[mAllPointCount];
        //第1个点特殊处理，即数组的中点
        points[mHalfPointCount] = new Point((int) (mCenterPoint.x + (isR2L ? mRadius : -mRadius)), mCenterPoint.y);
        //屏幕内的贝塞尔曲线点
        for (int i = mHalfPointCount + 1; i < mAllPointCount; i += 4) {
            float width = points[mHalfPointCount].x + waveWidth * (i / 4 - mWaveNum);
            points[i] = new Point((int) (waveWidth / 4 + width), (int) (mCenterPoint.y - mWaveHeight));
            points[i + 1] = new Point((int) (waveWidth / 2 + width), mCenterPoint.y);
            points[i + 2] = new Point((int) (waveWidth * 3 / 4 + width), (int) (mCenterPoint.y + mWaveHeight));
            points[i + 3] = new Point((int) (waveWidth + width), mCenterPoint.y);
        }
        //屏幕外的贝塞尔曲线点
        for (int i = 0; i < mHalfPointCount; i++) {
            int reverse = mAllPointCount - i - 1;
            points[i] = new Point((isR2L ? 2 : 1) * points[mHalfPointCount].x - points[reverse].x,
                    points[mHalfPointCount].y * 2 - points[reverse].y);
        }
        //对从右向左的贝塞尔点数组反序，方便后续处理
        return isR2L ? reverse(points) : points;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void drawWave(Canvas canvas, Paint paint, Point[] points, float waveOffset) {

        mWaveLimitPath.reset();
        mWavePath.reset();
        float height = lockWave ? 0 : mRadius - 2 * mRadius * (curValues/100);

        //moveTo和lineTo绘制出水波区域矩形
        mWavePath.moveTo(points[0].x + waveOffset, points[0].y + height);

        for (int i = 1; i < mAllPointCount; i += 2) {
            mWavePath.quadTo(points[i].x + waveOffset, points[i].y + height,
                    points[i + 1].x + waveOffset, points[i + 1].y + height);
        }
        //mWavePath.lineTo(points[mAllPointCount - 1].x, points[mAllPointCount - 1].y + height);
        //不管如何移动，波浪与圆路径的交集底部永远固定，否则会造成上移的时候底部为空的情况
        mWavePath.lineTo(points[mAllPointCount - 1].x, mCenterPoint.y + mRadius);
        mWavePath.lineTo(points[0].x, mCenterPoint.y + mRadius);
        mWavePath.close();
        mWaveLimitPath.addCircle(mCenterPoint.x, mCenterPoint.y, mRadius, Path.Direction.CW);
        //取该圆与波浪路径的交集，形成波浪在圆内的效果
        mWaveLimitPath.op(mWavePath, Path.Op.INTERSECT);
        canvas.drawPath(mWaveLimitPath, paint);
    }

    //绘制深色波浪(贝塞尔曲线)
    private void drawDarkWave(Canvas canvas) {
        mWavePaint.setColor(mDarkWaveColor);
        drawWave(canvas, mWavePaint, mDarkPoints, mDarkWaveOffset);
    }

    //绘制浅色波浪(贝塞尔曲线)
    private void drawLightWave(Canvas canvas) {
        mWavePaint.setColor(mLightWaveColor);
        //从右向左的水波位移应该被减去
        drawWave(canvas, mWavePaint, mLightPoints, isR2L ? -mLightWaveOffset : mLightWaveOffset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = viewWidth / 2;
        int y = viewHeight / 2;

        //背景
        canvas.drawArc(bgRect, startAngle, sweepAngle, false, mOutPaint);

        //设置渐变色
        rotateMatrix.setRotate(130, x, y);
        sweepGradient.setLocalMatrix(rotateMatrix);
        progressPaint.setShader(sweepGradient);

        //当前
        canvas.drawArc(bgRect, startAngle, currentAngle, false, progressPaint);

        double num = divide( currentAngle,5 );

        //刻度当前
        for (int i = 0; i < num + 1 ; i++) {
            canvas.save();
            canvas.rotate( - startAngle + i * 5 , x , y);
            linePaint.setColor(Color.parseColor(outLineColor));
            canvas.drawLine( x , (float) (lineWidth * 0.6) - linePadding, x, (float) (lineWidth * 0.6) + lineWidth - linePadding, linePaint);
            canvas.restore();
        }

        //刻度背景
        for (int i = 0; i < 72 - num; i++) {
            canvas.save();
            canvas.rotate( - startAngle + (i + (int)(num + 1)) * 5 , x , y);
            linePaint.setColor(Color.parseColor(outArcColor));
            canvas.drawLine( x , (float) (lineWidth * 0.6) - linePadding, x, (float) (lineWidth * 0.6) + lineWidth - linePadding, linePaint);
            canvas.restore();
        }

        drawLightWave(canvas);
        drawDarkWave(canvas);

        //文字
        canvas.drawText(String.format("%.0f", curValues), x, y - textSizeValue / 5, textValuePaint);
        canvas.drawText("百分比", x, y - textSizeValue / 5 + textSizeUnit + textSizePadding, textUnitPaint);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
            progressAnimator.removeAllUpdateListeners();
            progressAnimator = null;
        }
        stopWaveAnimator();
    }

    public static double divide(double num1, double num2) {
        BigDecimal big1 = getBigDecimal(num1);
        BigDecimal big2 = getBigDecimal(num2);
        return big1.divide(big2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static BigDecimal getBigDecimal(double num) {
        return new BigDecimal(Double.toString(num));
    }

    public void setCurrentValues(float currentValues) {
        if (currentValues > maxValues) {
            currentValues = maxValues;
        }
        if (currentValues < 0) {
            currentValues = 0;
        }
        this.curValues = currentValues;
        per = sweepAngle / maxValues;
        setAnimation(currentAngle, currentValues * per);
        startWaveAnimator();
    }

    private void setAnimation(float last, final float current) {
        progressAnimator = ValueAnimator.ofFloat(last,current);
        progressAnimator.setDuration(1000);
        progressAnimator.setTarget(currentAngle);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAngle = (float) animation.getAnimatedValue();
                curValues = currentAngle / per;
                invalidate();
            }
        });
        progressAnimator.start();
    }

    private void startWaveAnimator() {
        startLightWaveAnimator();
        startDarkWaveAnimator();
    }

    private void stopWaveAnimator() {
        if (mDarkWaveAnimator != null && mDarkWaveAnimator.isRunning()) {
            mDarkWaveAnimator.cancel();
            mDarkWaveAnimator.removeAllUpdateListeners();
            mDarkWaveAnimator = null;
        }
        if (mLightWaveAnimator != null && mLightWaveAnimator.isRunning()) {
            mLightWaveAnimator.cancel();
            mLightWaveAnimator.removeAllUpdateListeners();
            mLightWaveAnimator = null;
        }
    }

    private void startLightWaveAnimator() {
        if (mLightWaveAnimator != null && mLightWaveAnimator.isRunning()) {
            return;
        }
        mLightWaveAnimator = ValueAnimator.ofFloat(0, 2 * mRadius);
        mLightWaveAnimator.setDuration(1000);
        mLightWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mLightWaveAnimator.setInterpolator(new LinearInterpolator());
        mLightWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLightWaveOffset = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mLightWaveAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLightWaveOffset = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mLightWaveAnimator.start();
    }

    private void startDarkWaveAnimator() {
        if (mDarkWaveAnimator != null && mDarkWaveAnimator.isRunning()) {
            return;
        }
        mDarkWaveAnimator = ValueAnimator.ofFloat(0, 2 * mRadius);
        mDarkWaveAnimator.setDuration(1000);
        mDarkWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mDarkWaveAnimator.setInterpolator(new LinearInterpolator());
        mDarkWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDarkWaveOffset = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mDarkWaveAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDarkWaveOffset = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mDarkWaveAnimator.start();
    }

    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int)(dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 反转数组
     */
    public static <T> T[] reverse(T[] arrays) {
        if (arrays == null) {
            return null;
        }
        int length = arrays.length;
        for (int i = 0; i < length / 2; i++) {
            T t = arrays[i];
            arrays[i] = arrays[length - i - 1];
            arrays[length - i - 1] = t;
        }
        return arrays;
    }
}
