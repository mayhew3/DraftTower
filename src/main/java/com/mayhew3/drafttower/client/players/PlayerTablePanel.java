package com.mayhew3.drafttower.client.players;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mayhew3.drafttower.client.players.unclaimed.UnclaimedPlayerTable;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.PlayerDataSet.CBSSPORTS;

/**
 * Widget containing player table, position filter buttons, and paging controls.
 */
public class PlayerTablePanel extends Composite implements PlayerTablePanelView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String headerElement();
      String headerLine();
      String hideInjuries();
      String rightSideControls();
      String autoPick();
      String buttonContainer();
      String filterButton();
      String filterCheckBox();
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

  private final Map<PlayerDataSet, ToggleButton> dataSetButtons = new EnumMap<>(PlayerDataSet.class);
  private final Map<PositionFilter, ToggleButton> positionFilterButtons = new HashMap<>();
  private final Map<Position, CheckBox> positionOverrideCheckBoxes = new HashMap<>();
  private final TextBox nameSearch;
  private final CheckBox useForAutoPick;
  private final Button copyRanks;
  private final UnclaimedPlayerTable table;

  @Inject
  public PlayerTablePanel(final UnclaimedPlayerTable table,
      final PlayerTablePanelPresenter presenter) {
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
        presenter.setHideInjuries(event.getValue());
      }
    });
    rightSideControls.add(hideInjuries);

    useForAutoPick = new CheckBox("Use this wizard for auto-pick");
    useForAutoPick.setStyleName(CSS.autoPick());
    useForAutoPick.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        presenter.setUseForAutoPick(event.getValue());
      }
    });
    rightSideControls.add(useForAutoPick);

    copyRanks = new Button("Copy this order to MyRank");
    copyRanks.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        presenter.copyRanks();
      }
    });
    rightSideControls.add(copyRanks);

    table.addColumnSortHandler(new Handler() {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        presenter.updateOnSort();
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
          presenter.setPlayerDataSet(playerDataSet);
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
    for (final Position position : Position.REAL_POSITIONS) {
      final CheckBox checkBox = new CheckBox();
      checkBox.setValue(true);
      checkBox.setStyleName(CSS.filterCheckBox());
      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.toggleExcludedPosition(position);
        }
      });
      positionOverrideCheckBoxes.put(position, checkBox);
    }
    for (final PositionFilter positionFilter : PlayerTablePanelPresenter.POSITION_FILTERS) {
      FlowPanel buttonContainer = new FlowPanel();
      buttonContainer.setStyleName(CSS.buttonContainer());
      ToggleButton button = new ToggleButton(positionFilter.getName(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              presenter.setPositionFilter(positionFilter);
            }
          });
      button.addStyleName(CSS.filterButton());
      positionFilterButtons.put(positionFilter, button);
      if (positionFilter == PlayerTablePanelPresenter.POSITION_FILTERS.get(0)) {
        button.setDown(true);
      }
      buttonContainer.add(button);
      if (positionFilter.getPositions().size() == 1) {
        buttonContainer.add(positionOverrideCheckBoxes.get(positionFilter.getPositions().iterator().next()));
      }
      filterButtons.add(buttonContainer);
    }
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
        presenter.setNameFilter(event.getValue());
        clear.setVisible(!event.getValue().isEmpty());
      }
    });
    clear.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        nameSearch.setValue("");
        clear.setVisible(false);
        presenter.setNameFilter("");
      }
    });
    search.add(nameSearch);
    search.add(clear);
    pagerAndSearch.add(search);
    container.add(pagerAndSearch);

    container.add(table);

    initWidget(container);

    presenter.setView(this);
  }

  @Override
  public void setPositionFilter(PositionFilter positionFilter, boolean unfilledSelected) {
    for (Entry<PositionFilter, ToggleButton> buttonEntry : positionFilterButtons.entrySet()) {
      buttonEntry.getValue().setDown(buttonEntry.getKey() == positionFilter);
    }
    for (Entry<Position, CheckBox> checkBoxEntry : positionOverrideCheckBoxes.entrySet()) {
      checkBoxEntry.getValue().setVisible(unfilledSelected
          && (positionFilter.getPositions().isEmpty()
              || positionFilter.getPositions().contains(checkBoxEntry.getKey())));
    }
  }

  @Override
  public void setCopyRanksEnabled(boolean enabled) {
    copyRanks.setEnabled(enabled);
  }

  @Override
  public void updateDataSetButtons(PlayerDataSet playerDataSet) {
    for (Entry<PlayerDataSet, ToggleButton> buttonEntry : dataSetButtons.entrySet()) {
      buttonEntry.getValue().setDown(buttonEntry.getKey() == playerDataSet);
    }
  }

  @Override
  public void updateUseForAutoPickCheckbox(boolean usersAutoPickWizardTable, boolean shouldBeEnabled) {
    useForAutoPick.setValue(usersAutoPickWizardTable);
    useForAutoPick.setEnabled(shouldBeEnabled);
  }

  public void setQueueAreaTopProvider(Provider<Integer> queueAreaTopProvider) {
    table.setQueueAreaTopProvider(queueAreaTopProvider);
  }
}