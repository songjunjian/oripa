package junit.oripa.bind.state;

import static org.junit.Assert.*;

import javax.swing.JRadioButton;

import org.junit.Test;

import oripa.appstate.ApplicationState;
import oripa.bind.ApplicationStateButtonBinder;
import oripa.bind.BinderInterface;
import oripa.bind.ViewChangeBinder;
import oripa.paint.EditMode;
import oripa.resource.LabelStringResource_en;
import oripa.resource.ResourceHolder;
import oripa.resource.ResourceKey;
import oripa.resource.StringID;
import oripa.viewsetting.ChangeViewSetting;
import oripa.viewsetting.uipanel.OnInputCommandButtonSelected;

public class ButtonBinderTest {

	@Test
	public void testCreate() {

		BinderInterface<ChangeViewSetting> viewChangeBinder = new ViewChangeBinder();
		BinderInterface<ApplicationState<EditMode>> paintBinder = new ApplicationStateButtonBinder();

		//	JRadioButton editModeInputLineButton = new JRadioButton("InputLine", true);
		JRadioButton editModeInputLineButton = (JRadioButton) viewChangeBinder.createButton(
				JRadioButton.class, new OnInputCommandButtonSelected(), StringID.UI.INPUT_LINE_ID);

		ResourceHolder resources = ResourceHolder.getInstance();
		
		assertNotNull(editModeInputLineButton);
		
		String actualText = editModeInputLineButton.getText();
		assertNotNull(actualText);
		System.out.println(actualText);
		assertEquals( resources.getString(ResourceKey.LABEL, StringID.UI.INPUT_LINE_ID), editModeInputLineButton.getText());
	}

	@Test
	public void testCreateWithError() {
	}
}
