package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.ServletEndpoints;
import org.apache.http.protocol.UriPatternMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Servlet filter which delegates to injected servlets.
 */
public class ServletFilter implements Filter {

  private ServerComponent serverComponent;

  @Override
  public void init(FilterConfig config) throws ServletException {
    if (System.getProperty("gwtTests") != null) {
      // Don't inject server modules during tests - they're faked client-side.
      return;
    }

    serverComponent = DaggerServerComponent.create();
    serverComponent.injectEager();

    UriPatternMatcher<Servlet> servlets = new UriPatternMatcher<>();
    ServletContext servletContext = config.getServletContext();

    register(servlets, "/" + ServletEndpoints.LOGIN_ENDPOINT,
        serverComponent.loginServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.DRAFT_SOCKET_ENDPOINT,
        serverComponent.webSocketServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.UNCLAIMED_PLAYERS_ENDPOINT,
        serverComponent.unclaimedPlayerLookupServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.ADD_OR_REMOVE_FAVORITE_ENDPOINT,
        serverComponent.addOrRemoveFavoritePlayerServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.CHANGE_PLAYER_RANK_ENDPOINT,
        serverComponent.changePlayerRankServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.COPY_ALL_PLAYER_RANKS_ENDPOINT,
        serverComponent.copyAllPlayerRanksServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.SET_AUTOPICK_WIZARD_ENDPOINT,
        serverComponent.setAutoPickWizardServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.SET_CLOSER_LIMITS_ENDPOINT,
        serverComponent.setCloserLimitServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.QUEUE_ENDPOINT + "/*",
        serverComponent.queueServlet(), servletContext);
    register(servlets, "/" + ServletEndpoints.GRAPHS_ENDPOINT,
        serverComponent.graphsServlet(), servletContext);
    serverComponent.servletMapProvider().setUriPatternMatcher(servlets);
  }

  private void register(UriPatternMatcher<Servlet> servlets, String uriPattern, final Servlet servlet,
      final ServletContext servletContext) throws ServletException {
    servlet.init(new ServletConfig() {
      @Override
      public String getServletName() {
        return servlet.getClass().getCanonicalName();
      }

      @Override
      public ServletContext getServletContext() {
        return servletContext;
      }

      @Override
      public String getInitParameter(String s) {
        return null;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        return null;
      }
    });

    servlets.register(uriPattern, servlet);
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
    if (serverComponent == null) {
      chain.doFilter(req, resp);
      return;
    }

    HttpServletRequest request = (HttpServletRequest) req;
    String path = request.getRequestURI().substring(request.getContextPath().length());

    UriPatternMatcher<Servlet> uriPatternMatcher = serverComponent.servletMapProvider().get();
    Servlet servlet = uriPatternMatcher.lookup(path);

    if (servlet != null) {
      servlet.service(req, resp);
    } else {
      chain.doFilter(req, resp);
    }
  }

  @Override
  public void destroy() {}
}
