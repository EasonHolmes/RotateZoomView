package com.practice.noyet.rotatezoomimageview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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
