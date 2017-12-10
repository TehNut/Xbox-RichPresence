package info.tehnut.xboxrichpresence;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class XboxRichPresence {

    public static final DiscordRichPresence DISCORD_PRESENCE = new DiscordRichPresence();
    public static final String APPLICATION_ID = "389181528434802703";
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    public static final String API_BASE = "https://xboxapi.com/v2/";
    public static final String PRESENCE_ENDPOINT = "%s/presence";
    public static final String XUID_ENDPOINT = "xuid/%s";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final Thread CALLBACK_THREAD;

    public static boolean active = true;

    static {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        DiscordRPC.INSTANCE.Discord_Initialize(APPLICATION_ID, handlers, true, "");
        DISCORD_PRESENCE.state = "Setting things up";
        DISCORD_PRESENCE.largeImageKey = "main";

        CALLBACK_THREAD = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && active) {
                DiscordRPC.INSTANCE.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    DiscordRPC.INSTANCE.Discord_Shutdown();
                }
            }
        }, "RPC-Callback-Handler");

        CALLBACK_THREAD.start();

        DiscordRPC.INSTANCE.Discord_UpdatePresence(DISCORD_PRESENCE);
    }

    /*
     * Argument order:
     * 1. XboxAPI.com api key
     * 2. Xbox Gamertag to get status of
     * 3. Display while offline
     */
    public static void main(String... args) {
        if (args.length != 3) { // Assume we want a GUI
            XRPApplication.init_();
            return;
        }

        ApplicationConfig config = new ApplicationConfig(args[0], args[1], Boolean.parseBoolean(args[2]));
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new PresenceService(config), 0, 1, TimeUnit.MINUTES);
    }

    public static String sendRequest(String endpoint, String apiKey) {
        try {
            URL url = new URL(API_BASE + endpoint);
            System.out.println(DATE_FORMAT.format(new Date()) + " - Sending request to " + url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            urlConnection.setRequestProperty("X-AUTH", apiKey);

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String ret = reader.readLine();
            reader.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
