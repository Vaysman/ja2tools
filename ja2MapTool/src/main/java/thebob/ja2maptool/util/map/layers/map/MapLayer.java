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
package thebob.ja2maptool.util.map.layers.map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import thebob.assetloader.map.core.MapData;
import thebob.assetloader.map.core.components.IndexedElement;
import thebob.assetloader.tileset.Tileset;
import thebob.ja2maptool.util.compositor.SelectedTiles;
import thebob.ja2maptool.util.compositor.SelectionPlacementOptions;
import thebob.ja2maptool.util.map.events.MapEvent;
import thebob.ja2maptool.util.map.layers.base.TileLayer;
import thebob.ja2maptool.util.map.layers.base.TileLayerGroup;
import thebob.ja2maptool.util.map.layers.cursor.MapCursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static thebob.ja2maptool.util.map.MapUtils.checkContentFilters;

/**
 * @author the_bob
 */
public class MapLayer extends TileLayerGroup implements IMapLayerManager {

  MapData map;
  List<TileLayer> layers = new ArrayList<>();
  BooleanProperty render_limit = new SimpleBooleanProperty(true);
  BooleanProperty render_trim = new SimpleBooleanProperty(true);
  Short lastRoomNumber = null;
  BooleanProperty[] viewerButtons = null;
  private BooleanProperty[] displayButtons;

  @Override
  public Iterator<TileLayer> iterator() {
    return layers.iterator();
  }

  @Override
  public void loadMap(MapData map) {
    setLayerSize(map.getSettings().iColSize, map.getSettings().iRowSize);
    loadMapLayers(map);
    notifySubscribers(new MapEvent(MapEvent.ChangeType.MAP_LOADED));
  }

  private void loadMapLayers(MapData map) {
    layers.clear();
    this.map = map;

    limits = new int[]{
        map.getSettings().mapInfo.sEastGridNo.get(),
        map.getSettings().mapInfo.sWestGridNo.get(),
        map.getSettings().mapInfo.sNorthGridNo.get(),
        map.getSettings().mapInfo.sSouthGridNo.get()
    };

    for (int gridNo : limits) {
      System.out.println("Limit: " + gridNo + "( x=" + gridNoToCellX(gridNo) + ", y=" + gridNoToCellY(gridNo) + " )");
    }

    layers.add(new TileLayer(true, 0, 0, map.getLayers().landLayer));
    layers.add(new TileLayer(true, 0, 0, map.getLayers().objectLayer));
    layers.add(new TileLayer(true, 0, 0, map.getLayers().structLayer));
    layers.add(new TileLayer(true, 0, 0, map.getLayers().shadowLayer));
    layers.add(new TileLayer(false, 0, -50, map.getLayers().roofLayer));
    layers.add(new TileLayer(false, 0, -50, map.getLayers().onRoofLayer));

    bindLayerButtons();
  }

  @Override
  public void setMapTileset(Tileset tileset) {
    setTileset(tileset);
  }

  @Override
  public SelectedTiles getTilesForSelection(SelectedTiles selectedTiles) {

    int[] selectedCells = selectedTiles.getSelectedCells();
    int selectionSize = selectedCells.length;

    IndexedElement[][][] selectionLayers = new IndexedElement[layers.size()][selectionSize][];

    for (int L = 0; L < layers.size(); L++) {
      TileLayer layer = layers.get(L);
      IndexedElement[][] mapLayer = layer.getTiles();
      for (int i = 0; i < selectionSize; i++) {
        selectionLayers[L][i] = mapLayer[selectedCells[i]];
      }
    }

    short[] selectedRoomNumbers = new short[selectionSize];
    short[] mapRoomNunbers = map.getInfo().getGusWorldRoomInfo();
    for (int i = 0; i < selectionSize; i++) {
      selectedRoomNumbers[i] = mapRoomNunbers[selectedCells[i]];
    }

    selectedTiles.setLayers(selectionLayers);
    selectedTiles.setRoomNumbers(selectedRoomNumbers);

    selectedTiles.setName("Selection, " + selectionSize + " cells.");

    return selectedTiles;
  }

  private void scanRoomNumbers() {
    lastRoomNumber = Short.MIN_VALUE;
    short[] mapRoomNunbers = map.getInfo().getGusWorldRoomInfo();
    for (short n : mapRoomNunbers) {
      if (lastRoomNumber < n) {
        lastRoomNumber = n;
        System.out.println("thebob.ja2maptool.util.renderer.map.MapLayer.scanRoomNumbers(): " + n);
      }
    }
  }

  @Override
  public void appendTiles(MapCursor placement, SelectedTiles inputSelection, SelectionPlacementOptions options) {

    // clone this snippet in case we... do things to it
    SelectedTiles selection = new SelectedTiles(inputSelection);

    if (lastRoomNumber == null) {
      scanRoomNumbers();
    }

    short[] selectedRoomNumbers = selection.getRoomNumbers();
    int selectionSize = selectedRoomNumbers.length;
    int[] targetCells = new int[selectionSize];

    int cursorWidth = selection.getWidth();
    int cursorHeight = selection.getHeight();

    int startX = placement.getCellX() - cursorWidth / 2;
    int startY = placement.getCellY() - cursorHeight / 2;

    boolean[] placementOptions = options.getAsArray();

    for (int L = 0; L < layers.size(); L++) {

      if (placementOptions[L] == false) {
        if (checkContentFilters(L, selection, options) == false) {
          continue;
        }
      }

      int i = 0;
      for (int x = startX; x < startX + cursorWidth; x++) {
        for (int y = startY; y < startY + cursorHeight; y++) {
          int targetCell = rowColToPos(y, x);
          targetCells[i] = targetCell;

          if (selection.getLayers()[L][i] != null) {
            layers.get(L).getTiles()[targetCell] = selection.getLayers()[L][i];
          }

          i++;
        }
      }
    }

    // update room numbers in placed stuff
    short[] mapRoomNunbers = map.getInfo().getGusWorldRoomInfo();
    Map<Short, Short> numbersRemap = new HashMap<Short, Short>();

    for (int i = 0; i < selectionSize; i++) {
      short selectedRoomNumber = selectedRoomNumbers[i];

      if (selectedRoomNumber != 0) {
        if (numbersRemap.containsKey(selectedRoomNumber)) {
          selectedRoomNumber = numbersRemap.get(selectedRoomNumber);
        } else {
          numbersRemap.put(selectedRoomNumber, ++lastRoomNumber);
          System.out.println("thebob.ja2maptool.util.renderer.map.MapLayer.appendTiles() mapped room nr " + selectedRoomNumber + " to " + lastRoomNumber);
          selectedRoomNumber = lastRoomNumber;
        }
      }

      mapRoomNunbers[targetCells[i]] = selectedRoomNumber;
    }

    notifySubscribers(new MapEvent(MapEvent.ChangeType.LAYER_ALTERED));
  }

  @Override
  public void setMapLayerButtons(BooleanProperty[] viewerButtons) {
    this.viewerButtons = viewerButtons;
    bindLayerButtons();
  }

  @Override
  public void setMapDisplayButtons(BooleanProperty[] displayButtons) {
    this.displayButtons = displayButtons;
    bindDisplayButtons();
  }

  void bindLayerButtons() {
    if (viewerButtons != null) {
      for (int i = 0; i < layers.size(); i++) {
        viewerButtons[i].set(layers.get(i).isEnabled());
        layers.get(i).getEnabledProperty().bindBidirectional(viewerButtons[i]);
        layers.get(i).getEnabledProperty().addListener(event -> {
          notifySubscribers(new MapEvent(MapEvent.ChangeType.LAYER_ALTERED));
        });
      }
    }
  }

  private void bindDisplayButtons() {
    displayButtons[0].set(render_limit.getValue());
    displayButtons[1].set(render_trim.getValue());

    render_limit.bindBidirectional(displayButtons[0]);
    render_trim.bindBidirectional(displayButtons[1]);

    render_limit.addListener(event -> {
      notifySubscribers(new MapEvent(MapEvent.ChangeType.LAYER_ALTERED));
    });

    render_trim.addListener(event -> {
      notifySubscribers(new MapEvent(MapEvent.ChangeType.LAYER_ALTERED));
    });
  }

  @Override
  public String toString() {
    return "MapLayer{" + super.toString() + '}';
  }


  @Override
  public boolean limitDrawArea() {
    return render_limit.get();
  }

  @Override
  public boolean trimEdges() {
    return render_trim.get();
  }

}
