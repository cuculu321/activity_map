package com.example.mitsui.activity_map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.example.mitsui.activity_map.MapsActivity.StrctTest.arrayStr;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1000;
    private GoogleMap mMap;
    private LocationManager myLocationManager;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override

    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            String provider = getProvider();
            Location lastLocation = myLocationManager.getLastKnownLocation(provider);

            if (lastLocation != null) {
                setLocation(lastLocation);
            }

            mMap.setMyLocationEnabled(true);
            Toast.makeText(this, "Provider=" + provider, Toast.LENGTH_SHORT).show();
            myLocationManager.requestLocationUpdates(provider, 0, 0, this);
        } else {

            setDefaultLocation();
            confirmPermission();
        }

        //CSVの情報取得
        StrctTest strt = new StrctTest();

        CSVParser parser = new CSVParser();
        Context context = getApplicationContext();
        parser.parse(context);
        for(int i=0;i<arrayStr.size();i++) {
            Log.d("Googlemap", "Pin:" + arrayStr.get(i).id + "," + arrayStr.get(i).latitude + ", " + arrayStr.get(i).longitude + "");
            /*
            if(Double.parseDouble(arrayStr.get(i).id) <= 5000){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(arrayStr.get(i).latitude), Double.parseDouble(arrayStr.get(i).longitude)))
                        .title(arrayStr.get(i).id));
            }*/
        }
        Log.d("addMarker", "Use:" + arrayStr.get(3).id + "," + arrayStr.get(3).latitude + ", " + arrayStr.get(3).longitude + "");
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(36.595839, 136.735955))
                .title("Hello world"));

    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == MY_LOCATION_REQUEST_CODE) {

            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
                myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                myLocationManager.requestLocationUpdates(getProvider(), 0, 0, this);
            } else {
                Toast.makeText(this, "権限を取得できませんでした。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override

    public void onLocationChanged(Location location) {

        Toast.makeText(this, "LocationChanged実行", Toast.LENGTH_SHORT).show();
        setLocation(location);
        try {
            myLocationManager.removeUpdates(this);
        } catch (SecurityException e) {
        }
    }

    @Override

    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override

    public void onProviderEnabled(String s) {
    }

    @Override

    public void onProviderDisabled(String s) {
    }

    @Override

    public void onDestroy() {

        super.onDestroy();

        try {
            myLocationManager.removeUpdates(this);
        } catch (SecurityException e) {
        }
    }


    private String getProvider() {

        Criteria criteria = new Criteria();
        return myLocationManager.getBestProvider(criteria, true);
    }


    private void confirmPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this).setTitle("パーミッション説明")

                    .setMessage("このアプリを実行するには位置情報の権限を与えてやる必要です。よろしくお願い致します。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            // trueもfalseも結局同じrequestPermissionsを実行しているので一つにまとめるべきかも
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }

    }

    private void setDefaultLocation() {

        LatLng tokyo = new LatLng(35.681298, 139.766247);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 18));
    }


    private void setLocation(Location location) {

        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("now Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }

    public static class CSVParser {

        public static StrctTest parse(Context context) {
            // AssetManagerの呼び出し
            AssetManager assetManager = context.getResources().getAssets();
            StrctTest strt = new StrctTest();
            int i = 0;

            try {
                // CSVファイルの読み込み
                InputStream is = assetManager.open("shisetsu_hinan.csv");
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String line = "";
                while ((line = bufferReader.readLine()) != null) {
                    // 各行が","で区切られていて4つの項目があるとする
                    StringTokenizer st = new StringTokenizer(line, ",");
                    String id = st.nextToken();
                    String latitude = st.nextToken();
                    String longitude = st.nextToken();
                    String fourth = st.nextToken();

                    strt.setStructure(id, latitude, longitude);

                    Log.d("MyApp", "Parsed:" + arrayStr.get(i).id+","+arrayStr.get(i).latitude+", "+arrayStr.get(i).longitude+""); //Android Monitorへのlogの表示
                    i++;
                }
                bufferReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return strt;
        }
    }

    public static class StrctTest {
        /**
         * 構造体ArrayList
         */
        static ArrayList<MyStructure> arrayStr = new ArrayList<MyStructure>();

        public StrctTest() {
        }

        /**
         * 構造体ArrayListに値をセット.
         */
        public void setStructure(String name, String latitude, String longitude) {
            arrayStr.add(setStr(name, latitude, longitude));
        }

        public MyStructure setStr(String num1, String num2, String num3) {
            MyStructure str = new MyStructure();
            str.id = num1;
            str.latitude = num2;
            str.longitude = num3;
            return str;
        }

        class MyStructure {
            String id;
            String latitude;
            String longitude;
        }
    }
}