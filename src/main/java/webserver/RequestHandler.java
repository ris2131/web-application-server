package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import controller.Controller;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            //HTTP Request
            HttpRequest httpRequest = new HttpRequest(in);
            //response 영역
            HttpResponse httpResponse = new HttpResponse(out);

            //없으면 발급
            if( httpRequest.getSessionId() == null ){
                httpResponse.addHeader("Set-Cookie","JSESSIONID="+ UUID.randomUUID() );
            }


            //controller 역할(와.. 확실히 짧아졌다..)
            Controller controller = RequestMapping.getController(httpRequest.getPath());
            if(controller==null){
                String path = httpRequest.getPath();
                httpResponse.forward(path);
            }else{
                controller.service(httpRequest,httpResponse);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
