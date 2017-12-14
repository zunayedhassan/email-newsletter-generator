package zunayedhassan.EmailTemplateMaker;

import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Zunayed Hassan
 */
public class EmailTemplateMakerProgram extends BorderPane {
    private              VBox              _root                     = new VBox(20);
    private              TextField         _outputTextField          = new TextField();
    private              Button            _browseButton             = new Button("Browse", new ImageView(new Image(this.getClass().getResourceAsStream("icons/folder.png"))));
    private              Button            _openFileButton           = new Button(null, new ImageView(new Image(this.getClass().getResourceAsStream("icons/text-html.png"))));
    private              Button            _generateButton           = new Button("Generate", new ImageView(new Image(this.getClass().getResourceAsStream("icons/play-button.png"))));
    private              DirectoryChooser  _outputFolder             = new DirectoryChooser();
    private              Stage             _stage                    = null;
    private              Alert             _fileNotExistsAlert       = new Alert(Alert.AlertType.WARNING, "File not exists", ButtonType.CLOSE);
    
    /* Theme */
    private              String            _themeBodyBackground      = null;
    private              String            _themeFontFamily          = null;
    private              String            _themeBodyTextColor       = null;
    private              String            _themeWrapperBackground   = null;
    private              String            _themeCardBackground      = null;
    private              String            _themeAltTextColor        = null;
    private              String            _themeHeaderTextColor     = null;
    private              String            _themeBox                 = null;
    private              String            _themeBorderColor         = null;
    private              String            _themeButton              = null;
    private              String            _themeBottomNavBg         = null;
    private              String            _themeBottomNavTextColor  = null;
    private              String            _themeFooterBg            = null;
    private              String            _themeFooterTextColor     = null;
    private              boolean           _isThemeCardView          = false;
    private              ArrayList<String> _themeFileNames           = new ArrayList<>();
    private              ArrayList<String> _articleFileNames         = new ArrayList<>();
    private              ArrayList<String> _bottomNavFileNames       = new ArrayList<>();
    private              ArrayList<String> _footerFileNames          = new ArrayList<>();
    private              ArrayList<String> _headerFileNames          = new ArrayList<>();
    
    
    public EmailTemplateMakerProgram(Stage stage) {
        this._initializeData(stage);
        this._initializeGUI();
        this._initializeEvents();
        this._initializeFinally();
    }
    
    private void _initializeData(Stage stage) {
        this._stage = stage;
    }
    
    private void _initializeGUI() {
        this.setCenter(this._root);
        this._root.setAlignment(Pos.CENTER);
        
        HBox outputSettingsPane = new HBox(5);
        outputSettingsPane.setAlignment(Pos.CENTER);
        this._root.getChildren().addAll(
                outputSettingsPane,
                this._generateButton
        );
        
        outputSettingsPane.getChildren().addAll(
                new Label("Output:"),
                this._outputTextField,
                this._browseButton,
                this._openFileButton
        );
        
        this._browseButton.setCursor(Cursor.HAND);
        Tooltip browseButtonTooltip = new Tooltip("Browse for Output Folder");
        this._browseButton.setTooltip(browseButtonTooltip);
        
        this._openFileButton.setCursor(Cursor.HAND);
        Tooltip openFileButtonTooltip = new Tooltip("Open Email Template");
        this._openFileButton.setTooltip(openFileButtonTooltip);
        
        this._generateButton.setCursor(Cursor.HAND);
        Tooltip generateButtonTooltip = new Tooltip("Generate Email Template");
        this._generateButton.setTooltip(generateButtonTooltip);
        this._generateButton.setFont(Font.font(16));
        this._generateButton.setMinWidth(256);
        this._generateButton.setStyle("-fx-background-radius: 50; -fx-padding: 10;");
        
        ((Stage) this._fileNotExistsAlert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(this.getClass().getResourceAsStream(EmailTemplateMakerApp.ICON)));
    }
    
    private void _initializeEvents() {
        this._browseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                _onBrowse();
            }
        });
        
        this._openFileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                _onOpenFile();
            }
        });
        
        this._generateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                _generateEmailTemplates();
            }
        });
    }
    
    private void _generateEmailTemplates() {
        this._copyAssetsDirectory();
        
        ArrayList<String> categories = this._getTextFromFile("contents" + File.separator + "categories.txt");
        
        for (String categoryName: categories) {
            if (!categoryName.trim().equals("")) {
                categoryName = categoryName.trim();
                
                for (int i = 1; i <= 6; i++) {
                    String outputFileName = this._getOutputPath() + File.separator + categoryName + "00" + i + ".html";
                    this._onGenerateEmailTemplate(outputFileName);
                }
            }
        }
    }
    
    private String _getOutputPath() {
        return this._outputTextField.getText().trim();
    }
    
    private void _initializeFinally() {
        
    }
    
    private void _onBrowse() {
        File selectedFolder = this._outputFolder.showDialog(this._stage);
        
        if (selectedFolder != null) {
            this._outputTextField.setText(selectedFolder.getAbsolutePath());
        }
    }
    
    private void _onOpenFile() {
        File outputFolder = new File(this._getOutputPath());
        
        if (!outputFolder.exists()) {
            this._fileNotExistsAlert.showAndWait();
        }
        else {
            try {
                Runtime.getRuntime().exec("explorer.exe " + outputFolder);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    
    private ArrayList<String> _getTextFromFile(String fileName) {
        ArrayList<String> output = new ArrayList<>();
        
        try {
            Stream<String> stream = Files.lines(Paths.get(fileName));
            
            stream.forEach(new Consumer<String>() {
                @Override
                public void accept(String line) {
                    output.add(line);
                }
            });
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        
        return output;
    }
    
    private String _getStringFromArrayList(ArrayList<String> inputText) {
        String outputText = "";
        
        for (String line: inputText) {
            outputText += line + "\n";
        }
        
        return outputText;
    }
    
    private void _writeToFile(String inputText, String outputFileName) {
        BufferedWriter writer = null;
        
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write(inputText);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
    
    private void _addToList(ArrayList<String> granary, ArrayList<String> wheats) {
        for (String wheat: wheats) {
            granary.add(wheat);
        }
    }
    
    // Format: [properties: text-color]
    private String _getHtmlLineAfterReadingProperties(String html) {
        final String propertyIdentifier    = "[properties:";
        final String propertyIdentifierEnd = "]";
        
        if (html.contains(propertyIdentifier)) {
            int startIndex = html.indexOf(propertyIdentifier);
            int endIndex   = html.indexOf(propertyIdentifierEnd, startIndex);
            
            String property = html.substring(startIndex, endIndex).substring(propertyIdentifier.length()).trim();
            
            if (property.equals("body-background")) {
                html = html.replace(propertyIdentifier + " " + "body-background" + propertyIdentifierEnd, this._themeBodyBackground);
            }
            else if (property.equals("font-family")) {
                html = html.replace(propertyIdentifier + " " + "font-family" + propertyIdentifierEnd, this._themeFontFamily);
            }
            else if (property.equals("body-text-color")) {
                html = html.replace(propertyIdentifier + " " + "body-text-color" + propertyIdentifierEnd, this._themeBodyTextColor);
            }
            else if (property.equals("wrapper-background")) {
                html = html.replace(propertyIdentifier + " " + "wrapper-background" + propertyIdentifierEnd, this._themeWrapperBackground);
            }
            else if (property.equals("card-background")) {
                html = html.replace(propertyIdentifier + " " + "card-background" + propertyIdentifierEnd, this._themeCardBackground);
            }
            else if (property.equals("alt-text-color")) {
                html = html.replace(propertyIdentifier + " " + "alt-text-color" + propertyIdentifierEnd, this._themeAltTextColor);
            }
            else if (property.equals("header-text-color")) {
                html = html.replace(propertyIdentifier + " " + "header-text-color" + propertyIdentifierEnd, this._themeHeaderTextColor);
            }
            else if (property.equals("box")) {
                html = html.replace(propertyIdentifier + " " + "box" + propertyIdentifierEnd, this._themeBox);
            }
            else if (property.equals("border-color")) {
                html = html.replace(propertyIdentifier + " " + "border-color" + propertyIdentifierEnd, this._themeBorderColor);
            }
            else if (property.equals("button")) {
                html = html.replace(propertyIdentifier + " " + "button" + propertyIdentifierEnd, this._themeButton);
            }
            else if (property.equals("bottom-nav-bg")) {
                html = html.replace(propertyIdentifier + " " + "bottom-nav-bg" + propertyIdentifierEnd, this._themeBottomNavBg);
            }
            else if (property.equals("bottom-nav-text-color")) {
                html = html.replace(propertyIdentifier + " " + "bottom-nav-text-color" + propertyIdentifierEnd, this._themeBottomNavTextColor);
            }
            else if (property.equals("footer-bg")) {
                html = html.replace(propertyIdentifier + " " + "footer-bg" + propertyIdentifierEnd, this._themeFooterBg);
            }
            else if (property.equals("footer-text-color")) {
                html = html.replace(propertyIdentifier + " " + "footer-text-color" + propertyIdentifierEnd, this._themeFooterTextColor);
            }
            
             
            if (html.contains(propertyIdentifier)) {
                html = this._getHtmlLineAfterReadingProperties(html);
            }
        }
        
        return html;
    }
    
    private void _readAndApplyThemeFromHtmlLine(ArrayList<String> htmlLines) {
        for (int i = 0; i < htmlLines.size(); i++) {
            String htmlLine = htmlLines.get(i);
            htmlLine = this._getHtmlLineAfterReadingProperties(htmlLine);
            htmlLines.set(i, htmlLine);
        }
    }
    
    private void _loadTheme(String themeName) {
        ArrayList<String> themeLines = this._getTextFromFile(themeName);
        
        for (String line: themeLines) {
            if (!line.trim().equals("") && line.contains("~")) {
                String propertyName = line.split("~")[0].trim();
                String value        = line.split("~")[1].trim();
                
                if (propertyName.equals("body-background")) {
                    this._themeBodyBackground = value;
                }
                else if (propertyName.equals("font-family")) {
                    this._themeFontFamily = value;
                }
                else if (propertyName.equals("body-text-color")) {
                    this._themeBodyTextColor = value;
                }
                else if (propertyName.equals("wrapper-background")) {
                    this._themeWrapperBackground = value;
                }
                else if (propertyName.equals("card-background")) {
                    this._themeCardBackground = value;
                }
                else if (propertyName.equals("alt-text-color")) {
                    this._themeAltTextColor = value;
                }
                else if (propertyName.equals("header-text-color")) {
                    this._themeHeaderTextColor = value;
                }
                else if (propertyName.equals("box")) {
                    this._themeBox = value;
                }
                else if (propertyName.equals("border-color")) {
                    this._themeBorderColor = value;
                }
                else if (propertyName.equals("button")) {
                    this._themeButton = value;
                }
                else if (propertyName.equals("is-card-view")) {
                    this._isThemeCardView = value.toLowerCase().equals("true") ? true : false;
                }
                else if (propertyName.equals("bottom-nav-bg")) {
                    this._themeBottomNavBg = value;
                }
                else if (propertyName.equals("bottom-nav-text-color")) {
                    this._themeBottomNavTextColor = value;
                }
                else if (propertyName.equals("footer-bg")) {
                    this._themeFooterBg = value;
                }
                else if (propertyName.equals("footer-text-color")) {
                    this._themeFooterTextColor = value;
                }
            }
        }
        
        if (this._isThemeCardView) {
            this._themeBorderColor = "";
        }
        else {
            this._themeBox = "";
        }
    }
    
    private int _getRandom(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        
        return randomNum;
    }
    
    private int[] _getRandomArray(int min, int max, int total) {
        int[] array = new int[total];
        
        for (int i = 1; i <= total; i++) {
            int output = 1;
            boolean isValid = true;
            
            do {
                isValid = true;
                output = this._getRandom(min, max);
                
                for (int j = 0; j < array.length; j++) {
                    if (output == array[j]) {
                        isValid = false;
                        break;
                    }
                }
                
                if (isValid) {
                    break;
                }
                
            } while (isValid != true);
            
            if (output > 0) {
                array[i - 1] = output;
            }
        }
        
        return array;
    }
    
    private void _onGenerateEmailTemplate(String outputFileName) {
        if (outputFileName.trim().equals("")) {
            this._fileNotExistsAlert.showAndWait();
        }
        else {
            final String      assetsDirectory = "contents";
            
            ArrayList<String> output          = new ArrayList<>();
            
            // Load theme
            this._themeFileNames.clear();
            
            File[] themeFiles = new File(assetsDirectory + File.separator + "themes" + File.separator).listFiles();
            
            for (File file : themeFiles) {
                if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                    this._themeFileNames.add(file.getAbsolutePath());
                }
            }
            
            String themeFileName = this._themeFileNames.get(this._getRandom(1, this._themeFileNames.size()) - 1);
            this._loadTheme(themeFileName);
            
            // Start
            ArrayList<String> start           = this._getTextFromFile(assetsDirectory + File.separator + "main" + File.separator + "start.txt");
            this._readAndApplyThemeFromHtmlLine(start);
            this._addToList(output, start);
            
            // Header
            this._headerFileNames.clear();

            File[] headerFiles = new File(assetsDirectory + File.separator + "headers" + File.separator).listFiles();
            
            for (File file : headerFiles) {
                if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                    this._headerFileNames.add(file.getAbsolutePath());
                }
            }
            
            String            headerFileName = this._headerFileNames.get(this._getRandom(0, this._headerFileNames.size() - 1));
            ArrayList<String> header         = this._getTextFromFile(headerFileName);
            this._readAndApplyThemeFromHtmlLine(header);
            this._addToList(output, header);
            
            
            // Article
            this._articleFileNames.clear();
            
            File[] articleFiles = new File(assetsDirectory + File.separator + "articles" + File.separator).listFiles();
            
            for (File file : articleFiles) {
                if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                    this._articleFileNames.add(file.getAbsolutePath());
                }
            }
            
            int[] articleIndex = this._getRandomArray(0, this._articleFileNames.size() - 1, this._getRandom(3, this._articleFileNames.size() - 1));
            
            for (int i = 0; i < articleIndex.length; i++) {
                int    index           = articleIndex[i];
                String articleFileName = this._articleFileNames.get(index);
                
                ArrayList<String> body = this._getTextFromFile(articleFileName);
                this._readAndApplyThemeFromHtmlLine(body);
                this._addToList(output, body);
            }
            
            ArrayList<String> endWrapper = new ArrayList<>();
            endWrapper.add("</div>");
            this._addToList(output, endWrapper);

            // Bottom Navigation
            this._bottomNavFileNames.clear();
            
            File[] bottomNavigationFiles = new File(assetsDirectory + File.separator + "bottom-navigations" + File.separator).listFiles();
            
            for (File file : bottomNavigationFiles) {
                if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                    this._bottomNavFileNames.add(file.getAbsolutePath());
                }
            }
            
            String bottomNavFileName = this._bottomNavFileNames.get(this._getRandom(0, this._bottomNavFileNames.size() - 1));
            
            ArrayList<String> bottomNav = this._getTextFromFile(bottomNavFileName);
            this._readAndApplyThemeFromHtmlLine(bottomNav);
            this._addToList(output, bottomNav);
            
            // Footer
            this._footerFileNames.clear();
            
            File[] footerFiles = new File(assetsDirectory + File.separator + "footers" + File.separator).listFiles();
            
            for (File file : footerFiles) {
                if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
                    this._footerFileNames.add(file.getAbsolutePath());
                }
            }
            
            String footerFileName = this._footerFileNames.get(this._getRandom(0, this._footerFileNames.size() - 1));
            
            ArrayList<String> footer = this._getTextFromFile(footerFileName);
            this._readAndApplyThemeFromHtmlLine(footer);
            this._addToList(output, footer);
            
            
            // End
            ArrayList<String> end             = this._getTextFromFile(assetsDirectory + File.separator + "main" + File.separator + "end.txt");
            this._addToList(output, end);
            
            // Write to file
            this._writeToFile(this._getStringFromArrayList(output), outputFileName);
        }
    }
    
    private void _copyAssetsDirectory() {
        String dest = this._getOutputPath() + File.separator + "assets";
        
        if (!dest.equals("")) {
            String src = "contents" + File.separator + "assets";
            
            try {
                FileUtils.copyDirectory(new File(src), new File(dest));
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
