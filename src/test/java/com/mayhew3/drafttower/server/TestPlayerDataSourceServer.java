package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-only {@link TestPlayerDataSource}, thread-safe.
 */
public class TestPlayerDataSourceServer extends TestPlayerDataSource {
  public TestPlayerDataSourceServer(BeanFactory beanFactory) {
    super(beanFactory);
  }

  @Override
  protected List<DraftPick> createDraftPicksList() {
    return new CopyOnWriteArrayList<>();
  }
}