package com.mayhew3.drafttower.server;

import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.server.BindingAnnotations.DraftTimerListenerList;
import com.mayhew3.drafttower.server.DraftTimer.Listener;
import com.mayhew3.drafttower.shared.BeanFactory;
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
    install(new GinModuleAdapter(new SharedModule()));
    install(new GinModuleAdapter(new ServerTestSafeModule()));
    install(new GinModuleAdapter(new TestServerModule()));
    bind(DraftTowerWebSocket.class).to(DraftTowerWebSocketServlet.class).in(Singleton.class);
    bind(Lock.class).to(LockImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);

    install(new GuiceBerryModule());
    bind(GuiceBerryEnvMain.class).to(SimTestMain.class);
  }
}