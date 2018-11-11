package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import netscape.javascript.JSObject;

import java.util.logging.Logger;

public class Controller {
    @FXML
    private WebView webView;

    @FXML
    private Button navigateBack;

    @FXML
    private Button navigateReload;

    @FXML
    private Button navigateHome;

    private String startAddress;

    public  Controller() {
        startAddress = "http://crm.nsi1.test.xz-lab.ru/kkm/control";

    }

    @FXML
    private void initialize() {
        webView.getEngine().load(startAddress);

        navigateBack.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (webView.getEngine().getHistory().getCurrentIndex() > 0) {
                    webView.getEngine().getHistory().go(-1);
                }
            }
        });

        navigateReload.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                webView.getEngine().reload();
            }
        });

        navigateHome.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                webView.getEngine().load(startAddress);
            }
        });

        webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    JSObject win = (JSObject) webView.getEngine().executeScript("window");
                    win.setMember("app", new JavaApp());
                }
            }
        });

        webView.getEngine().locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

            }
        });
    }

    public class JavaApp {

        private void print(String json) {
            Logger.getAnonymousLogger().info(" receive json = "+json);
        }

        private void exit() {
            Platform.exit();
        }
    }
}
