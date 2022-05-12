package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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
            //요구사항1: request로 index.html 이 들어오면 bufferedReader 랑 fileReader 통해서 뿌려 줄 예정.
            //요구사항2: GET 을 뒤에 파라미터 떼 오는거.
            //dos 는 뿌려줄때 최종적으로 저기 담는다.
            DataOutputStream dos = new DataOutputStream(out);
            //input 만들기
            BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));//utf-8 설정 여기서.
            String line = br.readLine();// request 가 이 밑으로도 br.readLine 이 먹히고 무슨 정보가 있음.
            if(line==null){//없으면 무한루프 돈대.
                log.debug("run - line null");
                return;
            }
            log.debug("run - requestLine : {}", line);//requestLine example: GET /index.html HTTP/1.1
            

            //요청 라인 통해 위치 만들기
            String[] tokens =  line.split(" ");
            File file = new File("./webapp"+tokens[1]);
            log.debug("run - file path : {}", "./webapp"+tokens[1]);

            //requestHeader 만들어 주기
            Map<String, String> headerMap = new HashMap<>();
            int ContentLength=0;
            while(true){ //!"".equals(line)
                line = br.readLine();
                if("".equals(line)) break;

                HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
                headerMap.put(pair.getKey(), pair.getValue());

                if(headerMap.containsKey("Content-Length")){
                    ContentLength = Integer.parseInt(headerMap.get("Content-Length").trim());
                    log.debug("ContentLength: {}",ContentLength);
                }
                log.debug("run - requestHeader : {}",line);
            }


            String url = tokens[1];
            String requestPath = url;
            String params =null;
            Map<String,String> queryString = new HashMap<>();

            if(url.contains("?")){
                requestPath = url.substring(0,url.indexOf("?"));
                params = url.substring(url.indexOf("?")+1);
                log.debug("run - requestPath : {}", requestPath);
                log.debug("run - params      : {}", params);
            }

            switch(requestPath){
                case "/user/create":
                    if("GET".equals(tokens[0])){
                        queryString = HttpRequestUtils.parseQueryString(params);
                    }
                    else if("POST".equals(tokens[0])){
                        params = IOUtils.readData(br,ContentLength);
                        log.debug("params : {}",params);
                        queryString = HttpRequestUtils.parseQueryString(params);
                    }
                    User user = new User(queryString.get("userId"),queryString.get("password"),queryString.get("name"),queryString.get("email"));
                    log.debug("User : {}",user.toString());
                    DataBase.addUser(user);
                    break;

                default:
                    //body 내용 만들기.
                    byte[] body = Files.readAllBytes(file.toPath());
                    //body header 만들어서 dos 에 담기
                    response200Header(dos, body.length);
                    //body 내용 dos 에 담기
                    responseBody(dos, body);
                    break;
            }


            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
