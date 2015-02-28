package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.mayhew3.drafttower.shared.Player;

/**
* Column displaying player injuries.
*/
public class InjuryColumn extends Column<Player, SafeHtml> {

  public interface Templates extends SafeHtmlTemplates {
    @Template("<span class=\"{0}\" data-tooltip=\"{1}\">+</span>")
    SafeHtml injury(String className, String injury);
  }
  private static final Templates TEMPLATES = GWT.create(Templates.class);

  public InjuryColumn() {
    super(new SafeHtmlCell());
  }

  @Override
  public SafeHtml getValue(Player player) {
    if (player.getInjury() != null) {
      return TEMPLATES.injury(UnclaimedPlayerTable.CSS.injury(), player.getInjury());
    } else {
      return new SafeHtmlBuilder().appendHtmlConstant("&nbsp;&nbsp;&nbsp;").toSafeHtml();
    }
  }
}