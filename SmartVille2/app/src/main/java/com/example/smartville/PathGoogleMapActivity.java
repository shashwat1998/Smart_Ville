package com.example.smartville;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class PathGoogleMapActivity extends FragmentActivity implements OnMapReadyCallback {

	private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
			-73.998585);
	private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
	private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);
	String start,end;
	GoogleMap gMap;
	private double user_latitude;
	private double user_longitude;
	final String TAG = "PathGoogleMapActivity";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_path_google_map);
		Bundle extra=getIntent().getExtras();
		start=extra.getString("origin");
		end=extra.getString("dest");
		user_latitude=extra.getDouble("lat");
		user_longitude=extra.getDouble("lon");
		Log.e("fata",user_latitude+" "+user_longitude);
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		fm.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		gMap=googleMap;
		LatLng location=new LatLng(user_latitude,user_longitude);
		gMap.addMarker(new MarkerOptions().position(location).title("Your current Location"));
		String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+start+"&destination="+end+"&key=AIzaSyDos2inlsp6oooM293QYQ2pfkUAO1osn0Y";
		Log.e("fatal",url);
		ReadTask downloadTask = new ReadTask();
		downloadTask.execute(url);
		//googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BROOKLYN_BRIDGE,
		//		13));
	}

	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String url2 = "https://maps.googleapis.com/maps/api/directions/json?origin="+start+"&destination="+end+"&key=AIzaSyDos2inlsp6oooM293QYQ2pfkUAO1osn0Y";
			String output1 = "", output;
			Log.e("fatal",url2);
			try {
				URL url3 = new URL(url2);
				HttpURLConnection conn = (HttpURLConnection) url3.openConnection();
				conn.setRequestMethod("GET");
				//conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
				conn.setRequestProperty("Accept", "application/json");
				conn.setDoOutput(true);
				conn.setDoInput(true);
				while (conn == null) {

				}
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));

				while ((output = br.readLine()) != null) {
					output1 += output;
				}
				Log.e("Fatal",output1);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return output1;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}

	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;

			// traversing through routes
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);

				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);
					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(5);
				polyLineOptions.color(Color.RED);
			}

			Polyline pl=gMap.addPolyline(polyLineOptions);
		}
	}
}
