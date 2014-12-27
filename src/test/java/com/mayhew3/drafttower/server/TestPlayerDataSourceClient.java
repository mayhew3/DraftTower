package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-safe {@link TestPlayerDataSource}.
 */
@Singleton
public class TestPlayerDataSourceClient extends TestPlayerDataSource {
  @Inject
  public TestPlayerDataSourceClient(BeanFactory beanFactory) {
    super(beanFactory);
  }

  @Override
  protected List<DraftPick> createDraftPicksList() {
    return new ArrayList<>();
  }
}