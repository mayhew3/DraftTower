package com.mayhew3.drafttower.client.players;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.ClientDraftStatus;
import com.mayhew3.drafttower.shared.DraftPick;

import java.util.List;

/**
 * Base class for presenter/data provider for player tables.
 */
public abstract class PlayerDataProvider<T> implements
    DraftStatusChangedEvent.Handler {

  private final AsyncDataProvider<T> delegate;

  private PlayerTableView<T> view;
  protected ClientDraftStatus lastStatus;

  public PlayerDataProvider(EventBus eventBus) {
    this.delegate = new AsyncDataProvider<T>() {
      @Override
      protected void onRangeChanged(HasData<T> display) {
        rangeChanged(display);
      }
    };

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public void setView(PlayerTableView<T> view) {
    this.view = view;
    delegate.addDataDisplay(view);
  }

  protected PlayerTableView<T> getView() {
    return view;
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    boolean refresh = false;
    if (lastStatus == null) {
      refresh = true;
    } else {
      List<DraftPick> oldPicks = lastStatus.getDraftStatus().getPicks();
      List<DraftPick> newPicks = event.getStatus().getDraftStatus().getPicks();
      if (newPicks.size() < oldPicks.size()) {
        // Pick backed out.
        refresh = true;
      }
      if (newPicks.size() > oldPicks.size()) {
        // Picks have been made.
        refresh = needsRefresh(newPicks.subList(oldPicks.size(), newPicks.size()));
      }
    }
    if (refresh) {
      view.refresh();
    }
    lastStatus = event.getStatus();
  }

  protected boolean needsRefresh(List<DraftPick> newPicks) {
    for (DraftPick pick : newPicks) {
      long pickedPlayerId = pick.getPlayerId();
      if (Iterables.any(view.getVisibleItems(), createPredicate(pickedPlayerId))) {
        return true;
      }
    }
    return false;
  }

  protected abstract Predicate<T> createPredicate(long playerId);

  protected abstract void rangeChanged(HasData<T> display);
}