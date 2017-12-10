package info.tehnut.xboxrichpresence;

import club.minnced.discord.rpc.DiscordRPC;
import com.google.gson.Gson;
import info.tehnut.xboxrichpresence.xbox.Device;
import info.tehnut.xboxrichpresence.xbox.Presence;
import info.tehnut.xboxrichpresence.xbox.Title;

public class PresenceService implements Runnable {

    public ApplicationConfig config;
    public String xuid;

    public PresenceService(ApplicationConfig config) {
        this.config = config;
    }

    public void updateConfig(ApplicationConfig config) {
        if (!this.config.getGamertag().equals(config.getGamertag()))
            this.xuid = null;
        this.config = config;
    }

    @Override
    public void run() {
        if (config.getApiKey().isEmpty() || config.getGamertag().isEmpty())
            return;

        if (xuid == null || xuid.isEmpty())
            xuid = XboxRichPresence.sendRequest(String.format(XboxRichPresence.XUID_ENDPOINT, config.getGamertag()), config.getApiKey());

        String response = XboxRichPresence.sendRequest(String.format(XboxRichPresence.PRESENCE_ENDPOINT, xuid), config.getApiKey());
        Presence xboxPresence = new Gson().fromJson(response, Presence.class);

        if (!xboxPresence.state.equals("Offline")) {
            Device device = xboxPresence.devices[xboxPresence.devices.length - 1];
            Title title = device.titles[device.titles.length - 1];
            XboxRichPresence.DISCORD_PRESENCE.details = title.name;
            XboxRichPresence.DISCORD_PRESENCE.state = title.activity.richPresence;
            XboxRichPresence.DISCORD_PRESENCE.smallImageKey = "online";
            XboxRichPresence.DISCORD_PRESENCE.smallImageText = "Online (" + xboxPresence.state + ")";
        } else if (!config.shouldDisplayWhileOffline())
            DiscordRPC.INSTANCE.Discord_Shutdown();
        else {
            XboxRichPresence.DISCORD_PRESENCE.details = "";
            XboxRichPresence.DISCORD_PRESENCE.state = "Currently offline";
            XboxRichPresence.DISCORD_PRESENCE.smallImageKey = "offline";
            XboxRichPresence.DISCORD_PRESENCE.smallImageText = xboxPresence.state;
        }

        DiscordRPC.INSTANCE.Discord_UpdatePresence(XboxRichPresence.DISCORD_PRESENCE);
    }
}
