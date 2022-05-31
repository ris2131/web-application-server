package controller;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

public abstract class AbstractController implements Controller {
    /*
       service : httpRequest를 받아서 http Response 를 보내주는 역할.
       분기처리를 여기서 하나하나 해준다 보면 될듯?
       분기처리를 "제거" 한다기보단, 분기처리를 여기서 해서 저기에 "안보이게" 한다가 맞는거아님?
     */
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
        String path = httpRequest.getPath();
        HttpMethod methodType = httpRequest.getMethod();
        if(methodType.isGET()){
            doGet(httpRequest,httpResponse);
        } else if (methodType.isPost()) {
            doPost(httpRequest,httpResponse);
        }
    }

    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse){};
    public void doPost(HttpRequest httpRequest, HttpResponse httpResponse){};

}
