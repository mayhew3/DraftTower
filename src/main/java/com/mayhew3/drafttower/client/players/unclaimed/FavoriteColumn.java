package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.mayhew3.drafttower.shared.Player;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

/**
* Column displaying favorite star.
*/
public class FavoriteColumn extends Column<Player, Boolean> {

  public interface Templates extends SafeHtmlTemplates {
    @Template("<span>{0}</span>")
    SafeHtml favorite(SafeHtml starEntity);
  }
  private static final Templates TEMPLATES = GWT.create(Templates.class);

  private static class Renderer extends AbstractSafeHtmlRenderer<Boolean> {

    private SafeHtml filledStar;
    private SafeHtml emptyStar;

    private Renderer() {
      filledStar = new SafeHtmlBuilder().appendHtmlConstant("&starf;").toSafeHtml();
      emptyStar = new SafeHtmlBuilder().appendHtmlConstant("&star;").toSafeHtml();
    }

    @Override
    public SafeHtml render(Boolean value) {
      if (value) {
        return TEMPLATES.favorite(filledStar);
      } else {
        return TEMPLATES.favorite(emptyStar);
      }
    }
  }

  private static final Renderer RENDERER = new Renderer();

  private static class Cell extends AbstractSafeHtmlCell<Boolean> {

    public Cell() {
      super(RENDERER, CLICK);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Boolean value,
        NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      if (CLICK.equals(event.getType())) {
        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
          return;
        }
        if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
          // Ignore clicks that occur outside of the main element.
          valueUpdater.update(value);
        }
      }
    }

    @Override
    protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
      if (data != null) {
        sb.append(data);
      }
    }
  }

  public FavoriteColumn() {
    super(new Cell());
    setCellStyleNames(UnclaimedPlayerTable.CSS.favoriteColumn());
  }

  @Override
  public Boolean getValue(Player player) {
    return player.isFavorite();
  }
}