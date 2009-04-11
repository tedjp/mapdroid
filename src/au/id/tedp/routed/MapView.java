package au.id.tedp.routed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
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
    // These are floats on purpose so we can derive the center *pixel*
    // from the fractional tile number
    private float centerTileX, centerTileY;
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

    public void recalculateCenterPixel() {
        centerTileX = TileSet.getXTileNumberAsFloat(zoom, mLong);
        centerTileY = TileSet.getYTileNumberAsFloat(zoom, mLat);

        Log.d("Routed", String.format("recalculated X pixel %f from longitude %f, Y pixel %f from latitude %f",
                    centerTileX, mLong, centerTileY, mLat));
    }

    public void recalculateCoords() {
        mLat = TileSet.getLatitude(zoom, centerTileY);
        mLong = TileSet.getLongitude(zoom, centerTileX);

        Log.d("Routed", String.format("recalculated latitude %f from %fpx, longitude %f from %fpx",
                    mLat, centerTileY, mLong, centerTileX));
    }

    public void onMove(float pixelsX, float pixelsY) {
        Log.d("Routed", String.format("onMove called,X: %fpx, Y: %fpx", pixelsX, pixelsY));
        centerTileX += pixelsX / tileManager.getTileSize();
        centerTileY += pixelsY / tileManager.getTileSize();
        recalculateCoords();
        invalidate();
    }

    public void setCenterPixels(float pixelX, float pixelY) {
        Log.d("Routed", "setCenterPixels called");
    }

    public void setCenterCoords(float lat, float lon) {
        Log.d("Routed", "setCenterCoords called");
        mLat = lat;
        mLong = lon;
        recalculateCenterPixel();
        invalidate();
    }

    // XXX: Only recalculate this when setCenter() is called, or the Canvas size changes
    // X Coordinate on the Canvas where (0,0) (top left) of the Center tile should be drawn
    protected Point getCenterTileOrigin(Canvas c) {
        Rect clipBounds = c.getClipBounds();
        int x, y;
        // Center of visible canvas:
        x = (clipBounds.right - clipBounds.left) / 2 + clipBounds.left;
        y = (clipBounds.bottom - clipBounds.top) / 2 + clipBounds.top;
        x -= TileSet.getXPixel(zoom, mLong, tileManager.getTileSize());
        y -= TileSet.getYPixel(zoom, mLat, tileManager.getTileSize());
        return new Point(x, y);
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
        Point centerTileOrigin = getCenterTileOrigin(canvas);

        int thisTileOriginX = centerTileOrigin.x +
            (tile.getXTileNumber() - TileSet.getXTileNumber(zoom, mLong)) * tileSize;

        int thisTileOriginY = centerTileOrigin.y +
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
