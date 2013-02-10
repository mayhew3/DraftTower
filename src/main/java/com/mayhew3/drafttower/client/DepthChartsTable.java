package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Table displaying user's roster so far.
 */
public class DepthChartsTable extends CellTable<String> implements
    DraftStatusChangedEvent.Handler {

  private ListDataProvider<String> depthChartsProvider;
  private Map<String, Multimap<Position, DraftPick>> rosters;

  @Inject
  public DepthChartsTable(EventBus eventBus) {
    setPageSize(Integer.MAX_VALUE);
    addColumn(new TextColumn<String>() {
      @Override
      public String getValue(String teamName) {
        return teamName;
      }
    }, "Team");
    for (Position position : RosterUtil.POSITIONS_AND_COUNTS.keySet()) {
      addColumn(createPositionColumn(position), position.getShortName());
    }

    depthChartsProvider = new ListDataProvider<String>();
    depthChartsProvider.addDataDisplay(this);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  private Column<String, SafeHtml> createPositionColumn(final Position position) {
    return new Column<String, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(String teamName) {
        Collection<DraftPick> picks = rosters.get(teamName).get(position);
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscapedLines(
            Joiner.on('\n').join(Iterables.transform(picks, new Function<DraftPick, String>() {
              @Override
              public String apply(DraftPick input) {
                return input.getPlayerName();
              }
            })));
        return builder.toSafeHtml();
      }
    };
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    rosters = Maps.newHashMap();
    List<String> teamNames = Lists.newArrayList();
    List<DraftPick> picks = event.getStatus().getPicks();
    for (DraftPick draftPick : picks) {
      final String teamName = draftPick.getTeamName();
      if (teamNames.contains(teamName)) {
        break;
      }
      teamNames.add(teamName);
      rosters.put(teamName, RosterUtil.constructRoster(
          Lists.newArrayList(Iterables.filter(picks, new Predicate<DraftPick>() {
            @Override
            public boolean apply(DraftPick input) {
              return input.getTeamName().equals(teamName);
            }
          }))));
    }
    depthChartsProvider.setList(teamNames);
  }
}