package jsouplesse.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jsouplesse.dataaccess.Connector;
import jsouplesse.util.CrappyLogger;

public class Co2OkScrapeOnDemandApplication extends Application {

	private Connector connector;
	
	private CrappyLogger logger;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Since FailedScan is not yet used, do not initialize the Connector.
		connector = new Connector();
		
		// Instantiate a logger that writes to a log file.
		logger = new CrappyLogger(false);
		
		MainScreenBuilder builder = new MainScreenBuilder(connector, logger);
		GridPane mainScreen = builder.buildScreen();
		ScrollPane mainWrapper = addScrollBar(mainScreen);
		
		Scene main = new Scene(mainWrapper, 800, 550);
		
		primaryStage.setScene(main);
		primaryStage.setTitle("Co2ok scrape on demand");
		addOnCloseFunction(primaryStage);
		
		try {
			primaryStage.show();
		} catch (RuntimeException rex) {
			for (StackTraceElement ste : rex.getStackTrace()) {
				logger.log(ste.toString());
			}
		}
	}
	
	private ScrollPane addScrollBar(Pane screenPart) {
		// Create ScrollPane wrapper for the provided screen part.
		ScrollPane scrollPane = new ScrollPane(screenPart);
		// Set the properties: scroll only vertically, fill horizontally.
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		
		return scrollPane;
	}

	/**
	 * Sets the logic that should be executed when the user exits the application
	 * on the primary stage.
	 */
	private void addOnCloseFunction(Stage primaryStage) {
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				// Close the connection to the database.
				connector.close();
				logger.log("Connection to the database closed.");
				logger.deInitialize();
			}
		});
	}
	
	public static void main(String[] args) {
		launch();
	}
}
