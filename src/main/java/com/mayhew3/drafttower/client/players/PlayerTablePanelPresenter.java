package com.mayhew3.drafttower.client.players;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.mayhew3.drafttower.client.OpenPositions;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.client.players.unclaimed.UnclaimedPlayerDataProvider;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.TableSpec;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Presenter for player table controls.
 */
public class PlayerTablePanelPresenter implements
    LoginEvent.Handler,
    DraftStatusChangedEvent.Handler {

  static final List<PositionFilter> POSITION_FILTERS = Arrays.asList(
      null,  // Unfilled - populated in constructor.
      new PositionFilter("All", EnumSet.allOf(Position.class)),
      new PositionFilter(C),
      new PositionFilter(FB),
      new PositionFilter(SB),
      new PositionFilter(TB),
      new PositionFilter(SS),
      new PositionFilter(OF),
      new PositionFilter(DH),
      new PositionFilter(P));

  private final UnclaimedPlayerDataProvider tablePresenter;
  private final EventBus eventBus;

  private PositionFilter positionFilter;
  @VisibleForTesting
  final EnumSet<Position> excludedPositions = EnumSet.noneOf(Position.class);
  private PlayerDataSet wizardTable;

  private PlayerTablePanelView view;

  @Inject
  public PlayerTablePanelPresenter(
      OpenPositions openPositions,
      UnclaimedPlayerDataProvider tablePresenter,
      final EventBus eventBus) {
    this.tablePresenter = tablePresenter;
    this.eventBus = eventBus;
    POSITION_FILTERS.set(0, new PositionFilter("Unfilled", openPositions.get()));

    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public void setView(PlayerTablePanelView view) {
    this.view = view;
    updateCopyRanksEnabled(false);
    setPositionFilter(POSITION_FILTERS.get(0));
  }

  @Override
  public void onLogin(LoginEvent event) {
    wizardTable = event.getLoginResponse().getInitialWizardTable();
    if (wizardTable != null) {
      view.updateDataSetButtons(wizardTable);
      updateUseForAutoPick();
    }
    updateCopyRanksEnabled(true);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    setPositionFilter(positionFilter);
  }

  private void updateUseForAutoPick() {
    boolean usersAutoPickWizardTable = tablePresenter.getTableSpec().getPlayerDataSet() == wizardTable;
    boolean shouldBeEnabled = usersAutoPickWizardTable
        || tablePresenter.getSortedPlayerColumn().equals(PlayerColumn.WIZARD);
    view.updateUseForAutoPickCheckbox(usersAutoPickWizardTable, shouldBeEnabled);
  }

  private void updateCopyRanksEnabled(boolean defer) {
    List<PlayerColumn> invalidColumns = Lists.newArrayList(PlayerColumn.WIZARD, PlayerColumn.MYRANK);
    boolean enabled = !invalidColumns.contains(tablePresenter.getSortedPlayerColumn());
    view.setCopyRanksEnabled(enabled, defer);
  }

  public void updateOnSort() {
    updateUseForAutoPick();
    updateCopyRanksEnabled(false);
  }

  public void setPositionFilter(PositionFilter positionFilter) {
    this.positionFilter = positionFilter;
    boolean unfilledSelected = positionFilter == POSITION_FILTERS.get(0);
    view.setPositionFilter(positionFilter, unfilledSelected);
    EnumSet<Position> positions = EnumSet.copyOf(positionFilter.getPositions());
    if (unfilledSelected) {
      positions.removeAll(excludedPositions);
    }
    tablePresenter.setPositionFilter(positions);
  }

  public void setUseForAutoPick(boolean useForAutoPick) {
    if (useForAutoPick) {
      wizardTable = tablePresenter.getTableSpec().getPlayerDataSet();
    } else {
      wizardTable = null;
    }
    eventBus.fireEvent(new SetAutoPickWizardEvent(wizardTable));
  }

  public void copyRanks() {
    TableSpec tableSpec = tablePresenter.getTableSpec();
    if (tableSpec.getSortCol() != PlayerColumn.MYRANK) {
      eventBus.fireEvent(new CopyAllPlayerRanksEvent(tableSpec));
    }
  }

  public void setPlayerDataSet(PlayerDataSet playerDataSet) {
    view.updateDataSetButtons(playerDataSet);
    tablePresenter.setPlayerDataSet(playerDataSet);
    updateUseForAutoPick();
  }

  public void toggleExcludedPosition(Position position) {
    if (excludedPositions.contains(position)) {
      excludedPositions.remove(position);
    } else {
      excludedPositions.add(position);
    }
    setPositionFilter(POSITION_FILTERS.get(0));
  }

  public void setHideInjuries(boolean hideInjuries) {
    tablePresenter.setHideInjuries(hideInjuries);
  }

  public void setNameFilter(String value) {
    tablePresenter.setNameFilter(value);
  }
}