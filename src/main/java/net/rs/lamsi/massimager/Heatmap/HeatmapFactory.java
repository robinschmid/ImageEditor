package net.rs.lamsi.massimager.Heatmap;
import java.awt.Color;

import net.rs.lamsi.general.datamodel.image.Image2D;
import net.rs.lamsi.general.datamodel.image.data.twodimensional.XYIData2D;
import net.rs.lamsi.massimager.MyFreeChart.Plot.image2d.ImageRenderer;
import net.rs.lamsi.massimager.MyFreeChart.Plot.image2d.PlotImage2DChartPanel;
import net.rs.lamsi.massimager.Settings.SettingsImage;
import net.rs.lamsi.massimager.Settings.SettingsPaintScale;
import net.rs.lamsi.massimager.Settings.visualization.plots.SettingsThemes;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;





public class HeatmapFactory {
	// Variablen 
	

	// Image2D to Heatmap Image
	public static Heatmap generateHeatmap(Image2D image)  throws Exception { 
		SettingsPaintScale setPaint = image.getSettPaintScale();
		SettingsImage setImg = image.getSettImage();
		// get rotated and reflected dataset
		XYIData2D dat = image.toXYIArray(setImg); 
		// Heatmap erzeugen
		Heatmap h = createChart(image, setPaint, setImg, createDataset(image.getSettImage().getTitle(), dat.getX(), dat.getY(), dat.getI()));
		// TODO WHY?
		return h;
	}
	
	/*
	// For Triggered Scan Data
	// A File = A Line
	private Heatmap generateHeatmapDiscontinous(SettingsPaintScale settings, String title, MZChromatogram[] mzchrom, SettingsImage setDisconImage) {
		// TODO mehrere MZ machen
		// daten
		double spotsize = setDisconImage.getSpotsize();
		double xvelocity = setDisconImage.getVelocity();
		// Alle Spec
		// Erst Messpunkteanzahl ausrechnen 
		int scanpoints = 0;
		for(int i=0; i<mzchrom.length; i++) {
			scanpoints += mzchrom[i].getItemCount();
		}
		// Datenerstellen
		double[] x = new double[scanpoints];
		double[] y = new double[scanpoints];
		double[] z = new double[scanpoints];
		
		// jede linie f�r sich durchgehen und abarbeiten
		MZChromatogram chrom;
		int overallindex = 0;
		for(int c=0; c<mzchrom.length; c++) { 
			chrom = mzchrom[c];
			// Alle Messpunkte vom chrom durchgehen
			for(int i=0; i<chrom.getItemCount(); i++) { 
				// Daten eintragen
				x[i+overallindex] = chrom.getX(i).doubleValue()*xvelocity;
				y[i+overallindex] = c*spotsize;
				z[i+overallindex] = chrom.getY(i).doubleValue();
			}
			overallindex += chrom.getItemCount();
		} 
		// Heatmap erzeugen
		return createChart(settings, setDisconImage, createDataset(title, x, y, z));
	}
	
	
	// Generate Heatmap with Continous Data WIDHTOUT Triggerin every Line
	private Heatmap generateHeatmapContinous(SettingsPaintScale settings, MZChromatogram mzchrom, SettingsMSImage setMSICon) {  
		//
		double timePerLine = setMSICon.getTimePerLine(); 
		double spotsize = setMSICon.getSpotsize();
		double xvelocity = setMSICon.getVelocity();
		// Gr��e des Images aus Zeiten ableiten
		// deltaTime aus Daten lesen = Zeit zwischen Messungen 
		double overallTime = (mzchrom.getMaxX()-mzchrom.getMinX());
		double deltaTime = overallTime/mzchrom.getItemCount();
		// ist nur abgesch�tzt
		// Breite und H�he fest definieren rundet bisher ab 
		System.out.println("OverallTime = "+overallTime+ "   dT = "+deltaTime);
		// XYZ anzahl ist definiert durch messwerte im MZChrom 
		int scanpoints = mzchrom.getItemCount();
		double[] x = new double[scanpoints];
		double[] y = new double[scanpoints];
		double[] z = new double[scanpoints];
		// zeigt an wo man sich in der listData befindet
		int currenty = 0; 
		double lastTime = mzchrom.getMinX(); 
		double deltatime;
		// Alle MZChrom punkte durchgehen und in xyz eintragen
		// wenn Zeit gr��er als timePerLine dann y um eins vergr��ern
		for(int i=0; i<mzchrom.getItemCount(); i++) {
			deltatime = mzchrom.getX(i).doubleValue()-lastTime;
			// n�chste Zeile?
			if(deltatime>=timePerLine) {
				currenty++;
				// lastTime = mzchrom.getX(i).doubleValue(); 
				lastTime += timePerLine;
			}
			// Daten eintragen
			x[i] = mzchrom.getX(i).doubleValue()*xvelocity -lastTime*xvelocity;
			y[i] = currenty*spotsize;
			z[i] = mzchrom.getY(i).doubleValue();
		}
		
		// Vielleicht muss x und y umgedreht werden also dass erst jede Collumn durchgegangen wird
		// bisher wird jede Row durchgegangen

		return createChart(settings, setMSICon, createDataset("MSI", x, y, z));
	}
	*/

	// Diese wird aufgerufen um Heatmap zu generieren.
	// test heatmap bei ColorPicker Dialog
	public static Heatmap generateHeatmap(SettingsPaintScale settings, SettingsImage settImage,  String title, double[] xvalues, double[] yvalues, double[] zvalues)  throws Exception  {
		// chartpanel der Heatmap hinzuf�gen 
		return createChart(null, settings, settImage, createDataset(title, xvalues, yvalues, zvalues));
	}
	

	// erstellt ein JFreeChart Plot der heatmap
	private static Heatmap createChart(Image2D img, SettingsPaintScale settings, SettingsImage settImage, IXYZDataset dataset)  throws Exception  {
    	return createChart(img, settings, settImage, dataset, "x", "y");
    }
	
	// erstellt ein JFreeChart Plot der heatmap
	// bwidth und bheight (BlockWidth) sind die Maximalwerte
	private static Heatmap createChart(Image2D img, SettingsPaintScale settings, SettingsImage settImage, IXYZDataset dataset, String xTitle, String yTitle) throws Exception {
        // this min max values in array
        double zmin = dataset.getZMin();
        double zmax = dataset.getZMax();
        // minmax from selection only
        if(settings.isUsesMinMaxFromSelection() && img.getSelectedData()!=null && img.getSelectedData().size()>0) { 
	        settings.setMin(img.getMinIntensity(true));
	        settings.setMax(img.getMaxIntensity(true)); 
        } 
        // no data!
        if(zmin == zmax || zmax == 0) {
        	throw new Exception("Every data point has the same intensity of "+zmin);
        }
        else { 
	    	SettingsThemes setTheme = img.getSettTheme();
			// XAchse
	        NumberAxis xAxis = new NumberAxis(xTitle);
	        xAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
	        xAxis.setLowerMargin(0.0);
	        xAxis.setUpperMargin(0.0);
	        // Y Achse
	        NumberAxis yAxis = new NumberAxis(yTitle);
	        yAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
	        yAxis.setLowerMargin(0.0);
	        yAxis.setUpperMargin(0.0);
	        // XYBlockRenderer
	        ImageRenderer renderer = new ImageRenderer();
	        
	        // PaintScale f�r farbe? TODO mit Settings!
	        // TODO upper and lower value setzen!!!!
	        //two ways of min or max z value: 
	        // min max values by filter
	        if(settings.isUsesMinValues()==false) {
	        	// uses filter for min
	        	img.applyCutFilterMin(settings.getMinFilter());
	        	settings.setMin(img.getMinZFiltered());
	        }
	        if(settings.isUsesMaxValues()==false) {
	        	// uses filter for min
	        	img.applyCutFilterMax(settings.getMaxFilter());
	        	settings.setMax(img.getMaxZFiltered());
	        }
	        // creation of scale
	        // binary data scale? 1, 10, 11, 100, 101, 111, 1000, 1001
	        PaintScale scale = null;
	        scale = PaintScaleGenerator.generateStepPaintScale(zmin, zmax, settings); 
	        renderer.setPaintScale(scale);
	        renderer.setAutoPopulateSeriesFillPaint(true);
	        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
	        // TODO nicht feste Blockwidth!
	        // erstmal feste BlockWidth 
	        renderer.setBlockWidth(img.getMaxBlockWidth(settImage.getRotationOfData())); 
	        renderer.setBlockHeight(img.getMaxBlockHeight(settImage.getRotationOfData())); 
	        // Plot erstellen mit daten
	        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinesVisible(false);
	        plot.setRangeGridlinePaint(Color.white);
	        JFreeChart chart = new JFreeChart("XYBlockChartDemo1", plot);
	        // remove lower legend - wie farbskala rein? TODO
	        chart.removeLegend();
	        chart.setBackgroundPaint(Color.white);
	        
	        // Legend Generieren
	        PaintScale scaleBar = PaintScaleGenerator.generateStepPaintScaleForLegend(zmin, zmax, settings); 
			PaintScaleLegend legend = createScaleLegend(img, scaleBar, createScaleAxis(settings, dataset), settings.getLevels());   
			// adding legend in plot or outside
			if(setTheme.getTheme().isPaintScaleInPlot()) { // inplot
				XYTitleAnnotation ta = new XYTitleAnnotation(1, 0.0, legend,RectangleAnchor.BOTTOM_RIGHT);  
				ta.setMaxWidth(1);
				plot.addAnnotation(ta);
			}
			else chart.addSubtitle(legend);
			//
			chart.setBorderVisible(true); 
	
			
			// ChartPanel
			PlotImage2DChartPanel chartPanel = new PlotImage2DChartPanel(chart, img); 
			
			// add scale legend
			if(setTheme.getTheme().isShowScale())
				addScaleInPlot(img, setTheme, chartPanel);
			
			// theme
			img.getSettTheme().applyToChart(chart);
			
	 		//ChartUtilities.applyCurrentTheme(chart);
			//defaultChartTheme.apply(chart);
			chart.fireChartChanged();
			 
			// Heatmap
			Heatmap heat = new Heatmap(dataset, settings.getLevels(), chartPanel, scale, chart, plot, legend, img, renderer);
			
			// return Heatmap
	        return heat;
        }
    } 
	
	/**
	 * add scale to plot
	 * 2 ways: fix position or like paintscale
	 * @param img
	 * @param setTheme
	 * @param plot
	 */
	private static void addScaleInPlot( Image2D img,  SettingsThemes setTheme,  ChartPanel chartPanel) {
		// XYTitleAnnotation ta = new XYTitleAnnotation(1, 0.0, legend,RectangleAnchor.BOTTOM_RIGHT);  
		float value = setTheme.getTheme().getScaleValue();
		float factor = setTheme.getTheme().getScaleFactor();
		String unit = setTheme.getTheme().getScaleUnit(); 
		
		ScaleInPlot title = new ScaleInPlot(chartPanel, img, value, factor, unit);
		//XYDrawableAnnotation ta2 = new XYDrawableAnnotation(1000, 1000, legend.getWidth(), legend.getHeight(), legend);
		XYTitleAnnotation ta = new XYTitleAnnotation(setTheme.getTheme().getScaleXPos(), setTheme.getTheme().getScaleYPos(), title,RectangleAnchor.BOTTOM_RIGHT);  
		chartPanel.getChart().getXYPlot().addAnnotation(ta);
	}

	// erstellt XYZDataset aus xyz
	private static IXYZDataset createDataset(String title, double[] xvalues, double[] yvalues, double[] zvalues) {  
        IXYZDataset dataset = new IXYZDataset();
        dataset.addSeries(title, new double[][] { xvalues, yvalues, zvalues });
        return dataset;
    } 
	
	 
	// Eine PaintScaleLegend generieren
	private static PaintScaleLegend createScaleLegend(Image2D img, PaintScale scale, NumberAxis scaleAxis, int stepCount) { 
		SettingsThemes setTheme = img.getSettTheme();
		// create legend
		PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);
		legend.setBackgroundPaint(new Color(0,0,0,0));
		legend.setSubdivisionCount(stepCount);
		legend.setStripOutlineVisible(false);
		legend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
		legend.setAxisOffset(0);
		RectangleInsets rec = setTheme.getTheme().isPaintScaleInPlot()? RectangleInsets.ZERO_INSETS : new RectangleInsets(5, 0, 10, 5);
		legend.setMargin(rec);
		RectangleInsets rec2 = setTheme.getTheme().isPaintScaleInPlot()? RectangleInsets.ZERO_INSETS : new RectangleInsets(4, 0, 22, 2);
		legend.setPadding(rec2);
		legend.setStripWidth(10);
		legend.setPosition(RectangleEdge.RIGHT); 
		return legend;
	}	
	
	// ScaleAxis
	private static NumberAxis createScaleAxis(SettingsPaintScale settings, IXYZDataset dataset) {
		NumberAxis scaleAxis = new NumberAxis(null); 
//		scaleAxis.setLabel(null);
		return scaleAxis;
	}
	
/*
	//###############################################################################
	// Heatmap EXAMPLES
	public Heatmap getHeatmapChartPanelExample() {
		double[] x = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		double[] y = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		double[] z = {1,2,3,1,2,3,1,2,3,1,2,3,1,2,3};
		
		return createChart(createDataset("test", x, y, z));
	}
	public Heatmap getHeatmapChartPanelExample2() {
		double[] x = new double[400];
		double[] y = new double[400];
		double[] z = new double[400];
		
		for(int i=0; i<20; i++) {
			for(int k=0; k<20; k++) {
				x[i*20+k] = i;
				y[i*20+k] = k;
				z[i*20+k] =  i+k; 
			}
		}
		
		return createChart(createDataset("test", x, y, z));
	}
	public Heatmap getHeatmapChartPanelExample3() {
		double[] x = new double[400];
		double[] y = new double[400];
		double[] z = new double[400];
		
		for(int i=0; i<20; i++) {
			for(int k=0; k<20; k++) {
				x[i*20+k] = i*2.5;
				y[i*20+k] = k*2.1;
				z[i*20+k] =  i+k; 
			}
		}
		
		return createChart(createDataset("test", x, y, z));
	}

	public Heatmap getHeatmapChartPanelExampleDiscon() {
		double[] x = new double[400];
		double[] y = new double[400];
		double[] z = new double[400];
		
		Random rand = new Random(System.currentTimeMillis());
		
		for(int i=0; i<20; i++) {
			for(int k=0; k<20; k++) {
				// 7-10 mm  
				//neue zeile mit x=0
				if(i==0) x[i*20+k] = 0;
				else x[i*20+k] = x[i*20+k-20] + rand.nextInt(100)*0.03+7.0;
				// 25 mm
				y[i*20+k] = k*25;
				z[i*20+k] =  i+k; 
			}
		}
		
		return createChart(createDataset("test", x, y, z));
	}
*/


}
