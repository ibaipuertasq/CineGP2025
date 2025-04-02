package org.datanucleus;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class to start the cinema management REST server.
 */
public class Main {
    
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * 
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // Scan for JAX-RS resources in the specified package
        final ResourceConfig rc = new ResourceConfig().packages("org.datanucleus");
        
        // Create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args Command line arguments (not used)
     * @throws IOException If there's an error reading from console
     */
    public static void main(String[] args) throws IOException {
        // Start the server
        final HttpServer server = startServer();
        
        // Print server information
        System.out.println(String.format("Cinema server started at %s\n" + 
               "Available endpoints:\n" +
               "- GET/POST %susuarios\n" +
               "- GET/POST %speliculas\n" +
               "- GET/POST %sentradas\n" +
               "Press Enter to stop the server...", 
               BASE_URI, BASE_URI, BASE_URI, BASE_URI));
        
        // Wait for user input to stop the server
        System.in.read();
        
        // Shutdown server
        server.shutdownNow();
    }
}