package com.mayhew3.drafttower.client.myroster;

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.UIObject;

/**
 * A {@link CellTable} which generates debug IDs for its cells.
 */
public class DebugIdCellTable<T> extends CellTable<T> {
  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    for (int row = 0; row < getRowCount() + 1; row++) {
      TableRowElement rowElement;
      if (row == 0) {
        rowElement = getTableHeadElement().getRows().getItem(0);
      } else {
        rowElement = getRowElement(row - 1);
      }
      for (int col = 0; col < getColumnCount(); col++) {
        UIObject.ensureDebugId(rowElement.getCells().getItem(col),
            baseID + "-" + row + "-" + col);
      }
    }
  }
}