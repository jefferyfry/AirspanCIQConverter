package com.sprint.airspan.AirspanCIQConverter.xls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CIQConverter {
	
	private CIQConverterListener listener;
	
	private Document xmlTemplate;
	
	private XSSFWorkbook workbook;
	
	private DocumentBuilder dBuilder;
	
	public CIQConverter(InputStream xmlTemplateFile,InputStream xlsFile) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		xmlTemplate = dBuilder.parse(xmlTemplateFile);
		workbook = new XSSFWorkbook(xlsFile);
	}
	
	/**
	 * @param listener the listener to set
	 */
	public void setListener(CIQConverterListener listener) {
		this.listener = listener;
	}
	
	public List<Node> create(){
		ArrayList<Node> configNodes = new ArrayList<Node>();
		try {
			if(listener!=null)
				listener.start();
			XSSFSheet sheet = workbook.getSheet("PnpConfig");
			int rows = sheet.getPhysicalNumberOfRows();
			int cols = sheet.getRow(1).getPhysicalNumberOfCells();
			XSSFRow headerRow = sheet.getRow(1);
			DataFormatter fmt = new DataFormatter();
			Hashtable<String,Integer> propertyIndexCount = new Hashtable<String,Integer>();
			for(int row=2;row<rows;row++){
				propertyIndexCount.clear();
				try {
					Element oneConfig = (Element)xmlTemplate.getDocumentElement().cloneNode(true);
					XSSFRow ciqRow = sheet.getRow(row);
					if(ciqRow==null)
						continue;
					for(int col=0;col<cols;col++){
						String propertyName = headerRow.getCell(col).getStringCellValue();
						Integer propertyIndex = propertyIndexCount.get(propertyName);
						if(propertyIndex==null)
							propertyIndex = 0;
						NodeList properties = oneConfig.getElementsByTagName(propertyName);
						if(properties!=null && properties.getLength()>propertyIndex){
							XSSFCell cell = ciqRow.getCell(col);
							String cellValue=fmt.formatCellValue(cell); 
							if(cellValue.trim().length()>0)
								properties.item(propertyIndex).setTextContent(cellValue);
							propertyIndexCount.put(propertyName, (propertyIndex+1));
						}
					}
					
					clean(oneConfig);
					
					configNodes.add(oneConfig);
					
					if(listener!=null)
						listener.progress(row,rows);
				}
				catch(Exception e){
					if(listener!=null)
						listener.exception(e);
				}
			}
		}
		catch(Exception e){
			if(listener!=null)
				listener.exception(e);
		}
		return configNodes;
	}
	
	public void createForBulk(String outputFile){
		try {
			
			List<Node> configNodes = create();
			
			Document pnpConfigDocument = dBuilder.newDocument();
			Element rootElement = pnpConfigDocument.createElement("root");
			pnpConfigDocument.appendChild(rootElement);
			
			for(Node configNode:configNodes){
				pnpConfigDocument.adoptNode(configNode);
				rootElement.appendChild(configNode);
			}
			
			pnpConfigDocument.normalize();
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(pnpConfigDocument);
			StreamResult result = new StreamResult(new File(outputFile));
	 
			transformer.transform(source, result);
		}
		catch(Exception e){
			if(listener!=null)
				listener.exception(e);
		}
		finally {
			if(listener!=null)
				listener.finished();
		}
	}
	
	public void createToNetspanWebService(String url,String username,String password){
		try {
			List<Node> configNodes = create();
		}
		catch(Exception e){
			if(listener!=null)
				listener.exception(e);
		}
	}
	
	private static void clean(Node node){
		NodeList children = node.getChildNodes();
		if(children.getLength() > 0)
			for(int i=0;i<children.getLength();i++)
				clean(children.item(i));
		else if((node.getNodeType()!=Node.TEXT_NODE)||
				((node.getNodeType()==Node.TEXT_NODE)&&(node.getTextContent().trim().length()==0))){
			Node parentNode = node.getParentNode();
			parentNode.removeChild(node);
			clean(parentNode);
		}
	}
}
