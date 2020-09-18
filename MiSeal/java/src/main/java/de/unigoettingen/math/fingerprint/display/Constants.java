package de.unigoettingen.math.fingerprint.display;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Constants {

    private Constants() {}

    /**
     * bilinear, nearest neighbour, gaussian
     */
    public static final ObservableList<String> INTERPOLATIONS = FXCollections.observableArrayList("bilinear", "nearest neighbour", "gaussian");

    /**
     * none, average, gaussian
     */
    public static final ObservableList<String> SMOOTHINGS_2D = FXCollections.observableArrayList("none", "average", "gaussian");

    /**
     * none, gaussian
     */
    public static final ObservableList<String> SMOOTHINGS_1D = FXCollections.observableArrayList("none", "gaussian");
}
