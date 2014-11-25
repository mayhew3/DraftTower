package com.mayhew3.drafttower.server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestWrapper;
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.server.BindingAnnotations.DraftTimerListenerList;
import com.mayhew3.drafttower.server.DraftTimer.Listener;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.SharedModule;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Guice environment for simulated client tests.
 */
public class SimTestGuiceBerryEnv extends AbstractModule {
  private static class SimTestMain implements GuiceBerryEnvMain {
    @Override
    public void run() {}
  }

  @Provides
  public TestWrapper getTestWrapper(
      final TearDownAccepter tearDownAccepter,
      final DraftStatus draftStatus,
      final PlayerDataSource playerDataSource) {
    return new TestWrapper() {
      @Override
      public void toRunBeforeTest() {
        tearDownAccepter.addTearDown(new TearDown() {
          @Override
          public void tearDown() throws Exception {
            draftStatus.getConnectedTeams().clear();
            draftStatus.setCurrentPickDeadline(0);
            draftStatus.setCurrentTeam(1);
            draftStatus.getNextPickKeeperTeams().clear();
            draftStatus.setOver(false);
            draftStatus.setPaused(false);
            draftStatus.getPicks().clear();
            draftStatus.getRobotTeams().clear();
            draftStatus.setSerialId(0);
            ((TestPlayerDataSource) playerDataSource).reset();
          }
        });
      }
    };
  }

  @Provides @DraftTimerListenerList
  public List<Listener> getDraftTimerListenerList() {
    return new ArrayList<>();
  }

  @Provides @Singleton
  public BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Override
  protected void configure() {
    install(new GuiceBerryModule());
    bind(GuiceBerryEnvMain.class).to(SimTestMain.class);

    install(new GinModuleAdapter(new SharedModule()));
    install(new GinModuleAdapter(new ServerTestSafeModule()));
    install(new GinModuleAdapter(new TestServerModule()));
    bind(DraftTowerWebSocket.class).to(DraftTowerWebSocketServlet.class).in(Singleton.class);
    bind(Lock.class).to(LockImpl.class).in(Singleton.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }
}