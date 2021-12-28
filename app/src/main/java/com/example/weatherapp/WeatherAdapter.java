package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeatherModel> weatherModels;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModels) {
        this.context = context;
        this.weatherModels = weatherModels;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherModel model = weatherModels.get(position);
        holder.timeTextView.setText(model.getTime().split(" ")[1]);
        holder.temperatureTextView.setText(model.getTemperature()+"°C");
        holder.conditionTextView.setText(model.getWindSpeed()+"km/h");
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return weatherModels.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView timeTextView, conditionTextView, temperatureTextView;
        private ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            conditionTextView = itemView.findViewById(R.id.conditionTextView);
            temperatureTextView = itemView.findViewById(R.id.temperatureTextView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
