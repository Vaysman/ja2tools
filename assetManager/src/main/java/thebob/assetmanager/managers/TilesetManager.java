/* 
 * The MIT License
 *
 * Copyright 2017 the_bob.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package thebob.assetmanager.managers;

import ja2.xml.tilesets.TilesetDef;
import ja2.xml.tilesets.TilesetList;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import thebob.assetloader.dat.tileset.TilesetDataFileLoader;
import thebob.assetloader.dat.tileset.data.TilesetDataFile;
import thebob.assetloader.tileset.Tileset;
import thebob.assetloader.tileset.TilesetLoader;
import thebob.assetloader.vfs.VFSConfig;
import thebob.assetloader.xml.XmlLoader;
import thebob.assetmanager.AssetManager;

/**
 *
 * @author the_bob
 */
public class TilesetManager extends VFSContextBoundManager {

    Map<Integer, Tileset> tilesets = new HashMap<Integer, Tileset>();
    private short numTilesets;
    private short numFiles;

    public TilesetManager(AssetManager context) {
	super(context);
    }

    @Override
    public boolean init() {
	VFSConfig vfs = context.getVfs();

	if (vfs == null) {
	    return false;
	}

	if (vfs.getUseXmlTileset()) {
	    XmlLoader xml = context.getXml();
	    if (xml == null) {
		return false;
	    }

	    TilesetList tilesetDefs = xml.getTilesets().getTilesets();
	    if (tilesetDefs == null) {
		return false;
	    }

	    numFiles = tilesetDefs.getNumFiles();
	    numTilesets = tilesetDefs.getNumTilesets();

	    // System.out.println("thebob.assetloader.vfs.TilesetManager.init(): numFiles=" + numFiles + ", numTilesets=" + numTilesets);
	    if (numFiles == 0 || numTilesets == 0) {
		return false;
	    }

	    TilesetLoader loader = new TilesetLoader(vfs, numFiles);

	    for (TilesetDef tileDef : tilesetDefs.getTileset()) {
		Tileset tileset = loader.loadTilesetFromXmlDef(tileDef);
		tilesets.put((int) tileDef.getIndex(), tileset);
	    }
	} else {
	    ByteBuffer tilesetDefs = vfs.getFile("\\BinaryData\\JA2set.dat");
	    TilesetDataFile tileData = TilesetDataFileLoader.load(tilesetDefs);

	    numTilesets = (short) tileData.tilesetCount;
	    numFiles = (short) tileData.filesPerTileset;

	    TilesetLoader loader = new TilesetLoader(vfs, numFiles);

	    for (int i = 0; i < tileData.tilesetCount; i++) {
		Tileset tileset = loader.loadTilesetFromData(tileData.tilesets.get(i));
		tilesets.put(i, tileset);
	    }

	}

	return true;
    }

    public Tileset getTileset(int tilesetId) {
	return tilesets.get(tilesetId);
    }

    public Set<Integer> getTilesetIds() {
	return tilesets.keySet();
    }

    public short getNumTilesets() {
	return numTilesets;
    }

    public short getNumFiles() {
	return numFiles;
    }

    @Override
    public String toString() {
	return "TilesetManager{" + "tilesets=" + tilesets.size() + "}";
    }

}