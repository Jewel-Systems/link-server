package com.jewel_system.link_server;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Benjamin Claassen <BClaassen@live.com> on 8/6/2016.
 *
 * @author <a href="mailto:BClaassen@live.com">Benjamin Claassen</a>
 */
public class Configuration {
    public static HttpServer SERVER;

    public static String PROTOCOL = "http";
    public static InetAddress ADDRESS;
    public static int PORT = 53455;

    public static ConcurrentHashMap<String, String> ERRORS = new ConcurrentHashMap<>();

    public static String TYPE = "borrow";

    public static String WEB_BASE = Paths.get("web").toAbsolutePath().toString();

    static {
        try {
            fromJson(new String(Files.readAllBytes(Paths.get("config.json")), Charset.forName("UTF-8")));
        } catch (IOException ignored) {
        }
        try {
            ADDRESS = InetAddress.getByName("steve.zanity.net");
        } catch (Exception ignored) {
        }

        if (Files.exists(Paths.get(WEB_BASE + "/404.html"))) {
            ERRORS.put("404", Paths.get(WEB_BASE + "/404.html").toAbsolutePath().toString());
        }
    }

    private Configuration() {
    }

    /**
     * Converts configuration to JSON
     *
     * @return The finished JSON
     */
    public static String toJson() {

        JSONObject object = new JSONObject();
        JSONObject server = new JSONObject();
        server.put("protocol", PROTOCOL);
        server.put("host", ADDRESS.getHostName());
        server.put("port", PORT);
        server.put("base", WEB_BASE);
        object.put("server", server);

        JSONObject errors = new JSONObject();
        for (Map.Entry<String, String> entry : ERRORS.entrySet()) {
            errors.put(entry.getKey(), entry.getValue());
        }

        object.put("errors", errors);

        JSONObject machine = new JSONObject();
        machine.put("type", TYPE);

        return object.toString();
    }

    /**
     * Loads configuration from JSON
     *
     * @param json The JSON that needs to be converted to configuration
     * @throws UnknownHostException If the supplied host can't be reached
     */
    public static void fromJson(String json) throws UnknownHostException {
        JSONObject jsonz = json != null ? new JSONObject(json) : null;
        if (jsonz == null) {
            throw new IllegalStateException("JSON is required");
        }
        if (jsonz.has("server")) {
            JSONObject server = jsonz.getJSONObject("server");
            if (server.has("host")) {
                ADDRESS = InetAddress.getByName(server.getString("host"));
            }
            if (server.has("protocol")) {
                PROTOCOL = server.getString("protocol");
            }

            if (server.has("port")) {
                PORT = server.getInt("port");
            }

            if (server.has("base")) {
                WEB_BASE = Paths.get(server.getString("base")).toAbsolutePath().toString();
            }
        }
        if (jsonz.has("errors")) {
            JSONObject errors = jsonz.getJSONObject("errors");
            errors.keySet().forEach(k -> ERRORS.put(k, errors.getString(k)));
        }
        if (jsonz.has("machine")) {
            JSONObject machine = jsonz.getJSONObject("machine");
            if (machine.has("type")) {
                TYPE = machine.getString("type");
            }
        }
    }

    /**
     * Store configuration to disk
     * @throws IOException Thrown if can't write to disk
     */
    public static void store() throws IOException {
        Files.write(Paths.get("config.json"), toJson().getBytes("UTF-8"));
    }
}
