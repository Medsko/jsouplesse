package jsouplesse.gui;

import java.util.Collections;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import jsouplesse.scraping.CrawlStep;

public class AlertBuilder {

	
	public Alert buildResultFailErrorAlert(String message, List<CrawlStep> failedCrawlSteps) {
		
		// The list is in reverse. Modify it so it goes from first step to last.
		Collections.reverse(failedCrawlSteps);
		
		StringBuilder builder = new StringBuilder();
		
		for (CrawlStep crawlStep : failedCrawlSteps) {
			// Add the crawl step information to the message for the user.
			builder.append(crawlStep.toString());
		}
		// Convert the result to a text object and set it as content on a scroll pane.
		Text errorText = new Text(builder.toString());
		ScrollPane crawlPathScroll = new ScrollPane(errorText);
		// Construct an alert window and set the scroll pane containing the crawl steps on it.
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Result fail!");
		alert.getDialogPane().setContent(crawlPathScroll);
		// Skip that ugly bull shit.
		alert.setHeaderText(null);
		alert.setGraphic(null);
		// Set the modality, so the error blocks all other application screens.
		alert.initModality(Modality.APPLICATION_MODAL);
		// Set the preferred size.
		alert.setResizable(true);
		alert.getDialogPane().setPrefSize(400, 500);
		
		return alert;
	}
}
