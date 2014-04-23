package com.mayhew3.drafttower.client.players.queue;

import com.google.common.base.Joiner;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.players.PlayerTable;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.QueueEntry;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

/**
 * Table displaying players queue.
 */
public class QueueTable extends PlayerTable<QueueEntry> {

  private final QueueDataProvider presenter;

  @Inject
  public QueueTable(final QueueDataProvider presenter) {
    super(presenter);
    this.presenter = presenter;

    addStyleName(BASE_CSS.table());
    setPageSize(Integer.MAX_VALUE);

    addColumn(new IdentityColumn<>(new AbstractCell<QueueEntry>() {
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

      @Override
      public void render(Context context, QueueEntry object, SafeHtmlBuilder sb) {
        if (object.getPlayerId() >= 0) {
          super.render(context, object, sb);
        }
      }
    };
    addColumn(removeColumn);
    removeColumn.setFieldUpdater(new FieldUpdater<QueueEntry, String>() {
      @Override
      public void update(int index, QueueEntry entry, String value) {
        presenter.dequeue(entry);
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

    final SingleSelectionModel<QueueEntry> selectionModel = new SingleSelectionModel<>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        presenter.select(selectionModel.getSelectedObject());
      }
    });
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
  }

  private void initDragging(DragAndDropColumn<QueueEntry, String> column) {
    DroppableFunction onDrop = new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        Object draggableData = dragAndDropContext.getDraggableData();
        if (draggableData instanceof QueueEntry && ((QueueEntry) draggableData).getPlayerId() >= 0) {
          QueueEntry draggedPlayer = dragAndDropContext.getDraggableData();
          QueueEntry droppedPlayer = dragAndDropContext.getDroppableData();
          if (droppedPlayer == null
              || draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
            int targetPosition = getVisibleItems().indexOf(droppedPlayer);
            if (isTopDrop(dragAndDropContext, true)) {
              targetPosition--;
            }
            presenter.reorderQueue(draggedPlayer, targetPosition);
          }
        } else if (draggableData instanceof Player) {
          Player draggedPlayer = dragAndDropContext.getDraggableData();
          QueueEntry droppedPlayer = dragAndDropContext.getDroppableData();
          if (droppedPlayer == null
              || draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
            int targetPosition = getVisibleItems().indexOf(droppedPlayer) + 1;
            if (isTopDrop(dragAndDropContext, true)) {
              targetPosition--;
            }
            presenter.enqueue(draggedPlayer, droppedPlayer == null ? null : targetPosition);
          }
        }
      }
    };
    initDragging(column, onDrop);
  }
}