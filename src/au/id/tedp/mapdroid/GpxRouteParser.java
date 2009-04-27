package au.id.tedp.mapdroid;

/* Parses a GPX Route as described on
http://developers.cloudmade.com/wiki/routing-http-api/Documentation
*/

import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class GpxRouteParser extends DefaultHandler {
    private boolean in_desc = false;
    private boolean in_point = false;
    private RoutePoint point;
    private RouteGatherer gatherer;

    public static final String DESC = "desc";
    public static final String POINT = "rtept";

    public GpxRouteParser(RouteGatherer rg) {
        super();

        if (rg == null)
            throw(new IllegalArgumentException("A non-null RouteGatherer must be specified",
                        new NullPointerException()));

        gatherer = rg;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) {
        // TODO: Throw exceptions for unexpected states
        if (localName.equals(DESC))
            in_desc = true;

        else if (localName.equals(POINT)) {
            point = new RoutePoint();
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        // TODO: Throw exceptions for unexpected states
        if (localName.equals(POINT)) {
            gatherer.addPoint(point);
            point = null;
        }

        if (localName.equals(DESC))
            in_desc = false;
    }


    @Override
    public void characters(char[] ch, int start, int length) {
        if (in_desc && point != null) {
            point.setDescription(new String(ch, start, length));
            Log.d("MapdroidGpxRouteParser", point.getDescription());
        }
    }

    @Override
    public void endDocument() {
        gatherer.endRoute();
    }
}

/* vim: set ts=4 sw=4 et ai :*/
