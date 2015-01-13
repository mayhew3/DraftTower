package com.mayhew3.drafttower.client.graphs;

import com.google.common.base.Function;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.GetGraphsDataRequest;
import com.mayhew3.drafttower.shared.GraphsData;
import com.mayhew3.drafttower.shared.PlayerColumn;

import javax.inject.Inject;

/**
 * Presenter for team comparison graphs.
 */
public class BarGraphsPresenter implements DraftStatusChangedEvent.Handler {

  private final ServerRpc serverRpc;
  private final TeamsInfo teamsInfo;
  private final BeanFactory beanFactory;

  private BarGraphsView view;
  private boolean active;

  @Inject
  public BarGraphsPresenter(ServerRpc serverRpc,
        TeamsInfo teamsInfo,
        BeanFactory beanFactory,
        EventBus eventBus) {
    this.serverRpc = serverRpc;
    this.teamsInfo = teamsInfo;
    this.beanFactory = beanFactory;

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public void setView(BarGraphsView view) {
    this.view = view;
  }

  public void setActive(boolean active) {
    this.active = active;
    if (active) {
      update();
    }
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    if (active) {
      update();
    }
  }

  private void update() {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    AutoBean<GetGraphsDataRequest> requestBean =
        beanFactory.createGetGraphsDataRequest();
    GetGraphsDataRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());
    serverRpc.sendGraphsRequest(requestBean, new Function<GraphsData, Void>() {
      @Override
      public Void apply(GraphsData graphsData) {
        view.clear();
        for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
          if (graphStat == PlayerColumn.WIZARD) {
            Float[] values = new Float[10];
            for (int i = 0; i < 10; i++) {
              values[i] = graphsData.getTeamValues().get(Integer.toString(i + 1));
            }
            view.updateBar(PlayerColumn.WIZARD, values);
          } else {
            Float myValue = graphsData.getMyValues().get(graphStat);
            Float avgValue = graphsData.getAvgValues().get(graphStat);
            view.updateBar(graphStat, myValue, avgValue);
          }
        }
        return null;
      }
    });
  }
}