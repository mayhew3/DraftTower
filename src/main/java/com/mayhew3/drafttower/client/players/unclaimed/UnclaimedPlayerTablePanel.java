package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.ImageButtonsConstants;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mayhew3.drafttower.client.players.PositionFilter;
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
public class UnclaimedPlayerTablePanel extends Composite implements UnclaimedPlayerTablePanelView {

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

    @Source("UnclaimedPlayerTablePanel.css")
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
  private final InlineLabel clearSearch;
  private final CheckBox useForAutoPick;
  private final Button copyRanks;
  private final CheckBox hideInjuries;
  private final SimplePager pager;
  private final UnclaimedPlayerTable table;

  @Inject
  public UnclaimedPlayerTablePanel(final UnclaimedPlayerTable table,
      final UnclaimedPlayerTablePanelPresenter presenter) {
    this.table = table;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    FlowPanel rightSideControls = new FlowPanel();
    rightSideControls.addStyleName(CSS.rightSideControls());
    container.add(rightSideControls);

    hideInjuries = new CheckBox("Hide injured players");
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
    for (final PositionFilter positionFilter : UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS) {
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
      if (positionFilter == UnclaimedPlayerTablePanelPresenter.POSITION_FILTERS.get(0)) {
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
    pager = new SimplePager();
    pager.addStyleName(CSS.headerElement());
    pager.setDisplay(table);
    pagerAndSearch.add(pager);

    FlowPanel search = new FlowPanel();
    search.addStyleName(CSS.headerElement());
    search.addStyleName(CSS.search());
    search.add(new InlineLabel("Search: "));
    nameSearch = new TextBox();
    clearSearch = new InlineLabel(" X ");
    clearSearch.setStyleName(CSS.clearSearch());
    clearSearch.setVisible(false);
    nameSearch.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        presenter.setNameFilter(event.getValue());
        clearSearch.setVisible(!event.getValue().isEmpty());
      }
    });
    clearSearch.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        nameSearch.setValue("");
        clearSearch.setVisible(false);
        presenter.setNameFilter("");
      }
    });
    search.add(nameSearch);
    search.add(clearSearch);
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
  public void setCopyRanksEnabled(final boolean enabled, boolean defer) {
    if (defer) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          copyRanks.setEnabled(enabled);
        }
      });
    } else {
      copyRanks.setEnabled(enabled);
    }
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

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    for (Entry<PlayerDataSet, ToggleButton> dataSetButton : dataSetButtons.entrySet()) {
      dataSetButton.getValue().ensureDebugId(baseID + "-" + dataSetButton.getKey().getDisplayName());
    }
    for (Entry<PositionFilter, ToggleButton> positionFilterButton : positionFilterButtons.entrySet()) {
      positionFilterButton.getValue().ensureDebugId(baseID + "-" + positionFilterButton.getKey().getName());
    }
    for (Entry<Position, CheckBox> positionOverrideCheckbox : positionOverrideCheckBoxes.entrySet()) {
      CheckBox checkBox = positionOverrideCheckbox.getValue();
      String debugId = baseID + "-override-" + positionOverrideCheckbox.getKey().getShortName();
      checkBox.ensureDebugId(debugId);
      String checkboxDebugId = debugId + "-checkbox";
      UIObject.ensureDebugId(checkBox.getElement().getElementsByTagName("input").getItem(0), checkboxDebugId);
    }
    nameSearch.ensureDebugId(baseID + "-search");
    clearSearch.ensureDebugId(baseID + "-clear");
    copyRanks.ensureDebugId(baseID + "-copyRanks");
    UIObject.ensureDebugId(useForAutoPick.getElement().getElementsByTagName("input").getItem(0), baseID + "-autopick");
    UIObject.ensureDebugId(hideInjuries.getElement().getElementsByTagName("input").getItem(0), baseID + "-hideInjuries");

    ImageButtonsConstants imageButtonsConstants = GWT.create(ImageButtonsConstants.class);
    NodeList<Element> pagerButtons = pager.getElement().getElementsByTagName("img");
    for (int i = 0; i < pagerButtons.getLength(); i++) {
      Element button = pagerButtons.getItem(i);
      String buttonLabel = button.getAttribute("aria-label");
      if (buttonLabel.equals(imageButtonsConstants.firstPage())) {
        UIObject.ensureDebugId(button, baseID, "firstPage");
      }
      if (buttonLabel.equals(imageButtonsConstants.prevPage())) {
        UIObject.ensureDebugId(button, baseID, "prevPage");
      }
      if (buttonLabel.equals(imageButtonsConstants.nextPage())) {
        UIObject.ensureDebugId(button, baseID, "nextPage");
      }
      if (buttonLabel.equals(imageButtonsConstants.fastForward())) {
        UIObject.ensureDebugId(button, baseID, "fastForward");
      }
      if (buttonLabel.equals(imageButtonsConstants.lastPage())) {
        UIObject.ensureDebugId(button, baseID, "lastPage");
      }
    }

    table.ensureDebugId(baseID + "-table");
  }
}