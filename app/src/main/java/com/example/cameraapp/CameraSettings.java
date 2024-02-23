package com.example.cameraapp;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraSettings extends AppCompatActivity {

    WebView webViewSettings;
    private static final String TAG = "CameraSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        webViewSettings = findViewById(R.id.webViewSettings);

        // Enable JavaScript in WebView
        WebSettings webSettings = webViewSettings.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webViewSettings.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "onPageStarted: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e(TAG, "onReceivedError: " + error);
            }
        });

        // Load the camera feed URL in a background thread
        new CameraSettings.LoadUrlTask().execute("http://192.168.43.179/");
    }

    private class LoadUrlTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            try {
                // Perform network operations in the background
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // You can add more logic here if needed

            } catch (IOException e) {
                Log.e(TAG, "Error opening connection", e);
            }catch (Exception e){
                Log.e(TAG,"General error",e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Load WebView on the main thread after the background task is completed
            webViewSettings.loadUrl("http://192.168.43.179/");
        }
    }
}