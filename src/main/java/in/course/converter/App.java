package in.course.converter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class App implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final byte[] page;
    public App(int port) throws IOException {
        try (var stream = App.class.getResourceAsStream("/index.html")) {
            if (stream == null) throw new IOException("Missing index.html resource");
            page = stream.readAllBytes();
        }
        server = HttpServer.create(new InetSocketAddress(port), 64);
        server.setExecutor(executor);
        server.createContext("/", this::handle);
    }
    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }
    @Override public void close() { server.stop(1); executor.shutdownNow(); }
    private void handle(HttpExchange request) throws IOException {
        try {
            if (!"GET".equals(request.getRequestMethod())) {
                request.getResponseHeaders().set("Allow", "GET");
                send(request, 405, "{\"error\":\"Use GET for this endpoint.\"}");
                return;
            }
            switch (request.getRequestURI().getPath()) {
                case "/" -> send(request, 200, "text/html; charset=utf-8", page);
                case "/health" -> send(request, 200, "{\"status\":\"UP\"}");
                case "/units" -> send(request, 200, "{\"length\":[\"m\",\"km\",\"miles\",\"cm\"],\"mass\":[\"g\",\"kg\"],\"time\":[\"s\",\"min\",\"h\"]}");
                case "/convert" -> convert(request);
                default -> send(request, 404, "{\"error\":\"Endpoint not found.\"}");
            }
        } catch (IllegalArgumentException error) {
            send(request, 400, "{\"error\":\"" + error.getMessage() + "\"}");
        } finally { request.close(); }
    }
    private void convert(HttpExchange request) throws IOException {
        Map<String, String> params = query(request.getRequestURI().getRawQuery());
        if (!params.keySet().equals(java.util.Set.of("from", "to", "value")))
            throw new IllegalArgumentException("Provide exactly from, to and value.");
        double value;
        try { value = Double.parseDouble(params.get("value")); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("Enter a valid number."); }
        double result = Converter.convert(value, params.get("from"), params.get("to"));
        send(request, 200, "{\"from\":\"" + params.get("from") + "\",\"to\":\"" + params.get("to")
            + "\",\"value\":" + value + ",\"result\":" + result + "}");
    }
    private Map<String, String> query(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isBlank()) return result;
        for (String pair : raw.split("&", -1)) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid query parameters.");
            String key, value;
            try {
                key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            } catch (IllegalArgumentException error) { throw new IllegalArgumentException("Invalid query encoding."); }
            if (result.putIfAbsent(key, value) != null) throw new IllegalArgumentException("Do not repeat query parameters.");
        }
        return result;
    }
    private void send(HttpExchange request, int code, String body) throws IOException {
        send(request, code, "application/json; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }
    private void send(HttpExchange request, int code, String type, byte[] body) throws IOException {
        request.getResponseHeaders().set("Content-Type", type);
        request.getResponseHeaders().set("Cache-Control", "no-store");
        request.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        request.sendResponseHeaders(code, body.length);
        request.getResponseBody().write(body);
    }
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        if (port < 1 || port > 65535) throw new IllegalArgumentException("APP_PORT must be between 1 and 65535");
        App app = new App(port);
        Runtime.getRuntime().addShutdownHook(new Thread(app::close));
        app.start();
        System.out.println("Unit converter listening on port " + app.port());
    }
}
