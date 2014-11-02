package com.mayhew3.drafttower.client.players;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.UIObject;
import com.mayhew3.drafttower.shared.DraggableItem;

/**
 * Base class for tables supporting player drag and drop.
 */
public abstract class PlayerTable<T extends DraggableItem> extends CellTable<T>
    implements PlayerTableView<T> {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String table();
      String dragHelper();
      String dropHoverTop();
      String dropHoverBottom();

      int dragHelperXOffset();
      int dragHelperYOffset();
    }

    @Source("PlayerTable.css")
    Css css();
  }

  protected static final Resources.Css BASE_CSS = ((Resources) GWT.create(Resources.class)).css();

  private boolean isDragging;
  private int dragStartIndex;
  private Integer dragHoverIndex;
  private Runnable runAfterDrag;

  public PlayerTable(PlayerDataProvider<T> presenter,
      final PlayerDragController playerDragController) {
    playerDragController.addDropTarget(this);
    addDomHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        if (canStartDrag()) {
          isDragging = true;
          dragStartIndex = getRowIndex(event.getRelativeY(getElement()));
          event.preventDefault();
        }
      }
    }, MouseDownEvent.getType());
    addDomHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        if (isDragging) {
          T draggedItem = getVisibleItem(dragStartIndex);
          playerDragController.startDragging(
              draggedItem, getDragHelperContents(draggedItem),
              event.getClientX(), event.getClientY());
        }
        event.preventDefault();
        event.stopPropagation();
      }
    }, MouseMoveEvent.getType());
    addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        isDragging = false;
      }
    }, MouseUpEvent.getType());

    presenter.setView(this);
  }

  protected abstract SafeHtml getDragHelperContents(T draggedItem);

  public boolean isDropEnabled() {
    return true;
  }

  protected boolean canStartDrag() {
    return true;
  }

  public void onHover(MouseMoveEvent event) {
    onHoverOut();
    int relativeY = event.getRelativeY(getElement());
    int rowIndex = getRowIndex(relativeY);
    String hoverClass = isTopDrop(relativeY)
        ? BASE_CSS.dropHoverTop()
        : BASE_CSS.dropHoverBottom();
    getRowElement(rowIndex).addClassName(hoverClass);
    dragHoverIndex = rowIndex;
  }

  public void onHoverOut() {
    if (dragHoverIndex != null) {
      getRowElement(dragHoverIndex).removeClassName(BASE_CSS.dropHoverBottom());
      getRowElement(dragHoverIndex).removeClassName(BASE_CSS.dropHoverTop());
      dragHoverIndex = null;
    }
  }

  public abstract void onDrop(DraggableItem item, MouseUpEvent event);

  protected boolean isTopDrop(int relativeY) {
    int rowIndex = getRowIndex(relativeY);
    TableRowElement rowElement = getRowElement(rowIndex);
    return relativeY < rowElement.getOffsetTop() + rowElement.getOffsetHeight() / 2;
  }

  protected int getRowIndex(int relativeY) {
    int index = (relativeY - getHeaderHeight()) / getRowElement(0).getOffsetHeight();
    index = Math.max(index, 0);
    index = Math.min(index, getVisibleItemCount() - 1);
    return index;
  }

  public void dragFinished() {
    if (isDragging) {
      isDragging = false;
      if (runAfterDrag != null) {
        runAfterDrag.run();
        runAfterDrag = null;
      }
    }
  }

  @Override
  public void refresh() {
    if (isDragging) {
      runAfterDrag = new Runnable() {
        @Override
        public void run() {
          setVisibleRangeAndClearData(getVisibleRange(), true);
        }
      };
    } else {
      setVisibleRangeAndClearData(getVisibleRange(), true);
    }
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    for (int row = 0; row < getVisibleItemCount() + 1; row++) {
      TableRowElement rowElement;
      if (row == 0) {
        rowElement = getTableHeadElement().getRows().getItem(0);
      } else {
        rowElement = getRowElement(row - 1);
      }
      UIObject.ensureDebugId(rowElement, baseID + "-" + row);
      for (int col = 0; col < getColumnCount(); col++) {
        TableCellElement cell = rowElement.getCells().getItem(col);
        UIObject.ensureDebugId(cell, baseID + "-" + row + "-" + col);
        Element cellElement = cell.getFirstChildElement();
        if (cellElement.getChildCount() > 0) {
          Element cellChild = cellElement.getFirstChildElement();
          if (cellChild != null && cellChild.getTagName().equalsIgnoreCase("button")) {
            UIObject.ensureDebugId(cellChild, baseID + "-" + row + "-" + col + "-button");
          }
        }
      }
    }
  }
}