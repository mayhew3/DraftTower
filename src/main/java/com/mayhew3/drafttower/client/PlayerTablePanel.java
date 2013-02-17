package com.mayhew3.drafttower.client;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;

import java.util.Map;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Widget containing player table, position filter buttons, and paging controls.
 */
public class PlayerTablePanel extends Composite {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String hideInjuries();
      String filterButton();
      String table();
    }

    @Source("PlayerTablePanel.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private static final Position[] POSITIONS = {
      null, C, FB, SB, TB, SS, OF, DH, P, SP, RP, UNF
  };

  private Map<PlayerDataSet, ToggleButton> projectionSystemButtons = Maps.newEnumMap(PlayerDataSet.class);
  private ToggleButton allButton;
  private Map<Position, ToggleButton> positionFilterButtons = Maps.newEnumMap(Position.class);

  @Inject
  public PlayerTablePanel(final PlayerTable table) {
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

    HorizontalPanel projectionButtons = new HorizontalPanel();
    for (final PlayerDataSet playerDataSet : PlayerDataSet.values()) {
      ToggleButton button = new ToggleButton(playerDataSet.getDisplayName(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          for (Entry<PlayerDataSet, ToggleButton> buttonEntry : projectionSystemButtons.entrySet()) {
            buttonEntry.getValue().setDown(buttonEntry.getKey() == playerDataSet);
          }
          table.setPlayerDataSet(playerDataSet);
        }
      });
      button.addStyleName(CSS.filterButton());
      if (projectionSystemButtons.isEmpty()) {
        button.setDown(true);
      }
      projectionSystemButtons.put(playerDataSet, button);
      projectionButtons.add(button);
    }
    container.add(projectionButtons);

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
        button.setDown(true);
      }
      filterButtons.add(button);
    }
    container.add(filterButtons);

    SimplePager pager = new SimplePager();
    pager.setDisplay(table);
    container.add(pager);

    table.addStyleName(CSS.table());
    container.add(table);

    initWidget(container);
  }
}