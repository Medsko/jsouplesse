package jsouplesse.gui;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

public class InputSectionBuilder {
	
	public InputSection buildInputSectionForSelector(GridPane screen, int sectionNumber) {
		
		int rowNumberTagName = sectionNumber * 2;
		
		// Add a label for input field 'tag'.
		Label labelTagName = new Label("Tag: ");
		screen.add(labelTagName, 0, rowNumberTagName);
		// Add input field for 'tag'.
		TextField inputTagName = new TextField();
		inputTagName.setPrefHeight(40);
		// Add a tool tip for the tag input field.
		Tooltip tagTooltip = new Tooltip("The tag you want to search for web shop url's. This can "
				+ "also be a tag that contains the url, for instance: 'div' (input without quotes).");
		inputTagName.setTooltip(tagTooltip);
		screen.add(inputTagName, 1, rowNumberTagName);

		// Create a check box indicating whether the text in the final tag should be selected as output.
		CheckBox fetchTextInLinkCheckBox = new CheckBox("Fetch text in tag");
		screen.add(fetchTextInLinkCheckBox, 2, rowNumberTagName);
		
		int rowNumberAttribute = rowNumberTagName + 1;
		
		// Add a label for input field attribute.
		Label labelAttribute = new Label("Attribute:");
		screen.add(labelAttribute, 0, rowNumberAttribute);
		// Add input field for attribute.
		TextField inputAttribute = new TextField();
		inputAttribute.setPrefHeight(40);
		// Add a tool tip for the attribute input field.
		Tooltip attributeTooltip = new Tooltip("An attribute of the tag you want to scan for. "
				+ "For instance: class=\"web_shop\" (include quotes in input) or 'href' (without quotes).");
		inputAttribute.setTooltip(attributeTooltip);
		screen.add(inputAttribute, 1, rowNumberAttribute);

		// Create a check box so the user can indicate whether the retrieved 
		// link should be used to fetch a page.
		CheckBox fetchPageCheckBox = new CheckBox("Fetch linked page");
		screen.add(fetchPageCheckBox, 2, rowNumberAttribute);
		fetchPageCheckBox.setAlignment(Pos.BOTTOM_LEFT);
				
		// Create an InputSection and set the fields on it. 
		InputSection inputSection = new InputSection();
		inputSection.setLabelTag(labelTagName);
		inputSection.setInputTag(inputTagName);
		inputSection.setLabelAttribute(labelAttribute);
		inputSection.setInputAttribute(inputAttribute);
		inputSection.setFetchPageCheckBox(fetchPageCheckBox);
		inputSection.setFetchTextInLinkCheckBox(fetchTextInLinkCheckBox);
		
		return inputSection;
	}
	
}
