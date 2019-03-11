package jsouplesse.gui;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jsouplesse.dataaccess.Connector;
import jsouplesse.dataaccess.SqlHelper;
import jsouplesse.scraping.ScrapeService;
import jsouplesse.util.CrappyLogger;

public class MainScreenBuilder {
	
	private GridPane mainScreen;
	
//	private Connector connector;
	
	private SqlHelper sqlHelper;
	
	private CrappyLogger logger;
		
	private ScrapeService service;

	// Builders.
	/** Builds standard format buttons. */
	private ButtonBuilder buttonBuilder = new ButtonBuilder();
	
	/** Capable of building an entire new input section in one go. */
	private InputSectionBuilder inputSectionBuilder = new InputSectionBuilder();
	
	// Concrete screen elements.
	
	private TextField inputPageUrl;
	
	private CheckBox isTestRunCheckBox;
	
	private Button scrapeButton;
	
	private Button addInputSectionButton;
	
	private Button removeInputSectionButton;
	
	private Deque<InputSection> inputSections = new LinkedList<>();
	
	private int rowIndexButtons = 2;
	
	
	public MainScreenBuilder(Connector connector, CrappyLogger logger) {
		sqlHelper = new SqlHelper(connector);
		this.logger = logger;
	}
	
	public GridPane buildScreen() {
		// Instantiate the grid pane and set some basic features.
		mainScreen = new GridPane();
		mainScreen.setAlignment(Pos.CENTER);
		// Padding of 40 pixels on each side.
		mainScreen.setPadding(new Insets(40, 40, 40, 40));
		// Set horizontal gaps between columns.
		mainScreen.setHgap(10);
		// Set vertical gaps between rows.
		mainScreen.setVgap(10);
		
		// Define and set a header.
		Label header = new Label("Fill some fields and hit scrape!");
		header.setFont(Font.font("Calibri", FontWeight.BOLD, 20));
		mainScreen.add(header, 0,  0, 2, 1);
		GridPane.setHalignment(header, HPos.CENTER);
		GridPane.setMargin(header, new Insets(20, 0, 20, 0));

		// Define some column constraints.
		// Min-width 100, preferred-width 100 and max-width Double.MAX_VALUE.
		ColumnConstraints columnOneConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
		columnOneConstraints.setHalignment(HPos.RIGHT);
		
		ColumnConstraints columnTwoConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
		columnTwoConstraints.setHgrow(Priority.ALWAYS);
		columnTwoConstraints.setHalignment(HPos.RIGHT);
		
		ColumnConstraints columnThreeConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
		columnThreeConstraints.setHgrow(Priority.ALWAYS);
		columnThreeConstraints.setHalignment(HPos.LEFT);
		
		ColumnConstraints columnFourConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
		columnFourConstraints.setHgrow(Priority.ALWAYS);
		columnFourConstraints.setHalignment(HPos.LEFT);
		
		mainScreen.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstraints, 
				columnThreeConstraints, columnFourConstraints);

		service = new ScrapeService(sqlHelper, logger);
		
		addButtons();

		addInitialInputFields();		
		
		return mainScreen;
	}
	
	private void addInitialInputFields() {
		// Add a label for input field 'page URL'.
		Label inputPageUrlLabel = new Label("Page url: ");
		mainScreen.add(inputPageUrlLabel, 0, 1);
		// Add an input field for the page URL.
		inputPageUrl = new TextField(); 
		inputPageUrl.setPrefHeight(40);
		// Add a tool tip for the page URl input field.
		Tooltip pageUrlTooltip = new Tooltip("The full url of the page you want to scrape.");
		inputPageUrl.setTooltip(pageUrlTooltip);
		mainScreen.add(inputPageUrl, 1, 1);
		
		// Add a check box so the user can choose to do a test run.
		isTestRunCheckBox = new CheckBox("Test");
		isTestRunCheckBox.setTooltip(new Tooltip("Check to only retrieve one result."));
		mainScreen.add(isTestRunCheckBox, 2, 1);
		
		addDynamicInputFields();
	}
	
	private void addDynamicInputFields() {
		inputSections.offer(inputSectionBuilder.buildInputSectionForSelector(mainScreen, inputSections.size() + 1));
		rowIndexButtons += 2;
		GridPane.setRowIndex(scrapeButton, rowIndexButtons);
		GridPane.setRowIndex(addInputSectionButton, rowIndexButtons);
		GridPane.setRowIndex(removeInputSectionButton, rowIndexButtons);
	}
	
	private void removeDynamicInputFields() {
		// At least one input section should always be present.
		if (inputSections.size() == 1)
			return;
		// Get the most recently added input section from the Deque.
		InputSection inputSection = inputSections.pollLast();
		// Remove the section from the screen.
		inputSection.removeInputSectionFromScreen(mainScreen);
		// Update the row index and use it to reposition the buttons.
		rowIndexButtons -= 2;
		GridPane.setRowIndex(scrapeButton, rowIndexButtons);
		GridPane.setRowIndex(addInputSectionButton, rowIndexButtons);
		GridPane.setRowIndex(removeInputSectionButton, rowIndexButtons);
	}
	
	private void addButtons() {
		// Add a button that starts the search.
		scrapeButton = buttonBuilder.build("Scrape", createScrapeControl());
		mainScreen.add(scrapeButton, 0, rowIndexButtons);
		GridPane.setHalignment(scrapeButton, HPos.CENTER);
		GridPane.setMargin(scrapeButton, new Insets(20, 0, 20, 0));
		
		addInputSectionButton = buttonBuilder.build("Add step", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				addDynamicInputFields();
			}
		});
		mainScreen.add(addInputSectionButton, 1, rowIndexButtons);
		GridPane.setHalignment(addInputSectionButton, HPos.CENTER);
		GridPane.setMargin(addInputSectionButton, new Insets(20, 0, 20, 0));
		
		removeInputSectionButton = buttonBuilder.build("Remove step", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				removeDynamicInputFields();
			}
		});
		mainScreen.add(removeInputSectionButton, 2, rowIndexButtons);
		GridPane.setHalignment(removeInputSectionButton, HPos.CENTER);
		GridPane.setMargin(removeInputSectionButton, new Insets(20, 0, 20, 0));
	}
		
	/**
	 * Helper method to {@link #addScrapeButton(GridPane)}. Builds a 'scrape' button which
	 * triggers a scan of the web page when clicked.
	 */
	private EventHandler<ActionEvent> createScrapeControl() {
		
		return new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				// Check whether a value for page URL has been entered.		
				if (inputPageUrl.getText().isEmpty()) {
					showAlert(Alert.AlertType.ERROR, "Invalid input!", 
							"Please enter a valid page url.");
					return;
				}
				// Create a list to hold the input.
				ArrayList<ElementEvaluatorInput> completeInput = new ArrayList<>();
				
				// Check whether all input section have been properly filled.
				for (InputSection inputSection : inputSections) {
					// Make sure the input is valid.
					if (!inputSection.isValid()) {
						showAlert(Alert.AlertType.ERROR, "Invalid input!", 
								"Please enter at least a tag or attribute for each step.");
						return;
					}
					ElementEvaluatorInput input = inputSection.getElementEvaluatorInput();
					completeInput.add(input);
				}
				// Construct the input for the service.
				SpiderInput spiderInput = new SpiderInput();
				spiderInput.pageUrl = inputPageUrl.getText();
				spiderInput.isTestRun = isTestRunCheckBox.isSelected();
				spiderInput.inputList = completeInput;
				
				// Let the service do its thing with the input.
				if (!service.scrape(spiderInput)) {
					showAlertFromService();
					return;
				}
				
				// For now, only write to file (not to database).
				service.writeCompaniesToFile(service.getScraper(), true);
				showAlertFromService();
			}
		};
	}
	
	/**
	 * Displays the alert that the service created.
	 */
	private void showAlertFromService() {
		Alert alert = service.getAlert();
		alert.show();
	}

	private void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.initOwner(mainScreen.getScene().getWindow());
		alert.show();
	}
}
