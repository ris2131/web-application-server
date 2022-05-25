package webserver;

import db.DataBase;
import model.User;

public class LoginUserController extends AbstractController{
    @Override
    public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
        super.doPost(httpRequest, httpResponse);
        User tempUser = DataBase.findUserById(httpRequest.getParameter("userId"));//이렇게 넘겨 받는게 솔직히 엄청 위험해 보이긴 함..
        String redirectUrl = "/user/login_failed.html";
        //login 성공시 login.html
        if( tempUser != null && httpRequest.getParameter("password").equals(tempUser.getPassword()) ){//있고비번 같으면
            redirectUrl="/index.html";
            httpResponse.addHeader("Set-cookie", "logined=true");
            httpResponse.sendRedirect(redirectUrl);
        } else{
            redirectUrl = "/user/login_failed.html";
            httpResponse.sendRedirect(redirectUrl);
        }
    }
}
