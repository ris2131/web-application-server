package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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
            HttpRequest httpRequest = new HttpRequest(in);

            //input 만들기
            BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));//utf-8 설정 여기서.
            //requestPath 에 따라 행동 변화
            Map<String,String> cookieMap =null;//쿠키 여러개가 있을수 있어서 Map 으로 구성, ';' 로 떼는 Api 이용.
            if(httpRequest.getHeader("Cookie" ) != null ){
                cookieMap = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));
            }
            //requestPath 확장자 에 따라
            if(httpRequest.getPath().endsWith(".css")){
                DataOutputStream dos = new DataOutputStream(out);//dos 는 뿌려줄때 최종적으로 저기 담는다.
                byte[] body = Files.readAllBytes(new File("./webapp"+httpRequest.getPath()).toPath());
                response200CssHeader(dos,body.length);
                responseBody(dos,body);
            }
            else{
                DataOutputStream dos = new DataOutputStream(out);//dos 는 뿌려줄때 최종적으로 저기 담는다.
                switch(httpRequest.getPath()){
                    //POST 로 수정.
                    case "/user/create":
                        User user = new User(httpRequest.getParameter("userId"),httpRequest.getParameter("password"),httpRequest.getParameter("name"),httpRequest.getParameter("email"));

                        //작성자가 만든 저장 하는 api
                        DataBase.addUser(user);
                        log.debug("User : {} , DataBaseSize : {}",user.toString(),DataBase.findAll().size());
                        response302Header(dos,"/index.html");
                        break;

                    /*
                     * 로그인 하기
                     * 로그인 성공하면 /index.html 이동, 로그인 실패하면 /user/login_failed.html 로 이동
                     * post 방식
                     * */
                    case "/user/login":
                        //POST : read body

                        User tempUser = DataBase.findUserById(httpRequest.getParameter("userId"));//이렇게 넘겨 받는게 솔직히 엄청 위험해 보이긴 함..
                        String redirectUrl = "/user/login_failed.html";
                        //login 성공시 login.html
                        if( tempUser != null && httpRequest.getParameter("password").equals(tempUser.getPassword()) ){//있고비번 같으면
                            redirectUrl="/index.html";
                            response302HeaderWithLoginSuccessHeader(dos,redirectUrl);
                        } else{
                            redirectUrl = "/user/login_failed.html";
                            responseResource(out,redirectUrl);//200
                        }
                        break;
                    //요구사항 6 : 사용자 목록 출력
                    case "/user/list":
                        String logined="false";//쿠키 자체가 없을수도 있어서 그떄도 false 처리 해야해서 이렇게 했음.
                        if(cookieMap.containsKey("logined")){
                            logined=cookieMap.get("logined");
                        }
                        if("true".equals(logined)){
                            redirectUrl = "/user/list";//스위치문 지역변수 헷갈리네
                            Collection<User> userList = DataBase.findAll();

                            StringBuilder sb = new StringBuilder();
                            sb.append("<table border='1'>");
                            sb.append("<th>id</th>");
                            sb.append("<th>pw</th>");
                            sb.append("<th>name</th>");
                            sb.append("<th>email</th>");
                            //
                            for(User u : userList) {
                                sb.append("<tr>");
                                sb.append("<td>"+u.getUserId()+"</td>");
                                sb.append("<td>"+u.getPassword()+"</td>");
                                sb.append("<td>"+u.getName()+"</td>");
                                sb.append("<td>"+u.getEmail()+"</td>");
                                sb.append("</tr>");
                            }
                            //
                            sb.append("</table>");

                            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
                            response200HtmlHeader(dos,body.length);
                            responseBody(dos,body);
                        }else{
                            redirectUrl = "/index.html";
                            response302Header(dos,redirectUrl);
                        }
                        break;
                    default:

                        responseResource(out, httpRequest.getPath());
                        break;
                }
            }

            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    private void response200HtmlHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent ) {//map <string,>
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
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
            dos.writeBytes("HTTP/1.1 302 REDIRECT \r\n");
            dos.writeBytes("Location: " + url + "\r\n");//location을 추가 해주면, url 을 변경 해주네. "localhost:8080/index.html" 이 다시 실행 되는거지?(바로 ./webapp/index.html 을 찾는건 아닐거잖아?)
            dos.writeBytes("\r\n");
            log.debug("HTTP REQUEST [302] : success");
        }catch(IOException e){//이건 왜 쓰는거?
            log.error(e.getMessage());
        }
    }
    private void response302HeaderWithLoginSuccessHeader(DataOutputStream dos, String url) {
        try{
            dos.writeBytes("HTTP/1.1 302 REDIRECT \r\n");
            dos.writeBytes("Set-cookie: logined=true \r\n");
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
    //url resource 를 띄우는
    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
        response200HtmlHeader(dos,body.length);
        responseBody(dos,body);
    }
}
