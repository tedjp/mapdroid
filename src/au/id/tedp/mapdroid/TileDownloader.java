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
    private Messenger recipient;

    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = -1;

    public TileDownloader(Tile tile, Messenger replyTo) {
        this.tile = tile;
        this.recipient = replyTo;
    }

    public void run() {
        Message response = Message.obtain();
        response.what = RESULT_ERROR;

        try {
            URL url = new URL(tile.getUrl());

            tile.setBitmap(BitmapFactory.decodeStream(url.openStream()));
            response.obj = tile;

            response.what = RESULT_OK;
        }
        catch (Exception e) {
            Log.e("Mapdroid BitmapDownloader", e.getMessage());
        }
        finally {
            try {
                recipient.send(response);
            }
            catch (android.os.RemoteException e) {
                Log.e("Mapdroid BitmapDownloader", e.getMessage());
            }
        }
    }

}

/* vim: set ts=4 sw=4 et :*/
