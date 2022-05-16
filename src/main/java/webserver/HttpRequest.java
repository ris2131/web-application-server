package webserver;

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

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    String method;//get,post
    String path;//url 어디로 보내야할지.
    Map<String, String> headerMap = new HashMap<>();//헤더
    Map<String, String> params = new HashMap<>();// 파라미터
    //byte[] requestBody;//본문.

    //생성자.
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));//utf-8 설정 여기서.
        String line = br.readLine();// request 가 이 밑으로도 br.readLine 이 먹히고 무슨 정보가 있음.
        if(line==null){//없으면 무한루프 돈대.
            log.debug("run - line null");
            return;
        }
        log.debug("run - requestLine : {}", line);//requestLine example: GET /index.html HTTP/1.1

        //요청 라인 통해 위치 만들기(method, path , params 지정)
        String[] tokens =  line.split(" ");
        method = tokens[0];
        String url = tokens[1];
        path = url;
        if(url.contains("?")){
            path = url.substring(0,url.indexOf("?"));
            params = HttpRequestUtils.parseQueryString(url.substring(url.indexOf("?")+1));
        }
        log.debug("run - requestPath : {}", path);
        log.debug("run - params      : {}", params);

        //requestHeader 만들어 주기
        Map<String,String> cookieMap = new HashMap<>();//쿠키 여러개가 있을수 있어서 Map 으로 구성, ';' 로 떼는 Api 이용.//삭제?
        int ContentLength=0;//삭제?
        while(true){ //!"".equals(line)
            line = br.readLine();
            if(line.equals(""))break;
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            headerMap.put(pair.getKey(), pair.getValue());
            log.debug("run - requestHeader : {}",line);
            if(headerMap.containsKey("Content-Length")){//삭제
                ContentLength = Integer.parseInt(headerMap.get("Content-Length").trim());
            }
            //cookie 가 header에 있으면 parseCookie 해서 넣어주기.
            if(headerMap.containsKey("Cookie")){//삭제
                cookieMap = HttpRequestUtils.parseCookies(headerMap.get("Cookie"));
            }
        }

        if("POST".equals(method)){
            String body = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
            params = HttpRequestUtils.parseQueryString(body);
        }
    }

    String getHeader(String key){
        if(headerMap.containsKey(key)) return headerMap.get(key);
        else return null;
    }
    String getMethod(){
        return method;
    }
    String getPath(){
        return path;
    }
    String getParameter(String key){
        return params.get(key);
    }
}
