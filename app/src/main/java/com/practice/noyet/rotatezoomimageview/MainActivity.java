package com.practice.noyet.rotatezoomimageview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.ypy.eventbus.EventBus;

import java.io.File;
import java.math.BigDecimal;

public class MainActivity extends Activity {

    private ImageView mImageView;
    private MaterImageHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view);
        Picasso.with(this).load(R.drawable.alipay).placeholder(R.drawable.alipay).into(mImageView);
        helper = new MaterImageHelper(this, mImageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.recyclerImg();
    }
}
