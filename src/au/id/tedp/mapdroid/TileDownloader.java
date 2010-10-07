/*
 * This file is part of Mapdroid.
 * Copyright 2009 Ted Percival <ted@tedp.id.au>.
 *
 * Mapdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Mapdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mapdroid. If not, see <http://www.gnu.org/licenses/>.
 */
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
