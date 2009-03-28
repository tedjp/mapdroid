package au.id.tedp.routed;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.lang.Float;

class MapView extends View {
    private TileServer tileServer;
    private int zoom = 16;
    private float mLat, mLong;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        tileServer = new TileServer();
    }

    public void setCenter(float lat, float lon) {
        Log.d("Routed", "setCenter called");
        mLat = lat;
        mLong = lon;
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        Log.d("Routed", "redrawing");

        canvas.drawBitmap(
                tileServer.getTile(zoom, mLat, mLong),
                null,
                // Scale the bitmap to fit. Probably looks awful.
                new Rect(0, 0, getWidth(), getHeight()),
                null);
    }
}

/* vim: set ts=4 sw=4 et :*/
