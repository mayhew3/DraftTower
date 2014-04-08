package com.mayhew3.drafttower.server;

import java.util.UUID;

/**
 * UUID-based token generation.
 */
public class TokenGeneratorImpl implements TokenGenerator {
  @Override
  public String get() {
    return UUID.randomUUID().toString();
  }
}