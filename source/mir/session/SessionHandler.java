package mir.session;

public interface SessionHandler {
  public void processRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure;
}
