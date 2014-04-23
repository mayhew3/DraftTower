package com.mayhew3.drafttower.client.players;

import com.google.gwt.view.client.HasData;

/**
 * Interface for player tables.
 */
public interface PlayerTableView<T> extends HasData<T> {
  void refresh();
}