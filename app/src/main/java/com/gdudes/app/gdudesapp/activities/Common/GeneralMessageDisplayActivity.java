package com.gdudes.app.gdudesapp.activities.Common;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.CustomViewTypes.GDAppCompatActivity;
import com.gdudes.app.gdudesapp.GDTypes.GeneralMessageActivityContent;
import com.gdudes.app.gdudesapp.GDTypes.Users;
import com.gdudes.app.gdudesapp.R;

public class GeneralMessageDisplayActivity extends GDAppCompatActivity {

    Context mContext;
    Users LoggedInUser;
    TextView HeadingText = null;
    TextView DetailText = null;
    Button btnOK = null;
    GeneralMessageActivityContent mGeneralMessageActivityContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_message_display);

        HeadingText = (TextView) findViewById(R.id.HeadingText);
        DetailText = (TextView) findViewById(R.id.DetailText);
        btnOK = (Button) findViewById(R.id.btnOK);

        if (!getIntent().hasExtra("GeneralMessageActivityContent")) {
            finish();
        }
        mGeneralMessageActivityContent = getIntent().getExtras().getParcelable("GeneralMessageActivityContent");
        if (mGeneralMessageActivityContent == null) {
            finish();
        }
        HeadingText.setText(mGeneralMessageActivityContent.HeaderText);
        DetailText.setText(Html.fromHtml(mGeneralMessageActivityContent.DetailedText));
        if (mGeneralMessageActivityContent.ButtonText != null && !mGeneralMessageActivityContent.ButtonText.trim().equals("")) {
            btnOK.setText(mGeneralMessageActivityContent.ButtonText);
        }
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_general_message_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Point size = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(size);
        Double dWidth = size.x * 0.8;
        Double dHeight = size.y * 0.8;
        getWindow().setLayout(dWidth.intValue(), dHeight.intValue());
    }
}
