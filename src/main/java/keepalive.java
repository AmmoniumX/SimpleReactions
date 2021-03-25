import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class keepalive implements HttpHandler {

    private OutputStream os = null;

    public void handle(HttpExchange t) throws IOException {
        System.out.println("Handling message...");
        java.io.InputStream is = t.getRequestBody();

        System.out.println("Got request body. Reading request body...");
        byte[] b = new byte[1000];
        int a = 0;
        while (a != -1) {
            a = is.read(b);

        }
        System.out.println("This is the request: " + new String(b));

        String response = "<?xml version='1.0'?><root-node></root-node>";
        Headers header = t.getResponseHeaders();
        header.add("Connection", "Keep-Alive");
        header.add("Keep-Alive", "timeout=14 max=100");
        header.add("Content-Type", "application/soap+xml");
        t.sendResponseHeaders(200, response.length());

        if (os == null) {
            os = t.getResponseBody();
        }

        os.write(response.getBytes());

        System.out.println("Done with exchange. Closing connection");
        os.close();
    }
}
