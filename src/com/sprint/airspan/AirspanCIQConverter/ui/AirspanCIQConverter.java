package com.sprint.airspan.AirspanCIQConverter.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sprint.airspan.AirspanCIQConverter.xls.CIQConverter;
import com.sprint.airspan.AirspanCIQConverter.xls.CIQConverterListener;


@SuppressWarnings("serial")
public class AirspanCIQConverter extends JFrame implements WindowListener, ActionListener,CIQConverterListener {
	
	private Log log = LogFactory.getLog(AirspanCIQConverter.class);
	
	private String version = "1.1";
	
	private JFrame parent = this;
	private JRadioButton defaultRadioButton = new JRadioButton("Default Template");
	private JRadioButton customRadioButton = new JRadioButton("Custom Template");
	private JRadioButton bulkRadioButton = new JRadioButton("Generate Bulk Configuration File for Netspan   ");
	private JRadioButton netspanRadioButton = new JRadioButton("Send Configuration to Netspan Web Services   ");
	private PlaceholderTextField netspanServerAddress = new PlaceholderTextField(20);
	private PlaceholderTextField netspanServerUsername = new PlaceholderTextField(20);
	private PlaceholderPasswordTextField netspanServerPassword = new PlaceholderPasswordTextField(20);
	private JButton button1 = new JButton("Back");
	private JButton button2 = new JButton("Next");
	
	private JFileChooser customTemplateFileChooser = new JFileChooser();
	private DisabledPanel disabledPanel;
	private JFileChooser ciqFileChooser = new JFileChooser();
	
	private JLabel summaryTemplateTitleLabel = new JLabel("Airspan PnP Config Template XML: ");
	private JLabel summaryTemplateLabel = new JLabel();
	private JLabel summaryCIQTitleLabel = new JLabel("CIQ Spreadsheet: ");
	private JLabel summaryCIQLabel = new JLabel();
	private JLabel summaryOperationTitleLabel = new JLabel("Operation: ");
	private JLabel summaryOperationLabel = new JLabel();
	private JLabel summaryMoreInfoLabel = new JLabel();
	private JProgressBar progressBar = new JProgressBar (JProgressBar.HORIZONTAL);
	
	private String outputfile;
	
	private List<JPanel> panels = new ArrayList<JPanel>();
	private String buttonText[][] = {
			{null,"Next"}, //start
			{null,"Next"}, //template
			{"Back","Next"}, //ciq
			{"Back","Next"},//operation
			{"Back","Execute"} //summary
			};
	private int panelIndex=0;
	
	public AirspanCIQConverter() throws IOException{
		super(); 
		EventQueue queue = new EventQueue()
		{
		    protected void dispatchEvent(AWTEvent event)
		    {
		    	
		        if(event.getSource() instanceof JComponent){
		        	JComponent component = (JComponent)event.getSource();
		        	if (!component.isEnabled())
		        		return;
		        }
		        super.dispatchEvent(event);
		    }
		};
		
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(queue);
		setResizable(false);
		log.debug("Launching AirspanCIQConverter version " + version + "...");
		parent.setTitle("AirspanCIQConverter Version " + version);
		Image img = Toolkit.getDefaultToolkit().getImage(
				java.net.URLClassLoader.getSystemResource("sprint_logo_sm.jpg"));
		parent.setIconImage(img);
		setPreferredSize(new Dimension(500,400));
		addWindowListener(this);
		JPanel buttonPanel = new JPanel();
		Border buttonBorder = BorderFactory.createLineBorder(Color.gray);
		buttonPanel.setBorder(buttonBorder);
		buttonPanel.add(button1);
		button1.setVisible(false);
		button1.addActionListener(this);
		buttonPanel.add(button2);
		button2.addActionListener(this);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		panels.add(createStartPanel());
		panels.add(createTemplatePanel());
		panels.add(createCIQPanel());
		panels.add(createOperationPanel());
		panels.add(createSummaryPanel());
		getContentPane().add(panels.get(panelIndex), BorderLayout.CENTER);
	}
	
	private JPanel createStartPanel() throws IOException{
		Image logoImage = ImageIO.read(ClassLoader
				.getSystemResource("sprint_logo.png"));
		JPanel startPanel = new ScalableImagePanel(logoImage,false);
		return startPanel;
	}
	
	private JPanel createTemplatePanel(){
		JPanel templatePanel = new JPanel();
		templatePanel.setLayout(new BoxLayout(templatePanel, BoxLayout.Y_AXIS));
		Border templatePanelBorder = BorderFactory.createTitledBorder("Step 1: Choose Airspan PnP Config Template XML");
		templatePanel.setBorder(templatePanelBorder);
		defaultRadioButton.addActionListener(this);
		defaultRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		customRadioButton.addActionListener(this);
		customRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		ButtonGroup templateButtonGroup = new ButtonGroup();
		templateButtonGroup.add(defaultRadioButton);
		templateButtonGroup.add(customRadioButton);
		defaultRadioButton.setSelected(true);
		templatePanel.add(defaultRadioButton);
		templatePanel.add(customRadioButton);
		customTemplateFileChooser.setAlignmentX( Component.LEFT_ALIGNMENT );
		customTemplateFileChooser.setDragEnabled(true);
		customTemplateFileChooser.setToolTipText("Or drag-n-drop file here.");
		customTemplateFileChooser.setControlButtonsAreShown(false);
		customTemplateFileChooser.setFileFilter(new FileFilter(){

			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
					return true;
				return f.getName().endsWith(".xml");
			}

			@Override
			public String getDescription() {
				return "XML Files";
			}
			
		});
		customTemplateFileChooser.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(customTemplateFileChooser.getSelectedFile()!=null){
					button2.setEnabled(true);
					button2.doClick();
				}
				else
					button2.setEnabled(false);
			}
			
		});
		disabledPanel = new DisabledPanel(customTemplateFileChooser);
		disabledPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
		templatePanel.add(disabledPanel);
		return templatePanel;
	}
	
	private JPanel createCIQPanel(){
		JPanel ciqPanel = new JPanel();
		ciqPanel.setLayout(new BoxLayout(ciqPanel, BoxLayout.Y_AXIS));
		Border ciqPanelBorder = BorderFactory.createTitledBorder("Step 2: Choose CIQ Spreadsheet");
		ciqPanel.setBorder(ciqPanelBorder);
		ciqFileChooser.setAlignmentX( Component.LEFT_ALIGNMENT );
		ciqFileChooser.setDragEnabled(true);
		ciqFileChooser.setToolTipText("Or drag-n-drop file here.");
		ciqFileChooser.setControlButtonsAreShown(false);
		ciqFileChooser.setFileFilter(new FileFilter(){

			@Override
			public boolean accept(File f) {
				if(f.isDirectory())
					return true;
				return f.getName().endsWith(".xlsx");
			}

			@Override
			public String getDescription() {
				return "XLSX Files";
			}
			
		});
		ciqFileChooser.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(ciqFileChooser.getSelectedFile()!=null){
					button2.setEnabled(true);
					button2.doClick();
				}
				else
					button2.setEnabled(false);
			}
			
		});
		ciqPanel.add(ciqFileChooser);
		return ciqPanel;
	}
	
	private JPanel createOperationPanel(){
		JPanel operationPanel = new JPanel();
		operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.Y_AXIS));
		Border operationPanelBorder = BorderFactory.createTitledBorder("Step 3: Choose the Operation");
		operationPanel.setBorder(operationPanelBorder);
		bulkRadioButton.addActionListener(this);
		bulkRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		netspanRadioButton.addActionListener(this);
		netspanRadioButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		ButtonGroup operationButtonGroup = new ButtonGroup();
		operationButtonGroup.add(bulkRadioButton);
		operationButtonGroup.add(netspanRadioButton);
		bulkRadioButton.setSelected(true);
		operationPanel.add(bulkRadioButton);
		operationPanel.add(netspanRadioButton);
		JPanel netspanContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel inputsPanel = new JPanel();
		inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));
		netspanServerAddress.setPlaceholder("Netspan Server");
		netspanServerAddress.setEnabled(false);
		inputsPanel.add(netspanServerAddress);
		netspanServerUsername.setPlaceholder("Username");
		netspanServerUsername.setEnabled(false);
		inputsPanel.add(netspanServerUsername);
		netspanServerPassword.setPlaceholder("Password");
		netspanServerPassword.setEnabled(false);
		inputsPanel.add(netspanServerPassword);
		netspanContainer.add(inputsPanel);
		operationPanel.add(netspanContainer);
		
		return operationPanel;
	}
	
	private JPanel createSummaryPanel(){
		JPanel summaryPanel = new JPanel();
		summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
		Border summaryPanelBorder = BorderFactory.createTitledBorder("Summary");
		summaryPanel.setBorder(summaryPanelBorder);
		
		summaryTemplateTitleLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryTemplateTitleLabel);
		
		summaryTemplateLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryTemplateLabel);
		
		summaryPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
		summaryCIQTitleLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryCIQTitleLabel);
		
		summaryCIQLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryCIQLabel);
		
		summaryPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
		summaryOperationTitleLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryOperationTitleLabel);
		
		summaryOperationLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryOperationLabel);
		
		summaryMoreInfoLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
		summaryPanel.add(summaryMoreInfoLabel);
		
		summaryPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		summaryPanel.add(progressBar);
		
		return summaryPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source==button1 || source==button2){
			if(source==button1){
				panelIndex--;
				getContentPane().remove(panels.get(panelIndex+1));
			}
			else {
				panelIndex++;
				getContentPane().remove(panels.get(panelIndex-1));
			}
			
			if(panelIndex==2){
				if(customTemplateFileChooser.getSelectedFile()==null&&customRadioButton.isSelected()){
					button2.setEnabled(false);
					panelIndex=1;
					return;
				}
				if(ciqFileChooser.getSelectedFile()==null)
					button2.setEnabled(false);
			}
			else if(panelIndex==3){
				if(ciqFileChooser.getSelectedFile()==null){
					button2.setEnabled(false);
					panelIndex=2;
					return;
				}
			}
			else if(panelIndex==4){
				progressBar.setVisible(false);
				if(defaultRadioButton.isSelected())
					summaryTemplateLabel.setText("DefaultCIQTemplate.xml  ");
				else
					summaryTemplateLabel.setText(customTemplateFileChooser.getSelectedFile().getAbsolutePath()+"  ");
				File ciqFile = ciqFileChooser.getSelectedFile();
				summaryCIQLabel.setText(ciqFile.getAbsolutePath()+"    ");
				
				outputfile = ciqFile.getAbsolutePath().substring(0,ciqFile.getAbsolutePath().indexOf(".xlsx"))+".xml  ";
				if(bulkRadioButton.isSelected()){
					summaryOperationLabel.setText(bulkRadioButton.getText()+"  ");
					summaryMoreInfoLabel.setText("output to "+outputfile+"  ");
				}
				else
					summaryOperationLabel.setText(netspanRadioButton.getText()+"  ");
			}
			else if(panelIndex==5){
				panelIndex=4;
				try {
					InputStream templateFile;
					if(defaultRadioButton.isSelected())
						templateFile=ClassLoader.getSystemResourceAsStream("DefaultCIQTemplate.xml");
					else
						templateFile = new FileInputStream(customTemplateFileChooser.getSelectedFile());
					InputStream xlsxFile = new FileInputStream(ciqFileChooser.getSelectedFile().getAbsolutePath());
					final CIQConverter ciqConverter = new CIQConverter(templateFile, xlsxFile);
					ciqConverter.setListener(this);
					progressBar.setVisible(true);
					Thread ciqConverterThread = new Thread(){
						public void run(){
							ciqConverter.createForBulk(outputfile);
						}
					};
					
					ciqConverterThread.start();
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				return;
			}
			
			JPanel newPanel = panels.get(panelIndex);
			getContentPane().add(newPanel,BorderLayout.CENTER);
		
			String[] bt = buttonText[panelIndex];
			String bt1 = bt[0];
			String bt2 = bt[1];
			if(bt1==null)
				button1.setVisible(false);
			else {
				button1.setVisible(true);
				button1.setText(bt[0]);
			}
			
			if(bt2==null)
				button2.setVisible(false);
			else {
				button2.setVisible(true);
				button2.setText(bt[1]);
			}

			revalidate();
			repaint();
			pack();
			try {
				disabledPanel.setEnabled(!defaultRadioButton.isSelected());
			}
			catch(Exception ex){}
		}
		else if(source==defaultRadioButton){
			disabledPanel.setEnabled(false);
			button2.setEnabled(true);
		}
		else if(source==customRadioButton){
			disabledPanel.setEnabled(true);
			if(customTemplateFileChooser.getSelectedFile()==null)
				button2.setEnabled(false);
		}
		else if(source==bulkRadioButton){
			netspanServerAddress.setEnabled(false);
			netspanServerUsername.setEnabled(false);
			netspanServerPassword.setEnabled(false);
		}
		else if(source==netspanRadioButton){
			netspanServerAddress.setEnabled(true);
			netspanServerUsername.setEnabled(true);
			netspanServerPassword.setEnabled(true);
		}
		repaint();
	}
	
	/* (non-Javadoc)
	 * @see com.sprint.airspan.AirspanCIQConverter.xls.CIQConverterListener#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.sprint.airspan.AirspanCIQConverter.xls.CIQConverterListener#progress(int, int)
	 */
	@Override
	public void progress(int progress, int total) {
		progressBar.setMaximum(total);
		progressBar.setValue(progress);
	}

	/* (non-Javadoc)
	 * @see com.sprint.airspan.AirspanCIQConverter.xls.CIQConverterListener#finished()
	 */
	@Override
	public void finished() {
		try {
			if(Desktop.isDesktopSupported())
				Desktop.getDesktop().open(new File(outputfile));
			else
				System.out.println("Desktop not supported.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.sprint.airspan.AirspanCIQConverter.xls.CIQConverterListener#exception(java.lang.Exception)
	 */
	@Override
	public void exception(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) {
		try {
			UIManager.put("FileChooser.readOnly", Boolean.TRUE);
			try {
			    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			        if ("Nimbus".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
			    }
			} catch (Exception e) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			
			AirspanCIQConverter mainGui = new AirspanCIQConverter();
			mainGui.pack();
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			mainGui.setLocation(dim.width/2-mainGui.getSize().width/2, dim.height/2-mainGui.getSize().height/2);
			mainGui.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
