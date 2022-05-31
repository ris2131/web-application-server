package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    Map<String, String> headerMap = new HashMap<>();//헤더
    Map<String, String> params = new HashMap<>();// 파라미터
    //Map<String,String> cookieMap = new HashMap<>();//쿠키 여러개가 있을수 있어서 Map 으로 구성, ';' 로 떼는 Api 이용.
    RequestLine requestLine;
    //생성자.
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));//utf-8 설정 여기서.
        String line = br.readLine();// request 가 이 밑으로도 br.readLine 이 먹히고 무슨 정보가 있음.
        if(line==null){//없으면 무한루프 돈대.
            log.debug("run - line null");
            return;
        }
        //requestLine 으로 내용물 만들기(method, path, param)
        requestLine = new RequestLine(line);

        
        //requestHeader 만들어 주기
        while(true){ //!"".equals(line)
            line = br.readLine();
            if(line.equals(""))break;
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            headerMap.put(pair.getKey(), pair.getValue());
            log.debug("run - requestHeader : {}",line);
        }


        //? 로 인한  params 연결
        params = requestLine.getParams();
        //body 로 인한 params 연결
        if(getMethod().isPost()){
            String body = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
            Map<String,String>tempParams = HttpRequestUtils.parseQueryString(body);
            for(String tP : tempParams.keySet()){
                params.put(tP,tempParams.get(tP));
            }
        }
        log.debug("run - content-length: {}",headerMap.get("Content-Length"));
        log.debug("run - paramsALL   : {}", params);

    }
    public String getHeader(String key){
        if(headerMap.containsKey(key)) return headerMap.get(key);
        else return null;
    }
    public HttpMethod getMethod(){
        return requestLine.getMethod();
    }
    public String getPath(){
        return requestLine.getPath();
    }
    public String getParameter(String key){
        return params.get(key);
    }
    public HttpCookie getCookies() {
        return new HttpCookie(getHeader("Cookie"));
    }
    public String getSessionId(){
        return getCookies().getCookie("JSESSIONID");
    }
}
