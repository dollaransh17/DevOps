package in.course.converter;
import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class AppTest {
    private App app;
    private final HttpClient client = HttpClient.newHttpClient();
    @BeforeEach void start() throws Exception { app = new App(0); app.start(); }
    @AfterEach void stop() { app.close(); }
    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void returnsHealthAndUnits() throws Exception {
        assertEquals("{\"status\":\"UP\"}", get("/health").body());
        assertTrue(get("/units").body().contains("miles"));
    }
    @Test void servesPageAndConversion() throws Exception {
        assertTrue(get("/").body().contains("Unit converter"));
        var response = get("/convert?from=km&to=m&value=5");
        assertEquals(200, response.statusCode());
        assertEquals("application/json; charset=utf-8", response.headers().firstValue("Content-Type").orElseThrow());
        assertTrue(response.body().contains("\"result\":5000.0"));
    }
    @Test void rejectsMalformedRequests() throws Exception {
        for (String query : new String[]{"", "?from=m&to=km", "?from=m&to=kg&value=1", "?from=m&to=km&value=NaN", "?from=m&to=km&value=nope", "?from=m&to=km&value=1&value=2", "?from=m&to=km&value=1&extra=2", "?from=%22&to=m&value=1"})
            assertEquals(400, get("/convert" + query).statusCode(), query);
        assertEquals(404, get("/health/fake").statusCode());
    }
    @Test void rejectsPost() throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + "/convert")).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
        assertEquals("GET", response.headers().firstValue("Allow").orElseThrow());
    }
}
