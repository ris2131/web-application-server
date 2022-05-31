package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private HttpMethod method;
    private String path;//url 어디로 보내야할지.
    private Map<String, String> params = new HashMap<>();// 파라미터

    public RequestLine(String requestLine) {
        log.debug("run - requestLine : {}", requestLine);//requestLine example: GET /index.html HTTP/1.1

        //요청 라인 통해 위치 만들기(method, path , params 지정)
        String[] tokens =  requestLine.split(" ");
        method = HttpMethod.valueOf(tokens[0]);
        String url = tokens[1];
        path = url;
        if(url.contains("?")){
            path = url.substring(0,url.indexOf("?"));
            params = HttpRequestUtils.parseQueryString(url.substring(url.indexOf("?")+1));
        }
        log.debug("run - requestPath : {}", path);
        log.debug("run - params      : {}", params);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }

}
