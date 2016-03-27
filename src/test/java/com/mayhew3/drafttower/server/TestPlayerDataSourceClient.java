package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-safe {@link TestPlayerDataSource}.
 */
public class TestPlayerDataSourceClient extends TestPlayerDataSource {
  public TestPlayerDataSourceClient(BeanFactory beanFactory) {
    super(beanFactory);
  }

  @Override
  protected List<DraftPick> createDraftPicksList() {
    return new ArrayList<>();
  }
}