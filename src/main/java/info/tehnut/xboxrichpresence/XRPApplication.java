package info.tehnut.xboxrichpresence;

import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class XRPApplication extends Application {

    private static final ScheduledExecutorService SCHEDULE = Executors.newScheduledThreadPool(1);
    private static PresenceService service;

    public static void init_() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        ApplicationConfig config = JsonUtil.fromJson(TypeToken.get(ApplicationConfig.class), new File("xrp_config.json"), new ApplicationConfig("", "", true));

        // Construct elements
        List<Node> elements = new ArrayList<>();
        handleTop(elements);
        handleSeparator(elements);
        handleBottom(elements, config);

        VBox content = new VBox(elements.toArray(new Node[0]));
        content.setPadding(new Insets(25, 25, 25, 25));

        primaryStage.setScene(new Scene(content));
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Xbox Rich Presence for Discord");
        primaryStage.setOnCloseRequest(e -> {
            XboxRichPresence.active = false;
            SCHEDULE.shutdown();
        });
        primaryStage.show();

        service = new PresenceService(config);
        SCHEDULE.scheduleAtFixedRate(service, 0, 1, TimeUnit.MINUTES);
    }

    private static void handleTop(List<Node> elements) {
        elements.add(new Label("Current display:"));
        elements.add(new Label());
        elements.add(new Label("State: " + XboxRichPresence.DISCORD_PRESENCE.state));
        elements.add(new Label("Details: " + XboxRichPresence.DISCORD_PRESENCE.details));
    }

    private static void handleSeparator(List<Node> elements) {
        elements.add(new Label());
        elements.add(new Separator());
        elements.add(new Label());
    }

    private static void handleBottom(List<Node> elements, ApplicationConfig config) {
        elements.add(new Label("Options:"));
        elements.add(new Label());
        TextField apiKeyText = new TextField(config.getApiKey());
        elements.add(new HBox(new ArrayList<Node>() {{
            add(new Label("XboxAPI.com API Key: "));
            apiKeyText.setPrefColumnCount(30);
            add(apiKeyText);
        }}.toArray(new Node[0])));
        elements.add(new Label());

        TextField gamertagText = new TextField(config.getGamertag());
        elements.add(new HBox(new ArrayList<Node>() {{
            add(new Label("Gamertag: "));
            gamertagText.setPrefColumnCount(30);
            add(gamertagText);
        }}.toArray(new Node[0])));
        elements.add(new Label());

        CheckBox checkbox = new CheckBox();
        elements.add(new HBox(new ArrayList<Node>() {{
            add(new Label("Display while offline: "));
            checkbox.setSelected(config.shouldDisplayWhileOffline());
            add(checkbox);
        }}.toArray(new Node[0])));


        elements.add(new Label());
        Button button = new Button("Save");
        button.setOnAction(e -> {
            ApplicationConfig newConfig = new ApplicationConfig(apiKeyText.getText(), gamertagText.getText(), checkbox.isSelected());
            JsonUtil.toJson(newConfig, TypeToken.get(ApplicationConfig.class), new File("xrp_config.json"));
            service.updateConfig(newConfig);
            service.run();
        });
        elements.add(button);
    }
}
