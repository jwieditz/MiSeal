package de.unigoettingen.math.fingerprint.display.masks;

import javafx.scene.canvas.Canvas;

// modified https://stackoverflow.com/a/31946816
abstract class ResizableCanvas extends Canvas {

    public ResizableCanvas() {
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    protected void draw() {
        getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
