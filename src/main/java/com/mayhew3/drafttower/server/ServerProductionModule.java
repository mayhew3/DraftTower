package com.mayhew3.drafttower.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.CurrentTimeProviderImpl;

import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Live dependency bindings.
 */
public class ServerProductionModule extends AbstractModule {
  @Provides
  public DataSource getDatabase() throws NamingException {
    Context initCtx = new InitialContext();
    Context envCtx = (Context) initCtx.lookup("java:comp/env");
    return (DataSource) envCtx.lookup("jdbc/MySQL");
  }

  @Provides @Singleton
  public BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Override
  protected void configure() {
    bind(CurrentTimeProvider.class).to(CurrentTimeProviderImpl.class);
    bind(DraftTimer.class).to(DraftTimerImpl.class).in(Singleton.class);
    bind(DraftTowerWebSocket.class).to(DraftTowerWebSocketServlet.class).in(Singleton.class);
    bind(Lock.class).to(LockImpl.class).in(Singleton.class);
    bind(PlayerDataSource.class).to(PlayerDataSourceImpl.class);
    bind(TeamDataSource.class).to(TeamDataSourceImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }
}