package webserver;

import http.HttpMethod;
import http.RequestLine;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class RequestLineTest {
    RequestLine line;
    @Test
    public void create_method_GET(){
        line = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals(HttpMethod.GET,line.getMethod());
        assertEquals("/index.html",line.getPath());
    }
    @Test
    public void create_method_POST(){
        line = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals(HttpMethod.POST,line.getMethod());
        assertEquals("/index.html",line.getPath());
    }
    @Test
    public void create_method_path_and_params_GET(){
        line = new RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals(HttpMethod.GET,line.getMethod());
        assertEquals("/user/create",line.getPath());
        Map<String,String> params = line.getParams();
        assertEquals("javajigi",params.get("userId"));
        assertEquals("pass",params.get("password"));
        assertEquals(2,params.size());
    }
    /*
    @Test
    public void create_path_and_params_POST(){

    }
    */
}