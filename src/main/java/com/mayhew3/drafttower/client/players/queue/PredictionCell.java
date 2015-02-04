package com.mayhew3.drafttower.client.players.queue;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.mayhew3.drafttower.shared.QueueEntry;

/**
 * {@link TextCell} which displays a tooltip on hover.
 */
public class PredictionCell extends AbstractSafeHtmlCell<QueueEntry> {

  private static final float PICK_PROBABILITY_THRESHOLD = 0.5f;
  private static final float PICK_PROBABILITY_THRESHOLD_HIGH = 0.8f;
  private static final float PICK_PROBABILITY_THRESHOLD_MAX = 1.0f;

  public interface PredictionTemplate extends SafeHtmlTemplates {
    @Template("<div class=\"{1}\" data-tooltip=\"{0}\">!</div>")
    SafeHtml prediction(String tooltip, String classNames);
  }

  private static final PredictionTemplate TEMPLATE = GWT.create(PredictionTemplate.class);

  private static class PredictionRenderer extends AbstractSafeHtmlRenderer<QueueEntry> {

    @Override
    public SafeHtml render(QueueEntry queueEntry) {
      if (queueEntry.getPickProbability() > PICK_PROBABILITY_THRESHOLD) {
        String classNames = QueueTable.CSS.warning();
        if (queueEntry.getPickProbability() > PICK_PROBABILITY_THRESHOLD_HIGH) {
          classNames += " " + QueueTable.CSS.warningHigh();
        }
        String tooltip;
        if (queueEntry.getPickProbability() >= PICK_PROBABILITY_THRESHOLD_MAX) {
          tooltip = "Draft Wizard predicts that " + queueEntry.getPlayerName() + " " +
              "will definitely be picked before your next pick.";
        } else {
          tooltip = "Draft Wizard predicts a " +
              NumberFormat.getPercentFormat().format(queueEntry.getPickProbability()) +
              " chance that " + queueEntry.getPlayerName() + " will be picked before " +
              "your next pick.";
        }
        return TEMPLATE.prediction(tooltip, classNames);
      } else {
        return new SafeHtmlBuilder().toSafeHtml();
      }
    }
  }

  public PredictionCell() {
    super(new PredictionRenderer());
  }

  @Override
  protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(value);
    }
  }
}