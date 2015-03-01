package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.MaxClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.MinClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.PlayerDataSet;

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
      @AutoPickWizards Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables,
      @MinClosers Map<TeamDraftOrder, Integer> minClosers,
      @MaxClosers Map<TeamDraftOrder, Integer> maxClosers,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) throws DataSourceException {
    super(dataSource, beanFactory, teamDataSource, autoPickWizardTables, minClosers, maxClosers, teamTokens);
  }

  @Override
  protected void warmCaches() throws DataSourceException {
    // No-op.
  }
}