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

    public TileSet() {
        servers = new ArrayList<String>(3);
        pathPrefix = "";
        maxZoomLevel = 17;
    }

    // XXX: Floats are slow. Provide interface for mega-degrees (int)?
    // Another optimisation: save precomputed 2^[1-17] in an array
    // rather than using expensive Math.pow() float operations
    public static int getXTileNumber(int zoom, float longitude) {
        return (int)Math.floor(((longitude + 180) / 360) * (1 << zoom));
    }

    public static int getYTileNumber(int zoom, float latitude) {
        return (int)Math.floor((1 - Math.log(Math.tan(latitude * Math.PI / 180) + 1 / Math.cos(latitude * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
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
}

/* vim: set ts=4 sw=4 et :*/
