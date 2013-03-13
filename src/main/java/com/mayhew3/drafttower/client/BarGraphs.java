package com.mayhew3.drafttower.client;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.*;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.BarChart;
import com.google.gwt.visualization.client.visualizations.BarChart.Options;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.GraphsUrl;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.GetGraphsDataRequest;
import com.mayhew3.drafttower.shared.GraphsData;
import com.mayhew3.drafttower.shared.PlayerColumn;

import java.util.Map;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Widget containing team comparison bar graphs.
 */
public class BarGraphs extends Composite implements DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String me();
      String avg();
      String graph();
    }

    @Source("BarGraphs.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private static final Map<PlayerColumn, Float> MAX_VALUES = ImmutableMap.<PlayerColumn, Float>builder()
      .put(HR, 400f)
      .put(RBI, 1000f)
      .put(OBP, 0.5f)
      .put(SLG, 0.6f)
      .put(RHR, 800f)
      .put(SBCS, 150f)
      .put(INN, 1800f)
      .put(K, 1500f)
      .put(ERA, 4.5f)
      .put(WHIP, 1.4f)
      .put(WL, 50f)
      .put(S, 120f)
      .build();

  private final String graphsUrl;
  private final TeamsInfo teamsInfo;
  private final BeanFactory beanFactory;
  private final FlowPanel container;

  private boolean apiLoaded;

  @Inject
  public BarGraphs(@GraphsUrl String graphsUrl,
      TeamsInfo teamsInfo,
      BeanFactory beanFactory,
      EventBus eventBus) {
    this.graphsUrl = graphsUrl;
    this.teamsInfo = teamsInfo;
    this.beanFactory = beanFactory;

    container = new FlowPanel();
    container.setSize("820px", "750px");
    addLabels();

    VisualizationUtils.loadVisualizationApi(new Runnable() {
      @Override
      public void run() {
        apiLoaded = true;
      }
    }, BarChart.PACKAGE);

    addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          update();
        }
      }
    });
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);

    initWidget(container);
  }

  private void addLabels() {
    FlowPanel labels = new FlowPanel();
    Label me = new Label("Me");
    me.setStyleName(CSS.me());
    labels.add(me);
    Label avg = new Label("Avg");
    avg.setStyleName(CSS.avg());
    labels.add(avg);
    container.add(labels);
  }

  private Options getOptions(PlayerColumn graphStat) {
    Options options = Options.create();
    options.setColors("#aa4643", "#4572a7");
    options.setSize(400, 100);
    options.setEnableTooltip(true);
    options.setShowCategories(true);
    options.setTitle(graphStat.getLongName());
    options.setOption("titleFontSize", 12);
    options.setLegend(LegendPosition.NONE);
    options.setMin(0);
    options.setMax(MAX_VALUES.get(graphStat));
    return options;
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    if (isAttached()) {
      update();
    }
  }

  private void update() {
    if (!teamsInfo.isLoggedIn() || !apiLoaded) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, graphsUrl);
    try {
      AutoBean<GetGraphsDataRequest> requestBean =
          beanFactory.createGetGraphsDataRequest();
      GetGraphsDataRequest request = requestBean.as();
      request.setTeamToken(teamsInfo.getTeamToken());

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              GraphsData graphsData =
                  AutoBeanCodex.decode(beanFactory, GraphsData.class, response.getText()).as();
              container.clear();
              addLabels();
              for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
                DataTable data = DataTable.create();
                data.addColumn(ColumnType.STRING);
                data.addColumn(ColumnType.NUMBER, "Me");
                data.addColumn(ColumnType.NUMBER, "Avg");
                data.addRows(1);
                data.setValue(0, 0, "");
                data.setValue(0, 1, graphsData.getMyValues().get(graphStat));
                data.setValue(0, 2, graphsData.getAvgValues().get(graphStat));
                BarChart barChart = new BarChart(data, getOptions(graphStat));
                barChart.addStyleName(CSS.graph());
                container.add(barChart);
              }
            }

            @Override
            public void onError(Request request, Throwable exception) {
              // TODO
            }
          });
    } catch (RequestException e) {
      // TODO
      throw new RuntimeException(e);
    }
  }
}