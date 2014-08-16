package com.mayhew3.drafttower.client.players;

import com.google.gwt.event.shared.EventBus;
import com.mayhew3.drafttower.client.OpenPositions;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.client.players.unclaimed.UnclaimedPlayerDataProvider;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.EnumSet;

/**
 * Test for {@link PlayerTablePanelPresenter}.
 */
public class PlayerTablePanelPresenterTest {

  private EnumSet<Position> unfilledPositions;
  private UnclaimedPlayerDataProvider tablePresenter;
  private EventBus eventBus;
  private PlayerTablePanelView view;

  private PlayerTablePanelPresenter presenter;

  @Before
  public void setUp() {
    OpenPositions openPositions = Mockito.mock(OpenPositions.class);
    unfilledPositions = EnumSet.of(Position.C, Position.SB, Position.SS, Position.P);
    Mockito.when(openPositions.get()).thenReturn(unfilledPositions);
    tablePresenter = Mockito.mock(UnclaimedPlayerDataProvider.class);
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.WIZARD);
    eventBus = Mockito.mock(EventBus.class);
    view = Mockito.mock(PlayerTablePanelView.class);

    presenter = new PlayerTablePanelPresenter(
        openPositions,
        tablePresenter,
        eventBus);
    presenter.setView(view);

    Mockito.reset(view, tablePresenter, eventBus);
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.WIZARD);
  }

  @Test
  public void testUnfilledPositionFilterCreated() {
    PositionFilter unfilledPositionFilter = PlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    Assert.assertEquals("Unfilled", unfilledPositionFilter.getName());
    Assert.assertEquals(unfilledPositions, unfilledPositionFilter.getPositions());
    unfilledPositions.add(Position.TB);
    Assert.assertEquals(unfilledPositions, unfilledPositionFilter.getPositions());
  }

  @Test
  public void testViewSetUpOnLoginNonCopyableSortColumn() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(null);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginCopyableSortColumn() {
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.HR);
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(null);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).setCopyRanksEnabled(true, true);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginInitialWizardTableDifferentFromTableSpecSortedByWizard() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(PlayerDataSet.GURU);
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.GURU);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, true);
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginInitialWizardTableDifferentFromTableSpecNotSortedByWizard() {
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.HR);
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(PlayerDataSet.GURU);
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.GURU);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, false);
    Mockito.verify(view).setCopyRanksEnabled(true, true);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginInitialWizardTableSameAsTableSpec() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(PlayerDataSet.CBSSPORTS);
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.CBSSPORTS);
    Mockito.verify(view).updateUseForAutoPickCheckbox(true, true);
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testSetPositionFilter() {
    PositionFilter positionFilter = PlayerTablePanelPresenter.POSITION_FILTERS.get(1);
    presenter.setPositionFilter(positionFilter);
    Mockito.verify(view).setPositionFilter(positionFilter, false);
    Mockito.verify(tablePresenter).setPositionFilter(positionFilter.getPositions());
  }

  @Test
  public void testSetPositionFilterUnfilled() {
    PositionFilter unfilledPositionFilter = PlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    presenter.setPositionFilter(unfilledPositionFilter);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter, true);
    Mockito.verify(tablePresenter).setPositionFilter(unfilledPositionFilter.getPositions());
  }

  @Test
  public void testSetPositionFilterUnfilledExcluded() {
    presenter.excludedPositions.add(Position.SB);
    PositionFilter unfilledPositionFilter = PlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    presenter.setPositionFilter(unfilledPositionFilter);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter, true);
    EnumSet<Position> expectedPresenterPositions = unfilledPositionFilter.getPositions().clone();
    expectedPresenterPositions.remove(Position.SB);
    Mockito.verify(tablePresenter).setPositionFilter(expectedPresenterPositions);
  }

  @Test
  public void testSetUseForAutoPick() {
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.setUseForAutoPick(true);
    ArgumentCaptor<SetAutoPickWizardEvent> eventCaptor = ArgumentCaptor.forClass(SetAutoPickWizardEvent.class);
    Mockito.verify(eventBus).fireEvent(eventCaptor.capture());
    SetAutoPickWizardEvent firedEvent = eventCaptor.getValue();
    Assert.assertEquals(PlayerDataSet.CBSSPORTS, firedEvent.getWizardTable());
  }

  @Test
  public void testSetDontUseForAutoPick() {
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.setUseForAutoPick(false);
    ArgumentCaptor<SetAutoPickWizardEvent> eventCaptor = ArgumentCaptor.forClass(SetAutoPickWizardEvent.class);
    Mockito.verify(eventBus).fireEvent(eventCaptor.capture());
    SetAutoPickWizardEvent firedEvent = eventCaptor.getValue();
    Assert.assertEquals(null, firedEvent.getWizardTable());
  }

  @Test
  public void testCopyRanks() {
    TableSpec tableSpec = Mockito.mock(TableSpec.class);
    Mockito.when(tableSpec.getSortCol()).thenReturn(PlayerColumn.WIZARD);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
    presenter.copyRanks();
    ArgumentCaptor<CopyAllPlayerRanksEvent> eventCaptor = ArgumentCaptor.forClass(CopyAllPlayerRanksEvent.class);
    Mockito.verify(eventBus).fireEvent(eventCaptor.capture());
    CopyAllPlayerRanksEvent firedEvent = eventCaptor.getValue();
    Assert.assertEquals(tableSpec, firedEvent.getTableSpec());
  }

  @Test
  public void testCopyRanksSortedByRankAlready() {
    TableSpec tableSpec = Mockito.mock(TableSpec.class);
    Mockito.when(tableSpec.getSortCol()).thenReturn(PlayerColumn.MYRANK);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
    presenter.copyRanks();
    Mockito.verifyZeroInteractions(eventBus);
  }

  @Test
  public void testSetPlayerDataSet() {
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.setPlayerDataSet(PlayerDataSet.ROTOWIRE);
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.ROTOWIRE);
    Mockito.verify(tablePresenter).setPlayerDataSet(PlayerDataSet.ROTOWIRE);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, true);
  }

  @Test
  public void testToggleExcludedPositionAdd() {
    presenter.toggleExcludedPosition(Position.SB);
    Assert.assertTrue(presenter.excludedPositions.contains(Position.SB));
    PositionFilter unfilledPositionFilter = PlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter, true);
    EnumSet<Position> expectedPresenterPositions = unfilledPositionFilter.getPositions().clone();
    expectedPresenterPositions.remove(Position.SB);
    Mockito.verify(tablePresenter).setPositionFilter(expectedPresenterPositions);
  }

  @Test
  public void testToggleExcludedPositionRemove() {
    presenter.excludedPositions.add(Position.SB);
    presenter.toggleExcludedPosition(Position.SB);
    Assert.assertFalse(presenter.excludedPositions.contains(Position.SB));
    PositionFilter unfilledPositionFilter = PlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter, true);
    Mockito.verify(tablePresenter).setPositionFilter(unfilledPositionFilter.getPositions());
  }

  private void mockTableSpecWithPlayerDataSet(PlayerDataSet playerDataSet) {
    TableSpec tableSpec = Mockito.mock(TableSpec.class);
    Mockito.when(tableSpec.getPlayerDataSet()).thenReturn(playerDataSet);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
  }
}