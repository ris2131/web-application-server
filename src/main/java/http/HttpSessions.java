package http;

import java.util.HashMap;
import java.util.Map;

public class HttpSessions {
    private static Map<String, HttpSession> sessions = new HashMap<>();

    //ID 에 따른 session 찾아주는데, 없으면 만들어서 준다.
    public static HttpSession getSession(String id){
        HttpSession session = sessions.get(id);
        if(session == null){
            session = new HttpSession(id);
            sessions.put(id, session);
        }
        return session;
    }
    static void remove(String id){
        sessions.remove(id);
    }
}
