package net.rs.lamsi.massimager.Settings;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.rs.lamsi.general.datamodel.image.Image2D;
import net.rs.lamsi.utils.mywriterreader.BinaryWriterReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Settings implements Serializable {  
	// do not change the version!
	private static final long serialVersionUID = 1L;
	//
	//protected final String parameterElement = "parameter";
	//protected final String nameAttribute = "name";
	//
	protected final String description;
	protected final String path;
	protected final String fileEnding;


	public Settings(String description, String path, String fileEnding) {
		super();
		this.path = path;
		this.fileEnding = fileEnding; 
		this.description = description;
	}

	public abstract void resetAll();

	//##################################################################
	// xml write and read
	/**
	 * saves settings to a file
	 * @param file
	 * @throws IOException
	 */
	public void saveToXML(File file) throws IOException {
		saveToXML(new FileOutputStream(file));
	}
	
	/**
	 * saves settings to a file
	 * @param file
	 * @throws IOException
	 */
	public void saveToXML(OutputStream fos) throws IOException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document configuration = dBuilder.newDocument();
			Element configRoot = configuration.createElement("settings");
			configuration.appendChild(configRoot);

			// creates a new element for this settings class and appends Values
			appendSettingsToXML(configRoot, configuration);

			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer transformer = transfac.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");

			StreamResult result = new StreamResult(fos);
			DOMSource source = new DOMSource(configuration);
			transformer.transform(source, result); 
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * saves settings and calls abstract appendSettingsValuesToXML
	 * creates a new parent element for this settings class
	 * @param elParent
	 * @param doc
	 */
	public void appendSettingsToXML(Element elParent, Document doc) { 
		Element elSett = doc.createElement(description);
		elParent.appendChild(elSett);
		appendSettingsValuesToXML(elSett, doc);
	}

	/**
	 * append values of settings to xml
	 * gets called by appendSettingsToXML 
	 * @param elParent
	 * @param doc
	 */
	public abstract void appendSettingsValuesToXML(Element elParent, Document doc);

	//'''''''''''''''''''''''''''''''''
	// save specific values
	public static void toXML(Element elParent, Document doc, String name, Object o) {
		if(o!=null) {
		    //Element paramElement = doc.createElement(parameterElement);
		    //paramElement.setAttribute(nameAttribute, name);
			Element paramElement = doc.createElement(name);
		    elParent.appendChild(paramElement); 
		    
		    if(Color.class.isInstance(o)) {
		    	Color c = (Color) o;
		    	paramElement.setTextContent(String.valueOf(c.getRGB()));
		    	paramElement.setAttribute("alpha", String.valueOf(c.getAlpha()));
		    }
		    else if(File.class.isInstance(o)) {
		    	paramElement.setTextContent(((File)o).getAbsolutePath());
		    }
		    else paramElement.setTextContent(String.valueOf(o));
		}
	}
	
	
	//'''''''''''''''''''''''''''''''''
	// load
	/**
	 * load xml settings from a file
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public void loadFromXML(File file) throws IOException { 
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document configuration = dBuilder.parse(file);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			// load settings
			loadSettingsFromXML(configuration, xpath, "//settings");
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * load xml settings from a file
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public void loadFromXML(InputStream is) throws IOException { 
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document configuration = dBuilder.parse(is);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			// load settings
			loadSettingsFromXML(configuration, xpath, "//settings");
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * loads settings from xml. calls loadValuesFromXML
	 * @param doc
	 * @param xpath
	 * @param path the String path to the parent (need to add /child)
	 * @throws XPathExpressionException
	 */
	public void loadSettingsFromXML(Document doc, XPath xpath, String parentpath) throws XPathExpressionException {
		// root= settings 
		XPathExpression expr = xpath.compile(parentpath+"/"+description);
		NodeList nodes = (NodeList) expr.evaluate(doc,
				XPathConstants.NODESET);
		if (nodes.getLength() == 1) {
			Element el = (Element) nodes.item(0);
			loadValuesFromXML(el, doc);
		}
	}
	/**
	 * load values
	 * @param el
	 * @param doc
	 */
	public abstract void loadValuesFromXML(Element el, Document doc);

	

    /**
     * 
     * @param el
     * @return null if no value was found
     */
    public static Double doubleFromXML(final Element el) {
        final String numString = el.getTextContent();
        if (numString.length() > 0) {
            return Double.parseDouble(numString);
        }
        return null;
    }
    /**
     * 
     * @param el
     * @return null if no value was found
     */
    public static File fileFromXML(final Element el) {
        final String numString = el.getTextContent();
        if (numString.length() > 0) {
            return new File(numString);
        }
        return null;
    }
    /**
     * 
     * @param el
     * @return null if no value was found
     */
    public static Integer intFromXML(final Element el) {
        final String numString = el.getTextContent();
        if (numString.length() > 0) {
            return Integer.parseInt(numString);
        }
        return null;
    }
    /**
     * 
     * @param el
     * @return null if no value was found
     */
    public static Float floatFromXML(final Element el) {
        final String numString = el.getTextContent();
        if (numString.length() > 0) {
            return Float.parseFloat(numString);
        }
        return null;
    }

    /**
     * 
     * @param el
     * @return null if no value was found
     */
    public static Color colorFromXML(final Element el) {
        final String numString = el.getTextContent();
        if (numString.length() > 0) {
            int rgb =  Integer.parseInt(numString);
            int alpha = Integer.parseInt(el.getAttribute("alpha"));
            Color c = new Color(rgb);
            return new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
        }
        return null;
    }
    /**
     * 
     * @param el
     * @return null if no value was found
     */
    public static Boolean booleanFromXML(final Element el) {
        final String numString = el.getTextContent();
        if (numString.length() > 0) {
            return Boolean.parseBoolean(numString);
        }
        return null;
    }
	
	//'''''''''''''''''''''''''''''
	// load specific values

	//##################################################################
	// binary write and read
	/**
	 * binary read
	 * @param writer
	 * @param file
	 */
	protected void saveToFile(BinaryWriterReader writer, File file) { 
		writer.save2file(this, file);
		writer.closeOut();
	} 

	/**
	 * binary write
	 * @param writer
	 * @param file
	 * @return
	 */
	protected Settings loadFromFile(BinaryWriterReader writer, File file) { 
		Settings set = (Settings) writer.readFromFile(file);
		writer.closeIn();
		return set;
	}


	public void applyToImage(Image2D img) throws Exception {
		img.setSettings(this.copy());
	} 

	public String getPathSettingsFile() {
		return path;
	} 

	public String getFileEnding() {
		return fileEnding;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * returns a copy by binary copy
	 * @return
	 * @throws Exception 
	 */
	public Settings copy() throws Exception { 
		return BinaryWriterReader.deepCopy(this);
	}

}
