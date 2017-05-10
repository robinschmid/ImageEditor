package net.rs.lamsi.multiimager.utils.imageimportexport;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.rs.lamsi.general.datamodel.image.Image2D;
import net.rs.lamsi.general.datamodel.image.ImageGroupMD;
import net.rs.lamsi.general.datamodel.image.ImageOverlay;
import net.rs.lamsi.general.datamodel.image.data.multidimensional.DatasetContinuousMD;
import net.rs.lamsi.general.datamodel.image.data.multidimensional.DatasetMD;
import net.rs.lamsi.general.datamodel.image.data.multidimensional.ScanLineMD;
import net.rs.lamsi.general.datamodel.image.interf.Collectable2D;
import net.rs.lamsi.general.datamodel.image.interf.ImageDataset;
import net.rs.lamsi.general.datamodel.image.interf.MDDataset;
import net.rs.lamsi.general.settings.Settings;
import net.rs.lamsi.general.settings.SettingsHolder;
import net.rs.lamsi.general.settings.image.SettingsImage2D;
import net.rs.lamsi.general.settings.image.SettingsImageOverlay;
import net.rs.lamsi.general.settings.image.sub.SettingsGeneralImage;
import net.rs.lamsi.general.settings.image.sub.SettingsGeneralImage.XUNIT;
import net.rs.lamsi.general.settings.image.sub.SettingsImageContinousSplit;
import net.rs.lamsi.general.settings.image.visualisation.SettingsPaintScale;
import net.rs.lamsi.general.settings.importexport.SettingsImageDataImportTxt;
import net.rs.lamsi.general.settings.importexport.SettingsImageDataImportTxt.IMPORT;
import net.rs.lamsi.general.settings.importexport.SettingsImageDataImportTxt.ModeData;
import net.rs.lamsi.multiimager.Frames.ImageEditorWindow;
import net.rs.lamsi.multiimager.Frames.ImageEditorWindow.LOG;
import net.rs.lamsi.utils.FileAndPathUtil;
import net.rs.lamsi.utils.mywriterreader.TxtWriter;
import net.rs.lamsi.utils.mywriterreader.ZipUtil;

public class Image2DImportExportUtil {
	private static final TxtWriter txtWriter = new TxtWriter();
	private static final String SEPARATION = ";";

	
	//######################################################################################
		// Standard format as zipped text files and settings

		/**
		 * saves an imageGroup to one file
		 * @param group
		 * @param file
		 * @throws IOException
		 */
		public static void writeToStandardZip(ImageGroupMD group, File file) throws IOException {
			// parameters
			ZipParameters parameters = new ZipParameters();
	        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

	        //DEFLATE_LEVEL_FASTEST     - Lowest compression level but higher speed of compression
	        //DEFLATE_LEVEL_FAST        - Low compression level but higher speed of compression
	        //DEFLATE_LEVEL_NORMAL  - Optimal balance between compression level/speed
	        //DEFLATE_LEVEL_MAXIMUM     - High compression level with a compromise of speed
	        //DEFLATE_LEVEL_ULTRA       - Highest compression level but low speed
	        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
	        
	        Image2D[] images = group.getImagesOnly();

			// zip file 
			parameters.setSourceExternalStream(true); 
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
			
			// export group settings
			try {
				Settings sett = group.getSettings();
				parameters.setFileNameInZip(FileAndPathUtil.addFormat("group", sett.getFileEnding()));
				out.putNextEntry(null, parameters);
				sett.saveToXML(out);
				out.closeEntry();
			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	        // export xmatrix
			// intensity matrix
	        if(MDDataset.class.isInstance(images[0].getData())) {
	        	if(((MDDataset)images[0].getData()).hasXData()) {
		            String xmatrix = images[0].toXCSV(true, SEPARATION, false);
		    		try {
						parameters.setFileNameInZip("xmatrix.csv");
						out.putNextEntry(null, parameters);
		    			byte[] data = xmatrix.getBytes();
		    			out.write(data, 0, data.length);
		    			out.closeEntry();
		    		} catch (ZipException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }
	        // for all images:
	        NumberFormat format = new DecimalFormat("0000");
	        int c = 0;
	        for(Image2D img : images) {	
	        	c++;
	        	String num = format.format(c)+"_";
	        	try {
		        	// export ymatrix
					String matrix = img.toICSV(true, SEPARATION, false);
					parameters.setFileNameInZip(num+img.getTitle()+".csv");
					out.putNextEntry(null, parameters);
					byte[] data = matrix.getBytes();
					out.write(data, 0, data.length);
					out.closeEntry();
					
		        	// export settings
					Settings sett = img.getSettings();
					parameters.setFileNameInZip(FileAndPathUtil.addFormat(num+img.getTitle(), sett.getFileEnding()));
					out.putNextEntry(null, parameters);
					sett.saveToXML(out);
					out.closeEntry();
				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        // for all overlays
	        Collectable2D[] c2d = group.getOtherThanImagesOnly();
	        for(Collectable2D img : c2d) {	
	        	try {
		        	// export settings
					Settings sett = img.getSettings();
					parameters.setFileNameInZip(FileAndPathUtil.addFormat(img.getTitle(), sett.getFileEnding()));
					out.putNextEntry(null, parameters);
					sett.saveToXML(out);
					out.closeEntry();
				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        
	        try {
				File bg = group.getBGImagePath();
				if(bg!=null) {
				BufferedImage bgi = (BufferedImage)group.getBGImage();
				if(bgi!=null) {
					parameters.setFileNameInZip(bg.getName());
					out.putNextEntry(null, parameters);
					ImageIO.write(bgi, FileAndPathUtil.getFormat(bg).toUpperCase(), out);
					out.closeEntry();
				}
				}
			} catch (ZipException e) {
				e.printStackTrace();
			} finally {
		        try {
		        	out.finish();
		        } catch (ZipException e) {
					e.printStackTrace();
				}
			}
	        
	        // end 
			out.close();

	        
//	        // copy existing files directly
//			try {
//				ZipFile zipFile = new ZipFile(file);
//		        // copy background image over
//				File bg = group.getBGImagePath();
//				
//				if(bg!=null && bg.exists()) {
//					parameters.setFileNameInZip(bg.getName());
//					zipFile.addFile(bg, parameters);
//				}
//			} catch (ZipException e1) {
//				e1.printStackTrace();
//			}
		}

		/**
		 * read x, read y dimensions and settings
		 * @param f
		 * @return
		 * @throws IOException
		 */
		public static ImageGroupMD readFromStandardZip(File f) throws IOException {
			// read files from zip
			Map<String, InputStream> files = ZipUtil.readZip(f);
			// try to find xmatrix
			ArrayList<ScanLineMD> lines = new ArrayList<ScanLineMD>();
			ArrayList<SettingsImage2D> settings = new ArrayList<SettingsImage2D>();
			
			// only setting sno data for iamge overlays
			ArrayList<SettingsImageOverlay> overlays = new ArrayList<SettingsImageOverlay>();
			
			// background image
			Image bgimg = null;
			File bgFile = null;
			//
			InputStream is = files.get("xmatrix.csv");
			if(is!=null) {
	            ImageEditorWindow.log("reading x", LOG.MESSAGE);
				ArrayList<Float>[] x = null;

				// line by line add datapoints to current Scanlines
				BufferedReader br = null;
				try {
					br = txtWriter.getBufferedReader(is);
					String sline;
					int k = 0;
					while ((sline = br.readLine()) != null) {
						// try to seperate by seperation
						String[] sep = sline.split(SEPARATION);
						// data
						if(sep.length>0) {
							if(x==null) {
								//create new array
								x = new ArrayList[sep.length];
								for(int i=0; i<sep.length; i++) {
									x[i] = new ArrayList<Float>();
									x[i].add(Float.valueOf(sep[i]));
								}
							}
							else {
								for(int i=0; i<x.length; i++) {
									x[i].add(Float.valueOf(sep[i]));
								}
							}
						}
					}
					// create new lines and set x
					for(ArrayList<Float> xi : x) {
						lines.add(new ScanLineMD(xi));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					try {
						if(br!=null) br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
	            ImageEditorWindow.log("reading x (FINISHED)", LOG.MESSAGE);
			}
			// settings
			SettingsImage2D sett = new SettingsImage2D();

			// y data
	        for (String fileName : files.keySet()) {
	        	if(!fileName.equals("xmatrix.csv") && fileName.endsWith(".csv")) {
		            ImageEditorWindow.log("reading file: "+fileName, LOG.MESSAGE);
	        		System.out.println("read file: " + fileName);
	        		
					ArrayList<Double>[] y = null;
	        		
	        		BufferedReader br = null;
					try {
						br = txtWriter.getBufferedReader(files.get(fileName));
						String sline;
						int k = 0;
						while ((sline = br.readLine()) != null) {
							// try to seperate by seperation
							String[] sep = sline.split(SEPARATION);
							// data
							if(sep.length>1) {
								if(y==null) {
									//create new array
									y = new ArrayList[sep.length];
									for(int i=0; i<sep.length; i++) {
										y[i] = new ArrayList<Double>();
										y[i].add(Double.valueOf(sep[i]));
									}
								}
								else {
									// add data
									for(int i=0; i<y.length; i++) {
										y[i].add(Double.valueOf(sep[i]));
									}
								}
							}
						}
						// create new lines 
						// lines size can be 0, 1 or the length of y 
						// depending on the x matrix
						if(lines.size()<y.length)
							for(int i=lines.size(); i<y.length; i++)
								lines.add(new ScanLineMD());
						
						// set dimensions
						for(int i=0; i<y.length && i<lines.size(); i++)
								lines.get(i).addDimension(y[i]);
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					finally {
						try {
							if(br!=null) br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// load the correct settings file
					InputStream isSett = files.get(FileAndPathUtil.getRealFileName(fileName, sett.getFileEnding()));
					if(isSett!=null) {
			            ImageEditorWindow.log("reading settings file of: "+fileName, LOG.MESSAGE);
		        		System.out.println("read settings file of: " + fileName);
		        		
						settings.add(new SettingsImage2D());
						settings.get(settings.size()-1).loadFromXML(isSett);
		        	}
	        	}
	        	else if(SettingsHolder.getSettings().getSetGeneralPreferences().getFilePicture().accept(new File(fileName))) {
	        		// add as microscopic image
	        		try {
		        		bgimg = ImageIO.read(files.get(fileName));
		        		bgFile = new File(fileName);
					} catch (Exception e) {
						e.printStackTrace();
						ImageEditorWindow.log("ERROR: Cannot load microscopic image: "+fileName+"\n"+e.getMessage(), LOG.ERROR);
					}
	        	}
	        }
	        

			// add new Image to image group
			if(lines.size()>0) {
				DatasetMD data = new DatasetMD(lines);
				ImageGroupMD group = data.createImageGroup();
				// add bg image
				if(bgimg!=null) {
					group.setBackgroundImage(bgimg, bgFile);
				}
				
				// set settings to images
				for(int i=0; i<group.getImages().size(); i++)
					((Image2D)group.getImages().get(i)).setSettings(settings.get(i));
		        

		        // image overlays
				// settings
				SettingsImageOverlay tmpov = new SettingsImageOverlay();

				// y data
		        for (String fileName : files.keySet()) {
		        	if(fileName.endsWith(tmpov.getFileEnding())) {
			            ImageEditorWindow.log("reading overlay file: "+fileName, LOG.MESSAGE);
		        		System.out.println("read overlay file: " + fileName);
		        		
		        		// 
		        		InputStream isSett = files.get(fileName);
		        		SettingsImageOverlay settov = new SettingsImageOverlay();
						settov.loadFromXML(isSett);
						
						ImageOverlay newov;
						try {
							newov = new ImageOverlay(group, settov);
							group.add(newov);
						} catch (Exception e) {
							e.printStackTrace();
						}
		        	}
		        }
		        // load group settings
				InputStream isSett = files.get(FileAndPathUtil.getRealFileName("group", group.getSettings().getFileEnding()));
				if(isSett!=null) {
		            ImageEditorWindow.log("reading settings file of: "+group, LOG.MESSAGE);
	        		System.out.println("read settings file of: " + group);
	        		
					group.getSettings().loadFromXML(isSett);
	        	}
		        // return group
		        return group;
			}
			return null;
		}
		
		
	//######################################################################################
	// TEXT BASED
	/**
	 * switches the available modes for import
	 * @param files
	 * @param sett
	 * @return
	 * @throws Exception
	 */
	public static ImageGroupMD[] importTextDataToImage(File[] files, SettingsImageDataImportTxt sett, boolean sortFiles) throws Exception { 
		// get separation strng
		String separation = "";
		if(sett.getSeparation().equalsIgnoreCase("AUTO")) {
			// automatic separation TODO
			// separation = TextAnalyzer.getSeparationString(files, sett, separation);
		} 
		else {
			// normal separation
			separation = sett.getSeparation();
		} 
		// switch import mode
		switch(sett.getModeImport()){
		case MULTIPLE_FILES_LINES_TXT_CSV:
		case PRESETS_THERMO_NEPTUNE:
			return new ImageGroupMD[]{importTextFilesToImage(files, sett, separation, sortFiles)};
		case CONTINOUS_DATA_TXT_CSV:
			// do the import for one file after each other because one image=one file
			ArrayList<ImageGroupMD> list = new ArrayList<ImageGroupMD>(); 
			for(File f : files) {
				ImageGroupMD imgList = importTextFilesToImage(new File[]{f}, sett, separation, sortFiles);
				// add all 
				list.add(imgList);
			}
			return list.toArray(new ImageGroupMD[list.size()]);
		case ONE_FILE_2D_INTENSITY:  
			return new ImageGroupMD[]{import2DIntensityToImage(files, sett, separation)};
		case PRESETS_THERMO_MP17: 
			return importFromThermoMP17FileToImage(files, sett); 
		}

		return null;
	}

	// one file per image
	// importing a 2D intensity matrix
	// txt /csv / Excel
	// I0	i1	i2	i3	i4
	// i5	i6	i7	i8	i9 ...
	public static ImageGroupMD import2DIntensityToImage(File[] files, SettingsImageDataImportTxt sett, String separation) throws Exception { 		
		// read x only once
		ArrayList<Float>[] x = null; 
		float[] startXValue = null;
		int xcol = -1, ycol=-1; 

		ModeData mode = sett.getModeData();
		int xmatrix = -1;
		
		if(mode.equals(ModeData.X_MATRIX_STANDARD)) {
			// search for xmatrix.csv

			for(int f=0; f<files.length && xmatrix == -1; f++) {
				if(files[f].getName().startsWith("xmatrix")) {
					xmatrix = f;
					// import xmatrix
					File file = files[f];
					BufferedReader br = txtWriter.getBufferedReader(file);
					String s; 
					int dp = 0;
					while ((s = br.readLine()) != null  && (sett.getEndDP()==0 || dp<sett.getEndDP())) {
						dp++;
						if(dp>=sett.getStartDP()) {
							// try to separate by separation
							String[] sep = s.split(separation);
							// initialise
							if(x == null) {
								int minus = sett.getStartLine()==0? 0 : sett.getEndLine()-1;
								int size = sett.getEndLine()==0? sep.length-minus : Math.min(sep.length, sett.getEndLine()) -minus;
								x = new ArrayList[size];
								startXValue = new float[size];
							}
							// fill data
							int c = 0;
							for(int i=sett.getStartLine()==0? 0 : sett.getEndLine()-1; i<sep.length && (sett.getEndLine()==0 || i<sett.getEndLine()); i++) {
								if(x[c]==null) {
									x[c] = new ArrayList<Float>();
									startXValue[c] = Float.valueOf(sep[i]);
								}
								x[c].add(Float.valueOf(sep[i])-startXValue[c]);
								c++;
							}
						}
					}
				}
			}
		}
		// store data in ArrayList
		// x[line].get(dp)
		ArrayList<ScanLineMD> scanLines = new ArrayList<ScanLineMD>();
		  
				ArrayList<String> meta = new ArrayList<String>();  
				ArrayList<String> titles = new ArrayList<String>();
				ArrayList<File> flist = new ArrayList<File>();
		
		// one file is one dimension (image) of scanLines
		for(int f=0; f<files.length; f++) {
			// skip xmatrix
			if(f==xmatrix)
				continue;
			else {
				File file = files[f];
				ArrayList<Double>[] y = null;  
				// for metadata collection if selected in settings
				String metadata = "";
				String title = "";
	
				// read text file 
				// line by line
				BufferedReader br = txtWriter.getBufferedReader(file);
				String s;
				int k = 0;
				int line = 0;
				int dp = 0;
				while ((s = br.readLine()) != null) {
					// try to separate by separation
					String[] sep = s.split(separation);
					// data
					if(sep.length>1 && TextAnalyzer.isNumberValues(sep)) { 
						// increment dp 
						dp++;
						// 
						line = 0;
						// initialise data lists
						if(xcol==-1) {
							if(mode==ModeData.XYXY_ALTERN) {
								xcol = sep.length/2;
								// limits
								if(sett.getEndLine()!=0 && xcol>sett.getEndLine()) xcol = sett.getEndLine();
								if(sett.getStartLine()!=0) xcol = xcol+1-sett.getStartLine();
								ycol = xcol;
							}
							else {
								xcol = mode==ModeData.ONLY_Y || mode==ModeData.X_MATRIX_STANDARD? 0 : 1; 
								ycol = (sep.length-xcol);
								// limits
								if(sett.getEndLine()!=0 && ycol>sett.getEndLine()) ycol = sett.getEndLine();
								if(sett.getStartLine()!=0) ycol = ycol+1-sett.getStartLine();
							}
						}
						if(y==null) {
							if(mode!=ModeData.ONLY_Y && x==null) {
								startXValue = new float[xcol];
								x = new ArrayList[xcol];  
								for(int i=0; i<x.length; i++)
									x[i] = new ArrayList<Float>();
							}
							y = new ArrayList[ycol]; 
							for(int i=0; i<y.length; i++)
								y[i] = new ArrayList<Double>();
						}
						// add data if dp is not excluded
						if(sett.getStartDP()==0 || dp>=sett.getStartDP()) {
							// add data if line is not excluded
							for(int i=0; i<sep.length && (sett.getEndLine()==0 || line<sett.getMaxLines()); i++) {
								// x is only added in f==0 (first file)
								switch(mode) { // TODO
								case X_MATRIX_STANDARD:
								case ONLY_Y:
									if((sett.getStartLine()==0 || i+1>=sett.getStartLine())) {
										y[line].add(Double.valueOf(sep[i]));
										line++;
									}
									break;
								case XYXY_ALTERN:
									if(sett.getStartLine()==0 || i/2+1>=sett.getStartLine()) {
										if(i%2==1) {
											y[line].add(Double.valueOf(sep[i]));
											line++;
										}
										else if(f==0) {
											// first as 0
											if(x[line].size()==0) {
												x[line].add(0.f);
												startXValue[line] = Float.valueOf(sep[i]);
											}
											// relative to startX
											else x[line].add(Float.valueOf(sep[i])-startXValue[line]);
										}
									}
									break;
								case XYYY: 
									// add x once
									if(f==0 && i==0) {
										// first as 0
										if(x[0].size()==0) {
											x[0].add(0.f);
											startXValue[0] = Float.valueOf(sep[i]);
										}
										// relative to startX
										else x[0].add(Float.valueOf(sep[i])-startXValue[0]);
									}
									// add y
									if(i!=0){
										if(sett.getStartLine()==0 || i>=sett.getStartLine()) {
											y[line].add(Double.valueOf(sep[i]));
											line++;
										}
									}
									break;
								} 
							}  
						}
						// last dp added?
						if(sett.getEndDP()!=0 && dp>=sett.getEndDP())
							break;
					}
					// or metadata
					else {
						// title
						if(k==0 && s.length()<=30) {
							title = s;
						}
						metadata += s+"\n"; 
					}
					k++;
				}
				// Generate Lines
				for(int i=0; i<y.length; i++) {
					// create lines
					if(scanLines.size()<=i)
						scanLines.add(new ScanLineMD());
					// add data
					scanLines.get(i).addDimension(y[i]);
					switch(mode) { 
					case XYXY_ALTERN:
						scanLines.get(i).setX(x[i]);
						break;
					case XYYY:
						scanLines.get(i).setX(x[0]);
						break;
					case X_MATRIX_STANDARD:
						if(x.length==1) {
							scanLines.get(i).setX(x[0]);
						}
						else scanLines.get(i).setX(x[i]);
						break;
						//TODO
					}
	
				}
				titles.add(title);
				meta.add(metadata);
				flist.add(file);
			}
		}

		DatasetMD data = new DatasetMD(scanLines);
		ImageGroupMD group = data.createImageGroup(files[0].getParentFile());
		for(int i=0;i<group.getImages().size() && i<titles.size(); i++) 
			setSettingsImage2D(((Image2D)group.getImages().get(i)), flist.get(i), titles.get(i), meta.get(i));
		//return image 
		return group;
	}


	// one file per line
	// load text files with separation
	// time  SEPARATION   intensity
	// new try the first is not good
	public static ImageGroupMD importTextFilesToImage(File[] files, SettingsImageDataImportTxt sett, String separation, boolean sortFiles) throws Exception { 
		// reset title line
		titleLine = null;
		
		ArrayList<ScanLineMD> lines;
		if(sett.getModeImport()==IMPORT.PRESETS_THERMO_NEPTUNE)
			lines = importNeptuneTextFilesToScanLines(files, sett, separation, sortFiles);
		else lines = importTextFilesToScanLines(files, sett, separation, sortFiles);

		// parent directory as raw file path 
		File parent = files[0].getParentFile();
		if(SettingsImageDataImportTxt.class.isInstance(sett)) {
			if(((SettingsImageDataImportTxt)sett).isFilesInSeparateFolders())
				parent = parent.getParentFile();
			else if(((SettingsImageDataImportTxt)sett).getModeImport()==IMPORT.CONTINOUS_DATA_TXT_CSV)
				parent = files[0];
		}
		// for all images
		boolean continuous = sett.getModeImport().equals(IMPORT.CONTINOUS_DATA_TXT_CSV);
		boolean hardsplit = continuous && sett.isUseHardSplit() && !(sett.getSplitAfter()==0 || sett.getSplitAfter()==-1);
		
		ImageGroupMD group = new ImageGroupMD();
		// set data path and name
		group.getSettings().setName((parent.getName()));
		group.getSettings().setPathData(parent.getAbsolutePath());
		
		// add images
		Image2D realImages[] = new Image2D[lines.get(0).getImageCount()];
		ImageDataset data = null;
		if(continuous && !hardsplit) data = new DatasetContinuousMD(lines.get(0));
		else data = new DatasetMD(lines);
		for(int i=0; i<realImages.length; i++) {   
			// has title line? with xyyyy
			if(titleLine!=null && titleLine.length>=realImages.length+1)
				realImages[i] = createImage2D(parent, titleLine[i+1], metadata, data, i, continuous && !hardsplit);  
			else realImages[i] = createImage2D(parent, "", metadata, data, i, continuous && !hardsplit);  
			// add to group (also sets the group for this image)
			group.add(realImages[i]);
		}
		
		if(continuous && !hardsplit) {
			// set split settings for continuous data (non hardsplit)
			DatasetContinuousMD data2 = (DatasetContinuousMD)data;
			data2.setSplitSettings(new SettingsImageContinousSplit(sett.getSplitAfter(), sett.getSplitStart(), sett.getSplitUnit()));
		}

		//return image
		return group;
	}

	// needed for image creation from ArrayList<ScanLine>
	// titleline is always X y1 y2 y3 y4 (titles) and size = dimension+1
	private static String[] titleLine = null;
	private static String metadata = "";
	private static String title = "";
	/**
	 * 
	 * @param files
	 * @param sett
	 * @param separation
	 * @return an array of ArrayList<ScanLine> that can be converted to images
	 * @throws Exception
	 */
	public static ArrayList<ScanLineMD> importTextFilesToScanLines(File[] files, SettingsImageDataImportTxt sett, String separation, boolean sortFiles) throws Exception { 
		long time1 = System.currentTimeMillis();
		// sort text files by name:
		if(sortFiles)
			files = FileAndPathUtil.sortFilesByNumber(files);
		// images (getting created at first data reading)
		ArrayList<ScanLineMD> lines=null; 
		
		// excluded columns
		List<Integer> excludedCol = sett.getExcludeColumnsArray();
		// calc fist used column
		int firstCol = 0;
		if(excludedCol!=null) {
			for(int ex : excludedCol) {
				if(ex==firstCol) firstCol++;
				else break;
			}
		}
		
		// perform hardsplit
		boolean continuous = sett.getModeImport().equals(IMPORT.CONTINOUS_DATA_TXT_CSV);
		boolean hardsplit = continuous && sett.isUseHardSplit() && !(sett.getSplitAfter()==0 || sett.getSplitAfter()==-1);
		boolean wasStartErased = !hardsplit; 
		boolean scanLinesSkipped = !hardsplit;
		float startX = 0;
		int cDP = 0;
		
		// file after file open text file
		// start with starting line
		for(int i=(sett.getStartLine()==0 || continuous? 0 : sett.getStartLine()-1); i<files.length && (sett.getEndLine()==0 || i<=sett.getEndLine()); i++) {
			// data of one line
			ArrayList<Float> x  = null;
			// iList[dimension].get(dp)
			ArrayList<Double>[] iList=null;
			// more than one intensity column? then extract all cols (if true)
			boolean dataFound = false; 
			// for metadata collection if selected in settings
			String[] lastLine = null;
			// track data points
			int dp = 0;
			// starting data points for splitting of continuous data
			if(continuous && hardsplit && sett.getSplitUnit().equals(XUNIT.DP)) {
				dp -= sett.getSplitStartDP();
			}
			// read file
			File f = files[i]; 
			// line by line add datapoints to current Scanlines
			BufferedReader br = txtWriter.getBufferedReader(f);
			String sline;
			int k = 0;
			while ((sline = br.readLine()) != null) {
				// try to seperate by seperation
				String[] sep = sline.split(separation);
				// data
				if(sep.length>1 && TextAnalyzer.isNumberValues(sep)) {
					// increment
					dp++;
					// initialise
					// titleLine could be written one line before data lines
					if(!dataFound) {
						dataFound = true;  // call this only once 

						// create all new Images
						int colCount = sep.length;
						if(excludedCol!=null) {
							for(int ex = excludedCol.size()-1; ex>=0; ex--)
								if(excludedCol.get(ex)<colCount) colCount--;
								else break;
						}
						colCount +=  -(sett.isNoXData()? 0:1);
						iList = new ArrayList[colCount]; 
						
						// set titles only once
						if(titleLine==null && lastLine!=null && lastLine.length==sep.length) {
							int img = 1;
							int ex = 0; 
							titleLine = new String[colCount+1]; 
							titleLine[0] = "x";
							for(int s= (sett.isNoXData()? firstCol : firstCol+1); s<lastLine.length; s++) {
								boolean isExcluded = false;
								// add title if not excluded
								if(excludedCol!=null) {
									for( ; ex<excludedCol.size(); ex++) {
										if(excludedCol.get(ex)==s) {
											isExcluded= true;
											break;
										}
										if(excludedCol.get(ex)>s)
											break;
									}
								}
								if(!isExcluded) {
									titleLine[img] = lastLine[s];
									img++;
								}
							}
						}
						
						// has X data?
						if(!sett.isNoXData()) {
							x = new ArrayList<Float>(); 
							// startX for hardsplit
							startX = Float.valueOf(sep[firstCol]);
						}
						// Image creation 
						for(int img=0; img<iList.length; img++) {
							iList[img] = new ArrayList<Double>(); 
						} 
					}
					// hardsplit jumps over first datapoints
					if(hardsplit) {
						// hardsplit continuous with time data: erase startX
						if(!wasStartErased && XUNIT.s.equals(sett.getSplitUnit()) && ((int)(sett.getSplitStart()*1000))!=0) {
							float cx = Float.valueOf(sep[firstCol]);
							// x still < than start time?
							if(cx-startX<sett.getSplitStart())
								dp = 0;
							else {
								wasStartErased = true;
								startX = cx;
							}
						}
						else if(!scanLinesSkipped) {
							cDP++;
							if(XUNIT.s.equals(sett.getSplitUnit())) {
								float cx = Float.valueOf(sep[firstCol]);
								scanLinesSkipped = (cx-startX)>=sett.getSplitAfter()*(sett.getStartLine()-1);
							}
							else {
								scanLinesSkipped = cDP>=sett.getSplitAfterDP()*(sett.getStartLine()-1);
							}
							// still not?
							if(!scanLinesSkipped)
								dp = 0;
						}							
					}
					// add data if DP is in range of start/end dp
					if(dp>=sett.getStartDP() && dp>0 && (sett.getEndDP()==0 || dp<=sett.getEndDP())) { 
						// has X data?
						if(!sett.isNoXData()) {
							if(x.size()==0) startX = Float.valueOf(sep[firstCol]);
							x.add(Float.valueOf(sep[firstCol])-startX);
						}
						// add Data Points to all images 
						int img = 0;
						int ex = 0;
						for(int s= (sett.isNoXData()? firstCol : firstCol+1); s<sep.length; s++) {
							boolean isExcluded = false;
							// add Datapoint if not excluded
							if(excludedCol!=null) {
								for( ; ex<excludedCol.size(); ex++) {
									if(excludedCol.get(ex)==s) {
										isExcluded= true;
										break;
									}
									if(excludedCol.get(ex)>s)
										break;
								}
							}
							if(!isExcluded) {
								iList[img].add(Double.valueOf(sep[s]));
								img++;
							}
						}
					}
					
					// hardsplit continuous data
					if(hardsplit && (x==null || x.size()>1)) {
						// split after DP   / split after time
						boolean endOfLine = false;
						if(XUNIT.DP.equals(sett.getSplitUnit())) 
							endOfLine = dp>=sett.getSplitAfterDP();
						else {
							float xstart = x.get(0);
							float cx = x.get(lines.size()-1);
							int currentLine = lines==null? 1 : lines.size()+1;
							endOfLine = (cx-xstart)>=sett.getSplitAfter()*currentLine;
						}  
						// has reached end of line
						if(endOfLine) {
							// add line
							// init lines ArrayList
							if(lines==null)
								lines = new ArrayList<ScanLineMD>(); 
				
							// add new line
							lines.add(new ScanLineMD()); 
							// add data to line
							// has X data?
							if(!sett.isNoXData())
								lines.get(lines.size()-1).setX(x);
							// add all dimensions
							for(int img=0; img<iList.length; img++) {  
								// add data
								lines.get(lines.size()-1).addDimension(iList[img]);
								//reset iList
								iList[img].clear();
							} 
							// set dp = 0
							dp=0;
							// reset lists
							if(x!=null)
								x.clear();
							
							// enough lines?
							if(lines.size()>=sett.getEndLine() && sett.getEndLine()!=0)
								return lines;
						}
					}
				}
				// or metadata
				else {
					// title
					if(i==0 && k==0 && sline.length()<=30) {
						title = sline;
					}
					metadata += sline+"\n"; 
				}
				// last line
				lastLine = sep;
				k++;
			} 

			if(!hardsplit) {
				// init lines ArrayList
				if(lines==null)
					lines = new ArrayList<ScanLineMD>(); 
	
				// add new line
				lines.add(new ScanLineMD()); 
				// add data to line
				// has X data?
				if(!sett.isNoXData())
					lines.get(lines.size()-1).setX(x);
				// add all dimensions
				for(int img=0; img<iList.length; img++) {  
					// add data
					lines.get(lines.size()-1).addDimension(iList[img]);
				} 
			}
		}
		//return image
		return lines;
	}

	//################################################################################
	// Presets

	/**
	 * imports from Thermo MP17 Files (iCAP-Q) (different separations)
	 * 0		1	2			3	4	
	 * MainRuns	0	75As (1)	Y	7.5000022500006747
	 * or x values for m/z --> delete
	 * MainRuns	0	75As (1)	X [u]	74.9219970703125
	 * @param file
	 * @param sett
	 * @return
	 * @throws Exception
	 */
	public static ImageGroupMD[] importFromThermoMP17FileToImage(File[] file, SettingsImageDataImportTxt sett) throws Exception { 
		// images
		ArrayList<ImageGroupMD> images=new ArrayList<ImageGroupMD>();
		// all files
		for(File f : file) {
			ImageGroupMD group = importFromThermoMP17FileToImage(f, sett);
			if(group!=null)
				images.add(group);
		}
		//return image 
		ImageGroupMD imgArray [] = new ImageGroupMD[images.size()];
		imgArray = images.toArray(imgArray);
		return imgArray;
	}
	private static ImageGroupMD importFromThermoMP17FileToImage(File file, SettingsImageDataImportTxt sett) throws Exception { 
		// images
		// store data in ArrayList
		ArrayList<ScanLineMD> scanLines = new ArrayList<ScanLineMD>(); 
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<Float>[] x = null;
		boolean hasTimeAlready = false;
		// scan line count is known but number data points is unknown
		// iList[line].get(dp)
		ArrayList<Double>[] iList = null;
		// for metadata collection if selected in settings
		String metadata = "";
		String title = "";  
		// save where values are starting
		int valueindex = -1;
		// separation 
		String separation = sett.getSeparation();
		// separation for UTF-8 space 
		char splitc = 0;
		String splitUTF8 = String.valueOf(splitc);
		// count data points
		int dp = 0;
		// line by line
		BufferedReader br = txtWriter.getBufferedReader(file);
		String s;
		while ((s = br.readLine()) != null) {
			// try to separate by separation
			String[] sep = s.split(separation);
			// if sep.size==1 try and split symbol=space try utf8 space
			if(separation.equals(" ") && sep.length<=1) {
				sep = s.split(splitUTF8);
				if(sep.length>1)
					separation = splitUTF8; 
			}
			// is dataline? Y in col3? or col2
			if(sep.length>4 && ((valueindex!=5 && sep[3].equalsIgnoreCase("Y") && TextAnalyzer.isNumberValue(sep[4])) 
					|| (valueindex!=4 && sep[4].equalsIgnoreCase("Y") && TextAnalyzer.isNumberValue(sep[5])))) {
				if(valueindex==-1) {
					valueindex = TextAnalyzer.isNumberValue(sep[4])? 4:5;
				}
				// title in col2
				if(title=="") {
					title = sep[2]; 
				}
				else if(!title.equals(sep[2])) { 
					// a new element was found
					titles.add(title);
					// set to start values
					title = sep[2];
					// generate scan lines
					if(scanLines.size()==0) 
						for(ArrayList<Double> in : iList) 
							scanLines.add(new ScanLineMD());
					// add i dimension (image)
					for (int i = 0; i < iList.length; i++) {
						scanLines.get(i).addDimension(iList[i]);
					} 
					// reset
					iList = null;
					dp = 0;
				}
				// generate new list
				if(iList==null) {
					int lineCount = sep.length-valueindex;
					if(sett.getEndLine()!=0 && lineCount>sett.getEndLine()) lineCount = sett.getEndLine();
					lineCount -= sett.getStartLine();
					
					iList = new ArrayList[lineCount];
					for(int i=0; i<iList.length; i++) {
						iList[i] = new ArrayList<Double>();
					}
				}
				// dp increment
				dp++;
				// add all data points of this line (if inside start/end limits)
				if((dp>=sett.getStartDP()) && (sett.getEndDP()==0 || dp<=sett.getEndDP())) {
					for(int i=valueindex+sett.getStartLine(); i<sep.length && (sett.getEndLine()==0 || i-valueindex<sett.getEndLine()); i++) {
						try {
							iList[i-valueindex-sett.getStartLine()].add(Double.valueOf(sep[i]));
						}catch(Exception ex) { 
							iList[i-valueindex-sett.getStartLine()].add(-1.0);
						}
					}
				}
			} 
			// is dataline? Time in col3? or col2
			else if(!hasTimeAlready && sep.length>4 && ((valueindex!=5 && sep[3].equalsIgnoreCase("Time") && TextAnalyzer.isNumberValue(sep[4])) 
					|| (valueindex!=4 && sep[4].equalsIgnoreCase("Time") && TextAnalyzer.isNumberValue(sep[5])))) {
				if(valueindex==-1) {
					valueindex = TextAnalyzer.isNumberValue(sep[4])? 4:5;
				}
				// title in col2
				if(title=="") title = sep[2];
				else if(!title.equals(sep[2])) { 
					// a new element was found
					// stop search for time values
					title = "";
					hasTimeAlready = true;
					// generate scan lines
					if(scanLines.size()==0) 
						for(ArrayList<Float> in : x) 
							scanLines.add(new ScanLineMD());
					// add x to all scan lines
					for (int i = 0; i < x.length; i++) {
						scanLines.get(i).setX(x[i]);
					} 
					//
					dp=0;
				}
				// generate new list
				if(x==null) {
					x = new ArrayList[sep.length-valueindex];
					for(int i=0; i<x.length; i++) 
						x[i] = new ArrayList<Float>();
				}
				// add all intensities
				if((dp>=sett.getStartDP()) && (sett.getEndDP()==0 || dp<=sett.getEndDP())) {
					for(int i=valueindex+sett.getStartLine(); i<sep.length && (sett.getEndLine()==0 || i-valueindex<sett.getEndLine()); i++) {
						try {
							x[i-valueindex-sett.getStartLine()].add(Float.valueOf(sep[i]));
						}catch(Exception ex) { 
							x[i-valueindex-sett.getStartLine()].add(-1.f);
						}
					}
				}
			} 
		}
		// 
		if(iList!=null) {
			// a new element was found
			titles.add(title);
			// generate scan lines
			if(scanLines.size()==0) 
				for(ArrayList<Double> in : iList) 
					scanLines.add(new ScanLineMD());
			// add i dimension (image)
			for (int i = 0; i < iList.length; i++) {
				scanLines.get(i).addDimension(iList[i]);
			} 
		}
		// Generate Image2D from scanLines 
		DatasetMD data = new DatasetMD(scanLines); 
		return data.createImageGroup(file);
	}

	/**
	 * imports from Thermo Neptune files: .exp
	 * Sample ID: Sample2
	 * Cycle	Time	142Nd	
	 * 1	16:17:44:334	-1.4410645627666198e-004
	 * @param file
	 * @param sett
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<ScanLineMD> importNeptuneTextFilesToScanLines(File[] files, SettingsImageDataImportTxt sett, String separation, boolean sortFiles) throws Exception { 
		long time1 = System.currentTimeMillis();
		// sort text files by name:
		if(sortFiles)
			files = FileAndPathUtil.sortFilesByNumber(files);
		// images (getting created at first data reading)
		ArrayList<ScanLineMD> lines= new ArrayList<ScanLineMD>(); 
		// more than one intensity column? then extract all colls (if true)
		boolean dataFound = false; 
		// for metadata collection if selected in settings
		String[] lastLine = null;

		// file after file open text file
		for(int i=0; i<files.length; i++) {
			ArrayList<Float> xList = null; 
			// iList[dimension].get(dp)
			ArrayList<Double>[] iList = null;
			// read file
			File f = files[i]; 
			// start time
			float xstart = -1;
			// line by line add datapoints to current Scanlines 
			BufferedReader br = txtWriter.getBufferedReader(f);
			String s;
			int k=0;
			while ((s = br.readLine()) != null) {
				// try to seperate by seperation
				String[] sep = s.split(separation);
				// data
				if(sep.length>2 && TextAnalyzer.isNumberValue(sep[0]) && TextAnalyzer.isNumberValue(sep[2])) {
					// titleLine could be written one line before data lines
					if(!dataFound) {
						dataFound = true;  // call this only once 
						if(lastLine!=null && lastLine.length==sep.length) {
							// titlerow to: time y1 y2 y3
							titleLine = Arrays.copyOfRange(lastLine, 1, lastLine.length);
						}

						// create all new Images
						iList = new ArrayList[sep.length-2];
						xList = new ArrayList<Float>();
						// Image creation
						for(int img=0; img<iList.length; img++)
							iList[img] = new ArrayList<Double>(); 
					}

					// x 
					if(xstart==-1) 
						xstart = timeToSeconds(sep[1]);
					float x = timeToSeconds(sep[1])-xstart;  
					xList.add(x);
					// add Datapoints to all images
					for(int img=0; img<iList.length; img++) {
						double y = Double.valueOf(sep[img+2]);
						// add Datapoint
						iList[img].add(y); 
					}
				}
				// or metadata
				else {
					// title
					if(i==0 && k==0 && s.length()<=30) {
						title = s;
					}
					metadata += s+"\n"; 
				}
				// last line
				lastLine = sep;
				k++;
			} 

			lines.add(new ScanLineMD());
			lines.get(lines.size()-1).setX(xList);
			// for all images
			for(int img=0; img<iList.length; img++) { 
				// add new lines to each list 
				lines.get(lines.size()-1).addDimension(iList[img]);
			} 
		}
		//return image
		return lines;
	}

	/**
	 * creates a new Image2D with data and new settings
	 * @param file
	 * @param title
	 * @param metadata
	 * @param scanLines
	 * @param index 
	 * @return
	 */
	private static Image2D createImage2D(File file, String title, String metadata, ImageDataset data, int index, boolean continous) {
		return setSettingsImage2D(new Image2D(data, index), file, title, metadata);
	}
	/**
	 * creates a new Image2D with data and new settings
	 * @param file
	 * @param title
	 * @param metadata
	 * @param scanLines
	 * @param index 
	 * @return
	 */
	private static Image2D setSettingsImage2D(Image2D img, File file, String title, String metadata) {
		img.setSettings(SettingsPaintScale.createSettings(SettingsPaintScale.S_KARST_RAINBOW_INVERSE));
		// Generate Image2D from scanLines
		SettingsGeneralImage general = img.getSettings().getSettImage();
		// Metadata 
		general.setRAWFilepath(file.getPath());
		if(title=="") {
			general.setRAWFilepath(file.getParent());
			title = FileAndPathUtil.eraseFormat(file.getName());
		}
		general.setTitle(title);
		general.setMetadata(metadata);
		// Image creation
		// else just add it as normal matrix-data image
		return img;
	}

	/**
	 * creates a blank image2d without any data but with new settings
	 * @param file
	 * @param title
	 * @param metadata
	 * @param scanLines
	 * @return
	 */
	private static Image2D createImage2DBlank(File file, String title, String metadata) {
		// Generate Image2D from scanLines
		SettingsPaintScale paint = new SettingsPaintScale();
		SettingsGeneralImage general = new SettingsGeneralImage();
		paint.resetAll();
		general.resetAll();
		// Metadata 
		if(title=="") title = file.getName();
		general.setRAWFilepath(file.getPath());
		general.setTitle(title);
		general.setMetadata(metadata);
		// Image creation
		Image2D img = new Image2D(new SettingsImage2D(paint, general));
		return img;
	}




	public static final float[] F = {0.001f, 1, 60, 3600, 86400};
	/**
	 * s as time (hh:mm:ss:mmm
	 * @param s
	 * @return
	 */
	public static float timeToSeconds(String s) { 
		String[] split = s.split(":");

		float val = 0;
		for(int i=split.length-1; i>=0; i--) {
			val += Float.valueOf(split[i])*(F[split.length-1-i]);
		}
		return val;
	}
}
