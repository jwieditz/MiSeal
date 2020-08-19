package de.unigoettingen.math.fingerprint.display.controller;

import java.io.IOException;
import java.util.HashMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {

    public TabPane tabs;

    private HashMap<Tab, ImageTabController> controllers = new HashMap<>();

    public void onClickNew() throws IOException {
        createNewTab();
    }

    public void onClickClose() throws IOException {

    }

    public void createFirstTab() throws IOException {
        // workaround to use createNewTab while still showing only one tab at start and see the tab in Scene Builder
        tabs.getTabs().clear();

        createNewTab();
    }

    private void createNewTab() throws IOException {
        Tab tab = new Tab("New");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/imagetab.fxml"));

        tab.setContent(loader.load());
        tabs.getTabs().add(tab);
        loader.<ImageTabController> getController().setTab(tab);

        controllers.put(tab, loader.getController());

        tabs.getSelectionModel().select(tab);
    }

    public void onClickExport() {
        Tab selected = tabs.getSelectionModel().getSelectedItem();
        controllers.get(selected).exportCurrent();
    }

    public void onClickExportAll() {
        Tab selected = tabs.getSelectionModel().getSelectedItem();
        controllers.get(selected).exportAll();
    }

    public void onClickSave() {
        Tab selected = tabs.getSelectionModel().getSelectedItem();
        controllers.get(selected).saveCurrent();
    }
}
