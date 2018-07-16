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
package thebob.ja2maptool.model;

import thebob.assetloader.tileset.Tile;
import thebob.ja2maptool.util.tilesearch.TileSearchResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author the_bob
 */
public class TileMapping {

  int sourceType;
  int sourceIndex;
  int targetType;
  int targetIndex;
  MappingMode mappingMode = MappingMode.None;
  Map<Double, Tile> autoMapSuggestions = new HashMap<Double, Tile>();
  TileSearchResult autoMapResult = null;

  public TileMapping(int sourceType, int sourceIndex, int targetType, int targetIndex) {
    this.sourceType = sourceType;
    this.sourceIndex = sourceIndex;
    this.targetType = targetType;
    this.targetIndex = targetIndex;
  }

  public int getSourceType() {
    return sourceType;
  }

  public void setSourceType(int sourceType) {
    this.sourceType = sourceType;
  }

  public int getSourceIndex() {
    return sourceIndex;
  }

  public void setSourceIndex(int sourceIndex) {
    this.sourceIndex = sourceIndex;
  }

  public int getTargetType() {
    return targetType;
  }

  public void setTargetType(int targetType) {
    this.targetType = targetType;
  }

  public int getTargetIndex() {
    return targetIndex;
  }

  public void setTargetIndex(int targetIndex) {
    this.targetIndex = targetIndex;
  }

  public MappingMode getMappingMode() {
    return mappingMode;
  }

  public void setMappingMode(MappingMode mappingMode) {
    this.mappingMode = mappingMode;
  }

  @Override
  public String toString() {
    return "TileMapping{" + "sourceType=" + sourceType + ", sourceIndex=" + sourceIndex + ", targetType=" + targetType + ", targetIndex=" + targetIndex + '}';
  }

  public TileSearchResult getAutoMapResult() {
    return autoMapResult;
  }

  public void setAutoMapResult(TileSearchResult bestTile) {
    autoMapResult = bestTile;
  }

  public enum MappingMode {
    None,
    SameFile,
    MatchedFile,
    AutoMatched,
    Manual
  }

}
