package mircoders.servlet;

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleFailure;
import mir.util.HTTPRequestParser;
import mir.util.URLBuilder;
import mircoders.global.MirGlobal;

public class ServletModuleAbuse extends ServletModule {
  private static ServletModuleAbuse instance = new ServletModuleAbuse();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleAbuse() {
    logger = new LoggerWrapper("ServletModule.Abuse");
    defaultAction = "showsettings";
  }

  public void editfilter(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String type=requestParser.getParameterWithDefault("type", "");
    String id=requestParser.getParameterWithDefault("id", "");
    String expression=requestParser.getParameterWithDefault("expression", "");

    if (id.equals("")) {
      MirGlobal.abuse().addFilter(type, expression);
    }
    else {
      MirGlobal.abuse().setFilter(id, type, expression);
    }

    MirGlobal.abuse().save();

    showfilters(aRequest, aResponse);
  }

  public void deletefilter(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String id=requestParser.getParameterWithDefault("id", "");
    MirGlobal.abuse().deleteFilter(id);

    MirGlobal.abuse().save();

    showfilters(aRequest, aResponse);
  }

  public void showfilters(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();

    try {
      Map responseData = ServletHelper.makeGenerationData(new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      urlBuilder.setValue("module", "Abuse");
      urlBuilder.setValue("do", "showfilters");
      responseData.put("thisurl", urlBuilder.getQuery());

      responseData.put("filters", MirGlobal.abuse().getFilters());
      responseData.put("filtertypes", MirGlobal.abuse().getFilterTypes());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "abuse.filters.template");
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }


  public void showsettings(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();

    try {
      Map responseData = ServletHelper.makeGenerationData(new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      urlBuilder.setValue("module", "Abuse");
      urlBuilder.setValue("do", "showsettings");

      responseData.put("thisurl", urlBuilder.getQuery());

      responseData.put("articleactions", MirGlobal.abuse().getArticleActions());
      responseData.put("commentactions", MirGlobal.abuse().getCommentActions());

      responseData.put("disableop", new Boolean(MirGlobal.abuse().getOpenPostingDisabled()));
      responseData.put("passwordop", new Boolean(MirGlobal.abuse().getOpenPostingPassword()));
      responseData.put("logenabled", new Boolean(MirGlobal.abuse().getLogEnabled()));
      responseData.put("logsize", Integer.toString(MirGlobal.abuse().getLogSize()));
      responseData.put("usecookies", new Boolean(MirGlobal.abuse().getCookieOnBlock()));
      responseData.put("articleaction", MirGlobal.abuse().getArticleBlockAction());
      responseData.put("commentaction", MirGlobal.abuse().getCommentBlockAction());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "abuse.template");
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void savesettings(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    try {
      HTTPRequestParser parser = new HTTPRequestParser(aRequest);

      MirGlobal.abuse().setOpenPostingDisabled(parser.getParameterWithDefault("disableop", "").equals("1"));
      MirGlobal.abuse().setOpenPostingPassword(parser.getParameterWithDefault("passwordop", "").equals("1"));
      MirGlobal.abuse().setLogEnabled(parser.getParameterWithDefault("logenabled", "").equals("1"));

      try {
        MirGlobal.abuse().setLogSize(parser.getIntegerWithDefault("logsize", MirGlobal.abuse().getLogSize()));
      }
      catch (Throwable t) {
      }

      MirGlobal.abuse().setCookieOnBlock(parser.getParameterWithDefault("usecookies", "").equals("1"));

      MirGlobal.abuse().setArticleBlockAction(parser.getParameter("articleaction"));
      MirGlobal.abuse().setCommentBlockAction(parser.getParameter("commentaction"));

      MirGlobal.abuse().save();

      showsettings(aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void showlog(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      urlBuilder.setValue("module", "Abuse");
      urlBuilder.setValue("do", "showlog");
      responseData.put("thisurl", urlBuilder.getQuery());

      responseData.put("log", MirGlobal.abuse().getLog());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "abuse.log.template");
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }
}