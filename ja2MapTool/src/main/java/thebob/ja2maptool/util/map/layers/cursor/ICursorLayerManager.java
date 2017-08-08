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
package thebob.ja2maptool.util.map.layers.cursor;

import javafx.scene.input.MouseButton;
import thebob.assetloader.tileset.Tileset;
import thebob.ja2maptool.util.compositor.SelectedTiles;
import thebob.ja2maptool.util.map.layers.base.ITileLayerGroup;

/**
 * Extended CursorLayer interface including ITileLayerGroup access
 *
 * @author the_bob
 */
public interface ICursorLayerManager extends ICursorLayerControls, ITileLayerGroup {

    public void init(int mapRows, int mapCols, Tileset tileset);

    void sendCursor(double dx, double dy, boolean controlDown, boolean shiftDown, boolean altDown);

    void sendClick(double dx, double dy, MouseButton button, boolean controlDown, boolean shiftDown, boolean altDown);
    
    void updateCursor();

    public void setWindow(int windowOffsetX, int windowOffsetY, double scale);

    public void setCanvasSize(int canvasX, int canvasY);

    public SelectedTiles getSelection();

    public void setPlacementPreview(SelectedTiles selection);

    public MapCursor getPlacementCursor();
    public MapCursor getMainCursor();

}