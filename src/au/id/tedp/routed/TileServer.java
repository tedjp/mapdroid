package au.id.tedp.routed;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.URL;

class TileServer {
    private TileSet tileset;

    public TileServer() {
        tileset = new TileSet();
        tileset.addServer("a.tile.openstreetmap.org");
    }

    public Bitmap getTile(int zoom, float latitude, float longitude) {
        try {
            URL imgurl = new URL(tileset.getUriForTile(zoom,
                    TileSet.getXTileNumber(zoom, longitude),
                    TileSet.getYTileNumber(zoom, latitude)));
            return BitmapFactory.decodeStream(imgurl.openStream());
        }
        catch (java.net.MalformedURLException e) { // FIXME: evil.
            return null;
        }
        catch (java.io.IOException e) { // FIXME: Totally evil.
            return null;
        }

    }


}

/* vim: set ts=4 sw=4 et :*/
