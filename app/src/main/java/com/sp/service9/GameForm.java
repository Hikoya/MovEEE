package com.sp.service9;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sp.moveee.R;

public class GameForm extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_form);
        final ProgressDialog pd = ProgressDialog.show(this, "", "Loading...", true);
        pd.setCancelable(true);

        WebView wv = (WebView) findViewById(R.id.gf_wv);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setSupportZoom(true);
        //wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setLoadsImagesAutomatically(true);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv.loadUrl("https://goo.gl/forms/k08Ivi7lNLE9EQ0r2");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
    }
}