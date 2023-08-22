package telloflix.views.utils;

import javafx.scene.text.Font;

import java.util.List;

/**
 * Bekannt aus OOP2.
 *
 * ViewMixin unterstützt die Aufteilung eines GUIs in unabhängige Teile und forciert, dass alle diese Teile
 * denselben inneren Aufbau haben.
 *
 */
public interface ViewMixin {

    default void init() {
        initializeSelf();
        initializeParts();
        layoutParts();
        setupEventHandlers();
        setupValueChangedListeners();
        setupBindings();
    }

    default void initializeSelf() {
    }

    void initializeParts();

    void layoutParts();

    default void setupEventHandlers() {
    }

    default void setupValueChangedListeners() {
    }

    default void setupBindings() {
    }

    default void loadFonts(String... font){
        for(String f : font){
            Font.loadFont(getClass().getResourceAsStream(f), 0);
        }
    }

    default void addStylesheetFiles(String... stylesheetFile){
        for(String file : stylesheetFile){
            String stylesheet = getClass().getResource(file).toExternalForm();
            getStylesheets().add(stylesheet);
        }
    }

    List<String> getStylesheets();
}
