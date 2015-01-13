package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-only {@link TestPlayerDataSource}, thread-safe.
 */
@Singleton
public class TestPlayerDataSourceServer extends TestPlayerDataSource {
  @Inject
  public TestPlayerDataSourceServer(BeanFactory beanFactory) {
    super(beanFactory);
  }

  @Override
  protected List<DraftPick> createDraftPicksList() {
    return new CopyOnWriteArrayList<>();
  }
}