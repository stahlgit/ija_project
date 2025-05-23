package com.ui;
/*
 * author: xstahl01
 * This class is for visual representation of a game cell in the GUI.
 */

import com.common.GameNode;
import com.common.Position;
import com.common.Side;
import com.game.Game;
import com.utils.GameLogger;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class GameCell extends Pane {
    private static final int SIZE = 60;
    private final GameNode node;
    private final Runnable refreshCallback;
    private final GameLogger logger;

    public GameCell(GameNode node,  Runnable refreshCallback, GameLogger logger) {
        this.node = node;
        this.refreshCallback = refreshCallback;
        this.logger = logger;
        setPrefSize(SIZE, SIZE);
        setStyle("-fx-border-color: #444; -fx-border-width: 1px;");
        drawNode();
        setupClickHandler();
    }

    private void drawNode() {
        getChildren().clear();

        // Draw connectors
        drawConnectors();
        if (node.isBulb()) {
            Group bulbGroup = drawBulbNode();

            getChildren().add(bulbGroup);
        } else if (node.isPower()) {
            // Create lightning bolt path with dynamic coordinates
                Path bolt = drawPowerNode();

                    // Style the bolt
                bolt.setFill(Color.GOLD);
                bolt.setStroke(Color.ORANGERED);
                bolt.strokeWidthProperty().bind(widthProperty().divide(30));
                bolt.setEffect(new Glow(0.3));

                StackPane container = new StackPane();
                container.getChildren().add(bolt);
                container.setAlignment(Pos.CENTER); // Auto-centers the bolt
    
    
            getChildren().add(bolt);

        }
    }

    private Path drawPowerNode(){
        Path bolt = new Path();
        
        // Define proportional coordinates (relative to cell size)
        MoveTo start = new MoveTo();
        start.xProperty().bind(widthProperty().multiply(0.4));
        start.yProperty().bind(heightProperty().multiply(0.2));
        
        LineTo line1 = new LineTo();
        line1.xProperty().bind(widthProperty().multiply(0.6));
        line1.yProperty().bind(heightProperty().multiply(0.2));
        
        LineTo line2 = new LineTo();
        line2.xProperty().bind(widthProperty().multiply(0.45));
        line2.yProperty().bind(heightProperty().multiply(0.45));
        
        LineTo line3 = new LineTo();
        line3.xProperty().bind(widthProperty().multiply(0.7));
        line3.yProperty().bind(heightProperty().multiply(0.45));
        
        LineTo line4 = new LineTo();
        line4.xProperty().bind(widthProperty().multiply(0.35));
        line4.yProperty().bind(heightProperty().multiply(0.8));
        
        LineTo line5 = new LineTo();
        line5.xProperty().bind(widthProperty().multiply(0.5));
        line5.yProperty().bind(heightProperty().multiply(0.5));

        LineTo line6 = new LineTo();
        line6.xProperty().bind(widthProperty().multiply(0.33));
        line6.yProperty().bind(heightProperty().multiply(0.5));

        LineTo line7 = new LineTo();
        line7.xProperty().bind(widthProperty().multiply(0.4));
        line7.yProperty().bind(heightProperty().multiply(0.2));


        bolt.getElements().addAll(start, line1, line2, line3, line4, line5, line6, line7);
        return bolt;

    }

    private Group drawBulbNode(){
        Group bulbGroup = new Group();

        // Bulb body (teardrop shape)
        Ellipse bulbBody = new Ellipse();
        bulbBody.radiusXProperty().bind(widthProperty().divide(4)); // 25% width
        bulbBody.radiusYProperty().bind(heightProperty().divide(3)); // 33% height
        bulbBody.centerXProperty().bind(widthProperty().divide(2));
        bulbBody.centerYProperty().bind(heightProperty().divide(2));
        bulbBody.setFill(node.light() ? Color.YELLOW : Color.DODGERBLUE);
        bulbBody.strokeWidthProperty().bind(widthProperty().divide(30)); // Thin stroke
        bulbBody.setStroke(node.light() ? Color.GOLD : Color.ROYALBLUE);
    
        // Metal base (scales with cell size)
        Rectangle base = new Rectangle();
        base.widthProperty().bind(widthProperty().divide(4)); 
        base.heightProperty().bind(heightProperty().divide(8)); // 12.5% height
        base.xProperty().bind(widthProperty().divide(2).subtract(base.widthProperty().divide(2)));
        base.yProperty().bind(heightProperty().multiply(0.8)); // Position at 70% height
        base.setFill(Color.DIMGRAY);
        base.setArcWidth(5); // Rounded corners
        // Glow effect when lit
        if (node.light()) {
            bulbBody.setEffect(new Glow(0.8));
        }

        bulbGroup.getChildren().addAll(bulbBody, base);
        return bulbGroup;
    }

    private void drawConnectors() {
        for (Side side : node.getConnectors()) {
            Line connector = createConnectorLine(side);
            getChildren().add(connector);
        }
    }

    private Line createConnectorLine(Side side) {
        Line line = new Line();
        line.startXProperty().bind(widthProperty().divide(2));
        line.startYProperty().bind(heightProperty().divide(2));
        
        switch (side) {
            case NORTH -> {
                line.endXProperty().bind(widthProperty().divide(2));
                line.endYProperty().set(0); // Top edge
            }
            case SOUTH -> {
                line.endXProperty().bind(widthProperty().divide(2));
                line.endYProperty().bind(heightProperty()); // Bottom edge
            }
            case WEST -> {
                line.endXProperty().set(0); // Left edge
                line.endYProperty().bind(heightProperty().divide(2));
            }
            case EAST -> {
                line.endXProperty().bind(widthProperty()); // Right edge
                line.endYProperty().bind(heightProperty().divide(2));
            }
        }
        return line;
    }

    private void setupClickHandler() {
        setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY) {
                node.turn();
                if(logger != null) {
                    Position pos = node.getPosition();
                    logger.logMove(pos);
                }
                refreshCallback.run();
            }
        });
    }
}
