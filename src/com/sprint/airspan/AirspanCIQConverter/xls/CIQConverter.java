package com.sprint.airspan.AirspanCIQConverter.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class CIQConverter extends Thread {
	
	private CIQConverterListener listener;
	
	private Document xmlTemplate;
	
	private Document newDocument;
	
	private XSSFWorkbook workbook;
	
	private String outputFile;
	
	public CIQConverter(String xmlTemplateFile,String xlsFile,String outputFile) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		xmlTemplate = dBuilder.parse(xmlTemplateFile);
		workbook = new XSSFWorkbook(new FileInputStream(xlsFile));
		newDocument = dBuilder.newDocument();
		this.outputFile=outputFile;
	}
	
	/**
	 * @param listener the listener to set
	 */
	public void setListener(CIQConverterListener listener) {
		this.listener = listener;
	}
	
	public void process(){
		try {
			if(listener!=null)
				listener.start();
			XSSFSheet sheet = workbook.getSheetAt(0);
			int rows = sheet.getPhysicalNumberOfRows();
			int cols = sheet.getRow(0).getPhysicalNumberOfCells();
			XSSFRow headerRow = sheet.getRow(0);
			Element rootElement = newDocument.createElement("root");
			newDocument.appendChild(rootElement);
			for(int i=1;i<rows;i++){
				Element oneConfig = (Element)xmlTemplate.getDocumentElement().cloneNode(true);
				XSSFRow ciqRow = sheet.getRow(i);
				for(int j=0;j<cols;j++){
					String propertyName = headerRow.getCell(j).getStringCellValue();
					oneConfig.getElementsByTagName(propertyName).item(0).setTextContent(ciqRow.getCell(j).getStringCellValue());
				}
				newDocument.adoptNode(oneConfig);
				rootElement.appendChild(oneConfig);
				if(listener!=null)
					listener.progress(i,rows);
			}
			
			newDocument.normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(newDocument);
			StreamResult result = new StreamResult(new File(outputFile));
	 
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
			
			if(listener!=null)
				listener.finished();
		}
		catch(Exception e){
			e.printStackTrace();
			if(listener!=null)
				listener.exception(e);
		}
	}

	public void run(){
		process();
	}
	
	public static void main(String[] args){
		try {
			CIQConverter converter = new CIQConverter("MyCIQTemplate.xml","AirspanCIQ.xlsx","output.xml");
			
			converter.setListener(new CIQConverterListener() {
				
				@Override
				public void start() {
					System.out.print("Started");
				}
				
				@Override
				public void progress(int progress, int total) {
					System.out.format("%d of %d\n",progress,total);
				}
				
				@Override
				public void finished() {
					System.out.print("Finished");
				}
				
				@Override
				public void exception(Exception e) {
					e.printStackTrace();
				}
			});
			converter.process();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
