package com.ui;
/*
 * author: xstahl01
 * This class is responsible for handling the animation for switching between different screens in the UI.
 */

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AnimationHelper {
    private StackPane root;

    public AnimationHelper(StackPane root) {
        this.root = root;
    }

    public void animateSwitchTo(Runnable switchAction, String direction) {
        if (root.getChildren().isEmpty()) {
            switchAction.run();
            return;
        }
    
        Node current = root.getChildren().get(0);
        double sceneWidth = root.getWidth(); // should be 600 in your case
        double outToX = direction.equals("right") ? -sceneWidth : sceneWidth;
        double inFromX = direction.equals("right") ? sceneWidth : -sceneWidth;
    
        TranslateTransition outTransition = new TranslateTransition(Duration.millis(300), current);
        outTransition.setToX(outToX);
    
        outTransition.setOnFinished(e -> {
            // Remove current node after transition
            root.getChildren().clear();
    
            // Use Platform.runLater to ensure UI thread has a chance to process clearing
            javafx.application.Platform.runLater(() -> {
                switchAction.run();
    
                if (!root.getChildren().isEmpty()) {
                    Node next = root.getChildren().get(0);
                    next.setTranslateX(inFromX);
    
                    TranslateTransition inTransition = new TranslateTransition(Duration.millis(300), next);
                    inTransition.setToX(0);
                    inTransition.play();
                }
            });
        });
    
        outTransition.play();
    }
}
