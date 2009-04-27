package au.id.tedp.mapdroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.String;
import java.net.URL;
import org.apache.http.impl.client.DefaultHttpClient;

public class Picker extends Activity
{
    private ArrayAdapter<RoutePoint> raa;

    public class FindRouteButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!areCoordsValid()) {
                Toast.makeText(v.getContext(), "Invalid coordinates", Toast.LENGTH_SHORT).show();
                return;
            }

            MapView map = (MapView) findViewById(R.id.mapView);
            map.setCenterCoords(
                    Float.parseFloat(((TextView)findViewById(R.id.txtStartLat)).getText().toString()),
                    Float.parseFloat(((TextView)findViewById(R.id.txtStartLong)).getText().toString()));

            // Build the request
            // XXX: This is the wrong place to build the URI.
            StringBuilder sburi = new StringBuilder(150);
            sburi.append("http://routes.cloudmade.com/12d497bd108850b885b14af7567174fd/api/0.3/");
            // Fields have already been validated, so use the raw strings
            sburi.append(((TextView)findViewById(R.id.txtStartLat)).getText()).append(",");
            sburi.append(((TextView)findViewById(R.id.txtStartLong)).getText()).append(",");
            sburi.append(((TextView)findViewById(R.id.txtDestLat)).getText()).append(",");
            sburi.append(((TextView)findViewById(R.id.txtDestLong)).getText());
            // FIXME: Allow selection of non-car routes
            sburi.append("/car.gpx");

            /*
            CloudRoute route = new CloudRoute();
            try {
                route.request(new URL(sburi.toString()));
            } catch (Exception e) {
                Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            raa = new ArrayAdapter(v.getContext(), R.layout.list_item,
                    route.getPoints());

            ListView lv = (ListView)findViewById(R.id.lstDirections);
            lv.setAdapter((ListAdapter)raa);
            raa.notifyDataSetChanged();
            */
        }

        public void appendField(StringBuilder str, TextView txt) {
            str.append(txt.getText().toString());
        }

        /**
          Validates the coordinates in the TextEdit fields.
         */
        public boolean areCoordsValid() {
            return (   isValidCoord((TextView)findViewById(R.id.txtStartLat))
                    && isValidCoord((TextView)findViewById(R.id.txtStartLong))
                    && isValidCoord((TextView)findViewById(R.id.txtDestLat))
                    && isValidCoord((TextView)findViewById(R.id.txtDestLong)));
        }

        /**
          Determines whether the given TextView contains a valid coordinate.
          That is, a floating point value between -180 and +180.
          */
        public boolean isValidCoord(TextView tv) {
            final Float maxDegrees = new Float(180);
            final Float minDegrees = new Float(-180);

            Float f;

            try {
                f = Float.valueOf(tv.getText().toString());
            } catch (NumberFormatException e) {
                return false;
            }

            if (f.compareTo(maxDegrees) > 0 || f.compareTo(minDegrees) < 0)
                return false;

            return true;
        }
    }

    private void setMapLocation(float latitude, float longitude) {
        MapView map = (MapView) findViewById(R.id.mapView);
        map.setCenterCoords(latitude, longitude);
    }

    public static final String SAVED_LATITUDE_KEY = "map_center_latitude";
    public static final String SAVED_LONGITUDE_KEY = "map_center_longitude";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.main);

        final Button btnFindRoute = (Button) findViewById(R.id.btnFindRoute);
        btnFindRoute.setOnClickListener(new FindRouteButtonHandler());

        // XXX: Use the location from the saved state
        // Add a button to select the current location rather than
        // setting it to the current location every time.
        //prefill_fields((Context)this);

        float newLat, newLong;

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        if (savedState != null) {
            newLat = savedState.getFloat(SAVED_LATITUDE_KEY, (float)0.0);
            newLong = savedState.getFloat(SAVED_LONGITUDE_KEY, (float)0.0);
        } else if (settings != null) {
            newLat = settings.getFloat(SAVED_LATITUDE_KEY, (float)0.0);
            newLong = settings.getFloat(SAVED_LONGITUDE_KEY, (float)0.0);
        } else {
            newLat = (float)0.0;
            newLong = (float)0.0;
        }

        MapView map = (MapView) findViewById(R.id.mapView);
        map.setCenterCoords(newLat, newLong);
        updateLocationFields();

        //Debug.startMethodTracing("mapdroid");
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();

        MapView map = (MapView) findViewById(R.id.mapView);

        ed.putFloat(SAVED_LATITUDE_KEY, map.getLatitude());
        ed.putFloat(SAVED_LONGITUDE_KEY, map.getLongitude());
        ed.commit();
    }

    // XXX: Not sure whether this should be onSaveInstanceState or onPause
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        MapView map = (MapView) findViewById(R.id.mapView);
        if (map == null)
            return;

        outState.putFloat(SAVED_LATITUDE_KEY, map.getLatitude());
        outState.putFloat(SAVED_LONGITUDE_KEY, map.getLongitude());
    }

    @Override
    public void onDestroy() {
        //Debug.stopMethodTracing();
        super.onDestroy();
    }

    public void updateLocationFields() {
        EditText txtStartLat = (EditText) findViewById(R.id.txtStartLat);
        EditText txtStartLong = (EditText) findViewById(R.id.txtStartLong);

        MapView map = (MapView) findViewById(R.id.mapView);
        txtStartLat.setText(Float.toString(map.getLatitude()));
        txtStartLong.setText(Float.toString(map.getLongitude()));
    }

    public void prefill_fields(Context ctx) {
        Location loc = getLastLocation(ctx);
        EditText txtStartLat = (EditText) findViewById(R.id.txtStartLat);
        EditText txtStartLong = (EditText) findViewById(R.id.txtStartLong);

        if (loc == null) {
            txtStartLat.setText("");
            txtStartLong.setText("");
        } else {
            txtStartLat.setText(String.format("%f", loc.getLatitude()));
            txtStartLong.setText(String.format("%f", loc.getLongitude()));
        }
    }

    public Location getLastLocation(Context ctx) {
        // XXX: Can this be static final?
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        LocationManager locmgr = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        String bestProvider = locmgr.getBestProvider(criteria, true);
        if (bestProvider == null)
            return null;

        // XXX: Could be out-of-date or disabled
        return locmgr.getLastKnownLocation(bestProvider);
    }

    // We probably want to require high-res (GPS) location rather than low-res
    // but this will do for now.
    public String getLastLocationAsString(Context ctx) {
        Location loc = getLastLocation(ctx);

        if (loc == null)
            return null;

        return String.format("%f,%f", loc.getLatitude(), loc.getLongitude());
    }
}

/* vim: set ts=4 sw=4 et ai :*/
