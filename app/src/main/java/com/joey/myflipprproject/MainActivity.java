package com.joey.myflipprproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.joey.myflipprproject.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    TextView urlTextView;
    Button btnGoToFlickr;

    List<InterestingPhoto> interestingPhotoList;
    int currPhotoIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGoToFlickr = findViewById(R.id.btnGoToFlickr);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Fetching next photo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                nextPhoto();
            }
        });

        FlickrAPI flickrAPI = new FlickrAPI(this);
        flickrAPI.fetchInterestingPhotos();
    }

    public void receivedInterestingPhotos(List<InterestingPhoto> interestingPhotoList) {
        this.interestingPhotoList = interestingPhotoList;
        nextPhoto();
    }

    public void receivedPhotoBitmap(Bitmap bitmap) {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }

    public void nextPhoto() {
        if (interestingPhotoList != null && interestingPhotoList.size() > 0) {
            currPhotoIndex++;
            currPhotoIndex %= interestingPhotoList.size();

            TextView titleTextView = findViewById(R.id.titleTextView);
            TextView dateTakenTextView = findViewById(R.id.dateTakenTextView);

            InterestingPhoto interestingPhoto = interestingPhotoList.get(currPhotoIndex);
            titleTextView.setText(interestingPhoto.getTitle());
            dateTakenTextView.setText(interestingPhoto.getDateTaken());

            FlickrAPI flickrAPI = new FlickrAPI(this);
            flickrAPI.fetchPhotoBitmap(interestingPhoto.getPhotoURL());

//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(MainActivity.this, ViewInterestingPhotoActivity.class);
//                    intent.putExtra("selected_image", interestingPhotoList.get(currPhotoIndex));
//                }
//            });
            btnGoToFlickr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(interestingPhoto.getPhotoURL()));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}