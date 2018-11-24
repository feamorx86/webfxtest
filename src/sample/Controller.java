package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    private void configure() {
        webView.getEngine().load(startAddress);
    }

    private void setListeners() {
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

    @FXML
    public void onClickAtolExample(ActionEvent event) {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("atol_test_dialog.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Пример работы с кассой");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
            stage.showAndWait();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {

        configure();
        setListeners();


    }

    public class JavaApp {

        public void test(String json) {
            Logger.getAnonymousLogger().info(" receive json = "+json);
        }

        public void print(String json) {
            Logger.getAnonymousLogger().info(" receive json = "+json);
        }

        public void exit() {
            Platform.exit();
        }
    }
}
