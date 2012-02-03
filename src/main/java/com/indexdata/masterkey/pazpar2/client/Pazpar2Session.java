package com.indexdata.masterkey.pazpar2.client;

import org.apache.log4j.Logger;

/**
 * A Pazpar2 session object holding a Pazpar2 session ID and the target settings 
 * (if any) for the session.
 * <p/>
 * Pazpar2Session also keeps track of changes to the ClientCommand from one SP
 * search request to the next.
 * <p/>
 * (A repeated search (lack of change to the search) should omit another request to Pazpar2
 * A change of a torus query should re-initialize the entire session 
 * A change of a record filter should reset the record filtering)
 * <p/>
 * Note that Pazpar2Session is cached on the HTTP Session so setting new attributes
 * on this object should be done with caution.
 * 
 * @author jakub
 */
public class Pazpar2Session {
  private String sessionId;
  private ClientCommand latestSearchCommand = null;
  private ClientCommand previousSearchCommand = null;
  private Logger logger = Logger.getLogger(Pazpar2Session.class);

  public Pazpar2Session() {
  }

  /**
   * If the command is a search it is inserted as latest command
   * and the hitherto latest search command is now previous.
   * @param command
   */
  public void setSearchCommand(ClientCommand command) {
    if (command.search()) {
      logger.debug("Registering new command " + command.getQueryString()
        + " with Pazpar2Session object.");
      this.previousSearchCommand = this.latestSearchCommand;
      this.latestSearchCommand = command;
    } else {
      logger.error(
        "Attempt to set a non-search command as the search command for the session.");
    }
  }

  public ClientCommand getSearchCommand() {
    return latestSearchCommand;
  }

  public String getLatestQueryString() {
    return (latestSearchCommand == null) ? "" : latestSearchCommand.
      getQueryString();
  }

  /** 
   * Compares the current search with the previous search
   * 
   * @return true if any part of the query string changed since last search request
   */
  public boolean searchChanged() {
    if (latestSearchCommand != null && previousSearchCommand == null) {
      logger.debug("First search command on the http session: " + latestSearchCommand.
        getQueryString());
      return true;
    } else if (latestSearchCommand != null && previousSearchCommand != null) {
      logger.debug("Search: Comparing " + latestSearchCommand.getQueryString()
        + " with " + previousSearchCommand.getQueryString());
      return !(latestSearchCommand.getQueryString().equals(previousSearchCommand.
        getQueryString()));
    } else {
      logger.error("No search command encountered so far.");
      return false;
    }

  }

  public void resetQuery() {
    previousSearchCommand = null;
    latestSearchCommand = null;
  }

  /** 
   * Compares the record filtering on the current search with 
   * the previous search
   * 
   * @return true if any part of the filter query string changed since last search request
   */
  public boolean recordFilterChanged() {
    boolean changed = false;

    if (previousSearchCommand == null) {
      if (latestSearchCommand.hasRecordFilter()) {
        logger.debug(
          "Record filter found on latest search and no previous search");
        changed = true;
      }
    } else {
      if (!(latestSearchCommand.getRecordFilter() + "").equals(previousSearchCommand.
        getRecordFilter() + "")) {
        changed = true;
      }
    }
    if (recordFilterTargetCriteriaChanged()) {
      changed = true;
    }
    logger.debug("Record filter changed?: " + changed + ". Was: [" + (previousSearchCommand
      == null ? "no previous search" : previousSearchCommand.getRecordFilter())
      + "]. Is: [" + (latestSearchCommand == null ? "no current search" : latestSearchCommand.
      getRecordFilter()) + "]");
    return changed;

  }

  /**
   * Compares the target criteria associated with a record filter on
   * the current search with the previous search.
   * 
   * @return true if the target criteria changed since last search request
   */
  public boolean recordFilterTargetCriteriaChanged() {
    boolean changed = false;
    if (previousSearchCommand == null) {
      if (latestSearchCommand.hasRecordFilterTargetCriteria()) {
        logger.debug(
          "Record filter found on latest search and no previous search");
        changed = true;
      }
    } else {
      if (!(latestSearchCommand.getRecordFilterTargetCriteria() + "").equals(previousSearchCommand.
        getRecordFilterTargetCriteria() + "")) {
        changed = true;
      }
    }
    logger.debug("Record filter target criteria changed?: " + changed);
    return changed;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Compares the torus query on the current search with the 
   * previous search
   * 
   * @return true if the torus query changed since last search request.
   */
  public boolean torusQueryChanged() {
    boolean changed = false;
    if (previousSearchCommand == null) {
      if (latestSearchCommand.hasTorusParams()) {
        logger.debug(
          "Torus query found on latest search and there is no previous search");
        changed = true;
      }
    } else {
      logger.debug("Comparing torus params: " + previousSearchCommand.
        getTorusParams() + ", " + latestSearchCommand.getTorusParams());
      if (!(latestSearchCommand.getTorusParams() + "").equals(previousSearchCommand.
        getTorusParams() + "")) {
        changed = true;
      }
    }
    logger.debug("Torus query changed?: " + changed);
    return changed;

  }
}
