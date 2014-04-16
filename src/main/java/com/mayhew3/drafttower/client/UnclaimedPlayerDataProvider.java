package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;

/**
 * Data provider for player tables.
 */
public abstract class UnclaimedPlayerDataProvider extends AsyncDataProvider<Player> implements
    ChangePlayerRankEvent.Handler,
    SetAutoPickWizardEvent.Handler,
    CopyAllPlayerRanksEvent.Handler {

  protected final BeanFactory beanFactory;
  protected final ServerRpc serverRpc;
  protected final TeamsInfo teamsInfo;

  @Inject
  public UnclaimedPlayerDataProvider(
      BeanFactory beanFactory,
      ServerRpc serverRpc,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.serverRpc = serverRpc;
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
    AutoBean<ChangePlayerRankRequest> requestBean =
        beanFactory.createChangePlayerRankRequest();
    ChangePlayerRankRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setPlayerId(event.getPlayerId());
    request.setNewRank(event.getNewRank());
    request.setPrevRank(event.getPrevRank());

    serverRpc.sendChangePlayerRankRequest(requestBean, new Runnable() {
      @Override
      public void run() {
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
    AutoBean<CopyAllPlayerRanksRequest> requestBean =
        beanFactory.createCopyAllPlayerRanksRequest();
    CopyAllPlayerRanksRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setTableSpec(event.getTableSpec());

    serverRpc.sendCopyRanksRequest(requestBean, new Runnable() {
      @Override
      public void run() {
        for (HasData<Player> dataDisplay : getDataDisplays()) {
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
    AutoBean<SetWizardTableRequest> requestBean =
        beanFactory.createSetAutoPickWizardRequest();
    SetWizardTableRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setPlayerDataSet(event.getWizardTable());

    serverRpc.sendSetWizardTableRequest(requestBean, new Runnable() {
      @Override
      public void run() {
        for (HasData<Player> dataDisplay : getDataDisplays()) {
          dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
        }
      }
    });
  }
}