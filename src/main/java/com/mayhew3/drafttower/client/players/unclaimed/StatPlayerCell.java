package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.mayhew3.drafttower.client.players.PositionFilter;

import javax.inject.Provider;

/**
 * Table cell for a scoring stat.
 */
public class StatPlayerCell extends AbstractSafeHtmlCell<PlayerValue> {
  public StatPlayerCell(Provider<PositionFilter> positionFilterProvider) {
    super(new PlayerValueRenderer(positionFilterProvider));
  }

  @Override
  protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(value);
    }
  }
}