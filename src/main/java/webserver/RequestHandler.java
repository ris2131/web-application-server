package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            DataOutputStream dos = new DataOutputStream(out);
            //input 만들기
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String input = br.readLine();// 이 밑으로도 br.readLine 이 먹히고 무슨 정보가 있음.
            log.debug("run - bufferedReader.readline() : {}", input);//input example: GET /index.html HTTP/1.1

            //현재 작업경로 체크
            Path currentPath = Paths.get("");
            String path = currentPath.toAbsolutePath().toString();
            log.debug("run - default path : {}", path);

            //file 경로 만들기
            String[] inSplit =  input.split(" ");
            File file = new File(path+"/webapp"+inSplit[1]);
            log.debug("run - file path : {}", path+"/webapp"+inSplit[1]);

            //byte[] body = "Hello World".getBytes();
            byte[] body = Files.readAllBytes(file.toPath());

            response200Header(dos, body.length);
            responseBody(dos, body);

            br.close();
            //현재 작업경로 체크
            /*
            Path currentPath = Paths.get("");
            String path = currentPath.toAbsolutePath().toString();
            log.debug("현재 작업 경로: {}", path);
            */
            
            //기존 코드
            /*
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
            */
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
