package net.rs.lamsi.massimager.Settings;

import java.awt.Component;
import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.rs.lamsi.massimager.MyFileChooser.FileTypeFilter;
import net.rs.lamsi.massimager.Settings.image.SettingsExportGraphics;
import net.rs.lamsi.massimager.Settings.image.SettingsImage2DDataExport;
import net.rs.lamsi.massimager.Settings.image.SettingsImageContinousSplit;
import net.rs.lamsi.massimager.Settings.image.operations.SettingsImage2DOperations;
import net.rs.lamsi.massimager.Settings.image.operations.quantifier.SettingsImage2DQuantifier;
import net.rs.lamsi.massimager.Settings.image.operations.quantifier.SettingsImage2DQuantifierLinear;
import net.rs.lamsi.massimager.Settings.visualization.SettingsPlotSpectraLabelGenerator;
import net.rs.lamsi.massimager.Settings.visualization.plots.SettingsThemes;
import net.rs.lamsi.utils.FileAndPathUtil;
import net.rs.lamsi.utils.mywriterreader.BinaryWriterReader;

public class SettingsHolder extends Settings {
	// do not change the version!
	private static final long serialVersionUID = 1L;
	//
	//
	private static final SettingsHolder SETTINGS = new SettingsHolder();
	// Settings writer  
	private BinaryWriterReader settingsWriter = new BinaryWriterReader();
	//
	private Vector<Settings> settList;
	// save of super classes
	private Class[] classList;

	private SettingsGeneralValueFormatting setGeneralValueFormatting;
	private SettingsConverterRAW setConvertRAW;
	private SettingsDataSaver setDataSaver;
	// Nur die ganzen aktuellen Werte f�r discon images
	private SettingsGeneralImage setOESImage;

	private SettingsPaintScale setPaintScale;

	private SettingsChargeCalculator setChargeCalc; 

	// Visualization
	private SettingsPlotSpectraLabelGenerator setVisPlotSpectraLabelGenerator;
	private SettingsThemes setPlotStyle;

	// Export Settings
	private SettingsExportGraphics setGraphicsExport;

	// 
	private SettingsImage2DDataExport setImage2DDataExport;

	// settings for operations on images
	private SettingsImage2DQuantifier setQuantifier;

	private SettingsImage2DOperations setOperations;

	// for splitting data in image2dContinous
	private SettingsImageContinousSplit setSplitImgCon;


	public SettingsHolder() {
		// alle settings in einer gro�en
		super("/Settings/", "setall");
		// list
		settList = new Vector<Settings>();
		// einzelne settings 
		setDataSaver = new SettingsDataSaver("/Settings/Export/", "setds");
		settList.add(setDataSaver);
		setOESImage = new SettingsGeneralImage();
		settList.add(setOESImage);
		setPaintScale = new SettingsPaintScale();
		settList.add(setPaintScale);
		setChargeCalc = new SettingsChargeCalculator();
		settList.add(setChargeCalc);
		setGeneralValueFormatting = new SettingsGeneralValueFormatting();
		settList.add(setGeneralValueFormatting);

		// visualization only for Toolset
		setVisPlotSpectraLabelGenerator = new SettingsPlotSpectraLabelGenerator();
		settList.add(setVisPlotSpectraLabelGenerator);
		setPlotStyle = new SettingsThemes();
		settList.add(setPlotStyle);

		// Export settings
		setGraphicsExport = new SettingsExportGraphics();
		settList.add(setGraphicsExport);

		// export data of image2d
		setImage2DDataExport = new SettingsImage2DDataExport();
		settList.add(setImage2DDataExport); 

		// operations
		setQuantifier = new SettingsImage2DQuantifierLinear();
		settList.add(setQuantifier); 

		setOperations = new SettingsImage2DOperations();
		settList.add(setOperations); 

		// image2dContinous
		setSplitImgCon = new SettingsImageContinousSplit();
		settList.add(setSplitImgCon); 

		// save all in class list of super classes
		Class[] cl = {SettingsDataSaver.class, SettingsGeneralImage.class, SettingsPaintScale.class, SettingsChargeCalculator.class,
				SettingsGeneralValueFormatting.class, SettingsPlotSpectraLabelGenerator.class, SettingsThemes.class, SettingsExportGraphics.class,
				SettingsImage2DDataExport.class, SettingsImage2DOperations.class, SettingsImage2DQuantifier.class, SettingsImageContinousSplit.class};
		classList = cl;
	}

	// Alle auf start einstellungen
	@Override
	public void resetAll() {
		// TODO immer neue rein
		for(Settings s : settList)
			s.resetAll(); 
	}  


	public File saveSettingsToFile(Component parentFrame, Class settingsClass)  throws Exception { 
		return saveSettingsToFile(parentFrame, getSetByClass(settingsClass));
	}
	public File saveSettingsToFile(Component parentFrame, Settings cs)  throws Exception { 
		// Open new FC
		// create Path 
		File path = new File(FileAndPathUtil.getPathOfJar(), cs.getPathSettingsFile());
		FileAndPathUtil.createDirectory(path);
		JFileChooser fc = new JFileChooser(path); 
		FileTypeFilter ffilter = new FileTypeFilter(cs.getFileEnding(), "Save settings to");
		fc.addChoosableFileFilter(ffilter);  
		fc.setFileFilter(ffilter);
		// getting the file
		if (fc.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();  
			// extention anbringen
			file = ffilter.addExtensionToFileName(file);
			//
			cs.saveToFile(settingsWriter, file);
			return file;
		}
		else {
			return null;
		}
	} 

	public Settings loadSettingsFromFile(Component parentFrame, Class settingsClass) throws Exception {
		return loadSettingsFromFile(parentFrame, getSetByClass(settingsClass));
	}
	public Settings loadSettingsFromFile(Component parentFrame, Settings cs)  throws Exception {
		// TODO Auto-generated method stub 
		// Open new FC
		File path = new File(FileAndPathUtil.getPathOfJar(), cs.getPathSettingsFile());
		FileAndPathUtil.createDirectory(path);
		JFileChooser fc = new JFileChooser(path); 
		FileFilter ffilter = new FileTypeFilter(cs.getFileEnding(), "Load settings from");
		fc.addChoosableFileFilter(ffilter);  
		fc.setFileFilter(ffilter);
		// getting the file
		if (fc.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();   
			return loadSettingsFromFile(file, cs);
		}
		return null;
	} 

	public Settings loadSettingsFromFile(File file, Settings cs) {
		// Welches wurde geladen? 
		if(cs instanceof SettingsHolder) {
			// alle laden und setzen
			SettingsHolder sett = (SettingsHolder)(cs.loadFromFile(settingsWriter, file));
			// Alle settings aus geladenen holder kopieren
			settList = sett.getSettList();
			classList = sett.getClassList();
			// 
			return this;
		} 
		else { 
			Settings loaded = (cs.loadFromFile(settingsWriter, file));
			setSetByClass(loaded);
			return loaded;
		}
	}

	// more advanced getters
	/**
	 * 
	 * @param c super class of settings-class
	 * @return
	 */
	public Settings getSetByClass(Class c) { 
		for(Settings s : settList)
			if(c.isAssignableFrom(s.getClass()))
				return s;
		System.err.println("FAILED TO get settings "+c.getName());
		return null;
	}

	public void setSetByClass(Settings newOne) { 
		for(int i=0; i<classList.length; i++){
			Class c = classList[i];
			if(c.isAssignableFrom(newOne.getClass())) {
				settList.set(i, newOne);
				System.out.println("Set newOne settings: "+newOne.getClass().getName()+"   to: "+c.getName());
				return;
			}
		}
		System.err.println("FAILED TO SET newOne "+newOne.getClass().getName());
	}



	public Vector<Settings> getSettList() {
		return settList;
	}
	public Class[] getClassList() {
		return classList;
	}
	// GETTERS
	public SettingsConverterRAW getSetConvertRAW() { 
		// old
		return setConvertRAW;
	}  
	public void setSetConvertRAW(SettingsConverterRAW setConvertRAW) {
		// old
		this.setConvertRAW = setConvertRAW;
	} 

	public SettingsDataSaver getSetDataSaver() {
		return (SettingsDataSaver) getSetByClass(SettingsDataSaver.class);
	} 
	public SettingsGeneralImage getSetGeneralImage() {
		return (SettingsGeneralImage) getSetByClass(SettingsGeneralImage.class);
	} 

	public SettingsPaintScale getSetPaintScale() {
		return (SettingsPaintScale) getSetByClass(SettingsPaintScale.class);
	}


	public SettingsChargeCalculator getSetChargeCalc() {
		return (SettingsChargeCalculator) getSetByClass(SettingsChargeCalculator.class);
	}

	public SettingsGeneralValueFormatting getSetGeneralValueFormatting() {
		return (SettingsGeneralValueFormatting) getSetByClass(SettingsGeneralValueFormatting.class);
	}

	public SettingsPlotSpectraLabelGenerator getSetVisPlotSpectraLabelGenerator() {
		return (SettingsPlotSpectraLabelGenerator) getSetByClass(SettingsPlotSpectraLabelGenerator.class);
	}

	public SettingsExportGraphics getSetGraphicsExport() {
		return (SettingsExportGraphics) getSetByClass(SettingsExportGraphics.class);
	}


	public SettingsThemes getSetPlotStyle() {
		return (SettingsThemes) getSetByClass(SettingsThemes.class);
	}

	// SETTER
	public void setSetPlotStyle(SettingsThemes setPlotStyle) {
		setSetByClass(setPlotStyle);
	}
	public void setSetGraphicsExport(SettingsExportGraphics setGraphicsExport) {
		setSetByClass(setGraphicsExport);
	}
	public void setSetPaintScale(SettingsPaintScale setPaintScale) {
		setSetByClass(setPaintScale);
	}
	public void setSetOESImage(SettingsGeneralImage setOESImage) {
		setSetByClass(setOESImage);
	}
	public void setSetMSIDiscon(SettingsMSImage setMSIDiscon) {
		setSetByClass(setMSIDiscon);
	} 
	public void setSetMSICon(SettingsMSImage setMSICon) {
		setSetByClass(setMSICon);
	} 
	public void setSetDataSaver(SettingsDataSaver setDataSaver) {
		setSetByClass(setDataSaver);
	} 

	// get settings instance
	public static SettingsHolder getSettings() {
		return SETTINGS;
	}

	public BinaryWriterReader getSettingsWriter() {
		return settingsWriter;
	}

	public SettingsImage2DDataExport getSetImage2DDataExport() {
		// TODO Auto-generated method stub
		return setImage2DDataExport;
	}
}
