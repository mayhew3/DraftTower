package com.mayhew3.drafttower.shared;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Dependency bindings shared between client and server.
 */
public class SharedModule extends AbstractGinModule {
  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface Commissioner {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface NumTeams {}

  @Provides @Commissioner
  public int getCommissionerTeam() {
    return 1;  // TODO(m3): commisioner's team goes here
  }

  @Provides @NumTeams
  public int getNumTeams() {
    return 1;
  }

  @Override
  protected void configure() {
  }
}