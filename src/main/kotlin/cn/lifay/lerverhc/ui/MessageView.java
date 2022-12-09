package cn.lifay.lerverhc.ui;

import cn.hutool.core.thread.ThreadUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.TimerTask;

/**
 *@ClassName MessageView
 *@Description TODO
 *@Author 李方宇
 *@Date 2022/12/6 2:06
 **/
public class MessageView {

    public static void info(String msg) {
        Label label = new Label(msg);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setStyle("-fx-fill: #67C23A;-fx-text-fill: #67C23A;");
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #c2e7b0;" +
                "    -fx-background-radius: 10;");
        box.setMinWidth(40);
        box.setMinHeight(30);
        Scene s = new Scene(box);

        try {
            Platform.runLater(() -> {
                Stage stage = new Stage(StageStyle.TRANSPARENT);
                stage.setResizable(false);
                stage.setScene(s);
                stage.show();
                ThreadUtil.execAsync(() -> {
                    try {
                        Thread.sleep(3100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    stage.close();
                });
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}