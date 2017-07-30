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
package thebob.assetloader.map.structures.legacy;

import thebob.assetloader.map.helpers.AutoLoadingMapStruct;

/**
 *
 * @author the_bob
 */
public class OLD_EXITGRID extends AutoLoadingMapStruct {
    
    public final Signed16 iMapIndex = new Signed16(); //dnl ch86 170214 not exist in v1.12, add to simplify load
    public final Signed16 usGridNo = new Signed16(); // Sweet spot for placing mercs in new sector.
    public final Unsigned8 ubGotoSectorX = new Unsigned8();
    public final Unsigned8 ubGotoSectorY = new Unsigned8();
    public final Unsigned8 ubGotoSectorZ = new Unsigned8();

    @Override
    public int getOffsetAdjustment() {
        return -1;
    }
    
}