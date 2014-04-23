package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.inject.Provider;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

import static com.mayhew3.drafttower.shared.PlayerColumn.ELIG;

/**
 * Cell renderer for a {@link PlayerValue}.
 */
public class PlayerValueRenderer extends AbstractSafeHtmlRenderer<PlayerValue> {

  public interface Templates extends SafeHtmlTemplates {
    @Template("<span class=\"{0}\">{1}</span>")
    SafeHtml cell(String style, String value);
  }
  private static final Templates TEMPLATES = GWT.create(Templates.class);

  private final Provider<EnumSet<Position>> positionFilterProvider;

  public PlayerValueRenderer(Provider<EnumSet<Position>> positionFilterProvider) {
    this.positionFilterProvider = positionFilterProvider;
  }

  @Override
  public SafeHtml render(PlayerValue value) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    if (value.getValue() != null) {
      if (Position.isPitchersAndBattersFilter(positionFilterProvider.get())) {
        String style;
        if (ELIG.get(value.getPlayer()).contains(Position.P.getShortName())) {
          style = UnclaimedPlayerTable.CSS.pitcherStat();
        } else {
          style = UnclaimedPlayerTable.CSS.batterStat();
        }
        builder.append(TEMPLATES.cell(style, value.getValue()));
      } else {
        builder.appendEscaped(value.getValue());
      }
    }
    return builder.toSafeHtml();
  }
}