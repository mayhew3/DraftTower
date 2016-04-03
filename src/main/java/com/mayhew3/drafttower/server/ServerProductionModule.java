package com.mayhew3.drafttower.server;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.CurrentTimeProviderImpl;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Live dependency bindings.
 */
@Module
public class ServerProductionModule {
  @Provides
  public static DataSource getDatabase() {
    Context initCtx = null;
    try {
      initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      return (DataSource) envCtx.lookup("jdbc/MySQL");
    } catch (NamingException e) {
      throw new RuntimeException(e);
    }
  }

  @Provides @Singleton
  public static BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Provides
  public static CurrentTimeProvider getCurrentTimeProvider(CurrentTimeProviderImpl impl) {
    return impl;
  }

  @Provides @Singleton
  public static DraftTimer getDraftTimer(DraftTimerImpl impl) {
    return impl;
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
  public static PlayerDataSource getPlayerDataSource(PlayerDataSourceImpl impl) {
    return impl;
  }

  @Provides @Singleton
  public static PredictionModel getPredictionModel(PredictionModelImpl impl) {
    return impl;
  }

  @Provides
  public static TeamDataSource getTeamDataSource(TeamDataSourceImpl impl) {
    return impl;
  }

  @Provides
  public static TokenGenerator getTokenGenerator(TokenGeneratorImpl impl) {
    return impl;
  }
}