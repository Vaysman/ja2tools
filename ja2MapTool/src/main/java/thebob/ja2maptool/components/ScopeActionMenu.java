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
package thebob.ja2maptool.components;

import de.saxsys.mvvmfx.Scope;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import thebob.ja2maptool.ui.main.MainScreenView;

import java.util.List;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

/**
 * @author the_bob
 */
public class ScopeActionMenu<T extends Scope> extends Menu {

  String name;
  Class tabClass;
  T scope;
  List<T> scopeList;
  MenuItem openOption;
  MenuItem closeOption;
  Tab scopeTab;
  MainScreenView scopeTabParent;

  public ScopeActionMenu(String name, String description, T scope, List<T> scopeList, Tab scopeTab, MainScreenView scopeTabParent, Class tabClass) {
    super(name + " " + description);
    this.name = name;
    this.tabClass = tabClass;
    this.scope = scope;
    this.scopeTab = scopeTab;
    this.scopeList = scopeList;
    this.scopeTabParent = scopeTabParent;

    openOption = new MenuItem(scopeTab == null ? "Open tab" : "Goto tab");

    getItems().add(openOption);
    closeOption = new MenuItem("Discard");
    closeOption.setOnAction((ActionEvent event) -> {
      scopeList.remove(scope);
      if (scopeTabParent.getTabPane().getTabs().indexOf(scopeTab) != -1) {
        //TabPane tp = scopeTabParent.getTabPane();
        //Skin<? extends Skinnable> tps = tp.getSkin();
        //TabPaneSkin tpss = (TabPaneSkin)tps;
        //tpss.getBehavior().closeTab(scopeTab);

        scopeTab.getOnClosed().handle(new MouseEvent(MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 0, true, true, true, true, true, true, true, true, true, true, null));
      }
    });
    getItems().add(closeOption);
  }

  @Override
  public void show() {
    super.show();
    openOption.setText(((scopeTab == null) || (scopeTabParent.getTabPane().getTabs().indexOf(scopeTab) == -1)) ? "Open tab" : "Goto tab");

    if (scopeTab != null) {
      openOption.setOnAction((ActionEvent event) -> {
        if (scopeTabParent.getTabPane().getTabs().indexOf(scopeTab) == -1) {
          scopeTabParent.getTabPane().getTabs().add(scopeTab);
        }
        scopeTabParent.getTabPane().getSelectionModel().select(scopeTab);
      });
    } else {
      // tab was not created (loaded scope via map converter?)
      openOption.setOnAction((ActionEvent event) -> {
        scopeTabParent.addTab(tabClass, name, scope);
      });
    }
  }

}
