package au.id.tedp.mapdroid;

// temporary
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import java.net.URL;

class TileDownloader implements Runnable {
    private Tile tile;
    private Messenger recipientMessenger;
    private TileCache recipientCache;

    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = -1;

    public TileDownloader(Tile tile, Messenger replyTo, MemoryTileCache cacheTo) {
        this.tile = tile;
        this.recipientMessenger = replyTo;
        this.recipientCache = cacheTo;
    }

    public static Message buildNotification(Tile tile, boolean success) {
        Message response = Message.obtain();
        response.what = success ? RESULT_OK : RESULT_ERROR;
        response.obj = tile;

        return response;
    }

    public void run() {
        boolean succeeded = false;

        try {
            URL url = new URL(tile.getUrl());

            tile.setBitmap(BitmapFactory.decodeStream(url.openStream()));

            recipientCache.add(tile);
            succeeded = true;
        }
        catch (Exception e) {
            Log.e("Mapdroid BitmapDownloader", e.getMessage());
        }
        finally {
            try {
                Message response = buildNotification(tile, succeeded);
                recipientMessenger.send(response);
            }
            catch (android.os.RemoteException e) {
                Log.e("Mapdroid BitmapDownloader", e.getMessage());
            }
        }
    }

}

/* vim: set ts=4 sw=4 et :*/
