package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableTolerance;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTable;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

/**
 * Class description...
 */
abstract class PlayerTable<T> extends DragAndDropCellTable<T> implements
    DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String table();
      String dragHelper();
      String dropHover();
    }

    @Source("PlayerTable.css")
    Css css();
  }

  protected static final Resources.Css BASE_CSS = ((Resources) GWT.create(Resources.class)).css();

  protected final EventBus eventBus;

  public PlayerTable(final EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    // TODO: limit to status updates that change player list?
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  protected void initDragging(DragAndDropColumn<T, String> column, DroppableFunction onDrop) {
    DraggableOptions draggableOptions = column.getDraggableOptions();
    Element helper = DOM.createDiv();
    helper.addClassName(BASE_CSS.dragHelper());
    draggableOptions.setHelper(helper);
    draggableOptions.setCursor(Cursor.ROW_RESIZE);
    draggableOptions.setRevert(RevertOption.ON_INVALID_DROP);

    DroppableOptions droppableOptions = column.getDroppableOptions();
    droppableOptions.setTolerance(DroppableTolerance.POINTER);
    droppableOptions.setOnOver(new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        getTRParent(dragAndDropContext).addClassName(BASE_CSS.dropHover());
      }
    });
    DroppableFunction removeHover = new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        getTRParent(dragAndDropContext).removeClassName(BASE_CSS.dropHover());
      }
    };
    droppableOptions.setOnOut(removeHover);
    droppableOptions.setOnDeactivate(removeHover);
    droppableOptions.setOnDrop(onDrop);
  }

  private static Element getTRParent(DragAndDropContext dragAndDropContext) {
    Element droppable = (Element) dragAndDropContext.getDroppable();
    while (!droppable.getTagName().equalsIgnoreCase("tr")
        && droppable.hasParentElement()) {
      droppable = (Element) droppable.getParentElement();
    }
    return droppable;
  }
}