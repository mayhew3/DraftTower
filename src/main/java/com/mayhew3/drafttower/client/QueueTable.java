package com.mayhew3.drafttower.client;

import com.google.common.base.Joiner;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DequeuePlayerEvent;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.ReorderPlayerQueueEvent;
import com.mayhew3.drafttower.shared.QueueEntry;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableTolerance;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTable;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

/**
 * Table displaying players queue.
 */
public class QueueTable extends DragAndDropCellTable<QueueEntry> implements
    DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String table();
      String dragHelper();
      String dropHover();
    }

    @Source("QueueTable.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final EventBus eventBus;

  @Inject
  public QueueTable(QueueDataProvider dataProvider,
      final EventBus eventBus) {
    this.eventBus = eventBus;

    addStyleName(CSS.table());
    setPageSize(Integer.MAX_VALUE);

    addColumn(new IdentityColumn<QueueEntry>(new AbstractCell<QueueEntry>() {
      @Override
      public void render(Context context, QueueEntry value, SafeHtmlBuilder sb) {
        sb.append(context.getIndex() + 1);
      }
    }));

    DragAndDropColumn<QueueEntry, String> nameColumn =
        new DragAndDropColumn<QueueEntry, String>(new TextCell()) {
          @Override
          public String getValue(QueueEntry entry) {
            return entry.getPlayerName();
          }
        };
    initDragging(nameColumn);
    addColumn(nameColumn, "Player");

    DragAndDropColumn<QueueEntry, String> eligibilityColumn =
        new DragAndDropColumn<QueueEntry, String>(new TextCell()) {
          @Override
          public String getValue(QueueEntry entry) {
            return Joiner.on(", ").join(entry.getEligibilities());
          }
        };
    initDragging(eligibilityColumn);
    addColumn(eligibilityColumn, "Eligibility");

    Column<QueueEntry, String> removeColumn = new Column<QueueEntry, String>(new ButtonCell()) {
      @Override
      public String getValue(QueueEntry object) {
        return "Remove";
      }
    };
    addColumn(removeColumn);
    removeColumn.setFieldUpdater(new FieldUpdater<QueueEntry, String>() {
      @Override
      public void update(int index, QueueEntry entry, String value) {
        eventBus.fireEvent(new DequeuePlayerEvent(entry.getPlayerId()));
      }
    });

    addDragStartHandler(new DragStartEventHandler() {
      @Override
      public void onDragStart(DragStartEvent dragStartEvent) {
        QueueEntry entry = dragStartEvent.getDraggableData();
        dragStartEvent.getHelper().setInnerSafeHtml(
            new SafeHtmlBuilder().appendEscaped(
                entry.getPlayerName()).toSafeHtml());
      }
    });

    dataProvider.addDataDisplay(this);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  private void initDragging(DragAndDropColumn<QueueEntry, String> column) {
    DraggableOptions draggableOptions = column.getDraggableOptions();
    Element helper = DOM.createDiv();
    helper.addClassName(CSS.dragHelper());
    draggableOptions.setHelper(helper);
    draggableOptions.setCursor(Cursor.ROW_RESIZE);
    draggableOptions.setRevert(RevertOption.ON_INVALID_DROP);

    DroppableOptions droppableOptions = column.getDroppableOptions();
    droppableOptions.setTolerance(DroppableTolerance.POINTER);
    droppableOptions.setOnOver(new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        getTRParent(dragAndDropContext).addClassName(CSS.dropHover());
      }
    });
    DroppableFunction removeHover = new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        getTRParent(dragAndDropContext).removeClassName(CSS.dropHover());
      }
    };
    droppableOptions.setOnOut(removeHover);
    droppableOptions.setOnDeactivate(removeHover);
    droppableOptions.setOnDrop(new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        QueueEntry draggedPlayer = dragAndDropContext.getDraggableData();
        QueueEntry droppedPlayer = dragAndDropContext.getDroppableData();
        eventBus.fireEvent(new ReorderPlayerQueueEvent(
            draggedPlayer.getPlayerId(),
            getVisibleItems().indexOf(droppedPlayer)));
      }
    });
  }

  public static Element getTRParent(DragAndDropContext dragAndDropContext) {
    Element droppable = (Element) dragAndDropContext.getDroppable();
    while (!droppable.getTagName().equalsIgnoreCase("tr")
        && droppable.hasParentElement()) {
      droppable = (Element) droppable.getParentElement();
    }
    return droppable;
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }
}