package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.OpenPositions;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.client.players.UnfilledPositionsFilter;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.EnumSet;

/**
 * Test for {@link UnclaimedPlayerTablePanelPresenter}.
 */
public class UnclaimedPlayerTablePanelPresenterTest {

  private UnclaimedPlayerDataProvider tablePresenter;
  private EventBus eventBus;
  private UnclaimedPlayerTablePanelView view;

  private UnclaimedPlayerTablePanelPresenter presenter;

  @Before
  public void setUp() {
    BeanFactory beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    OpenPositions openPositions = Mockito.mock(OpenPositions.class);
    EnumSet<Position> unfilledPositionsSet = EnumSet.of(Position.C, Position.SB, Position.SS, Position.P);
    Mockito.when(openPositions.get()).thenReturn(unfilledPositionsSet);
    tablePresenter = Mockito.mock(UnclaimedPlayerDataProvider.class);
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.WIZARD);
    eventBus = Mockito.mock(EventBus.class);
    view = Mockito.mock(UnclaimedPlayerTablePanelView.class);

    presenter = new UnclaimedPlayerTablePanelPresenter(
        new UnfilledPositionsFilter(openPositions),
        tablePresenter,
        eventBus);
    presenter.setView(view);

    Mockito.reset(view, tablePresenter, eventBus);
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.WIZARD);
    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setSortCol(PlayerColumn.AB);
    tableSpec.setPlayerDataSet(PlayerDataSet.AVERAGES);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
  }

  @Test
  public void testUnfilledPositionFilterCreated() {
    PositionFilter unfilledPositionFilter = UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    Assert.assertTrue(unfilledPositionFilter instanceof UnfilledPositionsFilter);
  }

  @Test
  public void testViewSetUpOnLoginNonCopyableSortColumn() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(null);
    Mockito.when(loginResponse.getMinClosers()).thenReturn(0);
    Mockito.when(loginResponse.getMaxClosers()).thenReturn(7);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, true);
    Mockito.verify(view).setCloserLimits(0, 7);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginCopyableSortColumn() {
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.HR);
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(null);
    Mockito.when(loginResponse.getMinClosers()).thenReturn(0);
    Mockito.when(loginResponse.getMaxClosers()).thenReturn(7);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).setCopyRanksEnabled(true, true);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, false);
    Mockito.verify(view).setCloserLimits(0, 7);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginInitialWizardTableDifferentFromTableSpecSortedByWizard() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(PlayerDataSet.GURU);
    Mockito.when(loginResponse.getMinClosers()).thenReturn(0);
    Mockito.when(loginResponse.getMaxClosers()).thenReturn(7);
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.GURU);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, true);
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verify(view).setCloserLimits(0, 7);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginInitialWizardTableDifferentFromTableSpecNotSortedByWizard() {
    Mockito.when(tablePresenter.getSortedPlayerColumn()).thenReturn(PlayerColumn.HR);
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(PlayerDataSet.GURU);
    Mockito.when(loginResponse.getMinClosers()).thenReturn(0);
    Mockito.when(loginResponse.getMaxClosers()).thenReturn(7);
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.GURU);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, false);
    Mockito.verify(view).setCopyRanksEnabled(true, true);
    Mockito.verify(view).setCloserLimits(0, 7);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginInitialWizardTableSameAsTableSpec() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(PlayerDataSet.CBSSPORTS);
    Mockito.when(loginResponse.getMinClosers()).thenReturn(0);
    Mockito.when(loginResponse.getMaxClosers()).thenReturn(7);
    mockTableSpecWithPlayerDataSet(PlayerDataSet.CBSSPORTS);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).updateDataSetButtons(PlayerDataSet.CBSSPORTS);
    Mockito.verify(view).updateUseForAutoPickCheckbox(true, true);
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verify(view).setCloserLimits(0, 7);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testViewSetUpOnLoginCloserLimits() {
    LoginResponse loginResponse = Mockito.mock(LoginResponse.class);
    Mockito.when(loginResponse.getInitialWizardTable()).thenReturn(null);
    Mockito.when(loginResponse.getMinClosers()).thenReturn(2);
    Mockito.when(loginResponse.getMaxClosers()).thenReturn(4);
    presenter.onLogin(new LoginEvent(loginResponse));
    Mockito.verify(view).setCopyRanksEnabled(false, true);
    Mockito.verify(view).updateUseForAutoPickCheckbox(false, true);
    Mockito.verify(view).setCloserLimits(2, 4);
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testSetPositionFilter() {
    PositionFilter positionFilter = UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(1);
    presenter.setPositionFilter(positionFilter);
    Mockito.verify(view).setPositionFilter(positionFilter);
    Mockito.verify(tablePresenter).setPositionFilter(positionFilter, EnumSet.noneOf(Position.class));
  }

  @Test
  public void testSetPositionFilterUnfilled() {
    PositionFilter unfilledPositionFilter = UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    presenter.setPositionFilter(unfilledPositionFilter);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter);
    Mockito.verify(tablePresenter).setPositionFilter(unfilledPositionFilter, EnumSet.noneOf(Position.class));
  }

  @Test
  public void testSetPositionFilterUnfilledExcluded() {
    presenter.excludedPositions.add(Position.SB);
    PositionFilter unfilledPositionFilter = UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    presenter.setPositionFilter(unfilledPositionFilter);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter);
    Mockito.verify(tablePresenter).setPositionFilter(unfilledPositionFilter, EnumSet.of(Position.SB));
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
    Mockito.when(tableSpec.getSortCol()).thenReturn(PlayerColumn.PTS);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
    presenter.copyRanks();
    ArgumentCaptor<CopyAllPlayerRanksEvent> eventCaptor = ArgumentCaptor.forClass(CopyAllPlayerRanksEvent.class);
    Mockito.verify(eventBus).fireEvent(eventCaptor.capture());
    CopyAllPlayerRanksEvent firedEvent = eventCaptor.getValue();
    Assert.assertEquals(tableSpec, firedEvent.getTableSpec());
  }

  @Test
  public void testCopyRanksNotAllowedForWizardSort() {
    TableSpec tableSpec = Mockito.mock(TableSpec.class);
    Mockito.when(tableSpec.getSortCol()).thenReturn(PlayerColumn.WIZARD);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
    presenter.copyRanks();
    Mockito.verifyZeroInteractions(eventBus);
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
    PositionFilter unfilledPositionFilter = UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter);
    Mockito.verify(tablePresenter).setPositionFilter(unfilledPositionFilter, EnumSet.of(Position.SB));
  }

  @Test
  public void testToggleExcludedPositionRemove() {
    presenter.excludedPositions.add(Position.SB);
    presenter.toggleExcludedPosition(Position.SB);
    Assert.assertFalse(presenter.excludedPositions.contains(Position.SB));
    PositionFilter unfilledPositionFilter = UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(0);
    Mockito.verify(view).setPositionFilter(unfilledPositionFilter);
    Mockito.verify(tablePresenter).setPositionFilter(unfilledPositionFilter, EnumSet.noneOf(Position.class));
  }

  private void mockTableSpecWithPlayerDataSet(PlayerDataSet playerDataSet) {
    TableSpec tableSpec = Mockito.mock(TableSpec.class);
    Mockito.when(tableSpec.getPlayerDataSet()).thenReturn(playerDataSet);
    Mockito.when(tablePresenter.getTableSpec()).thenReturn(tableSpec);
  }
}