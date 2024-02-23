package com.example.cameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cameraapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    WebView webView;
    Button doorButton;
    TextView doorStatus,textView;
    private DatabaseReference doorRef;
    private Button captureBtn;

    FirebaseAuth auth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase login part

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        textView=findViewById(R.id.user_details);

        if(user==null){
            Intent intent=new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }else{
            textView.setText(user.getEmail());
        }


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //getSupportActionBar().setTitle("EchoEntry");

        doorRef = FirebaseDatabase.getInstance().getReference("/Door");

        webView = findViewById(R.id.webView);
        captureBtn = findViewById(R.id.captureBtn);
        doorButton = findViewById(R.id.doorBtn);
        doorStatus =findViewById(R.id.doorStatus);

        // Enable JavaScript in WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the camera feed URL in a background thread
        new LoadUrlTask().execute("http://192.168.43.179:81/stream");

        FirebaseAuth auth=FirebaseAuth.getInstance();
        FirebaseUser user =auth.getCurrentUser();

        doorRef=FirebaseDatabase.getInstance().getReference("/Door");
        updateButtonState();

        doorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDoorState();
            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method to capture the image
                captureImage();
            }
        });

    }

    private void captureImage() {
        // Get the width and height of the WebView
        int webViewWidth = webView.getWidth();
        int webViewHeight = webView.getHeight();

        // Create a bitmap with the same size as the WebView
        Bitmap bitmap = Bitmap.createBitmap(webViewWidth, webViewHeight, Bitmap.Config.ARGB_8888);

        // Create a Canvas with the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the WebView content onto the Canvas
        webView.draw(canvas);

        // Now, 'bitmap' contains the captured image

        // TODO: Save the bitmap to the gallery or perform any other actions
        // For example, you can save the image to the gallery using MediaStore
        // Note: Don't forget to request the necessary permissions for saving images

        // Example: Save the image to the gallery
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Captured Image", "Description");

        // Display a toast message
        Toast.makeText(this, "Image captured and saved to gallery!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        webView.pauseTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
    }
    private void updateButtonState() {
        doorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long doorState = (long) snapshot.getValue();
                doorButton.setText((doorState == 0 ? "Unlock" : "Lock"));
                doorStatus.setText("The Door is " + (doorState == 0 ? "Close" : "Open"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
    private void toggleDoorState() {
        doorRef.setValue(doorButton.getText().toString().contains("Unlock") ? 1 : 0);
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
                Log.e(TAG, "Error loading URL", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Load WebView on the main thread after the background task is completed
            webView.loadUrl("http://192.168.43.179:81/stream");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.menu_refresh){
            webView.reload();
            return true;
        }else if(id==R.id.camera_settings){
            Intent intent=new Intent(MainActivity.this, CameraSettings.class);
            startActivity(intent);
        }else if (id == R.id.logOut) {
            // Handle logout here
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
