package com.mayhew3.drafttower.client.graphs;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link BarGraphsPresenter}.
 */
public class BarGraphsPresenterTest {

  private BarGraphsPresenter presenter;
  private BeanFactory beanFactory;
  private TeamsInfo teamsInfo;
  private BarGraphsView view;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(true);
    ServerRpc serverRpc = Mockito.mock(ServerRpc.class);
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return ((Function<GraphsData, Void>) invocation.getArguments()[1]).apply(createGraphsData());
      }
    }).when(serverRpc).sendGraphsRequest(
        Mockito.<AutoBean<GetGraphsDataRequest>>any(),
        Mockito.<Function<GraphsData, Void>>any());
    presenter = new BarGraphsPresenter(
        serverRpc,
        teamsInfo,
        beanFactory,
        Mockito.mock(EventBus.class));
    view = Mockito.mock(BarGraphsView.class);
    presenter.setView(view);
  }

  private GraphsData createGraphsData() {
    GraphsData graphsData = beanFactory.createGraphsData().as();
    if (Scoring.CATEGORIES) {
      graphsData.setMyValues(new ImmutableMap.Builder<PlayerColumn, Float>()
          .put(PlayerColumn.HR, 1.0f)
          .put(PlayerColumn.RBI, 2.0f)
          .put(PlayerColumn.RHR, 3.0f)
          .build());
      graphsData.setAvgValues(new ImmutableMap.Builder<PlayerColumn, Float>()
          .put(PlayerColumn.HR, 4.0f)
          .put(PlayerColumn.RBI, 5.0f)
          .put(PlayerColumn.RHR, 6.0f)
          .build());
    } else {
      graphsData.setTeamValues(new ImmutableMap.Builder<String, Float>()
          .put("1", 100f)
          .put("2", 200f)
          .put("3", 300f)
          .put("4", 400f)
          .put("5", 500f)
          .put("6", 600f)
          .put("7", 700f)
          .put("8", 800f)
          .put("9", 900f)
          .put("10", 1000f)
          .build());
    }
    return graphsData;
  }

  @Test
  public void testStatusChangeWhenNotActive() {
    presenter.onDraftStatusChanged(Mockito.mock(DraftStatusChangedEvent.class));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testNoUpdateWhenNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    presenter.setActive(true);
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testUpdateOnSetActive() {
    presenter.setActive(true);
    Mockito.verify(view).clear();
    if (Scoring.CATEGORIES) {
      Mockito.verify(view).updateBar(PlayerColumn.HR, 1.0f, 4.0f);
      Mockito.verify(view).updateBar(PlayerColumn.RBI, 2.0f, 5.0f);
      Mockito.verify(view).updateBar(PlayerColumn.RHR, 3.0f, 6.0f);
    } else {
      Mockito.verify(view).updateBar(PlayerColumn.WIZARD,
          100f, 200f, 300f, 400f, 500f, 600f, 700f, 800f, 900f, 1000f);
    }
  }

  @Test
  public void testNoUpdateOnSetInactive() {
    presenter.setActive(false);
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testUpdateOnDraftStatusChange() {
    presenter.setActive(true);
    Mockito.reset(view);
    presenter.onDraftStatusChanged(Mockito.mock(DraftStatusChangedEvent.class));
    Mockito.verify(view).clear();
    if (Scoring.CATEGORIES) {
      Mockito.verify(view).updateBar(PlayerColumn.HR, 1.0f, 4.0f);
      Mockito.verify(view).updateBar(PlayerColumn.RBI, 2.0f, 5.0f);
      Mockito.verify(view).updateBar(PlayerColumn.RHR, 3.0f, 6.0f);
    } else {
      Mockito.verify(view).updateBar(PlayerColumn.WIZARD,
          100f, 200f, 300f, 400f, 500f, 600f, 700f, 800f, 900f, 1000f);
    }
  }
}