package com.tesla.contacts;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//       instead of the above import use the following:
// import org.nanohttpd.NanoHTTPD;

/**
 * https://github.com/NanoHttpd/nanohttpd
 * 服务成功启动
 * 由于手机连的网络是TengZhan 与电脑WIFI不同网段 --> 可以用山石VPN解决
 * 手机浏览器正常访问 http://localhost:8080/ 手机局域网 http://172.16.200.149:8080成功访问
 * 电脑WIFI无法访问
 */
public class App extends NanoHTTPD {
    public App() throws IOException {
        super("0.0.0.0",8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        return newFixedLengthResponse(msg + "</body></html>\n");
    }
}