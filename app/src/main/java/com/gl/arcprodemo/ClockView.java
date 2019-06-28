package com.gl.arcprodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;


//钟表
public class ClockView extends View {

    private Paint panPaint;
    private TextPaint textPaint;
    private Rect rect;

    private int viewWidth;
    private int viewHeight;

    private int textsize = 0;
    private double textSizeInnerPer = 0.8;  //内测比例
    private double textSizeWidthPer = 1.4;  //外侧宽度比例
    private float textTopSizePer = 3;  //上面文字比例
    private int textTopYPer = 11;//上面文字Y比例
    private int textBotH = 6; //下面文字比例
    private double textTop2SizePer = 1.1;

    private int hour = 9;
    private int minute = 30;
    private int second = 55;
    private int data1;

    private String topText = "MAKE";
    private String bot1Text = "I from where";
    private String bot2Text = "china";

    private float textwidth;
    private int textheight;
    private int bot1Width;
    private int bot2Width;

    private int panInnColor = Color.parseColor("#000000");
    private int panOutColor = Color.parseColor("#666666");

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

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
        textsize = viewWidth / 32;
        initPaint();
    }

    public void initPaint(){
        panPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textsize);
        rect = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width =  getWidth() / 2 ;
        int height =  getHeight() / 2;

        //表盘内测
        panPaint.setColor(panInnColor);
        canvas.drawCircle(width , height , (float) (width - textsize * textSizeInnerPer), panPaint);

        //表盘外侧
        panPaint.setColor(panOutColor);
        panPaint.setStrokeWidth((float) (textsize * textSizeWidthPer));
        panPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width, height, (float) (width - textsize * textSizeInnerPer), panPaint);

        //上面文字
        textPaint.setTextSize(textsize * textTopSizePer);
        textPaint.getTextBounds(topText, 0, topText.length(), rect);
        textwidth = rect.width();
        textheight = rect.height();
        canvas.drawText(topText, viewWidth / 2 - textwidth / 2, (float) (textsize * textTopYPer), textPaint);

        //下面文字
        textPaint.setTextSize((float) (textsize * textTop2SizePer));
        textPaint.getTextBounds(bot1Text, 0, bot1Text.length(), rect);
        bot1Width = rect.width();
        textPaint.getTextBounds(bot2Text, 0, bot2Text.length(), rect);
        bot2Width = rect.width();
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC);
        textPaint.setTypeface(font);
        canvas.drawText(bot1Text, viewWidth / 2 - bot1Width / 2, viewHeight / 2 + textsize * textBotH, textPaint);
        canvas.drawText(bot2Text, viewWidth / 2 - bot2Width / 2, viewHeight / 2 + textsize * (textBotH +1) + rect.height()/2, textPaint);

        //日期
        rect.set(viewWidth - textsize * 8, viewWidth / 2 - textsize, viewWidth - textsize * 5, viewWidth / 2 + textsize);
        panPaint.setColor(Color.parseColor("#666666"));
        panPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, panPaint);
        String s = String.valueOf(data1);
        textPaint.getTextBounds(s, 0, s.length(), rect);
        canvas.drawText(s, (float) (viewWidth - textsize * 6.5 - (rect.width() / 1.9)), viewWidth / 2 + rect.height() / 2, textPaint);

        //刻度
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(textsize);
        panPaint.setColor(Color.WHITE);
        panPaint.setStrokeWidth(textsize / 8);
        panPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 60; i++) {
            canvas.save();
            canvas.rotate(i * 6, width, height);
            if (i % 5 != 0) {
                panPaint.setStrokeWidth(textsize / 8);
                canvas.drawLine((float) (textsize * 0.25), height, (float) (textsize * 1.2), height, panPaint);
            } else {
                int j = 0;
                j = i == 0 ? 60 : i ;
                String value = j == 5 ? "0" + j : "" + j ;
                canvas.drawText(value , (float) (width - textsize * 0.5), (float) (textsize * 1.1), textPaint);
                panPaint.setStrokeWidth((float) (textsize * 0.5));
                canvas.drawLine((float) (textsize * 0.8) * 2, height, (float) (textsize * 0.8 * 2 + (float) (textsize * 2)), height, panPaint);
            }
            canvas.restore();
        }

        //时针
        panPaint.setStrokeWidth((float) (textsize * 0.3));
        canvas.save();
        canvas.rotate(360 / 12 * hour + 90 + minute * 0.5f, viewWidth / 2, viewWidth / 2);
        canvas.drawLine(viewHeight / 2 - viewWidth / 5, viewWidth / 2,viewWidth / 2, viewWidth / 2, panPaint);
        canvas.restore();

        //分针
        panPaint.setStrokeWidth((float) (textsize*0.2));
        canvas.save();
        canvas.rotate(360 / 60 * minute + 90, viewWidth / 2, viewWidth / 2);
        canvas.drawLine(viewHeight / 2 - viewWidth / 4, viewWidth / 2,viewWidth / 2, viewWidth / 2, panPaint);
        canvas.restore();

        //秒针
        panPaint.setStrokeWidth((float) (textsize*0.1));
        canvas.save();
        canvas.rotate(360 / 60 * second + 90, viewWidth / 2, viewWidth / 2);
        canvas.drawLine(viewHeight / 2 - viewWidth / 3, viewWidth / 2,viewWidth / 2, viewWidth / 2, panPaint);
        canvas.restore();

        //中心圆点
        panPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (textsize * 0.3), panPaint);

    }

    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR);
            minute = calendar.get(Calendar.MINUTE);
            second = calendar.get(Calendar.SECOND);
            data1 = calendar.get(Calendar.DAY_OF_MONTH);
            invalidate();
            mHandler.postDelayed(runnable,1000);
        }
    };

    public void start(){
        mHandler.removeCallbacks(runnable);
        mHandler.post(runnable);
    }

    public void stop(){
        mHandler.removeCallbacks(runnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
}
