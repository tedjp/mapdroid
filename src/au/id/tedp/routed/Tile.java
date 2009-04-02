package au.id.tedp.routed;

import android.graphics.Bitmap;

class Tile {
    private Bitmap bmp;
    private int zoomLevel;
    private int x, y;

    public Tile(Bitmap src, int zoom, int x, int y) {
        bmp = src;
        zoomLevel = zoom;
        this.x = x;
        this.y = y;
    }

    public Bitmap getBitmap() {
        return bmp;
    }

    public int getZoom() {
        return zoomLevel;
    }

    public int getXTileNumber() {
        return x;
    }

    public int getYTileNumber() {
        return y;
    }
}

/* vim: set ts=4 sw=4 et :*/
