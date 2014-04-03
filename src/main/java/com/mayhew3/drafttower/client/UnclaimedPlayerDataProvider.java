package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.server.GinBindingAnnotations.ChangePlayerRankUrl;
import com.mayhew3.drafttower.server.GinBindingAnnotations.CopyPlayerRanksUrl;
import com.mayhew3.drafttower.server.GinBindingAnnotations.SetAutoPickWizardUrl;
import com.mayhew3.drafttower.server.GinBindingAnnotations.UnclaimedPlayerInfoUrl;
import com.mayhew3.drafttower.shared.*;

import java.util.Set;

/**
 * Data provider for player tables.
 */
public abstract class UnclaimedPlayerDataProvider extends AsyncDataProvider<Player> implements
    ChangePlayerRankEvent.Handler,
    SetAutoPickWizardEvent.Handler,
    CopyAllPlayerRanksEvent.Handler {

  protected final BeanFactory beanFactory;
  protected final String playerInfoUrl;
  private final String changePlayerRankUrl;
  private final String copyPlayerRanksUrl;
  private final String setAutoPickWizardUrl;
  protected final TeamsInfo teamsInfo;

  @Inject
  public UnclaimedPlayerDataProvider(
      BeanFactory beanFactory,
      @UnclaimedPlayerInfoUrl String playerInfoUrl,
      @ChangePlayerRankUrl String changePlayerRankUrl,
      @CopyPlayerRanksUrl String copyPlayerRanksUrl,
      @SetAutoPickWizardUrl String setAutoPickWizardUrl,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.playerInfoUrl = playerInfoUrl;
    this.changePlayerRankUrl = changePlayerRankUrl;
    this.copyPlayerRanksUrl = copyPlayerRanksUrl;
    this.setAutoPickWizardUrl = setAutoPickWizardUrl;
    this.teamsInfo = teamsInfo;

    eventBus.addHandler(ChangePlayerRankEvent.TYPE, this);
    eventBus.addHandler(SetAutoPickWizardEvent.TYPE, this);
    eventBus.addHandler(CopyAllPlayerRanksEvent.TYPE, this);
  }

  @Override
  public void onChangePlayerRank(ChangePlayerRankEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, changePlayerRankUrl);
    AutoBean<ChangePlayerRankRequest> requestBean =
        beanFactory.createChangePlayerRankRequest();
    ChangePlayerRankRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setPlayerId(event.getPlayerId());
    request.setNewRank(event.getNewRank());
    request.setPrevRank(event.getPrevRank());

    RequestCallbackWithBackoff.sendRequest(requestBuilder,
        AutoBeanCodex.encode(requestBean).getPayload(),
        new RequestCallbackWithBackoff() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            for (HasData<Player> dataDisplay : getDataDisplays()) {
              dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
            }
          }
        });
  }

  @Override
  public void onCopyAllPlayerRanks(CopyAllPlayerRanksEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, copyPlayerRanksUrl);
    AutoBean<CopyAllPlayerRanksRequest> requestBean =
        beanFactory.createCopyAllPlayerRanksRequest();
    CopyAllPlayerRanksRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setTableSpec(event.getTableSpec());

    RequestCallbackWithBackoff.sendRequest(requestBuilder,
        AutoBeanCodex.encode(requestBean).getPayload(),
        new RequestCallbackWithBackoff() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            Set<HasData<Player>> dataDisplays = getDataDisplays();
            for (HasData<Player> dataDisplay : dataDisplays) {
              dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
            }
          }
        });
  }

  @Override
  public void onSetAutoPickWizard(SetAutoPickWizardEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, setAutoPickWizardUrl);
    AutoBean<SetWizardTableRequest> requestBean =
        beanFactory.createSetAutoPickWizardRequest();
    SetWizardTableRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setPlayerDataSet(event.getWizardTable());

    RequestCallbackWithBackoff.sendRequest(requestBuilder,
        AutoBeanCodex.encode(requestBean).getPayload(),
        new RequestCallbackWithBackoff() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            for (HasData<Player> dataDisplay : getDataDisplays()) {
              dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
            }
          }
        });
  }

}