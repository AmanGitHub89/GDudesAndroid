package com.gdudes.app.gdudesapp.activities.Settings;

import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gdudes.app.gdudesapp.R;
import com.gdudes.app.gdudesapp.activities.Common.GDCustomToolbarAppCompatActivity;

public class TermsAndPrivacyPolicyActivity extends GDCustomToolbarAppCompatActivity {

    public final static int TERMS_OF_USE = 0;
    public final static int PRIVACY_STATEMENT = 1;
    private int Activity_Mode = -1;

    WebView GDWebView;

    public TermsAndPrivacyPolicyActivity() {
        super("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdweb_view);

        Activity_Mode = getIntent().getExtras().getInt("Activity_Mode", -1);
        if (Activity_Mode == -1) {
            finish();
            return;
        }
        GDWebView = (WebView) findViewById(R.id.GDWebView);
        GDWebView.getSettings().setJavaScriptEnabled(true);
        GDWebView.getSettings().setLoadWithOverviewMode(true);
        GDWebView.getSettings().setUseWideViewPort(true);
        GDWebView.getSettings().setBuiltInZoomControls(true);
        GDWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        //GDWebView.getSettings().setPluginsEnabled(true);
        GDWebView.setWebViewClient(new GDWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(TermsAndPrivacyPolicyActivity.this, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    startActivity(i);
                    view.reload();
                    return true;
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        });

        if (Activity_Mode == TERMS_OF_USE) {
            setToolbarText("GDudes Term Of Use");
            GDWebView.loadUrl("http://www.gdudes.com/Default/TermsOfUse");
        } else {
            setToolbarText("GDudes Privacy Policy");
            GDWebView.loadUrl("http://www.gdudes.com/Default/PrivacyPolicy");
        }
        ShowTitleWithoutActions = true;
        HasActions = false;
        postCreate();
    }

    public static Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_gdweb_view, menu);
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

    private class GDWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }
}
