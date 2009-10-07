package au.id.tedp.mapdroid;

interface TileSource {
    public Tile getTile(int zoom, int x, int y);
}

/* vim: set ts=4 sw=4 et :*/
