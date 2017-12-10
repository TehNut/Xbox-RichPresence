package info.tehnut.xboxrichpresence;

public class ApplicationConfig {

    private final String apiKey;
    private final String gamertag;
    private final boolean displayWhileOffline;

    public ApplicationConfig(String apiKey, String gamertag, boolean displayWhileOffline) {
        this.apiKey = apiKey;
        this.gamertag = gamertag;
        this.displayWhileOffline = displayWhileOffline;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getGamertag() {
        return gamertag;
    }

    public boolean shouldDisplayWhileOffline() {
        return displayWhileOffline;
    }
}
