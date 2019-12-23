package com.example.lab3;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.example.lab3.models.Bank;
import com.example.lab3.models.Location;
import com.example.lab3.models.Results;
import com.example.lab3.service.BankLocationApi;
import com.example.lab3.services.GPSService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.logging.Logger;

import static com.example.lab3.client.BankLocationApiClient.getService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LiveData<Results> mResults;
    private Results results;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mResults = new MutableLiveData<>();
        results = new Results();

        if (!getPermissions()){
            startGPSService();
        }
    }

    private void startGPSService(){
        Intent intent = new Intent(getApplicationContext(), GPSService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED){
                startGPSService();
            }
            else {
                getPermissions();
            }
        }
    }

    private boolean getPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 111);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(getApplicationContext(), GPSService.class);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    build_retrofit_and_get_response((String) intent.getExtras().get("location"));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location-updates"));

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        BankLocationAsyncTask bankLocationAsyncTask = new BankLocationAsyncTask(this);
//        bankLocationAsyncTask.execute("41.99,21.42");
//
//        mResults.observe(this, new Observer<Results>() {
//            @Override
//            public void onChanged(@Nullable Results results) {
//                Logger logger = Logger.getLogger("OBSREVE");
//                setResults(results);
//                logger.info("onChangeddddddddddddddddddddddddddddddddddddd");
//            }
//        });
//        build_retrofit_and_get_response();

//        if (results.getResults() != null)
//        for (Bank bank : results.getResults()){
//            LatLng latLng = new LatLng(bank.getGeometry().getLocation().getLat(), bank.getGeometry().getLocation().getLng());
//            mMap.addMarker(new MarkerOptions().position(latLng).title(bank.getName()));

//            logger.info("bank"+bank.getName());
//        }


        Logger logger = Logger.getLogger("MARKERS");
        logger.info("puting Markersssssssssssssssss");
    }

    //this is the method that gets locations of banks with retrofit and puts markers on map
    //all that should be made with BankLocationAsyncTask and BankLocationApiClient
    //dont actually know if itll work the fix was a different apikey
    void build_retrofit_and_get_response(String location) {

        String url = "https://maps.googleapis.com/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BankLocationApi service = retrofit.create(BankLocationApi.class);
        Logger logger = Logger.getLogger("RETROFIT");
        logger.info("puting MARKERSSSSSSSSSSSSSSSSSSSS");

        Call<Results> call = service.getBankLocations(location);

        call.enqueue(new Callback<Results>() {
            @Override
            public void onResponse(Call<Results> call, Response<Results> response) {

                try {
                    mMap.clear();
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        String placeName = response.body().getResults().get(i).getName();
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(lat, lng);
                        // Position of Marker on Map
                        markerOptions.position(latLng);
                        // Adding Title to the Marker
                        markerOptions.title(placeName);
                        // Adding Marker to the Camera.
                        Marker m = mMap.addMarker(markerOptions);
                        // move map camera
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                        Logger logger = Logger.getLogger("MARKERS");
                        logger.info("puting MARKERSSSSSSSSSSSSSSSSSSSS");
                    }
                    Logger logger = Logger.getLogger("MARKERS");
                    logger.info("puting MARKERSSSSSSSSSSSSSSSSSSSS");
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Results> call, Throwable t) {
                Log.d("onFailure", t.toString());
            }

        });
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public void setmResults(LiveData<Results> mResults) {
        this.mResults = mResults;
    }
    private static class BankLocationAsyncTask extends AsyncTask<String, Integer, Results>{


        MapsActivity activity;

        BankLocationAsyncTask(MapsActivity activity){
            this.activity = activity;
        }

        @Override
        protected Results doInBackground(String... locations) {
            Results results = null;
            try {
               results = (getService().getBankLocations(locations[0]).execute().body());
                Logger logger = Logger.getLogger("RETROFIT");
                logger.info("fetched from internet");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(Results results) {
            MutableLiveData<Results> resultsLiveData = new MutableLiveData<>();
            resultsLiveData.postValue(results);
            activity.setmResults(resultsLiveData);
            Logger logger = Logger.getLogger("LIVEDATA");
            logger.info("changed");
        }
    }
}
