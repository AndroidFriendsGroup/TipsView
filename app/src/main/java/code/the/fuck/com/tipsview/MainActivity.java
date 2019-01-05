package code.the.fuck.com.tipsview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import code.the.fuck.com.tipsview.utils.UIHelper;
import code.the.fuck.com.tipsview.widget.TipsView;

public class MainActivity extends AppCompatActivity {

    private TextView tvTest;
    private TipsView mTipsView;
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
//                showTipsWithCustomizedView(v);
//                showTips(v);
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
        checkTipsView();
        //must remove first
        if (mTipsView.getParent() != null) {
            removeTips();
        }

        View tipsView = LayoutInflater.from(this).inflate(R.layout.layout_tips, null);

        mTipsView.show(v,tipsView);
        ((ViewGroup) getWindow().getDecorView()).addView(mTipsView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void showTipsWithCustomizedView(View v) {
        checkTipsView();
        //must remove first
        if (mTipsView.getParent() != null) {
            removeTips();
        }
        Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_test_share);
        Bitmap tipsBitmap = Bitmap.createScaledBitmap(srcBitmap, 150, 75, true);
        srcBitmap.recycle();
        mTipsView.show(v,tipsBitmap);
        ((ViewGroup) getWindow().getDecorView()).addView(mTipsView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void showTips(View v) {
        checkTipsView();
        //must remove first
        if (mTipsView.getParent() != null) {
            removeTips();
        }
        mTipsView.show(v);
        ((ViewGroup) getWindow().getDecorView()).addView(mTipsView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void checkTipsView() {
        if (mTipsView != null) return;
        mTipsView = new TipsView(this);
        mTipsView.setOnTapListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeTips();
            }
        });
    }

}
