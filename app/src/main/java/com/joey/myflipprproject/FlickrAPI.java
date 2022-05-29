package com.joey.myflipprproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FlickrAPI {
    //all Flickr API methods

    static final String BASE_URL = "https://api.flickr.com/services/rest";
    static final String API_KEY = "4f15d199e2e3dfd3238eb67cc4321a0d"; //I know this is bad, but it's not too bad for this project ;)
    static final String TAG = "bingo";

    com.joey.myflipprproject.MainActivity mainActivity;

    public FlickrAPI(com.joey.myflipprproject.MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void fetchInterestingPhotos() {
        //fetches most interesting photos from Flickr
        String url = BuildInterestingPhotosListURL();

        FetchInterestingPhotosListAsyncTask asyncTask = new FetchInterestingPhotosListAsyncTask();
        asyncTask.execute(url);
    }

    public String BuildInterestingPhotosListURL() {
        return BASE_URL + "?method=flickr.interestingness.getList"
                + "&api_key=" + API_KEY + "&format=json" + "&nojsoncallback=1"
                + "&extras=date_taken,url_sq";
    }

    public void fetchPhotoBitmap(String photoURL) {
        FetchInterestingPhotosListAsyncTask fetch = new FetchInterestingPhotosListAsyncTask();
        fetch.fetchPhotoBitmap1(photoURL);
    }

    class FetchInterestingPhotosListAsyncTask extends AsyncTask<String, Void, List<InterestingPhoto>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //show progressBar
            ProgressBar progressBar = mainActivity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<InterestingPhoto> doInBackground(String... strings) {

            String url = strings[0];
            List<InterestingPhoto> interestingPhotoList = new ArrayList<InterestingPhoto>();

            try {
                URL urlObject = new URL(url);
                HttpsURLConnection urlConnection = (HttpsURLConnection) urlObject.openConnection();

                //on success, download JSON
                String jsonResult = "";
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    jsonResult += (char) data;
                    data = reader.read();
                }
                Log.d(TAG, "doInBackground: " + jsonResult);

                JSONObject jsonObject = new JSONObject(jsonResult);
                JSONObject photosObject = jsonObject.getJSONObject("photos");
                JSONArray photoArray = photosObject.getJSONArray("photo");
                for (int i = 0; i < photoArray.length(); i++) {
                    JSONObject singlePhotoObject = photoArray.getJSONObject(i);
                    InterestingPhoto interestingPhoto = parseInterestingPhoto(singlePhotoObject);
                    if (interestingPhoto != null) {
                        interestingPhotoList.add(interestingPhoto);
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return interestingPhotoList;
        }

        private InterestingPhoto parseInterestingPhoto(JSONObject singlePhotoObject) {
            InterestingPhoto interestingPhoto = null;
            try {
                String id = singlePhotoObject.getString("id");
                String title = singlePhotoObject.getString("title");
                String dateTaken = singlePhotoObject.getString("datetaken");
                String photoURL = singlePhotoObject.getString("url_sq");
                interestingPhoto = new InterestingPhoto(id, title, dateTaken, photoURL);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return interestingPhoto;
        }

        @Override
        protected void onPostExecute(List<InterestingPhoto> interestingPhotos) {
            super.onPostExecute(interestingPhotos);

            Log.d(TAG, "onPostExecute: " + interestingPhotos.size());
            mainActivity.receivedInterestingPhotos(interestingPhotos);

            //hide progressBar
            ProgressBar progressBar = mainActivity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
        }

        public void fetchPhotoBitmap1(String photoURL) {
            FetchInterestingPhotosListAsyncTask.PhotoRequestAsyncTask asyncTask = new FetchInterestingPhotosListAsyncTask.PhotoRequestAsyncTask();
            asyncTask.execute(photoURL);
        }

        class PhotoRequestAsyncTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                ProgressBar progressBar = (ProgressBar) mainActivity.findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Bitmap doInBackground(String... strings) {
                Bitmap bitmap = null;

                try {
                    URL url = new URL(strings[0]);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                ProgressBar progressBar = (ProgressBar) mainActivity.findViewById(R.id.progressBar);
                progressBar.setVisibility(View.GONE);
                mainActivity.receivedPhotoBitmap(bitmap);
            }
        }
    }
}
