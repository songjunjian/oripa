/**
 * ORIPA - Origami Pattern Editor 
 * Copyright (C) 2005-2009 Jun Mitani http://mitani.cs.tsukuba.ac.jp/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package oripa.view.uipanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import oripa.Config;
import oripa.ORIPA;
import oripa.appstate.ApplicationState;
import oripa.bind.BinderInterface;
import oripa.bind.PaintActionSetter;
import oripa.bind.ApplicationStateButtonBinder;
import oripa.bind.ViewChangeBinder;
import oripa.bind.state.InputCommandStatePopper;
import oripa.bind.state.PaintBoundState;
import oripa.bind.state.PaintBoundStateFactory;
import oripa.bind.state.StatePopper;
import oripa.doc.TypeForChange;
import oripa.file.ImageResourceLoader;
import oripa.folder.Folder;
import oripa.geom.OriLine;
import oripa.paint.EditMode;
import oripa.paint.Globals;
import oripa.paint.PaintContext;
import oripa.paint.addvertex.AddVertexAction;
import oripa.paint.bisector.AngleBisectorAction;
import oripa.paint.byvalue.AngleMeasuringAction;
import oripa.paint.byvalue.LengthMeasuringAction;
import oripa.paint.byvalue.LineByValueAction;
import oripa.paint.byvalue.ValueDB;
import oripa.paint.deleteline.DeleteLineAction;
import oripa.paint.deletevertex.DeleteVertexAction;
import oripa.paint.line.TwoPointLineAction;
import oripa.paint.linetype.ChangeLineTypeAction;
import oripa.paint.mirror.MirrorCopyAction;
import oripa.paint.pbisec.TwoPointBisectorAction;
import oripa.paint.segment.TwoPointSegmentAction;
import oripa.paint.selectline.SelectLineAction;
import oripa.paint.symmetric.SymmetricalLineAction;
import oripa.paint.triangle.TriangleSplitAction;
import oripa.paint.vertical.VerticalLineAction;
import oripa.resource.ResourceHolder;
import oripa.resource.ResourceKey;
import oripa.resource.StringID;
import oripa.view.main.MainScreen;
import oripa.viewsetting.ChangeViewSetting;
import oripa.viewsetting.ViewChangeListener;
import oripa.viewsetting.main.MainScreenSettingDB;
import oripa.viewsetting.main.ScreenUpdater;
import oripa.viewsetting.model.ModelFrameSettingDB;
import oripa.viewsetting.render.RenderFrameSettingDB;
import oripa.viewsetting.uipanel.OnByValueButtonSelected;
import oripa.viewsetting.uipanel.OnChangeTypeButtonSelected;
import oripa.viewsetting.uipanel.OnInputCommandButtonSelected;
import oripa.viewsetting.uipanel.OnOtherCommandButtonSelected;
import oripa.viewsetting.uipanel.OnSelectButtonSelected;
import oripa.viewsetting.uipanel.UIPanelSettingDB;

public class UIPanel extends JPanel 
implements ActionListener, PropertyChangeListener, Observer {


	private UIPanelSettingDB settingDB = UIPanelSettingDB.getInstance();
	private ScreenUpdater screenUpdater = ScreenUpdater.getInstance();
	private ValueDB valueDB = ValueDB.getInstance();
	private PaintContext mouseContext = PaintContext.getInstance();
	ResourceHolder resources = ResourceHolder.getInstance();

	private class ButtonSelectionLinkedListener extends ViewChangeListener{

		AbstractButton modeButton;

		public ButtonSelectionLinkedListener(
				ChangeViewSetting command, AbstractButton modeButton) {

			super(command);
			this.modeButton = modeButton;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			
			if(modeButton == null){
				return;
			}
			
			editModeGroup.setSelected(modeButton.getModel(), true);

		}
	}	

	
	//---------------------------------------------------------------------------------------------------------------------------
	// Binding edit mode

	private BinderInterface<ChangeViewSetting> viewChangeBinder = new ViewChangeBinder();
	private BinderInterface<ApplicationState<EditMode>> paintBinder = new ApplicationStateButtonBinder();

	//	JRadioButton editModeInputLineButton = new JRadioButton("InputLine", true);
	JRadioButton editModeInputLineButton = (JRadioButton) viewChangeBinder.createButton(
			JRadioButton.class, new OnInputCommandButtonSelected(), StringID.UI.INPUT_LINE_ID);

	JRadioButton editModePickLineButton = (JRadioButton) paintBinder.createButton(
			JRadioButton.class, 
			new PaintBoundState(new SelectLineAction(mouseContext), StringID.Command.SELECT_ID, 
					new ActionListener[] {new ViewChangeListener(new OnSelectButtonSelected())}), 
			StringID.UI.SELECT_ID);

	JRadioButton editModeDeleteLineButton = (JRadioButton) paintBinder.createButton(
			JRadioButton.class, 
			new PaintBoundState(new DeleteLineAction(), StringID.Command.DELETE_LINE_ID, 
					new ActionListener[] {new ViewChangeListener(new OnOtherCommandButtonSelected())}), 
 
			StringID.UI.DELETE_LINE_ID);

	JRadioButton editModeLineTypeButton = (JRadioButton) paintBinder.createButton(
			JRadioButton.class, 
			new PaintBoundState(new ChangeLineTypeAction(), StringID.Command.CHANGE_LINE_TYPE_ID, 
					new ActionListener[] {new ViewChangeListener(new OnChangeTypeButtonSelected())}), 
			 
			StringID.UI.CHANGE_LINE_TYPE_ID);

	JRadioButton editModeAddVertex =(JRadioButton) paintBinder.createButton(
			JRadioButton.class, 
			new PaintBoundState(new AddVertexAction(), StringID.Command.ADD_VERTEX_ID, 
					new ActionListener[] {new ViewChangeListener(new OnOtherCommandButtonSelected())}),			 
			StringID.UI.ADD_VERTEX_ID);

	JRadioButton editModeDeleteVertex = (JRadioButton) paintBinder.createButton(
			JRadioButton.class, 
			new PaintBoundState(new DeleteVertexAction(), StringID.Command.DELETE_VERTEX_ID, 
					new ActionListener[] {new ViewChangeListener(new OnOtherCommandButtonSelected())}),
					StringID.UI.DELETE_VERTEX_ID);

	//---------------------------------------------------------------------------------------------------------------------------


	JRadioButton lineTypeSubButton = new JRadioButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.AUX_ID));
	JRadioButton lineTypeMountainButton = new JRadioButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.MOUNTAIN_ID));
	JRadioButton lineTypeValleyButton = new JRadioButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.VALLEY_ID));

	//---------------------------------------------------------------------------------------------------------------------------
	// Binding how to enter the line

	private PaintBoundStateFactory stateFactory = new PaintBoundStateFactory(this, 
			new ActionListener[] {new ButtonSelectionLinkedListener(new OnInputCommandButtonSelected(), editModeInputLineButton)} );
	private BinderInterface<ApplicationState<EditMode>> inputCommandBinder = new ApplicationStateButtonBinder();
	
	JRadioButton lineInputDirectVButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new TwoPointSegmentAction(), 
					StringID.Command.DIRECT_V_ID, null),
			 
			null);

	JRadioButton lineInputOnVButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new TwoPointLineAction(), 
					StringID.Command.ON_V_ID, null),
			StringID.Command.ON_V_ID);

	JRadioButton lineInputVerticalLineButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new VerticalLineAction(), 
					StringID.Command.VERTICAL_ID, null),
			StringID.Command.VERTICAL_ID);

	JRadioButton lineInputAngleBisectorButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new AngleBisectorAction(), 
					StringID.Command.BISECTOR_ID, null),
			StringID.Command.BISECTOR_ID);

	JRadioButton lineInputTriangleSplitButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new TriangleSplitAction(), 
					StringID.Command.TRIANGLE_ID, null),
			StringID.Command.TRIANGLE_ID);

	JRadioButton lineInputSymmetricButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new SymmetricalLineAction(), 
					StringID.Command.SYMMETRIC_ID, null),
			StringID.Command.SYMMETRIC_ID);

	JRadioButton lineInputMirrorButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new MirrorCopyAction(mouseContext), 
					StringID.Command.MIRROR_ID, null),
			StringID.Command.MIRROR_ID);

	JRadioButton lineInputByValueButton = (JRadioButton) paintBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new LineByValueAction(), 
					StringID.Command.BY_VALUE_ID, 
					new ActionListener[] {new ViewChangeListener(new OnByValueButtonSelected())} ),
			StringID.Command.BY_VALUE_ID);

	JRadioButton lineInputPBisectorButton = (JRadioButton) inputCommandBinder.createButton(
			JRadioButton.class, 
			stateFactory.create(new TwoPointBisectorAction(), 
					StringID.Command.PERPENDICULAR_BISECTOR_ID, null),
			StringID.Command.PERPENDICULAR_BISECTOR_ID);

	//---------------------------------------------------------------------------------------------------------------------------


	//	JRadioButton lineInputDirectVButton = new JRadioButton();
	//	JRadioButton lineInputVerticalLineButton = new JRadioButton();
	//	JRadioButton lineInputOnVButton = new JRadioButton();
	//	JRadioButton lineInputOverlapVButton = new JRadioButton();
	//	JRadioButton lineInputOverlapEButton = new JRadioButton();
	//	JRadioButton lineInputAngleBisectorButton = new JRadioButton();
	//	JRadioButton lineInputTriangleSplitButton = new JRadioButton();
	//	JRadioButton lineInputSymmetricButton = new JRadioButton();
	//	JRadioButton lineInputMirrorButton = new JRadioButton();
	//	JRadioButton lineInputByValueButton = new JRadioButton();
	//	JRadioButton lineInputPBisectorButton = new JRadioButton(); // perpendicular bisector
	ButtonGroup editModeGroup;
	// Text box
	JFormattedTextField textFieldLength;
	JFormattedTextField textFieldAngle;
	JFormattedTextField textFieldGrid;

	JButton buttonLength = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.MEASURE_ID));
	JButton buttonAngle = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.MEASURE_ID));

	JButton buildButton = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.FOLD_ID));
	JButton resetButton = new JButton("Reset");

	JCheckBox dispGridCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.UI.SHOW_GRID_ID), true);
	
	JButton gridSmallButton = new JButton("x2");
	JButton gridLargeButton = new JButton("x1/2");
	JButton gridChangeButton = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.GRID_SIZE_CHANGE_ID));

	JPanel mainPanel = new JPanel();
	JPanel subPanel1 = new JPanel();
	JPanel subPanel2 = new JPanel();
	JPanel gridPanel = new JPanel();
	JPanel lineTypePanel = new JPanel();
	// AlterLineType
	JPanel alterLineTypePanel = new JPanel();


	TypeForChange[] alterLine_comboData_from = 
		{TypeForChange.EMPTY, TypeForChange.RIDGE, TypeForChange.VALLEY};
	TypeForChange[] alterLine_comboData_to = 
		{TypeForChange.RIDGE, TypeForChange.VALLEY, TypeForChange.AUX, 
			TypeForChange.CUT, TypeForChange.DELETE, TypeForChange.FLIP};

	JComboBox<TypeForChange> alterLine_combo_from = new JComboBox<>(alterLine_comboData_from);
	JComboBox<TypeForChange> alterLine_combo_to = new JComboBox<>(alterLine_comboData_to);

	JCheckBox dispMVLinesCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.UI.SHOW_MV_ID), true);
	JCheckBox dispAuxLinesCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.UI.SHOW_AUX_ID), true);
	JCheckBox dispVertexCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.UI.SHOW_VERTICES_ID), false);
	JCheckBox doFullEstimationCheckBox = new JCheckBox(
			resources.getString(ResourceKey.LABEL, StringID.UI.FULL_ESTIMATION_ID), false);
	JButton buttonCheckWindow = new JButton(
			resources.getString(ResourceKey.LABEL, StringID.UI.CHECK_WINDOW_ID));
	MainScreen screen;


	//	private PaintContext context = PaintContext.getInstance();




	public UIPanel(MainScreen __screen) {

		//setModeButtonText();
		editModeInputLineButton.setSelected(true);

		this.screen = __screen;
		setPreferredSize(new Dimension(210, 400));

		settingDB.addObserver(this);
		screenDB.addObserver(this);

		//		alterLine_combo_from.setSelectedIndex(0);
		//		alterLine_combo_to.setSelectedIndex(0);
		//		alterLine_combo_from.actionPerformed(null);
		//		alterLine_combo_to.actionPerformed(null);

		// Edit mode
		editModeGroup = new ButtonGroup();
		editModeGroup.add(editModeInputLineButton);
		editModeGroup.add(editModePickLineButton);
		editModeGroup.add(editModeDeleteLineButton);
		editModeGroup.add(editModeLineTypeButton);
		editModeGroup.add(editModeAddVertex);
		editModeGroup.add(editModeDeleteVertex);

		JLabel l1 = new JLabel(
				resources.getString(ResourceKey.LABEL, StringID.UI.CHANGE_LINE_TYPE_FROM_ID));
		
		JLabel l2 = new JLabel(
				resources.getString(ResourceKey.LABEL, StringID.UI.CHANGE_LINE_TYPE_TO_ID));
		
		alterLineTypePanel.add(l1);
		alterLineTypePanel.add(alterLine_combo_from);
		alterLineTypePanel.add(l2);
		alterLineTypePanel.add(alterLine_combo_to);
		alterLineTypePanel.setVisible(false);

		// How to enter the line
		ButtonGroup lineInputGroup = new ButtonGroup();
		lineInputGroup.add(lineInputDirectVButton);
		lineInputGroup.add(lineInputOnVButton);
		lineInputGroup.add(lineInputTriangleSplitButton);
		lineInputGroup.add(lineInputAngleBisectorButton);
		lineInputGroup.add(lineInputVerticalLineButton);
		lineInputGroup.add(lineInputSymmetricButton);
		lineInputGroup.add(lineInputMirrorButton);
		lineInputGroup.add(lineInputByValueButton);
		lineInputGroup.add(lineInputPBisectorButton);

		ButtonGroup lineTypeGroup = new ButtonGroup();
		lineTypeGroup.add(lineTypeMountainButton);
		lineTypeGroup.add(lineTypeValleyButton);
		lineTypeGroup.add(lineTypeSubButton);

		lineTypePanel.setLayout(new GridBagLayout());
		lineTypePanel.add(lineTypeMountainButton);
		lineTypePanel.add(lineTypeValleyButton);
		lineTypePanel.add(lineTypeSubButton);

		lineTypeMountainButton.setSelected(true);


		mainPanel.setLayout(new GridBagLayout());


		int n = 0;
		GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
		gridBagConstraints0.gridx = 1;
		gridBagConstraints0.gridy = 0;
		gridBagConstraints0.gridwidth = 4;
		gridBagConstraints0.anchor = java.awt.GridBagConstraints.WEST;

		mainPanel.add(editModeInputLineButton, gridBagConstraints0);
		n++;


		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.gridwidth = 4;
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;

		mainPanel.add(lineTypePanel, gridBagConstraints1);
		n++;

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.gridwidth = 4;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
		mainPanel.add(editModePickLineButton, gridBagConstraints2);
		n++;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 1;
		gridBagConstraints3.gridy = 3;
		gridBagConstraints3.gridwidth = 4;
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
		mainPanel.add(editModeDeleteLineButton, gridBagConstraints3);
		n++;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 1;
		gridBagConstraints4.gridy = 4;
		gridBagConstraints4.gridwidth = 4;
		gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
		mainPanel.add(editModeLineTypeButton, gridBagConstraints4);
		n++;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 1;
		gridBagConstraints5.gridy = 5;
		gridBagConstraints5.gridwidth = 4;
		gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
		mainPanel.add(alterLineTypePanel, gridBagConstraints5);
		n++;
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 1;
		gridBagConstraints6.gridy = 6;
		gridBagConstraints6.gridwidth = 4;
		gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
		mainPanel.add(editModeAddVertex, gridBagConstraints6);
		n++;
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridx = 1;
		gridBagConstraints7.gridy = 7;
		gridBagConstraints7.gridwidth = 4;
		gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
		mainPanel.add(editModeDeleteVertex, gridBagConstraints7);
		n++;
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 1;
		gridBagConstraints8.gridy = 8;
		gridBagConstraints8.gridwidth = 4;
		gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;

		JLabel label1 = new JLabel("Command (1...9)");
		label1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		mainPanel.add(label1, gridBagConstraints8);
		n++;

		int gridy_base = 9;
		GridBagConstraints gridBagConstraints_i0 = new GridBagConstraints();
		gridBagConstraints_i0.gridx = 1;
		gridBagConstraints_i0.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i1 = new GridBagConstraints();
		gridBagConstraints_i1.gridx = 2;
		gridBagConstraints_i1.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i2 = new GridBagConstraints();
		gridBagConstraints_i2.gridx = 3;
		gridBagConstraints_i2.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i3 = new GridBagConstraints();
		gridBagConstraints_i3.gridx = 4;
		gridBagConstraints_i3.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i4 = new GridBagConstraints();

		gridy_base++;

		gridBagConstraints_i4.gridx = 1;
		gridBagConstraints_i4.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i5 = new GridBagConstraints();
		gridBagConstraints_i5.gridx = 2;
		gridBagConstraints_i5.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i6 = new GridBagConstraints();
		gridBagConstraints_i6.gridx = 3;
		gridBagConstraints_i6.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i7 = new GridBagConstraints();
		gridBagConstraints_i7.gridx = 4;
		gridBagConstraints_i7.gridy = gridy_base;
		GridBagConstraints gridBagConstraints_i8 = new GridBagConstraints();
		gridy_base++;
		gridBagConstraints_i8.gridx = 1;
		gridBagConstraints_i8.gridy = gridy_base;

		gridBagConstraints_i0.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i1.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i3.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i4.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i5.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i6.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i7.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints_i8.fill = java.awt.GridBagConstraints.HORIZONTAL;


		// put operation buttons in order
		mainPanel.add(lineInputDirectVButton, gridBagConstraints_i0);
		n++;
		mainPanel.add(lineInputOnVButton, gridBagConstraints_i1);
		n++;
		mainPanel.add(lineInputPBisectorButton, gridBagConstraints_i2);
		n++;
		mainPanel.add(lineInputAngleBisectorButton, gridBagConstraints_i3);
		n++;
		mainPanel.add(lineInputTriangleSplitButton, gridBagConstraints_i4);
		n++;
		mainPanel.add(lineInputVerticalLineButton, gridBagConstraints_i5);
		n++;
		mainPanel.add(lineInputSymmetricButton, gridBagConstraints_i6);
		n++;
		mainPanel.add(lineInputMirrorButton, gridBagConstraints_i7);
		n++;
		mainPanel.add(lineInputByValueButton, gridBagConstraints_i8);
		n++;


		lineInputDirectVButton.setSelected(true);

		//      lineInputDirectVButton.setIcon(new ImageIcon(getClass().getResource("/icon/segment.gif")));

		ImageResourceLoader imgLoader = new ImageResourceLoader();
		lineInputDirectVButton.setIcon(imgLoader.loadAsIcon("icon/segment.gif"));
		lineInputDirectVButton.setSelectedIcon(imgLoader.loadAsIcon("icon/segment_p.gif"));

		lineInputOnVButton.setIcon(imgLoader.loadAsIcon("icon/line.gif"));
		lineInputOnVButton.setSelectedIcon(imgLoader.loadAsIcon("icon/line_p.gif"));

		lineInputPBisectorButton.setIcon(imgLoader.loadAsIcon("icon/pbisector.gif"));
		lineInputPBisectorButton.setSelectedIcon(imgLoader.loadAsIcon("icon/pbisector_p.gif") );

		lineInputAngleBisectorButton.setIcon(imgLoader.loadAsIcon("icon/bisector.gif") );
		lineInputAngleBisectorButton.setSelectedIcon(imgLoader.loadAsIcon("icon/bisector_p.gif"));

		lineInputTriangleSplitButton.setIcon(imgLoader.loadAsIcon("icon/incenter.gif") );
		lineInputTriangleSplitButton.setSelectedIcon(imgLoader.loadAsIcon("icon/incenter_p.gif"));

		lineInputVerticalLineButton.setIcon(imgLoader.loadAsIcon("icon/vertical.gif"));
		lineInputVerticalLineButton.setSelectedIcon(imgLoader.loadAsIcon("icon/vertical_p.gif"));

		lineInputSymmetricButton.setIcon(imgLoader.loadAsIcon("icon/symmetry.gif"));
		lineInputSymmetricButton.setSelectedIcon(imgLoader.loadAsIcon("icon/symmetry_p.gif"));

		lineInputMirrorButton.setIcon(imgLoader.loadAsIcon("icon/mirror.gif"));
		lineInputMirrorButton.setSelectedIcon(imgLoader.loadAsIcon("icon/mirror_p.gif"));

		lineInputByValueButton.setIcon(imgLoader.loadAsIcon("icon/by_value.gif"));
		lineInputByValueButton.setSelectedIcon(imgLoader.loadAsIcon("icon/by_value_p.gif"));



		setLayout(new FlowLayout());
		add(mainPanel);


		//------------------------------------
		// Panel input for length and angle
		//------------------------------------
		JLabel subLabel1 = new JLabel(
				resources.getString(ResourceKey.LABEL, StringID.UI.LENGTH_ID));

		JLabel subLabel2 = new JLabel(
				resources.getString(ResourceKey.LABEL, StringID.UI.ANGLE_ID));

		subPanel1.setVisible(false);
		subPanel2.setVisible(false);

		NumberFormat doubleValueFormat = NumberFormat.getNumberInstance(Locale.US);
		doubleValueFormat.setMinimumFractionDigits(3);

		textFieldLength = new JFormattedTextField(doubleValueFormat);
		textFieldAngle = new JFormattedTextField(doubleValueFormat);

		textFieldLength.setColumns(4);
		textFieldAngle.setColumns(4);
		textFieldLength.setValue(new java.lang.Double(0.0));
		textFieldAngle.setValue(new java.lang.Double(0.0));

		textFieldLength.setHorizontalAlignment(JTextField.RIGHT);
		textFieldAngle.setHorizontalAlignment(JTextField.RIGHT);

		subPanel1.setLayout(new FlowLayout());
		subPanel2.setLayout(new FlowLayout());
		subPanel1.add(subLabel1);
		subPanel1.add(textFieldLength);
		subPanel1.add(buttonLength);
		subPanel2.add(subLabel2);
		subPanel2.add(textFieldAngle);
		subPanel2.add(buttonAngle);
		buttonLength.addActionListener(
				new PaintActionSetter(new LengthMeasuringAction()));
		buttonLength.addActionListener(
				new ButtonSelectionLinkedListener(new OnByValueButtonSelected(), editModeInputLineButton));

		buttonAngle.addActionListener(
				new PaintActionSetter(new AngleMeasuringAction()));
		buttonAngle.addActionListener(
				new ButtonSelectionLinkedListener(new OnByValueButtonSelected(), editModeInputLineButton));

		add(subPanel1);
		add(subPanel2);

		//------------------------------------
		// For the grid panel
		//------------------------------------
		JPanel divideNumSpecPanel = new JPanel();
		JLabel gridLabel1 = new JLabel(
			resources.getString(ResourceKey.LABEL, StringID.UI.GRID_DIVIDE_NUM_ID));

		textFieldGrid = new JFormattedTextField(new DecimalFormat("#"));
		textFieldGrid.setColumns(2);
		textFieldGrid.setValue(new Integer(Config.DEFAULT_GRID_DIV_NUM));
		textFieldGrid.setHorizontalAlignment(JTextField.RIGHT);
		gridChangeButton.addActionListener(this);

		divideNumSpecPanel.add(gridLabel1);
		divideNumSpecPanel.add(textFieldGrid);
		divideNumSpecPanel.add(gridChangeButton);

		JPanel gridButtonsPanel = new JPanel();
		gridButtonsPanel.add(gridSmallButton);
		n++;
		gridButtonsPanel.add(gridLargeButton);
		n++;

		n = 0;
		gridPanel.add(dispGridCheckBox);
		n++;
		gridPanel.add(divideNumSpecPanel);
		n++;
		gridPanel.add(gridButtonsPanel);
		n++;
		gridPanel.setLayout(new GridLayout(n, 1, 10, 2));
		gridPanel.setBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()));
		add(gridPanel);

		//------------------------------------
		// Buttons panel
		//------------------------------------
		JPanel buttonsPanel = new JPanel();
		n = 0;
		buttonsPanel.add(dispMVLinesCheckBox);
		n++;
		buttonsPanel.add(dispAuxLinesCheckBox);
		n++;
		buttonsPanel.add(dispVertexCheckBox);
		n++;
		buttonsPanel.add(buttonCheckWindow);
		n++;
		buttonsPanel.add(buildButton);
		n++;
		buttonsPanel.add(doFullEstimationCheckBox);
		n++;
		buttonsPanel.setLayout(new GridLayout(n, 1, 10, 2));

		add(buttonsPanel);

		// Shortcut
		// How to enter the line
		lineInputDirectVButton.setMnemonic('1');
		lineInputOnVButton.setMnemonic('2');
		lineInputPBisectorButton.setMnemonic('3');
		lineInputAngleBisectorButton.setMnemonic('4');
		lineInputTriangleSplitButton.setMnemonic('5');
		lineInputVerticalLineButton.setMnemonic('6');
		lineInputSymmetricButton.setMnemonic('7');
		lineInputMirrorButton.setMnemonic('8');
		lineInputByValueButton.setMnemonic('9');

		editModeInputLineButton.setMnemonic('I');
		editModePickLineButton.setMnemonic('S');
		editModeDeleteLineButton.setMnemonic('D');
		editModeLineTypeButton.setMnemonic('T');
		editModeDeleteVertex.setMnemonic('L');
		lineTypeSubButton.setMnemonic('A');
		lineTypeMountainButton.setMnemonic('M');
		lineTypeValleyButton.setMnemonic('V');

		ValueDB.getInstance().addObserver(this);

		lineInputDirectVButton.doClick();
		
		
		addListenerToComponents();

	}

	private void addListenerToComponents(){


		alterLine_combo_from.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				settingDB.setTypeFrom(
						(TypeForChange)alterLine_combo_from.getSelectedItem());				
			}
		});

		alterLine_combo_to.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TypeForChange selected = (TypeForChange)(alterLine_combo_to.getSelectedItem());
				settingDB.setTypeTo(selected);
			}
		});


		lineTypeMountainButton.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Globals.inputLineType = OriLine.TYPE_RIDGE;
			}
		});
		lineTypeValleyButton.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Globals.inputLineType = OriLine.TYPE_VALLEY;
			}
		});
		lineTypeSubButton.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Globals.inputLineType = OriLine.TYPE_NONE;
			}
		});

		editModeInputLineButton.addActionListener(new InputCommandStatePopper());
		
		editModeLineTypeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingDB.setTypeFrom(
						(TypeForChange)alterLine_combo_from.getSelectedItem());				
				settingDB.setTypeTo(
						(TypeForChange)alterLine_combo_to.getSelectedItem());	

			}
		});

		textFieldLength.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {

				double length = ValueDB.getInstance().getLength();
				try{
					length = java.lang.Double.valueOf(
							textFieldLength.getText());
					ValueDB.getInstance().setLength(length);
				}catch(Exception ex){
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		textFieldAngle.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {

				double angle = valueDB.getAngle();
				try{
					angle = java.lang.Double.valueOf(
							textFieldAngle.getText());
					valueDB.setAngle(angle);
				}catch(Exception ex){
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});



		dispGridCheckBox.addActionListener(this);
		gridSmallButton.addActionListener(this);
		gridLargeButton.addActionListener(this);
		buildButton.addActionListener(this);
		resetButton.addActionListener(this);
		dispVertexCheckBox.addActionListener(this);
		dispVertexCheckBox.setSelected(true);
		Globals.dispVertex = true;
		dispMVLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Globals.dispMVLines = dispMVLinesCheckBox.isSelected();
				screen.repaint();
			}
		});
		dispAuxLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Globals.dispAuxLines = dispAuxLinesCheckBox.isSelected();
				screen.repaint();
			}
		});

		doFullEstimationCheckBox.setSelected(true);
		Globals.bDoFullEstimation = true;
		doFullEstimationCheckBox.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Globals.bDoFullEstimation = doFullEstimationCheckBox.isSelected();
				screen.repaint();
			}
		});

		buttonCheckWindow.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				ORIPA.doc.buildOrigami3(false);
				ORIPA.doc.checkPatternValidity();
				ORIPA.checkFrame.setVisible(true);
				ORIPA.checkFrame.repaint();
			}
		});

	}


	//	private GraphicMouseAction previousMouseAction = null;
	private MainScreenSettingDB screenDB = MainScreenSettingDB.getInstance();

	@Override
	public void actionPerformed(ActionEvent ae) {		

		ScreenUpdater screenUpdater = ScreenUpdater.getInstance();

		if (ae.getSource() == dispGridCheckBox) {
			screenDB.setGridVisible(dispGridCheckBox.isSelected());
			screenDB.notifyObservers();

			screenUpdater.updateScreen();			

		} else if (ae.getSource() == gridSmallButton) {
			if (Globals.gridDivNum < 65) {
				Globals.gridDivNum *= 2;
				textFieldGrid.setValue(new Integer(Globals.gridDivNum));

				screenUpdater.updateScreen();			
			}
		} else if (ae.getSource() == gridLargeButton) {
			if (Globals.gridDivNum > 3) {
				Globals.gridDivNum /= 2;
				textFieldGrid.setValue(new Integer(Globals.gridDivNum));

				screenUpdater.updateScreen();			
			}
		} else if (ae.getSource() == dispVertexCheckBox) {
			Globals.dispVertex = dispVertexCheckBox.isSelected();

			screenUpdater.updateScreen();			
		} else if (ae.getSource() == resetButton) {
		} else if (ae.getSource() == buildButton) {
			boolean buildOK = false;
			ORIPA.doc.sortedFaces.clear();
			if (ORIPA.doc.buildOrigami3(false)) {
				buildOK = true;
			} else {
				if (JOptionPane.showConfirmDialog(
						ORIPA.mainFrame, resources.getString(ResourceKey.LABEL, StringID.Warning.FOLD_FAILED_DUPLICATION_ID), 
						"Failed", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
						== JOptionPane.YES_OPTION) {
					if (ORIPA.doc.buildOrigami3(false)) {
						buildOK = true;
					} else {
						JOptionPane.showMessageDialog(
								ORIPA.mainFrame, resources.getString(ResourceKey.LABEL, StringID.Warning.FOLD_FAILED_WRONG_STRUCTURE_ID), 
								"Failed Level1",JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}

			if (buildOK) {
				Folder folder = new Folder(ORIPA.doc);
				int answerNum = folder.fold();
				System.out.println("RenderFrame");
				if (answerNum != 0) {
					RenderFrameSettingDB renderSetting = RenderFrameSettingDB.getInstance();
					renderSetting.setFrameVisible(true);
					renderSetting.notifyObservers();
				}

			} else {
				ORIPA.doc.foldWithoutLineType();
			}

			ModelFrameSettingDB modelSetting = ModelFrameSettingDB.getInstance();
			modelSetting.setFrameVisible(true);
			modelSetting.notifyObservers();

			//			screen.modeChanged();

		} else if (ae.getSource() == gridChangeButton) {
			int value;
			try {
				value = Integer.valueOf(textFieldGrid.getText());
				System.out.println("type");

				if (value < 128 && value > 2) {
					textFieldGrid.setValue(value);
					Globals.gridDivNum = value;
					screenUpdater.updateScreen();			
				}
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}



	}


	@Override
	public void propertyChange(PropertyChangeEvent e) {
		//        if (e.getSource() == textFieldLength) {
		//            textFieldLength.setValue(java.lang.Double.valueOf(textFieldLength.getText()));
		//        } else if (e.getSource() == textFieldAngle) {
		//            textFieldAngle.setValue(java.lang.Double.valueOf(textFieldAngle.getText()));
		//        }
	}


	/**
	 * observes DB for reflecting the changes to views.
	 * toString() of given DB has to return a unique value among DB classes.
	 * @param o Observable class which implements toString() 
	 *          to return its class name.
	 * @param arg A parameter to specify the action 
	 *        for the given Observable object.
	 */
	@Override
	public void update(Observable o, Object arg) {

		//System.out.println(o.toString());

		if(o.toString().equals(ValueDB.getInstance().toString())){
			// update text field of values
			ValueDB valueDB = (ValueDB) o;
			textFieldAngle.setValue(valueDB.getAngle());
			textFieldLength.setValue(valueDB.getLength());
		}
		else if(settingDB.hasGivenName(o.toString())){
			// update GUI
			UIPanelSettingDB setting = (UIPanelSettingDB) o;

			subPanel1.setVisible(setting.isValuePanelVisible());
			subPanel2.setVisible(setting.isValuePanelVisible());

			alterLineTypePanel.setVisible(setting.isAlterLineTypePanelVisible());


			lineTypeMountainButton.setEnabled(setting.isMountainButtonEnabled());
			lineTypeValleyButton.setEnabled(setting.isValleyButtonEnabled());
			lineTypeSubButton.setEnabled(setting.isAuxButtonEnabled());

			repaint();
		}
		else if(screenDB.hasGivenName(o.toString())){
			if(screenDB.isGridVisible() != dispGridCheckBox.isSelected()){
				dispGridCheckBox.setSelected(screenDB.isGridVisible());

			}

			repaint();
		}

	}


	private void virtualClick(AbstractButton button){
		button.setSelected(true);
		button.doClick();
	}

}
