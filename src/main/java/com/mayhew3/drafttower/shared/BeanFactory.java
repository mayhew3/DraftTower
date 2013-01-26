package com.mayhew3.drafttower.shared;

import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * Factory interface for creating {@link AutoBean}s.
 */
@Singleton
public interface BeanFactory extends AutoBeanFactory {
  AutoBean<DraftStatus> createDraftStatus();
}