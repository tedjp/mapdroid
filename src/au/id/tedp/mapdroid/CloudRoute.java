package au.id.tedp.mapdroid;

/* Based on examples/client/ClientWithResponseHandler.java from Apache HttpClient */
/* A class for fetching *and* describing turn-by-turn routing instructions. */
/* Really should be split up */

import android.util.Log;
import java.util.ArrayList;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class CloudRoute implements RouteGatherer {
    private ArrayList<RoutePoint> points;

    public CloudRoute() {
	points = new ArrayList<RoutePoint>();
    }

    /* FIXME: Interface should accept a pair or list of co-ords */
    public void request(URL url) {
	/*
	HttpClient httpclient = new DefaultHttpClient();
	HttpGet httpget = new HttpGet("http://routes.cloudmade.com/12d497bd108850b885b14af7567174fd/api/0.3/40.332539,-111.728461,40.326539,-111.710061/car.gpx");
*/
	/* XXX: Is it possible to do line-by-line parsing of the result rather
	   than loading it all into memory? */
	/*
	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	String responseBody = httpclient.execute(httpget, responseHandler);
	*/


	/* This code based on
	    http://www.helloandroid.com/node/110?page=0%2C2
	 */

	try {
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser sp = spf.newSAXParser();
	    XMLReader xr = sp.getXMLReader();
	    xr.setContentHandler(new GpxRouteParser(this)); // XXX: Circular references?
	    xr.parse(new InputSource(url.openStream()));
	} catch (Exception e) {
	    /* FIXME: Provide user feedback */
	    Log.e("MapdroidCloudRoute", e.getMessage());
	}
    }

    public void addPoint(RoutePoint p) {
	points.add(p);
    }

    /* XXX: Not sure why I made this part of the interface. */
    public void endRoute() {
	Log.d("CloudRoute", "endRoute");
	for (RoutePoint r: points)
	    Log.d("CloudRoute", r.getDescription());
    }

    public ArrayList<RoutePoint> getPoints() {
	return points;
    }
}
