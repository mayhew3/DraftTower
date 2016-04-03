package com.mayhew3.drafttower.client.players.queue;

import com.google.common.base.Joiner;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.mayhew3.drafttower.client.players.PlayerDragController;
import com.mayhew3.drafttower.client.players.PlayerTable;
import com.mayhew3.drafttower.shared.DraggableItem;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.QueueEntry;

import javax.inject.Inject;

/**
 * Table displaying players queue.
 */
public class QueueTable extends PlayerTable<QueueEntry> {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String warning();
      String warningHigh();
    }

    @Source("QueueTable.css")
    Css css();
  }

  protected static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
    BASE_CSS.ensureInjected();
  }

  private final QueueDataProvider presenter;

  @Inject
  public QueueTable(final QueueDataProvider presenter,
      PlayerDragController playerDragController) {
    super(presenter, playerDragController);
    this.presenter = presenter;

    addStyleName(BASE_CSS.table());
    setPageSize(Integer.MAX_VALUE);

    addColumn(new IdentityColumn<>(new AbstractCell<QueueEntry>() {
      @Override
      public void render(Context context, QueueEntry value, SafeHtmlBuilder sb) {
        sb.append(context.getIndex() + 1);
      }
    }));

    Column<QueueEntry, String> nameColumn =
        new Column<QueueEntry, String>(new TextCell()) {
          @Override
          public String getValue(QueueEntry entry) {
            return entry.getPlayerName();
          }
        };
    addColumn(nameColumn, "Player");

    Column<QueueEntry, String> eligibilityColumn =
        new Column<QueueEntry, String>(new TextCell()) {
          @Override
          public String getValue(QueueEntry entry) {
            return Joiner.on(", ").join(entry.getEligibilities());
          }
        };
    addColumn(eligibilityColumn, "Eligibility");

    Column<QueueEntry, QueueEntryAndPrediction> warningColumn =
        new Column<QueueEntry, QueueEntryAndPrediction>(new PredictionCell()) {
          @Override
          public QueueEntryAndPrediction getValue(QueueEntry queueEntry) {
            QueueEntryAndPrediction queueEntryAndPrediction = new QueueEntryAndPrediction();
            queueEntryAndPrediction.queueEntry = queueEntry;
            queueEntryAndPrediction.pickPrediction = presenter.getPickPrediction(queueEntry.getPlayerId());
            return queueEntryAndPrediction;
          }
        };
    addColumn(warningColumn, "");

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

  @Override
  protected SafeHtml getDragHelperContents(QueueEntry draggedItem) {
    return new SafeHtmlBuilder().appendEscaped(draggedItem.getPlayerName()).toSafeHtml();
  }

  @Override
  protected boolean canStartDrag() {
    return getVisibleItem(0).getPlayerId() != QueueDataProvider.FAKE_ENTRY_ID;
  }

  @Override
  public void onDrop(DraggableItem item, MouseUpEvent event) {
    int relativeY = event.getRelativeY(getElement());
    int rowIndex = getRowIndex(relativeY);
    QueueEntry droppedPlayer = getVisibleItem(rowIndex);
    int targetPosition = rowIndex + 1;
    if (isTopDrop(relativeY)) {
      targetPosition--;
    }
    if (item instanceof QueueEntry && ((QueueEntry) item).getPlayerId() >= 0) {
      QueueEntry draggedPlayer = (QueueEntry) item;
      if (droppedPlayer == null
          || draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
        presenter.reorderQueue(draggedPlayer, targetPosition);
      }
    } else if (item instanceof Player) {
      Player draggedPlayer = (Player) item;
      if (droppedPlayer == null
          || draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
        presenter.enqueue(draggedPlayer, droppedPlayer == null ? null : targetPosition);
      }
    }
  }
}