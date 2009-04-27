package au.id.tedp.mapdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import java.lang.Float;
import java.util.Vector;

class MapView extends View {
    private TileServer tileServer;
    private int zoom = 15;
    // These are floats on purpose so we can derive the center *pixel*
    // from the fractional tile number
    private float centerTileX, centerTileY;
    private float mLat, mLong;
    private MotionHandler motionHandler;
    private GestureDetector gestureDetector;
    private Vector<Vector<Tile>> visibleTiles;
    private MapViewHandler handler;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        tileServer = new TileServer();
        motionHandler = new MotionHandler(this);
        gestureDetector = new GestureDetector(motionHandler);
        gestureDetector.setIsLongpressEnabled(false);
        setMinimumHeight(256);
        setMinimumWidth(256);
        handler = new MapViewHandler();
    }

    public float getLatitude() {
        return mLat;
    }

    public float getLongitude() {
        return mLong;
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

    protected Handler getHandler() {
        return (Handler)handler;
    }

    public static class MapViewHandler extends Handler {
        public void handleMessage(Message msg) {
            Log.d("Mapdroid", String.format("Received message: %s", msg.toString()));
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
        Log.d("Mapdroid", String.format("onMove called,X: %fpx, Y: %fpx", pixelsX, pixelsY));
        centerTileX += pixelsX / tileServer.getTileSize();
        centerTileY += pixelsY / tileServer.getTileSize();
        recalculateCoords();
        invalidate();
    }

    public void setCenterPixels(float pixelX, float pixelY) {
        Log.d("Mapdroid", "setCenterPixels called");
    }

    public void setCenterCoords(float lat, float lon) {
        Log.d("Mapdroid", "setCenterCoords called");
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
        x -= TileSet.getXPixel(zoom, mLong, tileServer.getTileSize());
        y -= TileSet.getYPixel(zoom, mLat, tileServer.getTileSize());
        return new Point(x, y);
    }

    /**
      Returns true if the tile was drawn on the canvas, else false.
      */
    protected boolean drawTileOnCanvas(Tile tile, Canvas canvas) {
        if (tile.getZoom() != zoom) {
            // Probably an old request. Don't draw it.
            Log.d("Mapdroid",
                    String.format("Ignoring tile with zoom level %d, current zoom level is %d",
                        tile.getZoom(), zoom));
            return false;
        }

        int tileSize = tileServer.getTileSize(); // Keep locally
        Point centerTileOrigin = getCenterTileOrigin(canvas);

        int thisTileOriginX = centerTileOrigin.x +
            (tile.getXTileNumber() - TileSet.getXTileNumber(zoom, mLong)) * tileSize;

        int thisTileOriginY = centerTileOrigin.y +
            (tile.getYTileNumber() - TileSet.getYTileNumber(zoom, mLat)) * tileSize;

        canvas.drawBitmap(tile.getBitmap(), null,
                new Rect(thisTileOriginX, thisTileOriginY,
                    thisTileOriginX + tileSize, thisTileOriginY + tileSize), null);

        return true;
    }

    protected void requestVisibleTiles(Canvas c) {

    }

    public void onDraw(Canvas canvas) {
        Log.d("Mapdroid", String.format("redrawing canvas of size %dx%d", canvas.getWidth(), canvas.getHeight()));
        Tile centerTile, rightTile;

        canvas.drawColor(Color.LTGRAY);
        Rect clipbounds = canvas.getClipBounds();

        Log.d("Mapdroid", String.format("Clip bounds: %d,%d %d,%d", clipbounds.left, clipbounds.top, clipbounds.right, clipbounds.bottom));

        // FIXME: For now we just grab the center tile
        try {
            centerTile = tileServer.getTile(zoom, mLat, mLong);
            rightTile = tileServer.getTile(zoom, centerTile.getXTileNumber() + 1, centerTile.getYTileNumber());
        } catch (java.io.IOException e) {
            // TODO: Load a default "tile unavailable" tile
            // For now, just return.
            return;
        }
        if (drawTileOnCanvas(centerTile, canvas) != true)
            Log.e("Mapdroid", "Failed to draw center tile");
        if (drawTileOnCanvas(rightTile, canvas) != true)
            Log.e("Mapdroid", "Failed to draw right tile");
    }
}

/* vim: set ts=4 sw=4 et :*/
