package com.gdudes.app.gdudesapp.BillingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;

public class DonateInfoActivity extends GDCustomToolbarAppCompatActivity {

    TextView WhyDonateAns;
    TextView WhatsinReturnAns;
    Button btnDonate;

    public DonateInfoActivity() {
        super("Donate");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_info);

        WhyDonateAns = (TextView) findViewById(R.id.WhyDonateAns);
        WhatsinReturnAns = (TextView) findViewById(R.id.WhatsinReturnAns);
        btnDonate = (Button) findViewById(R.id.btnDonate);

        String TextToSet = WhyDonateAns.getText().toString();
        TextToSet = TextToSet.replaceAll("__", "<b>");
        TextToSet = TextToSet.replaceAll("--", "</b>");
        TextToSet = TextToSet.replace("##", "<br>");
        WhyDonateAns.setText(Html.fromHtml(TextToSet));
        TextToSet = WhatsinReturnAns.getText().toString();
        TextToSet = TextToSet.replaceAll("__", "<b>");
        TextToSet = TextToSet.replaceAll("--", "</b>");
        TextToSet = TextToSet.replace("##", "<br>");
        WhatsinReturnAns.setText(Html.fromHtml(TextToSet));

        btnDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DonateInfoActivity.this, DonateOptionsActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        HasActions = false;
        ShowTitleWithoutActions = true;
        postCreate();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
