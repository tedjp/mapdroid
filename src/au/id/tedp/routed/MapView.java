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

class MapView extends View /*implements GestureDetector.SimpleOnGestureListener*/ {
    private TileManager tileManager;
    private int zoom = 15;
    private float mLat, mLong;

    // TODO: Implement onFling

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY)
    {
        // Canvas.translate()
        return false; // Unhandled, for now
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        tileManager = new TileManager();
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

    public boolean onTouchEvent(MotionEvent event) {
        // XXX Hack: testing left/right scrolling
        if (event.getX() < -1)
            mLong -= 0.002;
        else if (event.getX() > 1)
            mLong += 0.002;

        invalidate();
        return true;
    }
}

/* vim: set ts=4 sw=4 et :*/
