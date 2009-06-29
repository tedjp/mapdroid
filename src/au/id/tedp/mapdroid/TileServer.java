package au.id.tedp.mapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.net.URL;

class TileServer {
    private TileSet tileset;

    public TileServer() {
        tileset = new TileSet();
        tileset.addServer("a.tile.openstreetmap.org");
        tileset.addServer("b.tile.openstreetmap.org");
        tileset.addServer("c.tile.openstreetmap.org");
    }

    public int getMaxZoom() {
        return 18; /* umm... */
    }

    public void requestTile(int zoom, float latitude, float longitude)
    {
        /*
        return getTile(
                zoom,
                TileSet.getXTileNumber(zoom, longitude),
                TileSet.getYTileNumber(zoom, latitude));
                */
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
            Log.e("Mapdroid", e.toString());
            return null;
        }
    }

    public int getTileSize() {
        return tileset.getTileSize();
    }
}

/* vim: set ts=4 sw=4 et :*/
