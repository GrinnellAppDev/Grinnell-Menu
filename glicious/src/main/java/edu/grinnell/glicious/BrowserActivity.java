package edu.grinnell.glicious;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class BrowserActivity extends Activity {

    WebView mWebView;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        getActionBar().setTitle("G-licious");
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EF5350")));
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setInitialScale(100);
        String url = getApplicationContext().getResources().getString(R.string.app_url);

        // Add Progress Dialog
        mProgressDialog = new ProgressDialog(BrowserActivity.this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // Show dialog when loading and hide it after finishing loading
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(BrowserActivity.this, "Error:" + description, Toast.LENGTH_SHORT).show();
            }
        });

        mWebView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser_launch, menu);
        return true;
    }
    public void handleQuestionClick(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message)
                .setTitle("Info")
                .setPositiveButton("Ok", null);
        builder.create().show();
    }
}
