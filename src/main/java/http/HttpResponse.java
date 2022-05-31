package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//응답 데이터 처리 클래스
public class HttpResponse {

    private DataOutputStream dos = null;
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    //headers :  metaData 저장 하는 Map
    Map<String, String> headers = new HashMap<>();
    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }


    //forward : response 바로 뽑아주는것
    public void forward(String url){
        try {
            byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
            if(url.endsWith(".css")){
                addHeader("Content-Type", "text/css");
            } else if (url.endsWith("js")) {
                addHeader("Content-Type","application/javascript");
            } else{
                addHeader("Content-Type","text/html;charset=utf-8");
            }
            addHeader("Content-Length",body.length+"");
            response200Header(body.length);
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    //sendRedirect : 302 header 이용해서 새로 보내도록 하는것.
    //구 RequestHandler/response302Header()
    public void sendRedirect(String url){
        try{
            dos.writeBytes("HTTP/1.1 302 REDIRECT \r\n");//Found? Rediect?
            processHeaders();
            dos.writeBytes("Location: " + url + "\r\n");//location을 추가 해주면, url 을 변경 해주네. "localhost:8080/index.html" 이 다시 실행 되는거지?(바로 ./webapp/index.html 을 찾는건 아닐거잖아?)
            dos.writeBytes("\r\n");
        }catch(IOException e){//이건 왜 쓰는거?
            log.error(e.getMessage());
        }
    }

    //forwardBody 필요한지 모르겠다.. 안쓰이는거 같은데..
    //아아... 테이블 바로 forward 띄울때 이거 쓰이겠다!
    //url 로 forward 하는게 아니라 직접 html body 를 찍어주는 방식!
    //구 RequestHandler/responseResource()
    public void forwardBody(String body){
        byte[] contents = body.getBytes();
        addHeader("Content-Type","text/html;charset=utf-8");
        addHeader("Content-Length",contents.length+"");
        response200Header(contents.length);
        responseBody(contents);
    }
    public void addHeader(String key, String val){
        //headers 에 key,val 을 넣어주기
        if(!headers.containsKey(key)){
            headers.put(key,val);
        }
    }
    private void response200Header(int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
   private void processHeaders(){
       try {
           Set<String> keys = headers.keySet();
            for(String key:keys){
                dos.writeBytes(key+": "+headers.get(key)+"\r\n");
            }
       } catch (IOException e) {
           log.error(e.getMessage());
       }
   }

}
