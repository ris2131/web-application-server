package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import model.User;

public class LoginUserController extends AbstractController{
    @Override
    public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
        super.doPost(httpRequest, httpResponse);
        User user = DataBase.findUserById(httpRequest.getParameter("userId"));//이렇게 넘겨 받는게 솔직히 엄청 위험해 보이긴 함..
        String redirectUrl = "/user/login_failed.html";
        //login 성공시 login
        if( user != null && httpRequest.getParameter("password").equals(user.getPassword()) ){//있고비번 같으면
            redirectUrl="/index.html";
            //쿠키에 login 을 남기는것이 아닌 세션에 user 객체를 반환 한다.
            HttpSession session = httpRequest.getSession();
            session.setAttribute("user",user);
        }
        httpResponse.sendRedirect(redirectUrl);
    }
}
