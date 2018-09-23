package jsouplesse.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import jsouplesse.singlepage.ScrapeService;
import jsouplesse.singlepage.ScraperInput;

public class MainScreenBuilder {
	
	private GridPane mainScreen;
	
//	private Connector connector;
	
	private SqlHelper sqlHelper;
	
	private TextField inputPageUrl;
	
	private TextField inputTagName;
	
	private TextField inputAttribute;
	
	private ScrapeService service;
	
	public MainScreenBuilder(Connector connector) {
//		this.connector = connector;
		sqlHelper = new SqlHelper(connector);
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
		
		ColumnConstraints columnTwoConstraints = new ColumnConstraints(200, 200, Double.MAX_VALUE);
		// Makes the column grow horizontally when the window size is expanded.
		columnTwoConstraints.setHgrow(Priority.ALWAYS);
		columnTwoConstraints.setPercentWidth(75);
		
		mainScreen.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstraints);

		service = new ScrapeService(sqlHelper);
		
		addInputFields(mainScreen);
		
		addScrapeButton(mainScreen);
		
		return mainScreen;
	}
	
	private void addInputFields(GridPane mainScreen) {
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
		
		// Add a label for input field 'tag'.
		Label inputTagNameLabel = new Label("Tag: ");
		mainScreen.add(inputTagNameLabel, 0, 2);
		// Add input field for 'tag'.
		inputTagName = new TextField();
		inputTagName.setPrefHeight(40);
		// Add a tool tip for the tag input field.
		Tooltip tagTooltip = new Tooltip("The tag you want to search for web shop url's. This can "
				+ "also be a tag that contains the url, for instance: 'div' (input without quotes).");
		inputTagName.setTooltip(tagTooltip);
		mainScreen.add(inputTagName, 1, 2);

		// Add a label for input field attribute.
		Label inputAttributeLabel = new Label("Attribute:");
		mainScreen.add(inputAttributeLabel, 0, 3);
		// Add input field for attribute.
		inputAttribute = new TextField();
		inputAttribute.setPrefHeight(40);
		// Add a tool tip for the attribute input field.
		Tooltip attributeTooltip = new Tooltip("An attribute of the tag you want to scan for. "
				+ "For instance: class=\"web_shop\" (include quotes in input) or 'href' (without quotes).");
		inputAttribute.setTooltip(attributeTooltip);
		mainScreen.add(inputAttribute, 1, 3);
	}
	
	private void addScrapeButton(GridPane mainScreen) {
		// Add a button that starts the search.
		Button scrapeButton = createScrapeButton();
		mainScreen.add(scrapeButton, 1, 4);
		GridPane.setHalignment(scrapeButton, HPos.CENTER);
		GridPane.setMargin(scrapeButton, new Insets(20, 0, 20, 0));
	}
	
	/**
	 * Helper method to {@link #addScrapeButton(GridPane)}. Builds a 'scrape' button which
	 * triggers a scan of the web page when clicked.
	 */
	private Button createScrapeButton() {
		
		Button searchButton = new Button("Scrape");
		searchButton.setPrefHeight(50);
		searchButton.setDefaultButton(true);
		searchButton.setPrefWidth(175);
		
		searchButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				// Check the input.
				if (inputPageUrl.getText().isEmpty() 
						|| (inputTagName.getText().isEmpty()
							&& inputAttribute.getText().isEmpty())) {
					showAlert(Alert.AlertType.ERROR, "Invalid input!", 
							"Please enter at least a page url and either a tag or an attribute."
							+ "to search for.");
					return;
				}
				
				ScraperInput input = new ScraperInput();
				
				input.pageUrl = inputPageUrl.getText();
				input.tagName = inputTagName.getText();
				input.attribute = inputAttribute.getText();
				
				service.scrape(input);
				showAlertFromService();
				
				// For now, only write to file (not to database).
				service.writeCompaniesToFile(service.getScraper(), true);
				showAlertFromService();
			}
		});

		return searchButton;
	}
	
	/**
	 * Displays the alert that the service created.
	 */
	private void showAlertFromService() {
		Alert alert = service.getAlert();
		alert.initOwner(mainScreen.getScene().getWindow());
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
