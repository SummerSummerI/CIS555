package test.edu.upenn.cis455.hw1;

import java.util.Hashtable;

/**
 * Session Manager
 * 
 * @author cis455
 * 
 */
public class SessionManager {
    private static SessionManager _instance = null;
    public static Hashtable<String, MyHttpSession> sessionPool;

    private SessionManager() {
        sessionPool = new Hashtable<String, MyHttpSession>();
    }

    public static SessionManager getInstance() {
        if (_instance == null) {
            _instance = new SessionManager();
        }
        return _instance;
    }


    public static MyHttpSession getSession(String sessionId) {
        return sessionPool.get(sessionId);
    }

    public static void addSession(MyHttpSession m_session) {
        sessionPool.put(m_session.getId(), m_session);
    }

}