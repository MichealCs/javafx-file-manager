package com.konv.dolphinexplorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class ListView extends javafx.scene.control.ListView<String> {

    public static final KeyCombination SHORTCUT_NAVIGATE = new KeyCodeCombination(KeyCode.ENTER);
    public static final KeyCombination SHORTCUT_BACK = new KeyCodeCombination(KeyCode.BACK_SPACE);
    public static final KeyCombination SHORTCUT_DELETE = new KeyCodeCombination(KeyCode.DELETE);
    public static final KeyCombination SHORTCUT_REFRESH = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination SHORTCUT_NEW_FILE = new KeyCodeCombination(KeyCode.N,
            KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SHORTCUT_NEW_DIRECTORY = new KeyCodeCombination(KeyCode.N,
            KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCombination SHORTCUT_RENAME = new KeyCodeCombination(KeyCode.F6, KeyCombination.SHIFT_DOWN);

    private File mDirectory;
    private TextField mTextField;
    private ObservableList<String> mChildrenList;

    public ListView(String path) {
        super();
        mDirectory = new File(path);

        mChildrenList = FXCollections.observableArrayList();
        setItems(mChildrenList);

        mTextField = new TextField();
        mTextField.setStyle("-fx-font-size: 10px;");
        mTextField.setOnAction(e -> goToFile(mTextField.getText()));

        setOnKeyReleased(key -> {
            if (SHORTCUT_NAVIGATE.match(key)) {
                navigate(getSelectionModel().getSelectedItem());
            } else if (SHORTCUT_BACK.match(key)) {
                back();
            } else if (SHORTCUT_NEW_FILE.match(key)) {
                createFile();
            } else if (SHORTCUT_NEW_DIRECTORY.match(key)) {
                createDirectory();
            } else if (SHORTCUT_RENAME.match(key)) {
                rename();
            } else if (SHORTCUT_DELETE.match(key)) {
                delete();
            } else if (SHORTCUT_REFRESH.match(key)) {
                refresh();
            }
        });

        refresh();
    }

    public void refresh() {
        showList(getCurrentFilesList());
        updateTextField();
    }

    public TextField getTextField() {
        return mTextField;
    }

    public Path getSelectedFilePath() {
        File file = getSelection();
        return file.toPath();
    }

    public Path getDirectory() {
        return mDirectory.toPath();
    }

    public void createFile() {
        FileHelper.createFile(getDirectory());
        refresh();
    }

    public void createDirectory() {
        FileHelper.createDirectory(getDirectory());
        refresh();
    }

    public void delete() {
        FileHelper.delete(getSelectedFilePath());
    }

    public void rename() {
        FileHelper.rename(getSelectedFilePath());
        refresh();
    }

    private String[] getCurrentFilesList() {
        File[] listFiles = mDirectory.listFiles(file -> !file.isHidden());

        if (listFiles == null) {
            listFiles = new File[0];
        }

        Arrays.sort(listFiles, (f1, f2) -> {
            if ((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())) {
                return f1.compareTo(f2);
            }
            return f1.isDirectory() ? -1 : 1;
        });

        String[] list = new String[listFiles.length];
        for (int i = 0; i < list.length; ++i) {
            list[i] = listFiles[i].getName();
        }

        return list;
    }

    public File getSelection() {
        String name = getSelectionModel().getSelectedItem();
        if (name == null) return null;
        return new File(mDirectory.getAbsolutePath() + File.separator + name);
    }

    private void showList(String[] list) {
        if (list != null) {
            mChildrenList.clear();
            mChildrenList.addAll(list);
        } else {
            mChildrenList.clear();
        }
    }

    private void goToFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            updateTextField();
            return;
        }
        if (file.isDirectory()) {
            mDirectory = file;
            refresh();
        } else if (file.isFile()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception e) {

            }
        }
    }

    private void updateTextField() {
        mTextField.setText(mDirectory.getAbsolutePath());
    }

    private void navigate(String name) {
        String selectedPath = mDirectory.getAbsolutePath() + File.separator + name;
        File selectedFile = new File(selectedPath);
        if (selectedFile.isDirectory()) {
            mDirectory = selectedFile;
            refresh();
        } else {
            try {
                Desktop.getDesktop().open(selectedFile);
            } catch (Exception e) {
                DialogHelper.showException(e);
            }
        }
    }

    private void back() {
        File parent = mDirectory.getParentFile();
        if (parent != null) {
            mDirectory = parent;
            refresh();
        }
    }
}