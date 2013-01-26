package com.mayhew3.drafttower.shared;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * Factory interface for creating {@link AutoBean}s.
 */
public interface BeanFactory extends AutoBeanFactory {
  AutoBean<DraftStatus> createDraftStatus();
  AutoBean<DraftCommand> createDraftCommand();
  AutoBean<UnclaimedPlayerListRequest> createUnclaimedPlayerListRequest();
  AutoBean<UnclaimedPlayerListResponse> createUnclaimedPlayerListResponse();
  AutoBean<Player> createPlayer();
}