package au.id.tedp.routed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import java.lang.Float;

class MapView extends View {
    private TileManager tileManager;
    private int zoom = 15;
    private float mLat, mLong;
    private MotionHandler motionHandler;
    private GestureDetector gestureDetector;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        tileManager = new TileManager();
        motionHandler = new MotionHandler(this);
        gestureDetector = new GestureDetector(motionHandler);
        gestureDetector.setIsLongpressEnabled(false);
        setMinimumHeight(256);
        setMinimumWidth(256);
    }

    class MotionHandler extends GestureDetector.SimpleOnGestureListener {
        MapView owner;

        public MotionHandler(MapView owner) {
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

    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);

        return true;
    }

    public void onMove(float pixelsX, float pixelsY) {
        Log.d("Routed", "onMove called");
        if (pixelsX < 0)
            mLat -= 0.002;
        else if (pixelsX > 0)
            mLat += 0.002;

        if (pixelsY < 0)
            mLong -= 0.002;
        else if (pixelsY > 0)
            mLong += 0.002;

        invalidate();
    }

    public void setCenter(float lat, float lon) {
        Log.d("Routed", "setCenter called");
        mLat = lat;
        mLong = lon;
        invalidate();
    }

    // XXX: Only recalculate this when setCenter() is called, or the Canvas size changes
    // X Coordinate on the Canvas where (0,0) (top left) of the Center tile should be drawn
    protected int getCenterTileOriginX(Canvas c) {
        int x;
        x = c.getWidth() / 2; // Center of canvas
        x -= TileSet.getXPixel(zoom, mLong, tileManager.getTileSize());
        return x - 160; // FIXME: World's biggest hack because I don't know how to make my view smaller
    }
    // Y Coordinate on the Canvas where (0,0) (top left) of the Center tile should be drawn
    protected int getCenterTileOriginY(Canvas c) {
        int y;
        y = c.getHeight() / 2;
        y -= TileSet.getYPixel(zoom, mLat, tileManager.getTileSize());
        return y - 220; // FIXME: World's biggest hack because I don't know how to make my view smaller
    }

    /**
      Returns true if the tile was drawn on the canvas, else false.
      */
    protected boolean drawTileOnCanvas(Tile tile, Canvas canvas) {
        if (tile.getZoom() != zoom) {
            // Probably an old request. Don't draw it.
            Log.d("Routed",
                    String.format("Ignoring tile with zoom level %d, current zoom level is %d",
                        tile.getZoom(), zoom));
            return false;
        }

        int tileSize = tileManager.getTileSize(); // Keep locally

        int thisTileOriginX = getCenterTileOriginX(canvas) +
            (tile.getXTileNumber() - TileSet.getXTileNumber(zoom, mLong)) * tileSize;

        int thisTileOriginY = getCenterTileOriginY(canvas) +
            (tile.getYTileNumber() - TileSet.getYTileNumber(zoom, mLat)) * tileSize;

        // XXX: Does drawBitmap accept negative destination co-ords? If not we will
        // have to determine the subset of the Bitmap that shall be copied and pass
        // it as the first arg. (I hope not)
        canvas.drawBitmap(tile.getBitmap(), null,
                new Rect(thisTileOriginX, thisTileOriginY,
                    thisTileOriginX + tileSize, thisTileOriginY + tileSize), null);

        return true;
    }

    /**
      Returns an array of tile X-numbers that must be retrieved
      to fill the given canvas.
    public Array<int> tilesX(Canvas canvas) {

    }
    */

    public void onDraw(Canvas canvas) {
        Log.d("Routed", String.format("redrawing canvas of size %dx%d", canvas.getWidth(), canvas.getHeight()));
        Tile centerTile, rightTile;

        // FIXME: For now we just grab the center tile
        try {
            centerTile = tileManager.getTile(zoom, mLat, mLong);
            rightTile = tileManager.getTile(zoom, centerTile.getXTileNumber() + 1, centerTile.getYTileNumber());
        } catch (java.io.IOException e) {
            // TODO: Load a default "tile unavailable" tile
            // For now, just return.
            return;
        }
        if (drawTileOnCanvas(centerTile, canvas) != true)
            Log.e("Routed", "Failed to draw center tile");
        if (drawTileOnCanvas(rightTile, canvas) != true)
            Log.e("Routed", "Failed to draw right tile");
    }
}

/* vim: set ts=4 sw=4 et :*/
