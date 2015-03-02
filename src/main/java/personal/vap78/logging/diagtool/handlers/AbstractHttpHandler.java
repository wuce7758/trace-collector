package personal.vap78.logging.diagtool.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import personal.vap78.logging.diagtool.Session;

public class AbstractHttpHandler extends HttpHandler {

  public static final String DO_LOGIN_ALIAS = "/doLogin";
  public static final String TEXT_HTML = "text/html";
  public static final String UTF_8 = "UTF-8";
  public static final String SESSION_ID = "sessionId";

  protected static String readTemplate(String resource) {
    InputStream input = null;
    try {
      input = MainHttpHandler.class.getResourceAsStream(resource);
      byte[] buffer = new byte[1024];
      int counter = 0; 
      StringBuilder builder = new StringBuilder();
      
      while ((counter = input.read(buffer, 0, 1024)) != -1) {
        builder.append(new String(buffer, 0, counter));
      }
      return builder.toString();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to load template");
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
        }
      }
    }
  }

  @Override
  public void service(Request req, Response resp) throws Exception {
    req.setCharacterEncoding(UTF_8);
    resp.setCharacterEncoding(UTF_8);
    resp.setContentType(TEXT_HTML);
  }
  
  protected Session getSession(Request req, Response resp) throws IOException {
    Cookie cookie = getSessionCookie(req);
    if (cookie == null) {
      return null;
    }
    Session session = Session.getById(cookie.getValue());
    if (session == null || !session.isValid()) {
      cookie.setMaxAge(0);
      if (session != null) {
        Session.deleteSession(session.getId());
      }
      resp.addCookie(cookie);
      return null;
    }
    
    return session;
  }

  private Cookie getSessionCookie(Request req) {
    Cookie[] allCookies = req.getCookies();
    
    if (allCookies == null) {
      return null;
    }
    
    for (Cookie cookie : allCookies) {
      if (cookie.getName().equals(SESSION_ID)) {
        return cookie;
      }
    }
    return null;
  }
  
}
