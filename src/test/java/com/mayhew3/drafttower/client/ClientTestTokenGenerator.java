package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.server.TokenGenerator;

/**
 * GWT-compatible token generator for client tests.
 */
public class ClientTestTokenGenerator implements TokenGenerator {
  int lastToken = 0;

  @Override
  public String get() {
    return Integer.toString(lastToken++);
  }
}