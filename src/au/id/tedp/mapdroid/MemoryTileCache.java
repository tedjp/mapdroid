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
