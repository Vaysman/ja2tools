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

import thebob.ja2maptool.util.tilesearch.TileComparator;
import thebob.ja2maptool.util.tilesearch.com.telmomenezes.jfastemd.Feature2D;
import thebob.ja2maptool.util.tilesearch.com.telmomenezes.jfastemd.JFastEMD;
import thebob.ja2maptool.util.tilesearch.com.telmomenezes.jfastemd.Signature;
import java.util.HashMap;
import java.util.Map;
import thebob.assetloader.tileset.Tile;
import thebob.assetloader.tileset.Tileset;
import thebob.ja2maptool.scopes.mapping.TilesetMappingScope;
import thebob.ja2maptool.util.tilesearch.TileSearch;
import thebob.ja2maptool.util.tilesearch.TileSearchResult;
import thebob.ja2maptool.util.tilesearch.histogram.models.EMDHistogramComparator;
import thebob.ja2maptool.util.tilesearch.histogram.models.HistogramComparator;
import thebob.ja2maptool.util.tilesearch.histogram.models.SimpleHistogramComparator;

/**
 *
 * @author the_bob
 */
public class TileHistogramComparator implements TileComparator {

    public static double[][] getHistogram(Tile tile, int histogramSize) {
	return getHistogram(tile.getLoader().getImage(tile.getIndex()), tile.getLoader().getPalette(), histogramSize);
    }

    public static double[][] getHistogram(byte[] image, byte[][] palette, int histogramSize) {
	int histogramDivider = 256 / histogramSize;
	double[][] normalizedHistogram = new double[3][histogramSize];
	for (byte b : image) {
	    int index = b & 255;
	    normalizedHistogram[0][(palette[0][index] & 255) / histogramDivider]++;
	    normalizedHistogram[1][(palette[1][index] & 255) / histogramDivider]++;
	    normalizedHistogram[2][(palette[2][index] & 255) / histogramDivider]++;
	}
	for (int i = 0; i < histogramSize; i++) {
	    normalizedHistogram[0][i] /= (double) image.length;
	    normalizedHistogram[1][i] /= (double) image.length;
	    normalizedHistogram[2][i] /= (double) image.length;
	}
	return normalizedHistogram;
    }
    
    private double goodEnoughDistance = 0.005d;
    private double addResultDistance = 0.75d;

    public enum ComparatorType {
	Simple,
	EMD
    }

    int histogramSize;

    Tileset tileset;
    int files;

    HistogramComparator comparator;

    Map<Integer, TileHistogramCache> groups = new HashMap<Integer, TileHistogramCache>();

    public TileHistogramComparator(Tileset tileset, ComparatorType type, int histogramSize) {
	this.tileset = tileset;
	this.files = tileset.getFileCount();
	this.histogramSize = histogramSize;

	switch (type) {
	    case Simple:
		comparator = new SimpleHistogramComparator();
		break;
	    case EMD:
		comparator = new EMDHistogramComparator();
		break;
	    default:
		throw new AssertionError(type.name());
	}

	for (int i = 0; i < files; i++) {
	    groups.put(i, new TileHistogramCache(tileset.getTiles(i), histogramSize));
	}
    }

    public double[][] getHistogram(Tile tile) {
	return getHistogram(tile.getLoader().getImage(tile.getIndex()), tile.getLoader().getPalette(), histogramSize);
    }

    @Override
    public double compareTiles(Tile t1, Tile t2) {
	double[][] h1 = getHistogram(t1);
	double[][] h2 = getHistogram(t2);
	return comparator.compareHistograms(h1, h2);
    }

    @Override
    public TileSearchResult search(Tile tile) {
	TileSearchResult results = new TileSearchResult();

	double[][] sourceHistogram = getHistogram(tile);

	double bestMatch = Double.MAX_VALUE;
	int bestGroup = -1;
	int bestIndex = -1;

	
	if( TilesetMappingScope.getTileCategortyName( tile.getType() ).contains("SH") ){
	    // SimpleHistogramComparator.trim = 0;
	    return null; // trying to match shadows this way sucks, let's not bother
	} else {
	    // hack to make detecting tiles more accurate, but still get good results on shadow tiles
	    //SimpleHistogramComparator.trim = 1;
	}
	 
	for (Integer groupId : groups.keySet()) {
	    if( TilesetMappingScope.getTileCategortyName( groupId ).contains("SH") ){
		continue;   // Don't match against shadow tiles. The results aren't any good.
	    }
	    TileHistogramCache group = groups.get(groupId);

	    for (Integer tileIndex : group.getHistograms().keySet()) {
		double[][] targetHistogram = group.getHistograms().get(tileIndex);

		double result = comparator.compareHistograms(sourceHistogram, targetHistogram);
		if (result < bestMatch) {
		    bestMatch = result;
		    bestGroup = groupId;
		    bestIndex = tileIndex;

		    if (bestMatch <= addResultDistance) {
			results.getResults().add(group.getTile(bestIndex));
			results.getResultValues().add(bestMatch);
		    }

		    if (bestMatch <= goodEnoughDistance) {
			break;
		    }
		}
	    }
	    if (bestMatch <= goodEnoughDistance) {
		break;
	    }
	}

	results.setBestResult(bestMatch);
	return results;
    }

}
