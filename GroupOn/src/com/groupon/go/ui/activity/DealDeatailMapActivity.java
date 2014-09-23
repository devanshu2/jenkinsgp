package com.groupon.go.ui.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.groupon.go.R;
import com.groupon.go.constants.ApiConstants;
import com.groupon.go.constants.Constants;
import com.groupon.go.controller.ReverseGeoController;
import com.groupon.go.model.DealDetailResponse.OfferLocations;
import com.groupon.go.persistance.GrouponGoPrefs;
import com.groupon.go.utils.ProjectUtils;
import com.kelltontech.network.ServiceResponse;
import com.kelltontech.utils.ConnectivityUtils;
import com.kelltontech.utils.LocationUtils;
import com.kelltontech.utils.ToastUtils;
import com.kelltontech.utils.UiUtils;

/**
 * @author vineet.rajpoot
 */
public class DealDeatailMapActivity extends GroupOnBaseActivity implements OnInfoWindowClickListener {

	private GoogleMap				mMap;
	private LatLngBounds.Builder	mBuilderForAllDeals;
	private LatLngBounds.Builder	mBuilderForNearestDeal;
	private boolean					mIsShownNearestDeal;
	private boolean					mIsRouteDrawn;
	private double[]				mNearestMarkerLatLng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_detail_map);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setLogo(getResources().getDrawable(R.drawable.ic_space));
		getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.EXTRA_MERCHANT_NAME));
		getSupportActionBar().setSubtitle(getIntent().getStringExtra(Constants.EXTRA_CAT_NAME));

		mIsShownNearestDeal = true;
		mIsRouteDrawn = false;

		mBuilderForNearestDeal = new LatLngBounds.Builder();
		ArrayList<OfferLocations> offerLocations = getIntent().getParcelableArrayListExtra(Constants.EXTRA_LOCATION_LIST);
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.full_map);

		mNearestMarkerLatLng = getIntent().getDoubleArrayExtra(Constants.EXTRA_NEAREST_MARKER_LATLNG);
		mMap = fragment.getMap();
		mMap.setMyLocationEnabled(true);
		drawPathOnMap(mNearestMarkerLatLng);
		placeMarker(offerLocations);
		setInfoWindowAdapter();
		mMap.setOnInfoWindowClickListener(this);
	}

	/**
	 * 
	 */
	private void setInfoWindowAdapter() {
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {
				View v = getLayoutInflater().inflate(R.layout.map_info_window, null);
				TextView address = (TextView) v.findViewById(R.id.txv_marker_offer_title);
				address.setText(marker.getTitle());
				return v;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			break;
		}
		case R.id.action_show_all_deals: {
			if (mIsShownNearestDeal) {
				mIsShownNearestDeal = false;
				item.setTitle(getString(R.string.title_action_map_show_nearest));
				item.setIcon(R.drawable.ic_show_nearest);
				animateCamera(mBuilderForAllDeals);
			} else {
				mIsShownNearestDeal = true;
				item.setTitle(getString(R.string.title_action_map_show_all));
				item.setIcon(R.drawable.ic_show_all);
				if (mIsRouteDrawn) {
					animateCamera(mBuilderForNearestDeal);
				} else {
					animateCamera(null);
				}
			}
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * @param offerLocations
	 */
	private void placeMarker(ArrayList<OfferLocations> offerLocations) {
		if (offerLocations == null || offerLocations.isEmpty()) {
			return;
		}
		double[] latlng = null;
		mBuilderForAllDeals = new LatLngBounds.Builder();
		Location userLocation = GrouponGoPrefs.getDeviceLocation(this);

		if (userLocation != null) {
			LatLng userlatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
			if (LocationUtils.isGpsEnabled(this)) {
				mBuilderForAllDeals.include(userlatlng);
			}

		}
		for (int i = 0; i < offerLocations.size(); i++) {
			latlng = ProjectUtils.getLatLongFromString(offerLocations.get(i).getOffer_location());
			if (latlng == null || latlng.length < 2) {
				continue;
			}
			LatLng markerlatlng = new LatLng(latlng[0], latlng[1]);
			mBuilderForAllDeals.include(markerlatlng);
			ProjectUtils.placeMarker(mMap, markerlatlng, offerLocations.get(i).getMerchant_address(), R.drawable.ic_map_pin_dir);
		}
	}

	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		super.updateUi(serviceResponse);
		switch (serviceResponse.getDataType()) {
		case ApiConstants.GET_PATH_FOR_DIRECTION: {
			removeProgressDialog();
			if (!serviceResponse.isSuccess()) {
				ToastUtils.showToast(this, "error");
				return;
			}
			String line = (String) serviceResponse.getResponseObject();
			new ParserTask().execute(line);
			break;
		}

		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Location userLocation = GrouponGoPrefs.getDeviceLocation(this);
		if (LocationUtils.isLocationEnabled(this)) {
			if (userLocation == null) {
				ToastUtils.showToast(this, getString(R.string.toast_waiting_for_location));
				return;
			}
		} else {
			ToastUtils.showToast(this, getString(R.string.toast_enable_location));
			if (userLocation == null) {
				return;
			}
		}
		LatLng latLng = marker.getPosition();
		String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)", userLocation.getLatitude(), userLocation.getLongitude(), "My Position", latLng.latitude, latLng.longitude, marker.getTitle());
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(intent);
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

		return url;
	}

	public boolean drawPathOnMap(double[] nearestMarkerLatLng) {
		mBuilderForNearestDeal.include(new LatLng(nearestMarkerLatLng[0], nearestMarkerLatLng[1]));
		if (!ConnectivityUtils.isNetworkEnabled(this)) {
			mIsRouteDrawn = false;
			animateCamera(null);
			ToastUtils.showToast(this, getString(R.string.error_no_network));
			return true;
		}

		if (!LocationUtils.isGpsEnabled(this)) {
			mIsRouteDrawn = false;
			animateCamera(null);
			return true;
		}
		Location userLocation = mMap.getMyLocation();
		if (userLocation == null) {
			userLocation = GrouponGoPrefs.getDeviceLocation(this);
		}

		if (userLocation == null) {
			mIsRouteDrawn = false;
			animateCamera(null);
			return true;
		}
		float distance = LocationUtils.getDistance(userLocation.getLatitude(), userLocation.getLongitude(), nearestMarkerLatLng[0], nearestMarkerLatLng[1]);
		if (distance == -1 || distance > Constants.MAX_DISTANCE_KM_TO_SHOW_ROUTE * 1000) {
			mIsRouteDrawn = false;
			animateCamera(null);
			return true;
		}
		LatLng userlatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
		mBuilderForNearestDeal.include(userlatlng);
		mBuilderForNearestDeal.include(new LatLng(nearestMarkerLatLng[0], nearestMarkerLatLng[1]));
		showProgressDialog(getString(R.string.prog_loading));
		String url = getDirectionsUrl(userlatlng, new LatLng(nearestMarkerLatLng[0], nearestMarkerLatLng[1]));
		ReverseGeoController controller = new ReverseGeoController(this, this);
		controller.getData(ApiConstants.GET_PATH_FOR_DIRECTION, url);
		return false;
	}

	private void animateCamera(LatLngBounds.Builder builder) {
		CameraUpdate cu;
		if (builder != null) {
			LatLngBounds bounds = builder.build();
			cu = CameraUpdateFactory.newLatLngBounds(bounds, 650, 800, 0);
			mMap.animateCamera(cu);
		} else {
			CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mNearestMarkerLatLng[0], mNearestMarkerLatLng[1])).zoom(14f).build();
			cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
			mMap.animateCamera(cu);
		}

	}

	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

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
				polyLineOptions.width(UiUtils.getPixels(DealDeatailMapActivity.this, 5));
				polyLineOptions.color(Color.argb(223, 127, 153, 255));
			}
			mIsRouteDrawn = true;
			mMap.addPolyline(polyLineOptions);
			animateCamera(mBuilderForNearestDeal);
			removeProgressDialog();
		}
	}

	public class PathJSONParser {

		public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
			List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
			JSONArray jRoutes = null;
			JSONArray jLegs = null;
			JSONArray jSteps = null;
			try {
				jRoutes = jObject.getJSONArray("routes");
				/** Traversing all routes */
				for (int i = 0; i < jRoutes.length(); i++) {
					jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
					List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

					/** Traversing all legs */
					for (int j = 0; j < jLegs.length(); j++) {
						jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

						/** Traversing all steps */
						for (int k = 0; k < jSteps.length(); k++) {
							String polyline = "";
							polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
							List<LatLng> list = decodePoly(polyline);

							/** Traversing all points */
							for (int l = 0; l < list.size(); l++) {
								HashMap<String, String> hm = new HashMap<String, String>();
								hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
								hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
								path.add(hm);
							}
						}
						routes.add(path);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
			}
			return routes;
		}

		/**
		 * Method Courtesy : jeffreysambells.com/2010/05/27
		 * /decoding-polylines-from-google-maps-direction-api-with-java
		 * */
		private List<LatLng> decodePoly(String encoded) {

			List<LatLng> poly = new ArrayList<LatLng>();
			int index = 0, len = encoded.length();
			int lat = 0, lng = 0;

			while (index < len) {
				int b, shift = 0, result = 0;
				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;
				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
				poly.add(p);
			}
			return poly;
		}
	}

}
