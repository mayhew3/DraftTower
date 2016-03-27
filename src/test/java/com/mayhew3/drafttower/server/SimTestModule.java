package com.mayhew3.drafttower.server;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.server.BindingAnnotations.DraftTimerListenerList;
import com.mayhew3.drafttower.server.DraftTimer.Listener;
import com.mayhew3.drafttower.server.SimTest.CommissionerTeam;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.SharedModule;
import dagger.Module;
import dagger.Provides;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Dependency module for simulated client tests.
 */
@Module(includes = {
    SharedModule.class,
    ServerTestSafeModule.class,
    TestServerModule.class,
})
public class SimTestModule {

  @Provides @DraftTimerListenerList
  public static List<Listener> getDraftTimerListenerList() {
    return new ArrayList<>();
  }

  @Provides @Singleton
  public static BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Provides @CommissionerTeam
  public static String getCommissionerTeam() {
    return "1";
  }

  @Provides @Singleton
  public static DraftTowerWebSocket getDraftTowerWebSocket(DraftTowerWebSocketServlet impl) {
    return impl;
  }

  @Provides @Singleton
  public static Lock getLock(LockImpl impl) {
    return impl;
  }

  @Provides
  public static TokenGenerator getTokenGenerator(TokenGeneratorImpl impl) {
    return impl;
  }

  @Provides @Nullable
  public static DataSource dataSourceUnused() {
    return null;
  }
}