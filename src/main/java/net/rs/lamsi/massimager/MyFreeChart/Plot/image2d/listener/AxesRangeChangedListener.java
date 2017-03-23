package net.rs.lamsi.massimager.MyFreeChart.Plot.image2d.listener;

import net.rs.lamsi.massimager.MyFreeChart.Plot.PlotChartPanel;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;

public interface AxesRangeChangedListener {

	/**
	 * only if axis range has changed
	 * @param axis
	 * @param lastR
	 * @param newR
	 */
	public abstract void axesRangeChanged(PlotChartPanel chart, ValueAxis axis, boolean isDomainAxis, Range lastR, Range newR);
}
