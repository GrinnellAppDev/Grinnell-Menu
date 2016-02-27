package edu.grinnell.glicious;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowserActivity extends Activity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        getActionBar().setTitle("G-licious");
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#EF5350")));
//        getActionBar().hide();
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setInitialScale(100);
        mWebView.loadUrl("http://nutrition.grinnell.edu/NetNutrition/1/Mobile/Mobile");
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
//        Toast.makeText(this, "Hello World", Toast.LENGTH_LONG).show();
    }
}
