/*
 * Copyright (C) 2001, 2002 The Mir-coders group
 *
 * This file is part of Mir.
 *
 * Mir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Mir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, The Mir-coders gives permission to link
 * the code of this program with  any library licensed under the Apache Software License,
 * The Sun (tm) Java Advanced Imaging library (JAI), The Sun JIMI library
 * (or with modified versions of the above that use the same license as the above),
 * and distribute linked combinations including the two.  You must obey the
 * GNU General Public License in all respects for all of the code used other than
 * the above mentioned libraries.  If you modify this file, you may extend this
 * exception to your version of the file, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */

package mircoders.global;

import gnu.regexp.RE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.session.HTTPAdapters;
import mir.session.Request;
import mir.util.DateToMapAdapter;
import mir.util.InternetFunctions;
import mir.util.StringRoutines;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.localizer.MirAdminInterfaceLocalizer;

import org.apache.commons.collections.ExtendedProperties;


public class Abuse {
  private List filters;
  private int maxIdentifier;
  private LoggerWrapper logger;
  private int logSize;
  private boolean logEnabled;
  private boolean openPostingDisabled;
  private boolean openPostingPassword;
  private boolean cookieOnBlock;
  private String articleBlockAction;
  private String commentBlockAction;
  private List log;
  private String configFile = MirGlobal.config().getStringWithHome("Abuse.Config");


  private static final String IP_FILTER_TYPE="ip";
  private static final String REGEXP_FILTER_TYPE="regexp";
  private static String cookieName=MirGlobal.config().getString("Abuse.CookieName");
  private static int cookieMaxAge = 60*60*MirGlobal.config().getInt("Abuse.CookieMaxAge");

  public Abuse() {
    logger = new LoggerWrapper("Global.Abuse");
    filters = new Vector();
    maxIdentifier = 0;
    log = new Vector();

    logSize = 100;
    logEnabled = false;
    articleBlockAction = "";
    commentBlockAction = "";
    openPostingPassword = false;
    openPostingDisabled = false;
    cookieOnBlock = false;

    load();
  }

  public boolean checkIpFilter(String anIpAddress) {
    synchronized (filters) {
      Iterator i = filters.iterator();

      while (i.hasNext()) {
        Filter filter = (Filter) i.next();

        try {
          if ( (filter.getType().equals(IP_FILTER_TYPE)) &&
              InternetFunctions.isIpAddressInNetwork(anIpAddress, filter.getExpression())) {
            logger.debug("ip match on " + filter.getExpression());
            return true;
          }
        }
        catch (Throwable t) {
          logger.warn("error while checking ip address " + anIpAddress + " over network " + filter.expression + ": " + t.getMessage());
        }
      }

      return false;
    }
  }

  private boolean checkRegExpFilter(Entity anEntity) {
    synchronized (filters) {
      Iterator i = filters.iterator();

      while (i.hasNext()) {
        Filter filter = (Filter) i.next();

        if (filter.getType().equals(REGEXP_FILTER_TYPE)) {
          try {
            RE regularExpression = new RE(filter.getExpression(), RE.REG_ICASE);

            Iterator j = anEntity.getFields().iterator();
            while (j.hasNext()) {
              String field = anEntity.getValue( (String) j.next());

              if (field != null && regularExpression.isMatch(field.toLowerCase())) {
                logger.debug("regexp match on " + filter.getExpression());
                return true;
              }
            }
          }
          catch (Throwable t) {
            logger.warn("error while checking entity with regexp " + filter.getExpression() + ": " + t.getMessage());
          }
        }
      }

      return false;
    }
  }

  private void setCookie(HttpServletResponse aResponse) {
    Random random = new Random();

    Cookie cookie = new Cookie(cookieName, Integer.toString(random.nextInt(1000000000)));
    cookie.setMaxAge(cookieMaxAge);
    cookie.setPath("/");

    if (aResponse!=null)
      aResponse.addCookie(cookie);
  }

  private boolean checkCookie(List aCookies) {
    if (getCookieOnBlock()) {
      Iterator i = aCookies.iterator();

      while (i.hasNext()) {
        Cookie cookie = (Cookie) i.next();

        if (cookie.getName().equals(cookieName)) {
          logger.debug("cookie match");
          return true;
        }
      }
    }

    return false;
  }

  public boolean checkRequest(Request aRequest, HttpServletResponse aResponse, String anId, boolean anIsComment) {
    String address = "0.0.0.0";
    String browser = "unknown";
    List cookies = null;

    HttpServletRequest request = null;

    if (aRequest instanceof HTTPAdapters.HTTPParsedRequestAdapter) {
      request = ((HTTPAdapters.HTTPParsedRequestAdapter) aRequest).getRequest();
    }
    else if (aRequest instanceof HTTPAdapters.HTTPRequestAdapter) {
      request = ((HTTPAdapters.HTTPRequestAdapter) aRequest).getRequest();
    }
    if (request!=null) {
      browser = (String) request.getHeader("User-Agent");
      address = request.getRemoteAddr();
      cookies = Arrays.asList(request.getCookies());
    }

    if (anIsComment)
      logComment(address, anId , new Date(), browser);
    else
      logArticle(address, anId , new Date(), browser);

    return checkCookie(cookies) || checkIpFilter(address);
  }

  public void checkComment(EntityComment aComment, Request aRequest, HttpServletResponse aResponse) {
    try {
      long time = System.currentTimeMillis();

      MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation = MirGlobal.localizer().adminInterface().simpleCommentOperationForName(commentBlockAction);

      if (checkRequest(aRequest, aResponse, aComment.getId(), true) || checkRegExpFilter(aComment)) {
        logger.debug("performing operation " + operation.getName());
        operation.perform(null, MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("comment", aComment));
        setCookie(aResponse);
      }

      logger.info("checkComment: " + (System.currentTimeMillis()-time) + "ms");
    }
    catch (Throwable t) {
      t.printStackTrace(logger.asPrintWriter(logger.DEBUG_MESSAGE));
      logger.error("Abuse.checkComment: " + t.toString());
    }
  }

  public void checkArticle(EntityContent anArticle, Request aRequest, HttpServletResponse aResponse) {
    try {
      long time = System.currentTimeMillis();

      MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation = MirGlobal.localizer().adminInterface().simpleArticleOperationForName(articleBlockAction);

      if (checkRequest(aRequest, aResponse, anArticle.getId(), false) || checkRegExpFilter(anArticle)) {
        logger.debug("performing operation " + operation.getName());
        operation.perform(null, MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("content", anArticle));
        setCookie(aResponse);
      }

      logger.info("checkArticle: " + (System.currentTimeMillis()-time) + "ms");
    }
    catch (Throwable t) {
      t.printStackTrace(logger.asPrintWriter(logger.DEBUG_MESSAGE));
      logger.error("Abuse.checkArticle: " + t.toString());
    }
  }

  public boolean getLogEnabled() {
    return logEnabled;
  }

  public void setLogEnabled(boolean anEnabled) {
    logEnabled = anEnabled;
    truncateLog();
  }

  public int getLogSize() {
    return logSize;
  }

  public void setLogSize(int aSize) {
    logSize = aSize;
    truncateLog();
  }

  public boolean getOpenPostingDisabled() {
    return openPostingDisabled;
  }

  public void setOpenPostingDisabled(boolean anOpenPostingDisabled) {
    openPostingDisabled = anOpenPostingDisabled;
  }

  public boolean getOpenPostingPassword() {
    return openPostingPassword;
  }

  public void setOpenPostingPassword(boolean anOpenPostingPassword) {
    openPostingPassword = anOpenPostingPassword;
  }

  public boolean getCookieOnBlock() {
    return cookieOnBlock;
  }

  public void setCookieOnBlock(boolean aCookieOnBlock) {
    cookieOnBlock = aCookieOnBlock;
  }

  public String getArticleBlockAction() {
    return articleBlockAction;
  }

  public void setArticleBlockAction(String anAction) {
    articleBlockAction = anAction;
  }

  public String getCommentBlockAction() {
    return commentBlockAction;
  }

  public void setCommentBlockAction(String anAction) {
    commentBlockAction = anAction;
  }


  public List getLog() {
    synchronized(log) {
      List result = new Vector();

      Iterator i = log.iterator();
      while (i.hasNext()) {
        LogEntry logEntry = (LogEntry) i.next();
        Map entry = new HashMap();

        entry.put("ip", logEntry.getIpNumber());
        entry.put("id", logEntry.getId());
        entry.put("timestamp", new DateToMapAdapter(logEntry.getTimeStamp()));
        if (logEntry.getIsArticle())
          entry.put("type", "content");
        else
          entry.put("type", "comment");
        entry.put("browser", logEntry.getBrowserString());

        result.add(entry);
      }

      return result;
    }
  }

  public void logComment(String anIp, String anId, Date aTimeStamp, String aBrowser) {
    appendLog(new LogEntry(aTimeStamp, anIp, aBrowser, anId, false));
  }

  public void logArticle(String anIp, String anId, Date aTimeStamp, String aBrowser) {
    appendLog(new LogEntry(aTimeStamp, anIp, aBrowser, anId, true));
  }

  public void load() {
    try {
      ExtendedProperties configuration = new ExtendedProperties();

      try {
        configuration = new ExtendedProperties(configFile);
      }
      catch (FileNotFoundException e) {
      }

      getFilterConfig(filters, "abuse.filter", configuration);

      setOpenPostingDisabled(configuration.getString("abuse.openPostingDisabled", "0").equals("1"));
      setOpenPostingPassword(configuration.getString("abuse.openPostingPassword", "0").equals("1"));
      setCookieOnBlock(configuration.getString("abuse.cookieOnBlock", "0").equals("1"));
      setLogEnabled(configuration.getString("abuse.logEnabled", "0").equals("1"));
      setLogSize(configuration.getInt("abuse.logSize", 10));
      setArticleBlockAction(configuration.getString("abuse.articleBlockAction", ""));
      setCommentBlockAction(configuration.getString("abuse.commentBlockAction", ""));
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }
  public void save() {
    try {
      ExtendedProperties configuration = new ExtendedProperties();

      setFilterConfig(filters, "abuse.filter", configuration);

      configuration.addProperty("abuse.openPostingDisabled", getOpenPostingDisabled()?"1":"0");
      configuration.addProperty("abuse.openPostingPassword", getOpenPostingPassword()?"1":"0");
      configuration.addProperty("abuse.cookieOnBlock", getCookieOnBlock()?"1":"0");
      configuration.addProperty("abuse.logEnabled", getLogEnabled()?"1":"0");
      configuration.addProperty("abuse.logSize", Integer.toString(getLogSize()));
      configuration.addProperty("abuse.articleBlockAction", getArticleBlockAction());
      configuration.addProperty("abuse.commentBlockAction", getCommentBlockAction());

      configuration.save(new FileOutputStream(new File(configFile)), "Anti abuse configuration");
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }

  public List getFilterTypes() {
    List result = new Vector();

    Map entry = new HashMap();
    entry.put("resource", "ip");
    entry.put("id", IP_FILTER_TYPE);
    result.add(entry);

    entry = new HashMap();
    entry.put("resource", "regexp");
    entry.put("id", REGEXP_FILTER_TYPE);
    result.add(entry);

    return result;
  }

  public List getArticleActions() {
    try {
      List result = new Vector();

      Iterator i = MirGlobal.localizer().adminInterface().simpleArticleOperations().iterator();
      while (i.hasNext()) {
        MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation =
            (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) i.next();

        Map action = new HashMap();
        action.put("resource", operation.getName());
        action.put("identifier", operation.getName());

        result.add(action);
      }

      return result;
    }
    catch (Throwable t) {
      throw new RuntimeException("can't get article actions");
    }
  }

  public List getCommentActions() {
    try {
      List result = new Vector();

      Iterator i = MirGlobal.localizer().adminInterface().simpleCommentOperations().iterator();
      while (i.hasNext()) {
        MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation =
            (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) i.next();

        Map action = new HashMap();
        action.put("resource", operation.getName());
        action.put("identifier", operation.getName());

        result.add(action);
      }

      return result;
    }
    catch (Throwable t) {
      throw new RuntimeException("can't get comment actions");
    }
  }

  public List getFilters() {
    return getFiltersAsMaps(filters);
  }

  public void addFilter(String aType, String anExpression) {
    addFilter(filters, aType, anExpression);
  }

  public void setFilter(String anIdentifier, String aType, String anExpression) {
    setFilter(filters, anIdentifier, aType, anExpression);
  }

  public void deleteFilter(String anIdentifier) {
    deleteFilter(filters, anIdentifier);
  }

  public void validateIpFilter(String anIdentifier, String anArticleAction, String aCommentAction) throws Exception {
  }

  private List getFiltersAsMaps(List aFilters) {
    synchronized(aFilters) {
      List result = new Vector();

      Iterator i = aFilters.iterator();
      while (i.hasNext()) {
        Filter filter = (Filter) i.next();
        Map map = new HashMap();

        map.put("id", filter.getId());
        map.put("expression", filter.getExpression());
        map.put("type", filter.getType());

        result.add(map);
      }
      return result;
    }
  }

  private void addFilter(List aFilters, String aType, String anExpression) {
    Filter filter = new Filter();

    filter.setId(generateId());
    filter.setExpression(anExpression);
    filter.setType(aType);

    synchronized (aFilters) {
      aFilters.add(filter);
    }
  }

  private void setFilter(List aFilters, String anIdentifier, String aType, String anExpression) {
    synchronized (aFilters) {
      Filter filter = findFilter(aFilters, anIdentifier);

      if (filter!=null) {
        filter.setExpression(anExpression);
        filter.setType(aType);
      }
    }
  }

  private Filter findFilter(List aFilters, String anIdentifier) {
    synchronized (aFilters) {
      Iterator i = aFilters.iterator();
      while (i.hasNext()) {
        Filter filter = (Filter) i.next();

        if (filter.getId().equals(anIdentifier)) {
          return filter;
        }
      }
    }

    return null;
  }

  private void deleteFilter(List aFilters, String anIdentifier) {
    synchronized (aFilters) {
      Filter filter = findFilter(aFilters, anIdentifier);

      if (filter!=null) {
        aFilters.remove(filter);
      }
    }
  }

  private String generateId() {
    synchronized(this) {
      maxIdentifier = maxIdentifier+1;

      return Integer.toString(maxIdentifier);
    }
  }

  private static class Filter {
    private String identifier;
    private String expression;
    private String type;

    public Filter() {
      expression="";
      type="";
      identifier="";
    }

    public String getId() {
      return identifier;
    }

    public void setId(String anId) {
      identifier = anId;
    }

    public String getExpression() {
      return expression;
    }

    public void setExpression(String anExpression) {
      expression = anExpression;
    }

    public String getType() {
      return type;
    }

    public void setType(String aType) {
      type = aType;
    }
  }

  private void setFilterConfig(List aFilters, String aConfigKey, ExtendedProperties aConfiguration) {
    synchronized(aFilters) {
      Iterator i = aFilters.iterator();

      while (i.hasNext()) {
        Filter filter = (Filter) i.next();

        aConfiguration.addProperty(aConfigKey, filter.getType()+":"+filter.getExpression());
      }
    }
  }

  private void getFilterConfig(List aFilters, String aConfigKey, ExtendedProperties aConfiguration) {
    synchronized(aFilters) {
      aFilters.clear();

      Iterator i = Arrays.asList(aConfiguration.getStringArray(aConfigKey)).iterator();

      while (i.hasNext()) {
        String filter = (String) i.next();
        List parts = StringRoutines.separateString(filter, ":");

        if (parts.size()==2) {
          addFilter( (String) parts.get(0), (String) parts.get(1));
        }
      }
    }
  }

  private static class LogEntry {
    private String ipNumber;
    private String browserString;
    private String id;
    private Date timeStamp;
    private boolean isArticle;

    public LogEntry(Date aTimeStamp, String anIpNumber, String aBrowserString, String anId, boolean anIsArticle) {
      ipNumber = anIpNumber;
      browserString = aBrowserString;
      id = anId;
      isArticle = anIsArticle;
      timeStamp=aTimeStamp;
    }

    public String getIpNumber() {
      return ipNumber;
    }

    public String getBrowserString() {
      return browserString;
    }

    public String getId() {
      return id;
    }

    public Date getTimeStamp() {
      return timeStamp;
    }

    public boolean getIsArticle() {
      return isArticle;
    }
  }

  private void truncateLog() {
    synchronized(log) {
      if (!logEnabled)
        log.clear();
      else {
        while (log.size()>0 && log.size()>logSize) {
          log.remove(0);
        }
      }
    }
  };

  private void appendLog(LogEntry anEntry) {
    synchronized (log) {
      if (logEnabled) {
        log.add(anEntry);
        truncateLog();
      }
    }
  }

}