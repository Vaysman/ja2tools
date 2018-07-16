/*
 * The MIT License
 *
 * Copyright 2017 starcatter.
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
package thebob.ja2maptool.util.tilesearch.histogram;

import thebob.assetloader.tileset.Tile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author the_bob
 */
public class TileHistogramCache {

  Map<Integer, double[][]> tileHistograms = new HashMap<Integer, double[][]>();
  Map<Integer, Tile> tiles = new HashMap<Integer, Tile>();

  public TileHistogramCache(Tile[] tiles, int histogramSize) {

    for (Tile tile : tiles) {
      tileHistograms.put(tile.getIndex(), TileHistogramComparator.getHistogram(tile, histogramSize));
      this.tiles.put(tile.getIndex(), tile);
    }
  }

  public Map<Integer, double[][]> getHistograms() {
    return tileHistograms;
  }

  public Tile getTile(int index) {
    return tiles.get(index);
  }
}
