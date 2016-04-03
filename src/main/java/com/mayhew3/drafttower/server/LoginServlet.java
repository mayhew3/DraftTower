package com.mayhew3.drafttower.server;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.LoginResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet for logins.
 */
@Singleton
public class LoginServlet extends HttpServlet {

  private final LoginHandler loginHandler;

  @Inject
  public LoginServlet(LoginHandler loginHandler) {
    this.loginHandler = loginHandler;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Map<String, String> cookiesMap = new HashMap<>();
    for (Cookie cookie : req.getCookies()) {
      cookiesMap.put(cookie.getName(), cookie.getValue());
    }
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    AutoBean<LoginResponse> responseBean;
    try {
      responseBean = loginHandler.doLogin(cookiesMap, username, password);
    } catch (DataSourceException e) {
      throw new ServletException(e);
    }
    if (responseBean != null) {
      resp.setContentType("text/plain");
      resp.getWriter().append(AutoBeanCodex.encode(responseBean).getPayload());
      if (responseBean.as().getTeamToken() != null) {
        resp.addCookie(new Cookie(LoginResponse.TEAM_TOKEN_COOKIE,
            responseBean.as().getTeamToken()));
      }
    } else {
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}