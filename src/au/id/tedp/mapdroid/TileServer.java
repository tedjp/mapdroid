package au.id.tedp.mapdroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import java.net.URL;

class TileServer extends Thread {
    private Handler mHandler; // XXX: Example code makes this public
    private TileSet tileset;

    public void run() {
        Looper.prepare();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.obj == null)
                    return;

                // XXX: Do stuff.
            }
        };

        Looper.loop();
    }

    public TileServer() {
        tileset = new TileSet();
        tileset.addServer("a.tile.openstreetmap.org");
        tileset.addServer("b.tile.openstreetmap.org");
        tileset.addServer("c.tile.openstreetmap.org");
    }

    public int getMaxZoom() {
        return 18; /* umm... */
    }

    public void requestTile(int zoom, float latitude, float longitude, Messenger notify) {
        requestTile(
                zoom,
                TileSet.getXTileNumber(zoom, longitude),
                TileSet.getYTileNumber(zoom, latitude),
                notify);
    }

    public void requestTile(int zoom, int x, int y, Messenger notify) {
        Tile tile = new Tile(
                tileset.getUriForTile(zoom, x, y),
                zoom, x, y);

        TileDownloader downloader = new TileDownloader(tile, notify);
        Thread thr = new Thread(downloader, "TileDownloader");
        thr.start();
    }

    /*
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
    */

    public int getTileSize() {
        return tileset.getTileSize();
    }
}

/* vim: set ts=4 sw=4 et :*/
