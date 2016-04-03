package com.mayhew3.drafttower.shared;

import dagger.Module;
import dagger.Provides;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Dependency bindings shared between client and server.
 */
@Module
public class SharedModule {
  @Qualifier
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public @interface NumTeams {}

  @Provides @NumTeams
  public static int getNumTeams() {
    return 10;
  }
}