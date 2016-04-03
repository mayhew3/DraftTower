package com.mayhew3.drafttower.server;

import org.apache.http.protocol.UriPatternMatcher;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.Servlet;

/**
 * Dependency provider for mapping from URIs to servlets.
 */
@Singleton
public class ServletMapProvider implements Provider<UriPatternMatcher<Servlet>> {

  private UriPatternMatcher<Servlet> uriPatternMatcher;

  @Inject
  public ServletMapProvider() {}

  public void setUriPatternMatcher(UriPatternMatcher<Servlet> uriPatternMatcher) {
    this.uriPatternMatcher = uriPatternMatcher;
  }

  @Override
  public UriPatternMatcher<Servlet> get() {
    return uriPatternMatcher;
  }
}
