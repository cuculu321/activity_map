package com.example.mitsui.activity_map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.example.mitsui.activity_map.MapsActivity.StrctTest.arrayStr;
import static com.example.mitsui.activity_map.R.id;
import static com.example.mitsui.activity_map.R.layout;
import com.example.mitsui.activity_map.MapDBHelper;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener{

    private static final int MY_LOCATION_REQUEST_CODE = 1000;
    private GoogleMap mMap;
    private LocationManager myLocationManager;
    boolean switchcheck;
    LatLng myLocation = new LatLng(1.0,1.0);

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(id.map);
        mapFragment.getMapAsync(this);

        Switch switchButton = (Switch) findViewById(id.Switch1);
        // switchButtonのオンオフが切り替わった時の処理を設定
        switchButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener(){
                    public void onCheckedChanged(CompoundButton comButton, boolean isChecked){
                        // オンなら
                        if(isChecked){
                            switchcheck = true;
                        }
                        // オフなら
                        else{
                            switchcheck = false;
                        }
                    }
                }
        );
    }

    @Override

    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        double my_Latitude = 10.0;
        double my_Longtitude = 10;
        Context context = getApplicationContext();
        MapDBHelper helper = new MapDBHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            String provider = getProvider();
            Location lastLocation = myLocationManager.getLastKnownLocation(provider);

            if (lastLocation != null) {
                setLocation(lastLocation);
                my_Latitude = getLocation(lastLocation, "Latitude"); //現在地の緯度経度取得
                my_Longtitude = getLocation(lastLocation, "Longtitude");
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
        parser.parse(context);

        ArrayList<Double> latitude = new ArrayList<Double>();
        ArrayList<Double> longtitude = new ArrayList<Double>();

        final LatLng start_position = new LatLng(my_Latitude, my_Longtitude);

        for(int i=1;i<arrayStr.size();i++) {

            Log.d("Googlemap", "Pin:" + myLocationManager + arrayStr.get(i).id + "," +arrayStr.get(i).name+ "," + arrayStr.get(i).latitude + ", " + arrayStr.get(i).longitude);
            latitude.add(Double.parseDouble(arrayStr.get(i).latitude));
            longtitude.add(Double.parseDouble(arrayStr.get(i).longitude));

            /*mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude.get(i-1), longtitude.get(i-1)))
                    .title(arrayStr.get(i).name).icon(BitmapDescriptorFactory.fromAsset("hinan_jo.bmp")));*/

            /*
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(LatitudetreeSet.floor(my_Location.latitude), LongtitudetreeSet.floor(my_Location.longitude)))
                    .title(arrayStr.get(i).name).icon(BitmapDescriptorFactory.fromAsset("hinan_jo.bmp2")));*/

            //データベースへの追加
            ContentValues values = new ContentValues();
            values.put("id", Integer.parseInt(arrayStr.get(i).id));
            values.put("name", arrayStr.get(i).name);
            values.put("lat", Double.parseDouble(arrayStr.get(i).latitude));
            values.put("lng", Double.parseDouble(arrayStr.get(i).longitude));

            long id = database.insert("hinanjo", null, values);
            if (id == -1){
                //失敗した失敗した場合
            }
        }

        Cursor all_cursor = database.rawQuery("select id,name,lat,lng from hinanjo", null);
        /*while(all_cursor.moveToNext()){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(all_cursor.getDouble(2), all_cursor.getDouble(3)))
                    .title(all_cursor.getString(1)).icon(BitmapDescriptorFactory.fromAsset("hinanjo_marker2.png")));
        }*/

        Cursor neigborhood_cursor = database.rawQuery("select id, name, lat, lng from hinanjo where lng between ? and ?",
                new String[]{String.valueOf(my_Longtitude -0.2),  String.valueOf(my_Longtitude +0.2)});
        while(neigborhood_cursor.moveToNext()){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(neigborhood_cursor.getDouble(2), neigborhood_cursor.getDouble(3)))
                    .title(neigborhood_cursor.getString(1)).icon(BitmapDescriptorFactory.fromAsset("hinanjo_marker_orange.png")));
        }

        Cursor not_neigborhood_cursor = database.rawQuery("select id, name, lat, lng from hinanjo where lng not between ? and ?",
                new String[]{String.valueOf(my_Longtitude -0.2),  String.valueOf(my_Longtitude +0.2)});
        while(not_neigborhood_cursor.moveToNext()){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(not_neigborhood_cursor.getDouble(2), not_neigborhood_cursor.getDouble(3)))
                    .title(not_neigborhood_cursor.getString(1)).icon(BitmapDescriptorFactory.fromAsset("hinanjo_marker2.png")));
        }

        database.close();

        /*LatitudetreeSet.subSet(my_Latitude-0.01,my_Latitude+0.01);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(LatitudetreeSet.floor(my_Latitude), LongtitudetreeSet.floor(my_Longtitude)))
                .title(String.valueOf(LatitudetreeSet.floor(my_Latitude))));*/
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(switchcheck == true) {

                        // TODO Auto-generated method stub
                        LatLng goal_position = marker.getPosition();
                        double my_Latitude = 0;
                        double my_Longtitude = 0;
                        LatLng start_position = myLocation;

                        Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        intent.setData(Uri.parse("http://maps.google.com/maps?saddr="+start_position.latitude+","+start_position.longitude+"&daddr="+goal_position.latitude+","+goal_position.longitude));
                        startActivity(intent);
                    }
                    return false;
                }
            });
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
        double mylocation_latitude = getLocation(location, "Latitude");
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

        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));

    }

    private double getLocation(Location location, String want) {
        if (location.getLatitude() != 0){
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
        }

         if(want == "Latitude"){
             return location.getLatitude();
         }else{
             return location.getLongitude();
         }
    }

    public static class CSVParser {

        public static StrctTest parse(Context context) {
            // AssetManagerの呼び出し
            AssetManager assetManager = context.getResources().getAssets();
            StrctTest strt = new StrctTest();
            int i = 0;

            try {
                // CSVファイルの読み込み
                InputStream is = assetManager.open("shisetsu_hinan_mini.csv");
                InputStreamReader inputStreamReader = new InputStreamReader(is, "SJIS");
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String line = "";

                while ((line = bufferReader.readLine()) != null) {
                    // 各行が","で区切られていて8つの項目があるとする
                    String[] RowData = line.split(",");

                    String id = RowData[0];
                    String name = RowData[1];
                    String longitude = RowData[2];
                    String latitude = RowData[3];


                    strt.setStructure(id, name, latitude, longitude);

                    Log.d("MyApp", "Parsed:" + arrayStr.get(i).id+"," +arrayStr.get(i).name+"."+arrayStr.get(i).latitude+", "+arrayStr.get(i).longitude); //Android Monitorへのlogの表

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
        public void setStructure(String id, String name, String latitude, String longitude) {
            arrayStr.add(setStr(id, name, latitude, longitude));
        }

        public MyStructure setStr(String num1, String num2, String num3, String num4) {
            MyStructure str = new MyStructure();
            str.id = num1;
            str.latitude = num3;
            str.longitude = num4;
            str.name = num2;

            return str;
        }

        class MyStructure {
            String id;
            String latitude;
            String longitude;
            String name;

        }
    }
}