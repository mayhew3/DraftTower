package com.mayhew3.drafttower.client;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
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
import com.mayhew3.drafttower.client.events.IsUsersAutoPickTableSpecEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickTableSpecEvent;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.TableSpec;

import java.util.Map;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Widget containing player table, position filter buttons, and paging controls.
 */
public class PlayerTablePanel extends Composite implements
    IsUsersAutoPickTableSpecEvent.Handler,
    LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String hideInjuries();
      String autoPick();
      String filterButton();
    }

    @Source("PlayerTablePanel.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private static final Position[] POSITIONS = {
      UNF, null, C, FB, SB, TB, SS, OF, DH, P
  };

  private Map<PlayerDataSet, ToggleButton> dataSetButtons = Maps.newEnumMap(PlayerDataSet.class);
  private ToggleButton allButton;
  private Map<Position, ToggleButton> positionFilterButtons = Maps.newEnumMap(Position.class);
  private final TextBox nameSearch;
  private final CheckBox useForAutoPick;
  private final Button copyRanks;
  private final UnclaimedPlayerTable table;

  @Inject
  public PlayerTablePanel(final UnclaimedPlayerTable table, final EventBus eventBus) {
    this.table = table;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    CheckBox hideInjuries = new CheckBox("Hide injured players");
    hideInjuries.setStyleName(CSS.hideInjuries());
    hideInjuries.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        table.setHideInjuries(event.getValue());
      }
    });
    container.add(hideInjuries);

    HorizontalPanel dataSetButtonPanel = new HorizontalPanel();
    for (final PlayerDataSet playerDataSet : PlayerDataSet.values()) {
      ToggleButton button = new ToggleButton(playerDataSet.getDisplayName(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          updateDataSetButtons(playerDataSet);
          table.setPlayerDataSet(playerDataSet);
        }
      });
      button.addStyleName(CSS.filterButton());
      if (dataSetButtons.isEmpty()) {
        button.setDown(true);
      }
      dataSetButtons.put(playerDataSet, button);
      dataSetButtonPanel.add(button);
    }
    container.add(dataSetButtonPanel);

    HorizontalPanel filterButtons = new HorizontalPanel();
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
    container.add(filterButtons);

    container.add(new InlineLabel("Search: "));
    nameSearch = new TextBox();
    final InlineLabel clear = new InlineLabel(" X ");
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
    container.add(nameSearch);
    container.add(clear);

    useForAutoPick = new CheckBox("Use this order for auto-pick");
    useForAutoPick.setStyleName(CSS.autoPick());
    useForAutoPick.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        eventBus.fireEvent(new SetAutoPickTableSpecEvent(table.getTableSpec()));
      }
    });
    container.add(useForAutoPick);

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
    container.add(copyRanks);

    updateCopyRanksEnabled();
    table.addColumnSortHandler(new Handler() {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        updateCopyRanksEnabled();
      }
    });

    SimplePager pager = new SimplePager();
    pager.setDisplay(table);
    container.add(pager);

    container.add(table);

    initWidget(container);

    eventBus.addHandler(IsUsersAutoPickTableSpecEvent.TYPE, this);
    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  private void updateCopyRanksEnabled() {
    copyRanks.setEnabled(table.getSortedColumn() != PlayerColumn.MYRANK);
  }

  private void updateDataSetButtons(PlayerDataSet playerDataSet) {
    for (Entry<PlayerDataSet, ToggleButton> buttonEntry : dataSetButtons.entrySet()) {
      buttonEntry.getValue().setDown(buttonEntry.getKey() == playerDataSet);
    }
  }

  @Override
  public void onSetAutoPickTableSpec(IsUsersAutoPickTableSpecEvent event) {
    useForAutoPick.setValue(event.isUsersAutoPickTableSpec());
    useForAutoPick.setEnabled(!event.isUsersAutoPickTableSpec());
  }

  @Override
  public void onLogin(LoginEvent event) {
    updateDataSetButtons(event.getLoginResponse().getInitialTableSpec().getPlayerDataSet());
  }
}