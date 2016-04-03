package com.mayhew3.drafttower.server;

import javax.inject.Inject;
import java.util.UUID;

/**
 * UUID-based token generation.
 */
public class TokenGeneratorImpl implements TokenGenerator {

  @Inject
  public TokenGeneratorImpl() {}

  @Override
  public String get() {
    return UUID.randomUUID().toString();
  }
}