package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import ru.atol.drivers10.fptr.Fptr;
import ru.atol.drivers10.fptr.IFptr;

import javafx.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AtolTestDialog {
    @FXML
    private TextArea log;
    private StringBuilder logBuilder = new StringBuilder();

    @FXML
    private TextField operatorParameter;

    @FXML
    private TextField operatorValue;


    private IFptr fptr;

    private void log(String message) {
        logBuilder.append(message).append('\n');
        log.setText(logBuilder.toString());
    }

    private void log(String message, Throwable ex) {
        logBuilder.append(message).append('\n');
        logBuilder.append(ex.toString());
        log.setText(logBuilder.toString());
    }

    private void logError(String message) {
        log(message+" code: "+fptr.errorCode()+", "+fptr.errorDescription());
    }

    @FXML
    private void initialize() {
        log("Initialization");
        try {
            fptr = new Fptr();

            log("set settings");
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, String.valueOf(IFptr.LIBFPTR_PORT_USB));
            fptr.applySingleSettings();

            if (fptr.errorCode() != 0) {
                logError("initialization fail");
            } else {
                log("initialization success");
            }
        } catch (Throwable e) {
            log("Initialization fail", e);
        }


    }

    @FXML
    public void onClickConnect(ActionEvent event) {
        log("connect");
        try {
            fptr.open();
            if (fptr.errorCode() != 0) {
                logError("Fail to connect");
            } else {
                log("Connect success");
            }

        } catch (Throwable e) {
            log("connect fail", e);
        }
    }

    @FXML
    public void onClickDisconnect(ActionEvent event) {
        log("disconnect / destroy");
        try {
            fptr.destroy();
            if (fptr.errorCode() != 0) {
                logError("fail to disconnect");
            } else {
                log("disconnect success");
            }
        } catch (Throwable e) {
            log("disconnect fail", e);
        }
    }


    private void setParameter(int key, String value) {
        log("set parameter: key="+key+", value="+value);
        try {
            fptr.setParam(key, value);
            if (fptr.errorCode() != 0) {
                logError("fail to set parameter");
            }
        } catch(Throwable e) {
            log("fail to setParam", e);
        }
    }

    @FXML
    public void onClickOperatorSetParameter(ActionEvent event) {
       try {
           int key = Integer.parseInt(operatorParameter.getText());
           String value = operatorParameter.getText();
           setParameter(key, value);
       } catch (Throwable e) {
           log("Ошибка задания параметра", e);
       }
    }

    @FXML
    public void onClickOperatorUseDefault(ActionEvent event) {
        log("set default parameters");
        setParameter(1021, "Иванов И.И.");
        setParameter(1203, "1203");
    }

    @FXML
    public void onClickOperatorLogin(ActionEvent event) {
        log("operator login");
        try {
            fptr.operatorLogin();
            if (fptr.errorCode() != 0) {
                logError("Operator login fail");
            } else {
                log("login success");
            }
        } catch (Throwable e) {
            log("Fail to login");
        }
    }

    @FXML
    void onClicEndOfExample(ActionEvent event) {
        log("остальной кусок кода ");
        // Открытие чека (с передачей телефона получателя)
        fptr.setParam(IFptr.LIBFPTR_PARAM_RECEIPT_TYPE, IFptr.LIBFPTR_RT_SELL);
        fptr.setParam(1008, "+79161234567");
        fptr.openReceipt();

        // Регистрация позиции
        fptr.setParam(IFptr.LIBFPTR_PARAM_COMMODITY_NAME, "Чипсы LAYS");
        fptr.setParam(IFptr.LIBFPTR_PARAM_PRICE, 73.99);
        fptr.setParam(IFptr.LIBFPTR_PARAM_QUANTITY, 5);
        fptr.setParam(IFptr.LIBFPTR_PARAM_TAX_TYPE, IFptr.LIBFPTR_TAX_VAT18);
        fptr.setParam(1212, 1);
        fptr.setParam(1214, 7);
        fptr.registration();

        // Регистрация итога (отрасываем копейки)
        fptr.setParam(IFptr.LIBFPTR_PARAM_SUM, 369.0);
        fptr.receiptTotal();

        // Оплата наличными
        fptr.setParam(IFptr.LIBFPTR_PARAM_PAYMENT_TYPE, IFptr.LIBFPTR_PT_CASH);
        fptr.setParam(IFptr.LIBFPTR_PARAM_PAYMENT_SUM, 1000);
        fptr.payment();

        // Закрытие чека
        fptr.closeReceipt();

        while (fptr.checkDocumentClosed() < 0) {
            // Не удалось проверить состояние документа. Вывести пользователю текст ошибки, попросить устранить неполадку и повторить запрос
            System.out.println(fptr.errorDescription());
            continue;
        }

        if (!fptr.getParamBool(IFptr.LIBFPTR_PARAM_DOCUMENT_CLOSED)) {
            // Документ не закрылся. Требуется его отменить (если это чек) и сформировать заново
            fptr.cancelReceipt();
            return;
        }

        if (!fptr.getParamBool(IFptr.LIBFPTR_PARAM_DOCUMENT_PRINTED)) {
            // Можно сразу вызвать метод допечатывания документа, он завершится с ошибкой, если это невозможно
            while (fptr.continuePrint() < 0) {
                // Если не удалось допечатать документ - показать пользователю ошибку и попробовать еще раз.
                System.out.println(String.format("Не удалось напечатать документ (Ошибка \"%s\"). Устраните неполадку и повторите.", fptr.errorDescription()));
                continue;
            }
        }

        // Запрос информации о закрытом чеке
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_LAST_DOCUMENT);
        fptr.fnQueryData();
        System.out.println(String.format("Fiscal Sign = %s", fptr.getParamString(IFptr.LIBFPTR_PARAM_FISCAL_SIGN)));
        System.out.println(String.format("Fiscal Document Number = %d", fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENT_NUMBER)));

        // Формирование слипа ЕГАИС
        fptr.beginNonfiscalDocument();

        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, "ИНН: 111111111111 КПП: 222222222");
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.printText();

        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, "КАССА: 1               СМЕНА: 11");
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.printText();

        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, "ЧЕК: 314  ДАТА: 20.11.2017 15:39");
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.printText();

        fptr.setParam(IFptr.LIBFPTR_PARAM_BARCODE, "https://check.egais.ru?id=cf1b1096-3cbc-11e7-b3c1-9b018b2ba3f7");
        fptr.setParam(IFptr.LIBFPTR_PARAM_BARCODE_TYPE, IFptr.LIBFPTR_BT_QR);
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.setParam(IFptr.LIBFPTR_PARAM_SCALE, 5);
        fptr.printBarcode();

        fptr.printText();

        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, "https://check.egais.ru?id=cf1b1096-3cbc-11e7-b3c1-9b018b2ba3f7");
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT_WRAP, IFptr.LIBFPTR_TW_CHARS);
        fptr.printText();

        fptr.printText();

        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT,
                "10 58 1c 85 bb 80 99 84 40 b1 4f 35 8a 35 3f 7c " +
                        "78 b0 0a ff cd 37 c1 8e ca 04 1c 7e e7 5d b4 85 " +
                        "ff d2 d6 b2 8d 7f df 48 d2 5d 81 10 de 6a 05 c9 " +
                        "81 74");
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT_WRAP, IFptr.LIBFPTR_TW_WORDS);
        fptr.printText();

        fptr.endNonfiscalDocument();

        // Отчет о закрытии смены
        fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_CLOSE_SHIFT);
        fptr.report();

        // Получение информации о неотправленных документах
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_OFD_EXCHANGE_STATUS);
        fptr.fnQueryData();
        System.out.println(String.format("Unsent documents count = %d", fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENTS_COUNT)));
        System.out.println(String.format("First unsent document number = %d", fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENT_NUMBER)));
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        System.out.println(String.format("First unsent document date = %s", df.format(fptr.getParamDateTime(IFptr.LIBFPTR_PARAM_DATE_TIME))));

        // Завершение работы
        fptr.close();

    }

//    public void onClickOperatorLogOut(MouseEvent event) {
//        log("operator logout");
//        try {
//            fptr.opeoperatorLogin();
//            log("logout success");
//        } catch (Throwable e) {
//            log("Fail to logout");
//        }
//    }
}
