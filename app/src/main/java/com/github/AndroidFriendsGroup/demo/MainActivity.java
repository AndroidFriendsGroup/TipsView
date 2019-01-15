package com.github.AndroidFriendsGroup.demo;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.AndroidFriendsGroup.R;
import com.github.AndroidFriendsGroup.demo.utils.UIHelper;
import com.github.AndroidFriendsGroup.widget.TipsViewBuilder;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvTest;
    private Button mBtnRandomLayout;
    Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        tvTest = (TextView) findViewById(R.id.tv_test);
        mBtnRandomLayout = (Button) findViewById(R.id.btn_random_layout);
        tvTest.post(new Runnable() {
            @Override
            public void run() {
                randomLayout();
            }
        });

        tvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTipsWithLayout(v);
            }
        });

        mBtnRandomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomLayout();
            }
        });

    }

    private void randomLayout() {
        float x = mRandom.nextFloat() * (UIHelper.getScreenWidth() - tvTest.getWidth());
        float y = mRandom.nextFloat() * (UIHelper.getScreenHeight() - tvTest.getHeight());

        tvTest.animate()
                .translationX(x)
                .translationY(y)
                .start();
    }

    private void showTipsWithLayout(View v) {
        View tipsView = LayoutInflater.from(this).inflate(R.layout.layout_tips, null);
        TipsViewBuilder
                .with(this)
                .append(BitmapFactory.decodeResource(getResources(),R.drawable.ic_test_share)).target(v).gravity(Gravity.LEFT)
                .show(this);
    }

}
