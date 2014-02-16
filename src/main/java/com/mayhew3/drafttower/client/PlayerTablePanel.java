package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.TableSpec;

import java.util.*;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.PlayerDataSet.CBSSPORTS;
import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Widget containing player table, position filter buttons, and paging controls.
 */
public class PlayerTablePanel extends Composite implements
    LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String headerElement();
      String headerLine();
      String hideInjuries();
      String rightSideControls();
      String autoPick();
      String filterButton();
      String search();
      String clearSearch();
    }

    @Source("PlayerTablePanel.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private static class PositionFilter {
    private final String name;
    private final EnumSet<Position> positions;

    private PositionFilter(String name, EnumSet<Position> positions) {
      this.name = name;
      this.positions = positions;
    }
    
    private PositionFilter(Position singlePosition) {
      this.name = singlePosition.getShortName();
      this.positions = EnumSet.of(singlePosition);
    }
  }

  private static final List<PositionFilter> POSITION_FILTERS = Arrays.asList(
      null,  // Unfilled - populated in constructor. 
      new PositionFilter("All", EnumSet.allOf(Position.class)),
      new PositionFilter("Batters", Position.BATTING_POSITIONS),
      new PositionFilter(C),
      new PositionFilter(FB),
      new PositionFilter(SB),
      new PositionFilter(TB),
      new PositionFilter(SS),
      new PositionFilter(OF),
      new PositionFilter(DH),
      new PositionFilter(P));

  private final Map<PlayerDataSet, ToggleButton> dataSetButtons = new EnumMap<>(PlayerDataSet.class);
  private final Map<PositionFilter, ToggleButton> positionFilterButtons = new HashMap<>();
  private final TextBox nameSearch;
  private final CheckBox useForAutoPick;
  private final Button copyRanks;
  private final UnclaimedPlayerTable table;
  private PlayerDataSet wizardTable;

  @Inject
  public PlayerTablePanel(final UnclaimedPlayerTable table,
      OpenPositions openPositions,
      final EventBus eventBus) {
    POSITION_FILTERS.set(0, new PositionFilter("Unfilled", openPositions.get()));
    this.table = table;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    FlowPanel rightSideControls = new FlowPanel();
    rightSideControls.addStyleName(CSS.rightSideControls());
    container.add(rightSideControls);

    CheckBox hideInjuries = new CheckBox("Hide injured players");
    hideInjuries.setStyleName(CSS.hideInjuries());
    hideInjuries.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        table.setHideInjuries(event.getValue());
      }
    });
    rightSideControls.add(hideInjuries);


    useForAutoPick = new CheckBox("Use this wizard for auto-pick");
    useForAutoPick.setStyleName(CSS.autoPick());
    useForAutoPick.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
          wizardTable = table.getTableSpec().getPlayerDataSet();
        } else {
          wizardTable = null;
        }
        eventBus.fireEvent(new SetAutoPickWizardEvent(wizardTable));
      }
    });
    rightSideControls.add(useForAutoPick);


    copyRanks = new Button("Copy this order to MyRank");
    copyRanks.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        TableSpec tableSpec = table.getTableSpec();
        if (tableSpec.getSortCol() != PlayerColumn.MYRANK) {
          eventBus.fireEvent(new CopyAllPlayerRanksEvent(tableSpec));
        }
      }
    });
    rightSideControls.add(copyRanks);

    updateCopyRanksEnabled();
    table.addColumnSortHandler(new Handler() {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        updateCopyRanksEnabled();
        updateUserForAutoPickCheckbox();
      }
    });

    FlowPanel buttonPanels = new FlowPanel();
    buttonPanels.addStyleName(CSS.headerLine());
    container.add(buttonPanels);

    HorizontalPanel dataSetButtonPanel = new HorizontalPanel();
    dataSetButtonPanel.addStyleName(CSS.headerElement());
    for (final PlayerDataSet playerDataSet : PlayerDataSet.values()) {
      ToggleButton button = new ToggleButton(playerDataSet.getDisplayName(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          updateDataSetButtons(playerDataSet);
          table.setPlayerDataSet(playerDataSet);
          updateUserForAutoPickCheckbox();
        }
      });
      button.addStyleName(CSS.filterButton());
      if (playerDataSet == CBSSPORTS) {
        button.setDown(true);
      }
      dataSetButtons.put(playerDataSet, button);
      dataSetButtonPanel.add(button);
    }
    buttonPanels.add(dataSetButtonPanel);

    HorizontalPanel filterButtons = new HorizontalPanel();
    filterButtons.addStyleName(CSS.headerElement());
    for (final PositionFilter positionFilter : POSITION_FILTERS) {
      ToggleButton button = new ToggleButton(positionFilter.name,
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              for (Entry<PositionFilter, ToggleButton> buttonEntry : positionFilterButtons.entrySet()) {
                buttonEntry.getValue().setDown(buttonEntry.getKey() == positionFilter);
              }
              table.setPositionFilter(positionFilter.positions);
            }
          });
      button.addStyleName(CSS.filterButton());
      positionFilterButtons.put(positionFilter, button);
      if (positionFilter == POSITION_FILTERS.get(0)) {
        button.setDown(true);
      }
      filterButtons.add(button);
    }
    table.setPositionFilter(POSITION_FILTERS.get(0).positions);
    buttonPanels.add(filterButtons);

    FlowPanel pagerAndSearch = new FlowPanel();
    SimplePager pager = new SimplePager();
    pager.addStyleName(CSS.headerElement());
    pager.setDisplay(table);
    pagerAndSearch.add(pager);

    FlowPanel search = new FlowPanel();
    search.addStyleName(CSS.headerElement());
    search.addStyleName(CSS.search());
    search.add(new InlineLabel("Search: "));
    nameSearch = new TextBox();
    final InlineLabel clear = new InlineLabel(" X ");
    clear.setStyleName(CSS.clearSearch());
    clear.setVisible(false);
    nameSearch.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        table.setNameFilter(event.getValue());
        clear.setVisible(!event.getValue().isEmpty());
      }
    });
    clear.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        nameSearch.setValue("");
        clear.setVisible(false);
        table.setNameFilter("");
      }
    });
    search.add(nameSearch);
    search.add(clear);
    pagerAndSearch.add(search);
    container.add(pagerAndSearch);

    container.add(table);

    initWidget(container);

    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  private void updateCopyRanksEnabled() {
    List<PlayerColumn> invalidColumns = Lists.newArrayList(PlayerColumn.WIZARD, PlayerColumn.MYRANK);
    copyRanks.setEnabled(!invalidColumns.contains(table.getSortedColumn()));
  }

  private void updateDataSetButtons(PlayerDataSet playerDataSet) {
    for (Entry<PlayerDataSet, ToggleButton> buttonEntry : dataSetButtons.entrySet()) {
      buttonEntry.getValue().setDown(buttonEntry.getKey() == playerDataSet);
    }
  }

  private void updateUserForAutoPickCheckbox() {
    boolean usersAutoPickWizardTable = table.getTableSpec().getPlayerDataSet() == wizardTable;
    boolean shouldBeEnabled = usersAutoPickWizardTable || table.getSortedColumn().equals(PlayerColumn.WIZARD);

    useForAutoPick.setValue(usersAutoPickWizardTable);
    useForAutoPick.setEnabled(shouldBeEnabled);
  }

  @Override
  public void onLogin(LoginEvent event) {
    wizardTable = event.getLoginResponse().getInitialWizardTable();
    if (wizardTable != null) {
      updateDataSetButtons(wizardTable);
      updateUserForAutoPickCheckbox();
    }
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        updateCopyRanksEnabled();
      }
    });
  }
}