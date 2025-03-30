package org.datanucleus;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * The Main class represents the entry point of the application.
 * It starts a simple HTTP server and provides a basic REST endpoint.
 */
public class Main {

    /**
     * The main method of the application.
     * It starts the HTTP server.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("Server is running on http://localhost:8080/");
    }

    /**
     * A simple HTTP handler to handle HTTP requests.
     */
    static class HelloHandler implements HttpHandler {

        /**
         * Handles incoming HTTP requests and sends a response.
         * 
         * @param exchange The HTTP exchange object.
         * @throws IOException If an I/O error occurs.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Server is running. Welcome to CineGP2025!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}