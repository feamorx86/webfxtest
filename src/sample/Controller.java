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

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
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
        //startAddress = "http://crm.nsi1.test.xz-lab.ru/kkm/control";
        startAddress = "https://feamorx86.ru";

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
        readConfig();
        configureSSL();
        configure();
        setListeners();
    }

    private void configureSSL() {
        try {
            InputStream derInputStream = getClass().getResourceAsStream(properties.getProperty("certificate_file"));
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
            String alias = cert.getSubjectX500Principal().getName();

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry(alias, cert);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(trustStore, null);
            KeyManager[] keyManagers = kmf.getKeyManagers();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(keyManagers, trustManagers, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//            URL url = new URL(someURL);

//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setSSLSocketFactory(sslContext.getSocketFactory());


//
//            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            keyStore.load(trustStore, trustStorePassword);
//            trustStore.close();
//
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null);
//            trustStore.setCertificateEntry(alias, cert);
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//            kmf.init(trustStore, null);
//            KeyManager[] keyManagers = kmf.getKeyManagers();
//
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//            tmf.init(trustStore);
//            TrustManager[] trustManagers = tmf.getTrustManagers();
//
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(keyManagers, trustManagers, null);
            URL url = new URL(startAddress);
            url.openConnection().getContent();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Properties properties;

    private void readConfig() {
        properties = new Properties();
        try {
            InputStream in = getClass().getResourceAsStream("/resources/config.properties");
            properties.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
