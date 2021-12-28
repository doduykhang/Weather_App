package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;
    private TextView temperatureTextView, conditionTextView, cityNameTextView;
    private ImageView imageView;
    private TextInputEditText cityTextInput;
    private RecyclerView weatherRecyclerView;
    private ImageView bgImageView;
    ArrayList<WeatherModel> weatherModels;
    WeatherAdapter weatherAdapter;

    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;

    private String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.loadingProgressBar);
        relativeLayout = findViewById(R.id.homeRelativeLayout);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        conditionTextView = findViewById(R.id.conditionTextView);
        cityNameTextView = findViewById(R.id.cityNameTextView);
        imageView = findViewById(R.id.imageView);
        cityTextInput = findViewById(R.id.cityTextInput);
        weatherRecyclerView = findViewById(R.id.weatherRecycleView);
        bgImageView = findViewById(R.id.bgImageView);

        weatherModels = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModels);
        weatherRecyclerView.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        cityNameTextView.setText(cityName);
        getWeather(cityName);
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";

        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addressList = gcd.getFromLocation(latitude,longitude,10);
            for(Address address : addressList){
                if(address != null){
                    String city = address.getLocality();
                    if(city != null && !city.equals(""))
                        cityName = city;
//                    else
//                        Toast.makeText(this,"User city not found",Toast.LENGTH_SHORT).show();

                }
            }
        }catch(IOException ex){
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT);
        }
        return cityName;
    }

    private void getWeather(String cityName){
        String uri = "http://api.weatherapi.com/v1/forecast.json?key=1c85a18f46ab446ca2634434212812&q="+cityName+"&days=1&aqi=no&alerts=no";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,uri,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
                weatherModels.clear();
                try {
                    JSONObject current = response.getJSONObject("current");
                    String temperature = current.getString("temp_c");
                    temperatureTextView.setText(temperature+"°C");

                    String condition = current.getJSONObject("condition").getString("text");
                    conditionTextView.setText(condition);

                    String conditionIcon = current.getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(imageView);

                    JSONObject forecastObject = response.getJSONObject("forecast");
                    JSONObject forecastArray = forecastObject.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hour = forecastArray.getJSONArray("hour");

                    int isDay = current.getInt("is_day");
                    if(isDay == 1)
                        bgImageView.setImageDrawable(getResources().getDrawable(R.drawable.day));
                    else
                        bgImageView.setImageDrawable(getResources().getDrawable(R.drawable.night));
                    for (int i = 0; i < hour.length(); i++) {
                        JSONObject hourObject = hour.getJSONObject(i);
                        String time = hourObject.getString("time");
                        String temper = hourObject.getString("temp_c");
                        String image = hourObject.getJSONObject("condition").getString("icon");
                        String wind = hourObject.getString("wind_kph");
                        weatherModels.add(new WeatherModel(time,temper,image,wind));
                    }
                    weatherAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter a valid city name",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void searchWeather(View view) {
        String city = cityTextInput.getText().toString();
        if(city != null){
            cityNameTextView.setText(city);
            getWeather(city);
        }else {
            Toast.makeText(this,"User city not found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this,"Please grant permission",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}