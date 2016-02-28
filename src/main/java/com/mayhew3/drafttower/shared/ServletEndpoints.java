package com.mayhew3.drafttower.shared;

/**
 * URL paths for servlets.
 */
public interface ServletEndpoints {
  String CLOCK_SYNC = "sync";
  String CLOCK_SYNC_SEP = "-";

  String LOGIN_ENDPOINT = "login";
  String DRAFT_SOCKET_ENDPOINT = "socket";
  String UNCLAIMED_PLAYERS_ENDPOINT = "unclaimed";
  String ADD_OR_REMOVE_FAVORITE_ENDPOINT = "favorite";
  String CHANGE_PLAYER_RANK_ENDPOINT = "changeRank";
  String COPY_ALL_PLAYER_RANKS_ENDPOINT = "copyRanks";
  String SET_AUTOPICK_WIZARD_ENDPOINT = "autoPickWizard";
  String SET_CLOSER_LIMITS_ENDPOINT = "closers";
  String QUEUE_ENDPOINT = "queue";
  String GRAPHS_ENDPOINT = "graphs";

  String QUEUE_GET = "get";
  String QUEUE_ADD = "add";
  String QUEUE_REMOVE = "remove";
  String QUEUE_REORDER = "reorder";

  String LOGIN_GUEST = "_guest";
}