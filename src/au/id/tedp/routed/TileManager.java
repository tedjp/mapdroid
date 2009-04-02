package au.id.tedp.routed;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.net.URL;

class TileManager {
    private TileSet tileset;

    public TileManager() {
        tileset = new TileSet();
        tileset.addServer("a.tile.openstreetmap.org");
    }

    public Tile getTile(int zoom, float latitude, float longitude)
        throws java.io.IOException
    {
        return getTile(
                zoom,
                TileSet.getXTileNumber(zoom, longitude),
                TileSet.getYTileNumber(zoom, latitude));
    }

    public Tile getTile(int zoom, int x, int y)
        throws java.io.IOException
    {
        try {
            URL imgurl = new URL(tileset.getUriForTile(zoom, x, y));
            return new Tile(
                    BitmapFactory.decodeStream(imgurl.openStream()),
                    zoom, x, y);
        }
        catch (java.net.MalformedURLException e) {
            Log.e("Routed", e.toString());
            return null;
        }
    }

    public int getTileSize() {
        return tileset.getTileSize();
    }
}

/* vim: set ts=4 sw=4 et :*/
