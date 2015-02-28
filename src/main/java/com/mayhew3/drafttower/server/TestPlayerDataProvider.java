package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * {@link PlayerDataProvider} for tests which skips cache warming.
 */
@Singleton
public class TestPlayerDataProvider extends PlayerDataProvider {

  @Inject
  public TestPlayerDataProvider(
      PlayerDataSource dataSource,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) throws DataSourceException {
    super(dataSource, beanFactory, teamDataSource, teamTokens);
  }

  @Override
  protected void warmCaches() throws DataSourceException {
    // No-op.
  }
}