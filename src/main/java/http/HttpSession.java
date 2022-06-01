package http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//내가 손수 다작성한 HttpSession.. ㅎ
public class HttpSession {
    //인자값들 attribute 와 객체들 젖장
    private Map<String, Object> values = new HashMap<>();
    private String id;

    public HttpSession(String id) {
        this.id = id;
    }

    //현재 세션에 할당 되어있는 고유한 세션 아이디를 반환.
    public String getId(){
        return id;
    }

    //현재 세션에 value 인자로 전달되는 객체를 name 인자 이름으로 저장.
    public void setAttribute(String name, Object value){
        //중복 name attribute 있을때 어떻게 해야할지 이런것들 중요한가?
        values.put(name,value);
    }

    //현재 세션에 name 인자로 저장되어 있는 객체 값을 찾아 반환.
    public Object getAttribute(String name){
        return values.get(name);
    }

    //현재 세션에 name 인자로 저장되어 있는 객체 값을 삭제
    public void removeAttribute(String name){
        values.remove(name);
    }

    //현재 세션에 저장 되어 있는 모든 값을 삭제
    public void invalidate(){
        HttpSessions.remove(id);
    }
}
