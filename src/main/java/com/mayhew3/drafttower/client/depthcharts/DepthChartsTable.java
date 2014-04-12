package com.mayhew3.drafttower.client.depthcharts;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import java.util.Collection;

/**
 * Table displaying all rosters so far.
 */
public class DepthChartsTable extends CellTable<Integer>  {

  private final DepthChartsPresenter presenter;

  @Inject
  public DepthChartsTable(final TeamsInfo teamsInfo,
      DepthChartsPresenter presenter) {
    setPageSize(Integer.MAX_VALUE);
    addColumn(new TextColumn<Integer>() {
      @Override
      public String getValue(Integer team) {
        return teamsInfo.getShortTeamName(team);
      }
    }, "Team");
    for (Position position : RosterUtil.POSITIONS_AND_COUNTS.keySet()) {
      addColumn(createPositionColumn(position), position.getShortName());
    }

    this.presenter = presenter;
    presenter.addDataDisplay(this);
  }

  private Column<Integer, SafeHtml> createPositionColumn(final Position position) {
    return new Column<Integer, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Integer team) {
        Collection<DraftPick> picks = presenter.getPicks(team, position);
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
}