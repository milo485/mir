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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import mir.config.MirPropertiesConfiguration;
import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.session.Request;
import mir.util.GeneratorFormatAdapters;
import mir.util.StringRoutines;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityUsers;
import mircoders.localizer.MirAdminInterfaceLocalizer;
import mircoders.localizer.MirAntiAbuseFilterType;


public class Abuse {
  private List filterRules;
  private Map filterTypes;
  private List filterTypeIds;
  private int maxIdentifier;
  private LoggerWrapper logger;
  private LoggerWrapper adminUsageLogger;
  private int logSize;
  private boolean logEnabled;
  private boolean openPostingDisabled;
  private boolean openPostingPassword;
  private boolean cookieOnBlock;
  private String articleBlockAction;
  private String commentBlockAction;
  private List log;
  private String configFile = MirGlobal.config().getStringWithHome("Abuse.Config");

  private MirPropertiesConfiguration configuration;

  private static String cookieName=MirGlobal.config().getString("Abuse.CookieName");
  private static int cookieMaxAge = 60*60*MirGlobal.config().getInt("Abuse.CookieMaxAge");

  public Abuse() {
    logger = new LoggerWrapper("Global.Abuse");
    adminUsageLogger = new LoggerWrapper("AdminUsage");
    filterRules = new Vector();
    maxIdentifier = 0;
    log = new Vector();

    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (Throwable e) {
      throw new RuntimeException("Can't get configuration: " + e.getMessage());
    }

    logSize = 100;
    logEnabled = false;
    articleBlockAction = "";
    commentBlockAction = "";
    openPostingPassword = false;
    openPostingDisabled = false;
    cookieOnBlock = false;

    try {
      filterTypes = new HashMap();
      filterTypeIds = new Vector();

      Iterator i = MirGlobal.localizer().openPostings().getAntiAbuseFilterTypes().iterator();

      while (i.hasNext()) {
        MirAntiAbuseFilterType filterType = (MirAntiAbuseFilterType) i.next();
        filterTypes.put(filterType.getName(), filterType);
        filterTypeIds.add(filterType.getName());
      }
    }
    catch (Throwable t) {
      throw new RuntimeException("Can't get filter types: " + t.getMessage());
    }

    load();
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

  FilterRule findMatchingFilter(Entity anEntity, Request aRequest) {
    Iterator iterator = filterRules.iterator();

    while (iterator.hasNext()) {
      FilterRule rule = (FilterRule) iterator.next();

      if (rule.test(anEntity, aRequest))
        return rule;
    }

    return null;
  }

  public void checkComment(EntityComment aComment, Request aRequest, HttpServletResponse aResponse) {
    logComment(aComment, aRequest);

    try {
      long time = System.currentTimeMillis();

      FilterRule filterRule = findMatchingFilter(aComment, aRequest);

      if (filterRule!=null) {
        logger.debug("Match for " + filterRule.getType()+" rule '"+ filterRule.getExpression()+"'");
        filterRule.setLastHit(new GregorianCalendar().getTime());
        MirGlobal.performCommentOperation(null, aComment, filterRule.getCommentAction());
        setCookie(aResponse);
        save();
      }

      logger.info("checkComment: " + (System.currentTimeMillis()-time) + "ms");
    }
    catch (Throwable t) {
      t.printStackTrace(logger.asPrintWriter(logger.DEBUG_MESSAGE));
      logger.error("Abuse.checkComment: " + t.toString());
    }
  }

  public void checkArticle(EntityContent anArticle, Request aRequest, HttpServletResponse aResponse) {
    logArticle(anArticle, aRequest);

    try {
      long time = System.currentTimeMillis();

      FilterRule filterRule = findMatchingFilter(anArticle, aRequest);

      if (filterRule!=null) {
        logger.debug("Match for " + filterRule.getType() + " rule '" + filterRule.getExpression()+"'");
        filterRule.setLastHit(new GregorianCalendar().getTime());
        MirGlobal.performArticleOperation(null, anArticle, filterRule.getArticleAction());
        setCookie(aResponse);
        save();
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
    if (!configuration.getString("Abuse.DisallowIPLogging", "0").equals("1"))
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
      try {
        List result = new Vector();

        Iterator i = log.iterator();
        while (i.hasNext()) {
          LogEntry logEntry = (LogEntry) i.next();
          Map entry = new HashMap();

          entry.put("ip", logEntry.getIpNumber());
          entry.put("id", logEntry.getId());
          entry.put("timestamp", new GeneratorFormatAdapters.DateFormatAdapter(logEntry.getTimeStamp(), MirPropertiesConfiguration.instance().getString("Mir.DefaultTimezone")));
          if (logEntry.getIsArticle())
            entry.put("type", "content");
          else
            entry.put("type", "comment");
          entry.put("browser", logEntry.getBrowserString());

          result.add(entry);
        }

        return result;
      }
      catch (Throwable t) {
        throw new RuntimeException(t.toString());
      }
    }
  }

  public void logComment(Entity aComment, Request aRequest) {
    String ipAddress = aRequest.getHeader("ip");
    String id = aComment.getId();
    String browser = aRequest.getHeader("User-Agent");

    logComment(ipAddress, id, new Date(), browser);
  }

  public void logArticle(Entity anArticle, Request aRequest) {
    String ipAddress = aRequest.getHeader("ip");
    String id = anArticle.getId();
    String browser = aRequest.getHeader("User-Agent");

    logArticle(ipAddress, id, new Date(), browser);
  }

  public void logComment(String anIp, String anId, Date aTimeStamp, String aBrowser) {
    appendLog(new LogEntry(aTimeStamp, anIp, aBrowser, anId, false));
  }

  public void logArticle(String anIp, String anId, Date aTimeStamp, String aBrowser) {
    appendLog(new LogEntry(aTimeStamp, anIp, aBrowser, anId, true));
  }

  public void load() {
    synchronized (filterRules) {
      try {
        ExtendedProperties configuration = new ExtendedProperties();

        try {
          configuration = new ExtendedProperties(configFile);
        }
        catch (FileNotFoundException e) {
        }

        getFilterConfig(filterRules, "abuse.filter", configuration);

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
  }

  public void save() {
    synchronized (filterRules) {
      try {
        ExtendedProperties configuration = new ExtendedProperties();

        setFilterConfig(filterRules, "abuse.filter", configuration);

        configuration.addProperty("abuse.openPostingDisabled", getOpenPostingDisabled() ? "1" : "0");
        configuration.addProperty("abuse.openPostingPassword", getOpenPostingPassword() ? "1" : "0");
        configuration.addProperty("abuse.cookieOnBlock", getCookieOnBlock() ? "1" : "0");
        configuration.addProperty("abuse.logEnabled", getLogEnabled() ? "1" : "0");
        configuration.addProperty("abuse.logSize", Integer.toString(getLogSize()));
        configuration.addProperty("abuse.articleBlockAction", getArticleBlockAction());
        configuration.addProperty("abuse.commentBlockAction", getCommentBlockAction());

        configuration.save(new FileOutputStream(new File(configFile)), "Anti abuse configuration");
      }
      catch (Throwable t) {
        throw new RuntimeException(t.toString());
      }
    }
  }

  public List getFilterTypes() {
    try {
      List result = new Vector();

      Iterator i = filterTypeIds.iterator();
      while (i.hasNext()) {
        String id = (String) i.next();

        Map action = new HashMap();
        action.put("resource", id);
        action.put("identifier", id);

        result.add(action);
      }

      return result;
    }
    catch (Throwable t) {
      throw new RuntimeException("can't get article actions");
    }
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
    List result = new Vector();

    synchronized(filterRules) {
      Iterator i = filterRules.iterator();
      while (i.hasNext()) {
        FilterRule filter = (FilterRule) i.next();
        result.add(filter.clone());
      }
      return result;
    }
  }

  public String addFilter(String aType, String anExpression, String aComments, String aCommentAction, String anArticleAction) {
    return addFilter(aType, anExpression, aComments, aCommentAction, anArticleAction, null);
  }

  public String addFilter(String aType, String anExpression, String aComments, String aCommentAction, String anArticleAction, Date aListHit) {
    return addFilter(filterRules, aType, anExpression, aComments, aCommentAction, anArticleAction, aListHit);
  }

  public FilterRule getFilter(String anId) {
    synchronized (filterRules) {
      FilterRule result = (FilterRule) findFilter(filterRules, anId);
      if (result==null)
        return result;
      else
        return (FilterRule) result.clone();
    }
  }

  public String setFilter(String anIdentifier, String aType, String anExpression, String aComments, String aCommentAction, String anArticleAction) {
    return setFilter(filterRules, anIdentifier, aType, anExpression, aComments, aCommentAction, anArticleAction);
  }

  public void deleteFilter(String anIdentifier) {
    deleteFilter(filterRules, anIdentifier);
  }

  private String addFilter(List aFilters, String aType, String anExpression, String aComments, String aCommentAction, String anArticleAction, Date aLastHit) {
    MirAntiAbuseFilterType type = (MirAntiAbuseFilterType) filterTypes.get(aType);

    if (type==null)
      return "invalidtype";

    if (!type.validate(anExpression)) {
      return "invalidexpression";
    }

    FilterRule filter = new FilterRule();

    filter.setId(generateId());
    filter.setExpression(anExpression);
    filter.setType(aType);
    filter.setComments(aComments);
    filter.setArticleAction(anArticleAction);
    filter.setCommentAction(aCommentAction);
    filter.setLastHit(aLastHit);

    synchronized (aFilters) {
      aFilters.add(filter);
    }

    return null;
  }

  private String setFilter(List aFilters, String anIdentifier, String aType, String anExpression, String aComments, String aCommentAction, String anArticleAction) {
    MirAntiAbuseFilterType type = (MirAntiAbuseFilterType) filterTypes.get(aType);

    if (type==null)
      return "invalidtype";

    if (!type.validate(anExpression)) {
      return "invalidexpression";
    }

    synchronized (aFilters) {
      FilterRule filter = findFilter(aFilters, anIdentifier);

      if (filter!=null) {
        filter.setExpression(anExpression);
        filter.setType(aType);
        filter.setCommentAction(aCommentAction);
        filter.setArticleAction(anArticleAction);
        filter.setComments(aComments);
      }

      return null;
    }
  }

  private FilterRule findFilter(List aFilters, String anIdentifier) {
    synchronized (aFilters) {
      Iterator i = aFilters.iterator();
      while (i.hasNext()) {
        FilterRule filter = (FilterRule) i.next();

        if (filter.getId().equals(anIdentifier)) {
          return filter;
        }
      }
    }

    return null;
  }

  private void deleteFilter(List aFilters, String anIdentifier) {
    synchronized (aFilters) {
      FilterRule filter = findFilter(aFilters, anIdentifier);

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

  public class FilterRule {
    private String identifier;
    private String expression;
    private String type;
    private String comments;
    private String articleAction;
    private String commentAction;
    private Date lastHit;

    public FilterRule() {
      expression = "";
      type = "";
      identifier = "";
      comments = "";
      articleAction = articleBlockAction;
      commentAction = commentBlockAction;
      lastHit = null;
    }

    public Date getLastHit() {
      return lastHit;
    }

    public void setLastHit(Date aDate) {
      lastHit = aDate;
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

    public void setComments(String aComments) {
      comments = aComments;
    }

    public String getComments() {
      return comments;
    }

    public String getArticleAction() {
      return articleAction;
    }

    public void setArticleAction(String anArticleAction) {
      articleAction = anArticleAction;
    }

    public String getCommentAction() {
      return commentAction;
    }

    public void setCommentAction(String aCommentAction) {
      commentAction = aCommentAction;
    }

    public boolean test(Entity anEntity, Request aRequest) {
      MirAntiAbuseFilterType filterType = (MirAntiAbuseFilterType) filterTypes.get(type);
      try {
        if (filterType != null)
          return filterType.test(expression, anEntity, aRequest);
      }
      catch (Throwable t) {
        logger.error("error while testing "+type+"-filter '"+expression+"'");
      }

      return false;
    };

    public Object clone() {
      FilterRule result = new FilterRule();
      result.setComments(getComments());
      result.setExpression(getExpression());
      result.setId(getId());
      result.setType(getType());
      result.setArticleAction(getArticleAction());
      result.setCommentAction(getCommentAction());
      result.setLastHit(getLastHit());

      return result;
    }
  }

  private void setFilterConfig(List aFilters, String aConfigKey, ExtendedProperties aConfiguration) {
    synchronized(aFilters) {
      Iterator i = aFilters.iterator();

      while (i.hasNext()) {
        FilterRule filter = (FilterRule) i.next();

        String filterconfig =
            StringRoutines.replaceStringCharacters(filter.getType(), new char[] { '\\', ':'}, new String[] { "\\\\", "\\:"} ) + ":" +
            StringRoutines.replaceStringCharacters(filter.getExpression(), new char[] { '\\', ':'}, new String[] { "\\\\", "\\:"} ) + ":" +
            StringRoutines.replaceStringCharacters(filter.getArticleAction(), new char[] { '\\', ':'}, new String[] { "\\\\", "\\:"} ) + ":" +
            StringRoutines.replaceStringCharacters(filter.getCommentAction(), new char[] { '\\', ':'}, new String[] { "\\\\", "\\:"} ) + ":" +
            StringRoutines.replaceStringCharacters(filter.getComments(), new char[] { '\\', ':'}, new String[] { "\\\\", "\\:"})  + ":";

        if (filter.getLastHit()!=null)
          filterconfig = filterconfig + filter.getLastHit().getTime();

        aConfiguration.addProperty(aConfigKey, filterconfig);
      }
    }
  }

  private void getFilterConfig(List aFilters, String aConfigKey, ExtendedProperties aConfiguration) {
    synchronized(aFilters) {
      aFilters.clear();

      if (aConfiguration.getStringArray(aConfigKey)!=null) {

        Iterator i = Arrays.asList(aConfiguration.getStringArray(aConfigKey)).
            iterator();

        while (i.hasNext()) {
          String filter = (String) i.next();
          List parts = StringRoutines.splitStringWithEscape(filter, ':', '\\');
          if (parts.size() == 2) {
            parts.add(articleBlockAction);
            parts.add(commentBlockAction);
            parts.add("");
            parts.add("");
          }

          if (parts.size() >= 5) {
            Date lastHit = null;

            if (parts.size()>=6) {
              String lastHitString = (String) parts.get(5);

              try {
                lastHit = new Date(Long.parseLong(lastHitString));
              }
              catch (Throwable t) {
              }
            }

            addFilter( (String) parts.get(0), (String) parts.get(1), (String) parts.get(4), (String) parts.get(3), (String) parts.get(2), lastHit);
          }
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

  public void logAdminUsage(EntityUsers aUser, String aDescription) {
    try {
      String user = "unknown (" + aUser.toString() +")";
      if (user!=null)
        user = aUser.getValue("login");
      adminUsageLogger.info(user + ": " + aDescription);
    }
    catch (Throwable t) {
      logger.error("Error while logging admin usage ("+aUser.toString()+", "+aDescription+"): " +t.toString());
    }
  }
}