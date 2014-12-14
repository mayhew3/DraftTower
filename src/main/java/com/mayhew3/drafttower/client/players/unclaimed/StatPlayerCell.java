package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Provider;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

/**
 * Table cell for a scoring stat.
 */
public class StatPlayerCell extends AbstractSafeHtmlCell<PlayerValue> {
  public StatPlayerCell(Provider<EnumSet<Position>> positionFilterProvider) {
    super(new PlayerValueRenderer(positionFilterProvider));
  }

  @Override
  protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(value);
    }
  }
}