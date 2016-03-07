package com.mayhew3.drafttower.server.database.player;

public abstract class FieldConversion<T> {
  abstract T parseFromString(String value) throws NumberFormatException;
}
