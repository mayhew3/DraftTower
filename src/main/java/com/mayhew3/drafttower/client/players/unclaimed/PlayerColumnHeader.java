package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.inject.Provider;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

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
  private final Provider<EnumSet<Position>> positionFilterProvider;

  public PlayerColumnHeader(PlayerColumn column, PlayerColumn pitcherColumn,
      Provider<EnumSet<Position>> positionFilterProvider) {
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
      if (Position.isPitcherFilter(positionFilterProvider.get())
          || column.getShortName().equals(pitcherColumn.getShortName())) {
        return new SafeHtmlBuilder()
            .appendEscaped(pitcherColumn.getShortName())
            .toSafeHtml();
      }
      if (Position.isPitchersAndBattersFilter(positionFilterProvider.get())) {
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
      if (Position.isPitcherFilter(positionFilterProvider.get())) {
        return pitcherColumn.getLongName();
      }
      if (Position.isPitchersAndBattersFilter(positionFilterProvider.get())) {
        return column.getLongName() + "/" + pitcherColumn.getLongName();
      }
    }
    return column.getLongName();
  }
}