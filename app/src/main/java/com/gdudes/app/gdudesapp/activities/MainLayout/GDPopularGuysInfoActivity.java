package com.gdudes.app.gdudesapp.activities.MainLayout;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.R;

public class GDPopularGuysInfoActivity extends GDAppCompatActivity {

    Context mContext;
    ImageView btnClose;
    Button btnOK = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_guys_info);
        mContext = GDPopularGuysInfoActivity.this;

        btnOK = (Button) findViewById(R.id.btnOK);
        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        Double dWidth = size.x * 0.85;
        Double dHeight = size.y * 0.7;
        getWindow().setLayout(dWidth.intValue(), dHeight.intValue());
    }
}
