package au.id.tedp.mapdroid;

import android.util.Log;
import java.util.Hashtable;

class MemoryTileCache implements TileCache {
    private Hashtable<TileIdentifier, Tile> tiles;

    public MemoryTileCache() {
        tiles = new Hashtable<TileIdentifier, Tile>();
    }

	synchronized public Tile getTile(int zoom, int x, int y) {
        TileIdentifier id = new TileIdentifier(zoom, x, y);
        return tiles.get(id);
	}

    synchronized public void add(Tile t) {
        TileIdentifier id = new TileIdentifier(
                t.getZoom(), t.getXTileNumber(), t.getYTileNumber());
        tiles.put(id, t);
    }

    private class TileIdentifier {
        private int zoom, x, y;

        public TileIdentifier(int zoom, int x, int y) {
            this.zoom = zoom;
            this.x = x;
            this.y = y;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            if (obj == null || obj.getClass() != this.getClass())
                return false;

            TileIdentifier ti = (TileIdentifier) obj;

            if (ti.x == x && ti.y == y && ti.zoom == zoom)
                return true;

            return false;
        }

        public int hashCode() {
            return (x << 16 | y);
        }
    }
}

/* vim: set ts=4 sw=4 et :*/
