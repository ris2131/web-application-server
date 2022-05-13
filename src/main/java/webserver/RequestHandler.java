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

            //requestPath 에 따라 행동 변화
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
                    //작성자가 만든 저장 하는 api
                    DataBase.addUser(user);

                    //요구사항 3.3 어디로 보내줄지
                    //body 에 담아서 보내야 할듯? 땡!
                    //header 내용 dos에 담기(body 내용을 따로 담을 필요도 없네)
                    response302Header(dos,"/index.html");

                    break;

                default:
                    //body 내용 만들기.
                    File file = new File("./webapp"+requestPath);
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
    //HTTP 응답상태 코드 302
    //"Temporally moved" 상태!
    //요청된 리소스가 임시적으로 이동페이지로 이동했다는 뜻.
    //http status 마다 원하는 정보가 다르네!
    private void response302Header(DataOutputStream dos , String url ){//length 는 필요 없나봐
        try{
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: " + url + "\r\n");//location을 추가 해주면, url 을 변경 해주네. "localhost:8080/index.html" 이 다시 실행 되는거지?(바로 ./webapp/index.html 을 찾는건 아닐거잖아?)
            dos.writeBytes("\r\n");
            log.debug("HTTP REQUEST [302] : success");
        }catch(IOException e){//이건 왜 쓰는거?
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
