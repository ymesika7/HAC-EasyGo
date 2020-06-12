package com.example.haceasygo.Model;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomMapTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
    private final static int NOT_SET = 42000; //high randum number

    private AssetManager mAssets;
    private int floorLevel = NOT_SET;
    private int building = NOT_SET;

    // Set up array of the required tile zone by zoom level
    private static final SparseArray<Rect> TILE_ZOOMS = new SparseArray<Rect>() {{
        put(18, new Rect(156718, 106640, 156720, 106641));
        put(19, new Rect(313437, 213280, 313440, 213283));
        put(20, new Rect (626874,426561,626880,426566));
        put(21, new Rect(1253748,853122,1253760,853132 ));
    }};

    /** Constructor
     * @param assets activity context
     * @param floor required floor
     * @param building required building
     */
    public CustomMapTileProvider(AssetManager assets, int floor, int building) {
        this.building = building;
        floorLevel = floor;
        mAssets = assets;
    }

    /** Get specific tile
     * @param x x index of tile
     * @param y y index of tile
     * @param zoom required zoom level
     */
    public Tile getTile(int x, int y, int zoom) {
        if (hasTile(x, y, zoom)) {
            byte[] image = readTileImage(x, y, zoom);
            return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
        } else {
            return NO_TILE;
        }
    }

    /** Check if specific tile is exist
     * @param x x index of tile
     * @param y y index of tile
     * @param zoom required zoom level
     */
    private boolean hasTile(int x, int y, int zoom) {
        Rect b = TILE_ZOOMS.get(zoom);
        return b == null ? false : (b.left <= x && x <= b.right && b.top <= y && y <= b.bottom);
    }

    /** Get full tile image by definition zone
     * @param x x index of image
     * @param y y index of image
     * @param zoom required zoom level
     */
    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {
            in = mAssets.open(getTileFileName(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    /** Get tiles image directory path
     * @param x x index of image
     * @param y y index of image
     * @param zoom required zoom level
     */
    private String getTileFileName(int x, int y, int zoom) {
        if(building != NOT_SET )
            zoom = zoom * building;

        if(floorLevel == NOT_SET )
            return (zoom + "/" + x + "/" + y + ".png");
        else if(floorLevel == 0)
            return ((zoom*10) + "/" + x + "/" + y + ".png");
        else if(floorLevel == 1)
            return ((zoom*11) + "/" + x + "/" + y + ".png");
        else if(floorLevel == 2)
            return ((zoom*12) + "/" + x + "/" + y + ".png");
        else if(floorLevel == 3)
            return ((zoom*13) + "/" + x + "/" + y + ".png");
        else if(floorLevel == 4)
            return ((zoom*15) + "/" + x + "/" + y + ".png"); ///its 15 not 14 !!!!
        else //then floorLevel = -1 or floorLevel = 4
            return ((zoom*16) + "/" + x + "/" + y + ".png");
    }
}
