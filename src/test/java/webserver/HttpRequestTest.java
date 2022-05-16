package webserver;



import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";
    InputStream in;
    HttpRequest request;

    @Test
    public void request_GET() throws Exception{
        in = new FileInputStream(new File (testDirectory+ "Http_GET.txt"));
        request = new HttpRequest(in);

        assertEquals(HttpMethod.GET,request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParameter("userId"));
    }
    @Test
    public void request_POST() throws Exception {
        in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        request = new HttpRequest(in);

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getParameter("userId"));
    }

    @Test
    public void request_POST2() throws Exception {
        in = new FileInputStream(new File(testDirectory + "Http_POST2.txt"));
        request = new HttpRequest(in);

        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("1", request.getParameter("id"));
        assertEquals("javajigi", request.getParameter("userId"));
    }

}