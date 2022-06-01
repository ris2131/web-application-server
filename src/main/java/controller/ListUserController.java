package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
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

        if(!checkLogined(httpRequest.getSession())){
            redirectUrl = "/index.html";
            httpResponse.sendRedirect(redirectUrl);
            return;
        }

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
        return;
    }
    public boolean checkLogined(HttpSession session){
        Object user = session.getAttribute("user");
        if(user ==null){
            return false;
        }
        return true;
    }
}
