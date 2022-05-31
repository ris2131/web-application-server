package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUserController extends  AbstractController{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
        super.doPost(httpRequest, httpResponse);
        User user = new User(httpRequest.getParameter("userId"),httpRequest.getParameter("password"),httpRequest.getParameter("name"),httpRequest.getParameter("email"));

        //작성자가 만든 저장 하는 api
        DataBase.addUser(user);

        httpResponse.sendRedirect("/index.html");
        log.debug("post done");
    }
}
