package com.gl.arcprodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArcProActivity extends AppCompatActivity {

    @BindView(R.id.clock)
    ClockView clock;
    @BindView(R.id.cv)
    CircleView cv;

    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.mul)
    MulColorView mul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_demo);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tv)
    public void onViewClicked() {
        clock.start();
        cv.setCurrentValues(50);
        mul.setData("92.2", "230", "399.01", "108", "111", "200");
        mul.setMColor("#123456", "#fea123", "#DC143C", "#78da10", "#1121de", "#aacc18");
        mul.startAnim();
    }
}
