package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.server.TokenGenerator;

import javax.inject.Inject;

/**
 * GWT-compatible token generator for client tests.
 */
public class ClientTestTokenGenerator implements TokenGenerator {
  private int lastToken = 0;

  @Inject
  public ClientTestTokenGenerator() {}

  @Override
  public String get() {
    return Integer.toString(lastToken++);
  }
}