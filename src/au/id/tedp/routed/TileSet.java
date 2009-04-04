// TileSet: A single style of tile images that may be used side-by-side to
// render a map.
package au.id.tedp.routed;

// Tiles@Home:
// http://b.tah.openstreetmap.org/Tiles/tile/17/24870/49479.png
//        a-f
// Mapnik (default):
// http://c.tile.openstreetmap.org/17/24870/49479.png
//        a-c
// CloudMade:
// http://b.tile.cloudmade.com/YOUR-API-KEY-GOES-HERE/1/256/15/17599/10746.png
//        a-c

import java.lang.Double;
import java.lang.String;
import java.util.ArrayList;
import java.util.Iterator;

class TileSet {
    private ArrayList<String> servers;
    private String pathPrefix;
    private int maxZoomLevel;
    private int tileSize = 256; /* in pixels */

    public TileSet() {
        servers = new ArrayList<String>(3);
        pathPrefix = "";
        maxZoomLevel = 17;
    }

    public static int getXPixel(int zoom, float longitude, int tileWidth) {
        return Math.round(getXTileNumberAsFloat(zoom, longitude) % 1 * tileWidth);
    }

    public static int getYPixel(int zoom, float latitude, int tileHeight) {
        return Math.round(getYTileNumberAsFloat(zoom, latitude) % 1 * tileHeight);
    }

    public static float getXTileNumberAsFloat(int zoom, float longitude) {
        return ((longitude + 180) / 360) * (1 << zoom);
    }

    public static float getYTileNumberAsFloat(int zoom, float latitude) {
        double lat = (double) latitude;
        return (float)((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
    }

    public static float getLongitude(int zoom, float centerPixelX) {
        return (float)(centerPixelX / (1 << zoom) * 360.0 - 180.0);
    }

    public static float getLatitude(int zoom, float centerPixelY) {
        return (float)(Math.atan(Math.sinh(Math.PI * (1 - 2 * (double)centerPixelY / (1 << zoom)))) * 180 / Math.PI);
    }

    // XXX: Floats are slow. Provide interface for mega-degrees (int)?
    // Another optimisation: save precomputed 2^[1-17] in an array
    // rather than using expensive Math.pow() float operations
    public static int getXTileNumber(int zoom, float longitude) {
        return (int)Math.floor(getXTileNumberAsFloat(zoom, longitude));
    }

    // See getXTileNumber
    public static int getYTileNumber(int zoom, float latitude) {
        return (int)Math.floor(getYTileNumberAsFloat(zoom, latitude));
    }

    public void addServer(String hostname) {
        servers.add(hostname);
    }
//    public void removeServer(String hostname);

    public void setPathPrefix(String prefix) {
        pathPrefix = prefix;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public String getUriForTile(int zoom, int x, int y) {
        StringBuilder sb = new StringBuilder(128);
        if (servers.isEmpty())
            return null;

        // XXX Hack: just choose the first server
        sb.append("http://");
        sb.append(servers.get(0)).append(pathPrefix);
        sb.append(String.format("/%d/%d/%d.png", zoom, x, y));

        return sb.toString();
    }

    public int getTileSize() {
        return tileSize;
    }
}

/* vim: set ts=4 sw=4 et :*/
