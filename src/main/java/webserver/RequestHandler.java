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
            //HTTP Request
            HttpRequest httpRequest = new HttpRequest(in);

            //header 에 긴 cookie 있으면 처리
            Map<String,String> cookieMap =null;//쿠키 여러개가 있을수 있어서 Map 으로 구성, ';' 로 떼는 Api 이용.
            if(httpRequest.getHeader("Cookie" ) != null ){
                cookieMap = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));
            }
            
            //response 영역
            HttpResponse httpResponse = new HttpResponse(out);

            //requestPath 확장자 에 따라 => controller 역할
            switch(httpRequest.getPath()){
                //POST 로 수정.
                case "/user/create":
                    User user = new User(httpRequest.getParameter("userId"),httpRequest.getParameter("password"),httpRequest.getParameter("name"),httpRequest.getParameter("email"));

                    //작성자가 만든 저장 하는 api
                    DataBase.addUser(user);
                    log.debug("User : {} , DataBaseSize : {}",user.toString(),DataBase.findAll().size());

                    //response302Header(dos,"/index.html");
                    httpResponse.sendRedirect("/index.html");
                    break;
                case "/user/login":
                    //POST : read body

                    User tempUser = DataBase.findUserById(httpRequest.getParameter("userId"));//이렇게 넘겨 받는게 솔직히 엄청 위험해 보이긴 함..
                    String redirectUrl = "/user/login_failed.html";
                    //login 성공시 login.html
                    if( tempUser != null && httpRequest.getParameter("password").equals(tempUser.getPassword()) ){//있고비번 같으면
                        redirectUrl="/index.html";
                        //response302HeaderWithLoginSuccessHeader(dos,redirectUrl);
                        httpResponse.addHeader("Set-cookie", "logined=true");
                        httpResponse.sendRedirect(redirectUrl);
                    } else{
                        redirectUrl = "/user/login_failed.html";
                        //responseResource(out,redirectUrl);//200
                        httpResponse.sendRedirect(redirectUrl);
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

                        //byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
                        //response200HtmlHeader(dos,body.length);
                        //responseBody(dos,body);
                        httpResponse.forwardBody(sb.toString());
                    }else{
                        redirectUrl = "/index.html";
                        //response302Header(dos,redirectUrl);
                        httpResponse.sendRedirect(redirectUrl);
                    }
                    break;
                default:
                    //responseResource(out, httpRequest.getPath());
                    httpResponse.forward(httpRequest.getPath());
                    break;
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
