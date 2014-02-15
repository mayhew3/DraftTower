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
import com.mayhew3.drafttower.client.events.IsUsersAutoPickWizardTableEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.TableSpec;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.PlayerDataSet.CBSSPORTS;
import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Widget containing player table, position filter buttons, and paging controls.
 */
public class PlayerTablePanel extends Composite implements
    IsUsersAutoPickWizardTableEvent.Handler,
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

  private static final Position[] POSITIONS = {
      UNF, null, BAT, C, FB, SB, TB, SS, OF, DH, P
  };

  private final Map<PlayerDataSet, ToggleButton> dataSetButtons = new EnumMap<>(PlayerDataSet.class);
  private ToggleButton allButton;
  private final Map<Position, ToggleButton> positionFilterButtons = new EnumMap<>(Position.class);
  private final TextBox nameSearch;
  private final CheckBox useForAutoPick;
  private final Button copyRanks;
  private final UnclaimedPlayerTable table;

  @Inject
  public PlayerTablePanel(final UnclaimedPlayerTable table, final EventBus eventBus) {
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
          eventBus.fireEvent(new SetAutoPickWizardEvent(table.getTableSpec().getPlayerDataSet()));
        } else {
          eventBus.fireEvent(new SetAutoPickWizardEvent(null));
        }
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
    for (final Position position : POSITIONS) {
      ToggleButton button = new ToggleButton(position == null ? "All" : position.getShortName(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (position != null) {
                allButton.setDown(false);
              }
              for (Entry<Position, ToggleButton> buttonEntry : positionFilterButtons.entrySet()) {
                buttonEntry.getValue().setDown(buttonEntry.getKey() == position);
              }
              table.setPositionFilter(position);
            }
          });
      button.addStyleName(CSS.filterButton());
      if (position != null) {
        positionFilterButtons.put(position, button);
      } else {
        allButton = button;
      }
      if (position == UNF) {
        button.setDown(true);
      }
      filterButtons.add(button);
    }
    table.setPositionFilter(UNF);
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

    eventBus.addHandler(IsUsersAutoPickWizardTableEvent.TYPE, this);
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

  @Override
  public void onSetAutoPickWizard(IsUsersAutoPickWizardTableEvent event) {
    boolean usersAutoPickWizardTable = event.isUsersAutoPickWizardTable();
    boolean shouldBeEnabled = usersAutoPickWizardTable || table.getSortedColumn().equals(PlayerColumn.WIZARD);

    useForAutoPick.setValue(usersAutoPickWizardTable);
    useForAutoPick.setEnabled(shouldBeEnabled);
  }

  @Override
  public void onLogin(LoginEvent event) {
    PlayerDataSet initialWizardTable = event.getLoginResponse().getInitialWizardTable();
    if (initialWizardTable != null) {
      updateDataSetButtons(initialWizardTable);
    }
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        updateCopyRanksEnabled();
      }
    });
  }
}