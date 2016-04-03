package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.shared.PlayerColumn;

import javax.inject.Provider;

/**
* Class description...
*/
class PlayerColumnHeader extends Header<SafeHtml> {

  public interface Templates extends SafeHtmlTemplates {
    @Template("<span title=\"{0}\">{1}</span>")
    SafeHtml header(String longName, SafeHtml shortName);

    @Template("<span class=\"{0}\">" +
        "<span class=\"{1}\">{2}</span>" +
        "/" +
        "<span class=\"{3}\">{4}</span>" +
        "</span>")
    SafeHtml splitHeader(String className,
        String batterStatClassName,
        String batterShortName,
        String pitcherClassName,
        String pitcherShortName);
  }
  private static final Templates TEMPLATES = GWT.create(Templates.class);

  private final PlayerColumn column;
  private final PlayerColumn pitcherColumn;
  private final Provider<PositionFilter> positionFilterProvider;

  public PlayerColumnHeader(PlayerColumn column, PlayerColumn pitcherColumn,
      Provider<PositionFilter> positionFilterProvider) {
    super(new SafeHtmlCell());
    this.column = column;
    this.pitcherColumn = pitcherColumn;
    this.positionFilterProvider = positionFilterProvider;
  }

  @Override
  public SafeHtml getValue() {
    return TEMPLATES.header(getLongName(), getShortName());
  }

  private SafeHtml getShortName() {
    if (pitcherColumn != null) {
      if (positionFilterProvider.get().isPitcherFilter()
          || column.getShortName().equals(pitcherColumn.getShortName())) {
        return new SafeHtmlBuilder()
            .appendEscaped(pitcherColumn.getShortName())
            .toSafeHtml();
      }
      if (positionFilterProvider.get().isPitchersAndBattersFilter()) {
        return TEMPLATES.splitHeader(UnclaimedPlayerTable.CSS.splitHeader(),
            UnclaimedPlayerTable.CSS.batterStat(),
            column.getShortName(),
            UnclaimedPlayerTable.CSS.pitcherStat(),
            pitcherColumn.getShortName());
      }
    }
    return new SafeHtmlBuilder()
        .appendEscaped(column.getShortName())
        .toSafeHtml();
  }

  private String getLongName() {
    if (pitcherColumn != null) {
      if (positionFilterProvider.get().isPitcherFilter()) {
        return pitcherColumn.getLongName();
      }
      if (positionFilterProvider.get().isPitchersAndBattersFilter() &&
          !column.getLongName().equals(pitcherColumn.getLongName())) {
        return column.getLongName() + "/" + pitcherColumn.getLongName();
      }
    }
    return column.getLongName();
  }
}