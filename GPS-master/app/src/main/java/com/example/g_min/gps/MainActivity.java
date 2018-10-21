package com.example.g_min.gps;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {
    private static final String TAG = "";
    private ListView listView;
    private ArrayList<String> list;
    private Button button;
    private LocationHandler handler = null;
    private Location initLoc = null;
    private Location currentLoc = null;
    private Location prevLoc = null;
    private float btwDistance, totalDistance, instaVelocity, btwVelocity, totalVelocity;
    private String data;
    private boolean permissions_granted;
    private final static int PERMISSION_REQUEST_CODE = 999;
    private final static String LOGTAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //define listView and Button
        listView = findViewById(R.id.listView);
        button = findViewById(R.id.button);
        list = new ArrayList<String>();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click();
            }
        });

        //initiate the handler
        if (handler == null) {
            this.handler = new LocationHandler(this);
            this.handler.addObserver(this);
        }

        // check permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    public boolean isPermissions_granted() {
        return permissions_granted;
    }

    //permission granted or not
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // we have only asked for FINE LOCATION
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissions_granted = true;
                Log.i(LOGTAG, "Fine location permission granted.");
            }
            else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Fine location permission not granted.");
            }
        }

    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof LocationHandler) {
            final Location l = (Location) o;
            final double lat = l.getLatitude();
            final double lon = l.getLongitude();
            final long time = l.getTime();

            MainActivity.this.currentLoc = new Location("");
            MainActivity.this.currentLoc.setLatitude(lat);
            MainActivity.this.currentLoc.setLongitude(lon);
            MainActivity.this.currentLoc.setTime(time);
        }

    }


    private void click(){

        final SharedPreferences preferences = getApplicationContext().getSharedPreferences("Private", 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("CLICKED", true).apply();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (initLoc == null) {

                    MainActivity.this.initLoc = new Location("");
                    MainActivity.this.initLoc.setLatitude(currentLoc.getLatitude());
                    MainActivity.this.initLoc.setLongitude(currentLoc.getLongitude());

                    MainActivity.this.btwDistance = 0.0f;
                    MainActivity.this.totalDistance = 0.0f;
                    MainActivity.this.btwVelocity = 0.0f;
                    MainActivity.this.totalVelocity = 0.0f;
                    MainActivity.this.instaVelocity = 0.0f;

                    MainActivity.this.prevLoc = initLoc;
                    data = initLoc.getLatitude() +","+initLoc.getLongitude()+"," + btwDistance
                            +","+totalDistance +"," +btwVelocity + ","+ totalVelocity+ "," + instaVelocity;

                } else {
                    MainActivity.this.btwDistance = currentLoc.distanceTo(prevLoc);
                    MainActivity.this.totalDistance = currentLoc.distanceTo(initLoc);
                    MainActivity.this.instaVelocity = currentLoc.getSpeed();
                    MainActivity.this.btwVelocity = btwDistance / (currentLoc.getTime() - prevLoc.getTime());
                    MainActivity.this.totalVelocity = totalDistance / (currentLoc.getTime() - initLoc.getTime());
                    data = currentLoc.getLatitude() +"," + currentLoc.getLongitude() +","+btwDistance
                            + ","+totalDistance +"," +btwVelocity + ","+ totalVelocity + "," + instaVelocity;
                }


                MainActivity.this.prevLoc = currentLoc;

                if(preferences.getBoolean("CLICKED", true)){
                    list.add(0, data);
                    listView.setAdapter(new Adapter(MainActivity.this, list));
                    editor.putBoolean("CLICKED", false).apply();
                }
            }
        });
    }


    class Adapter extends BaseAdapter{

        private MainActivity mainActivity;
        private ArrayList<String> list = new ArrayList<String>();

        public Adapter(MainActivity mainActivity, ArrayList<String> list) {
            this.mainActivity = mainActivity;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            String [] data = (list.get(position).split(","));


            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.the_list, null, false);
            }
            ((TextView) convertView.findViewById(R.id.lat)).setText(data[0]);

            ((TextView) convertView.findViewById(R.id.lon)).setText(data[1]);

            ((TextView) convertView.findViewById(R.id.distance2points)).setText(data[2]);

            ((TextView) convertView.findViewById(R.id.totaldistance)).setText(data[3]);

            ((TextView) convertView.findViewById(R.id.velocity2points)).setText(data[4]);

            ((TextView) convertView.findViewById(R.id.totalvelocity)).setText(data[5]);

            ((TextView) convertView.findViewById(R.id.instavelocity)).setText(data[6]);



            return convertView;
        }
    }

    // rotation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            Log.d(TAG, "langscape");
        }
        else{
            Log.d(TAG, "portrait");
        }

    }
}
