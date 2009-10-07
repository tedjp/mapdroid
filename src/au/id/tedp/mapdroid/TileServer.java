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
    private MemoryTileCache memTileCache;

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
        memTileCache = new MemoryTileCache();
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

    protected boolean provideTileFromCache(int zoom, int x, int y, Messenger notify) {
        Tile tile = memTileCache.getTile(zoom, x, y);

        if (tile == null) {
            Log.d("Mapdroid", String.format("Tile %d,%d not provided from cache", x, y));
            return false;
        }

        Log.d("Mapdroid", String.format("Tile %d,%d PROVIDED FROM CACHE", x, y));

        Message response = TileDownloader.buildNotification(tile, true);
        try {
            notify.send(response);
        }
        catch (android.os.RemoteException e) {
            Log.e("Mapdroid", e.toString());
            return false;
        }
        return true;
    }

    public void requestTile(int zoom, int x, int y, Messenger notify) {
        Log.d("Mapdroid", String.format("Requested tile %d,%d", x, y));

        if (provideTileFromCache(zoom, x, y, notify))
            return;

        Tile tile = new Tile(
                tileset.getUriForTile(zoom, x, y),
                zoom, x, y);

        TileDownloader downloader = new TileDownloader(tile, notify, memTileCache);
        Thread thr = new Thread(downloader, "TileDownloader");
        thr.start();
    }

    public int getTileSize() {
        return tileset.getTileSize();
    }
}

/* vim: set ts=4 sw=4 et :*/
