package com.mayhew3.drafttower.client;

import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * {@link Matcher} performing deep equals comparisons on autobeans.
 */
public class AutoBeanMatcher<T> extends BaseMatcher<T> {

  private final T expected;

  public AutoBeanMatcher(T expected) {
    this.expected = expected;
  }

  @Override
  public boolean matches(Object actual) {
    return AutoBeanUtils.deepEquals(
        AutoBeanUtils.getAutoBean(expected),
        AutoBeanUtils.getAutoBean(actual));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(expected);
  }
}