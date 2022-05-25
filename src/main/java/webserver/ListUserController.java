package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ListUserController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);

    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
        super.doGet(httpRequest, httpResponse);
        String redirectUrl = "/user/login_failed.html";
        String logined="false";//쿠키 자체가 없을수도 있어서 그떄도 false 처리 해야해서 이렇게 했음.
        if(httpRequest.getCookie("logined") !=null){
            logined=httpRequest.getCookie("logined");
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

            httpResponse.forwardBody(sb.toString());
        }else{
            redirectUrl = "/index.html";
            httpResponse.sendRedirect(redirectUrl);
        }
    }
}
