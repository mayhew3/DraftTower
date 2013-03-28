package com.mayhew3.drafttower.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.QueueEntry;
import com.mayhew3.drafttower.shared.QueueEntryPredicate;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import java.util.List;

/**
 * Table displaying players queue.
 */
public class QueueTable extends PlayerTable<QueueEntry> {

  @Inject
  public QueueTable(QueueDataProvider dataProvider,
      final EventBus eventBus) {
    super(eventBus);

    addStyleName(BASE_CSS.table());
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

    final SingleSelectionModel<QueueEntry> selectionModel = new SingleSelectionModel<QueueEntry>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        QueueEntry player = selectionModel.getSelectedObject();
        if (player.getPlayerId() >= 0) {
          eventBus.fireEvent(new PlayerSelectedEvent(player.getPlayerId(), player.getPlayerName()));
        }
      }
    });
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

    dataProvider.addDataDisplay(this);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
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
            eventBus.fireEvent(new ReorderPlayerQueueEvent(
                draggedPlayer.getPlayerId(),
                targetPosition));
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
            eventBus.fireEvent(new EnqueuePlayerEvent(
                draggedPlayer.getPlayerId(),
                droppedPlayer == null ? null : targetPosition));
          }
        }
      }
    };
    initDragging(column, onDrop);
  }

  @Override
  protected boolean needsRefresh(List<DraftPick> oldPicks, List<DraftPick> newPicks) {
    for (int i = oldPicks.size(); i < newPicks.size(); i++) {
      long pickedPlayerId = newPicks.get(i).getPlayerId();
      if (Iterables.any(getVisibleItems(), new QueueEntryPredicate(pickedPlayerId))) {
        return true;
      }
    }
    return false;
  }
}