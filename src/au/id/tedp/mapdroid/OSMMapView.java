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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import java.lang.Float;
import java.util.ArrayList;

class OSMMapView extends View implements GpsStatus.Listener, LocationListener {
    private TileServer tileServer;
    private int tileSize;
    private int zoom = 15;
    // These are floats on purpose so we can derive the center *pixel*
    // from the fractional tile number
    private float centerTileX, centerTileY;
    private float mLat, mLong;
    private MotionHandler motionHandler;
    private GestureDetector gestureDetector;
    private ArrayList<Tile> visibleTiles;
    private MapViewHandler handler;
    public Messenger messenger;

    public OSMMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        visibleTiles = new ArrayList<Tile>(6);
        tileServer = new TileServer();
        tileSize = tileServer.getTileSize();

        motionHandler = new MotionHandler(this);
        gestureDetector = new GestureDetector(motionHandler);
        gestureDetector.setIsLongpressEnabled(false);
        // XXX: Unnecessary, these should be removed unless they are advisory
        setMinimumHeight(256);
        setMinimumWidth(256);
        handler = new MapViewHandler();
        messenger = new Messenger(handler);
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int newzoom) {
        if (newzoom < 0 || newzoom > tileServer.getMaxZoom())
            return;

        zoom = newzoom;
        recalculateCenterPixel();
        getVisibleTiles();
    }

    public float getLatitude() {
        return mLat;
    }

    public float getLongitude() {
        return mLong;
    }

    class MotionHandler extends GestureDetector.SimpleOnGestureListener {
        OSMMapView owner;

        public MotionHandler(OSMMapView owner) {
            super();
            this.owner = owner;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY)
        {
            owner.onMove(velocityX, velocityY);
            return true;
        }
    }

    // XXX: Pretty sure this math is bogus
    public int getLeftTileNumber() {
        return ((int) centerTileX) - getWidth() / 2 / tileSize - 1;
    }

    public int getRightTileNumber() {
        return ((int) centerTileX) + getWidth() / 2 / tileSize + 1;
    }

    public int getTopTileNumber() {
        return ((int) centerTileY) - getHeight() / 2 / tileSize - 1;
    }

    public int getBottomTileNumber() {
        return ((int) centerTileY) + getHeight() / 2 / tileSize + 1;
    }

    // XXX: OK for this to be non-static? I guess.
    public class MapViewHandler extends Handler {
        public void handleMessage(Message msg) {
            Log.d("Mapdroid", String.format("Received message: %s", msg.toString()));

            if (msg.what == TileDownloader.RESULT_OK) {
                visibleTiles.add((Tile)msg.obj);
                invalidate();
            }

            // Causes error.
            //msg.recycle();
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Just be whatever size we're told is available.
        // It might be nicer to adhere to the SuggestedMinimum dimensions.
        setMeasuredDimension(
                View.MeasureSpec.getSize(widthMeasureSpec),
                View.MeasureSpec.getSize(heightMeasureSpec));
    }

    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);

        return true;
    }

    public void recalculateCenterPixel() {
        centerTileX = TileSet.getXTileNumberAsFloat(zoom, mLong);
        centerTileY = TileSet.getYTileNumberAsFloat(zoom, mLat);

        Log.d("Mapdroid", String.format("recalculated X pixel %f from longitude %f, Y pixel %f from latitude %f",
                    centerTileX, mLong, centerTileY, mLat));
    }

    public void recalculateCoords() {
        mLat = TileSet.getLatitude(zoom, centerTileY);
        mLong = TileSet.getLongitude(zoom, centerTileX);

        Log.d("Mapdroid", String.format("recalculated latitude %f from %fpx, longitude %f from %fpx",
                    mLat, centerTileY, mLong, centerTileX));
    }

    public void onMove(float pixelsX, float pixelsY) {
        // FIXME: (1) Don't allow scrolling so far off the map that it disappears
        // FIXME: (2) Wrap the map to the left/right so it is continuous
        Log.d("Mapdroid", String.format("onMove called,X: %fpx, Y: %fpx", pixelsX, pixelsY));
        centerTileX += pixelsX / tileServer.getTileSize();
        centerTileY += pixelsY / tileServer.getTileSize();
        recalculateCoords();

        Log.d("Mapdroid", String.format("Left tile number: %d, right tile number: %d",
                    getLeftTileNumber(), getRightTileNumber()));
        Log.d("Mapdroid", String.format("Top tile number: %d, bottom tile number: %d",
                    getTopTileNumber(), getBottomTileNumber()));

        getVisibleTiles();
    }

    public void setCenterPixels(float pixelX, float pixelY) {
        Log.d("Mapdroid", "setCenterPixels called (currently does nothing)");
    }

    public void setCenterCoords(float lat, float lon) {
        Log.d("Mapdroid", String.format("setCenterCoords: %f %f", lon, lat));
        mLat = lat;
        mLong = lon;
        recalculateCenterPixel();
        getVisibleTiles();
    }

    private void getVisibleTiles() {
        int centerx, centery, x, y;

        visibleTiles.clear();

        centerx = TileSet.getXTileNumber(zoom, mLong);
        centery = TileSet.getYTileNumber(zoom, mLat);

        // Center tile is most important; grab it first
        tileServer.requestTile(zoom, centerx, centery, messenger);

        for (x = getLeftTileNumber(); x <= getRightTileNumber(); ++x) {
            for (y = getTopTileNumber(); y <= getBottomTileNumber(); ++y) {
                // Don't re-request the center tile
                if (x != centerx || y != centery)
                    tileServer.requestTile(zoom, x, y, messenger);
            }
        }
    }

    // XXX: Only recalculate this when setCenter() is called, or the Canvas size changes
    // X Coordinate on the Canvas where (0,0) (top left) of the Center tile should be drawn
    protected Point getCenterTileOrigin(Canvas c) {
        Rect clipBounds = c.getClipBounds();
        int x, y;
        // Center of visible canvas:
        x = (clipBounds.right - clipBounds.left) / 2 + clipBounds.left;
        y = (clipBounds.bottom - clipBounds.top) / 2 + clipBounds.top;
        x -= TileSet.getXPixel(zoom, mLong, tileServer.getTileSize());
        y -= TileSet.getYPixel(zoom, mLat, tileServer.getTileSize());
        return new Point(x, y);
    }

    /**
      Returns true if the tile was drawn on the canvas, else false.
FIXME: use exceptions, dummy
      */
    protected boolean drawTileOnCanvas(Tile tile, Canvas canvas) {
        if (tile.getZoom() != zoom) {
            // Probably an old request. Don't draw it.
            Log.d("Mapdroid",
                    String.format("Ignoring tile with zoom level %d, current zoom level is %d",
                        tile.getZoom(), zoom));
            return false;
        }

        Bitmap bmp = tile.getBitmap();

        if (bmp == null) {
            Log.d("Mapdroid", "Ignoring Tile without a Bitmap");
            return false;
        }

        Point centerTileOrigin = getCenterTileOrigin(canvas);

        int thisTileOriginX = centerTileOrigin.x +
            (tile.getXTileNumber() - TileSet.getXTileNumber(zoom, mLong)) * tileSize;

        int thisTileOriginY = centerTileOrigin.y +
            (tile.getYTileNumber() - TileSet.getYTileNumber(zoom, mLat)) * tileSize;

        canvas.drawBitmap(bmp, null,
                new Rect(thisTileOriginX, thisTileOriginY,
                    thisTileOriginX + tileSize, thisTileOriginY + tileSize), null);

        return true;
    }

    protected void drawVisibleTiles(Canvas canvas) {
        int i;

        for (i = 0; i < visibleTiles.size(); ++i) {
            drawTileOnCanvas(visibleTiles.get(i), canvas);
        }
    }

    public void onDraw(Canvas canvas) {
        if (visibleTiles.isEmpty()) {
            Log.w("Mapdroid", "No tiles, requesting visible tiles...");
            getVisibleTiles();
        } else {
            drawVisibleTiles(canvas);
        }
    }

    public void onGpsStatusChanged(int event) {
        Log.d("Mapdroid", String.format("GPS status changed, event: %d", event));
    }


    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.d("Mapdroid", "onLocationChanged()");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Mapdroid", "onProviderDisabled()");

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Mapdroid", "onProviderEnabled()");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Mapdroid", "onStatusChanged()");
    }

    public void startLocationUpdates(Context context) {
        LocationManager locmgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locmgr.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L, 0.0f, this);

        // XXX: Unnecessary
//        locmgr.addGpsStatusListener(this);
    }

    public void stopLocationUpdates(Context context) {
        LocationManager locmgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locmgr.removeUpdates(this);
    }
}

/* vim: set ts=4 sw=4 et :*/
