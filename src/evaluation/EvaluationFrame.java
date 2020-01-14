package evaluation;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import models.Evaluator;

public class EvaluationFrame implements Evaluator {
	public abstract static class EvaluationMeasure {
		public abstract double register(Object source, double outcome);
	}
	
	private JFrame frame = null;
	private HashMap<String, XYSeries> serii = new HashMap<String, XYSeries>();
    private XYSeriesCollection dataset = new XYSeriesCollection();
	private HashMap<String, EvaluationMeasure> singleMeasureEvaluators = new HashMap<String, EvaluationMeasure>();
	private long time;
	private Class<?> measureClass;
	
	public EvaluationFrame() {
		time = 0;
	}
	
	public EvaluationFrame(Class<?> measureClass) {
		frame = new JFrame(measureClass.getSimpleName());
		frame.setSize(800, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	    time = 0;
	    this.measureClass = measureClass;
	}
	
	public long getTime() {
		return time;
	}
	
	public final double register(String measure, Object source, double outcome) {
		EvaluationMeasure evaluator = singleMeasureEvaluators.get(measure);
		if(evaluator==null)
			singleMeasureEvaluators.put(measure, evaluator = createMeasure());
		return evaluator.register(source, outcome);
	}
	
	protected EvaluationMeasure createMeasure() {
		try {
			return (EvaluationMeasure)measureClass.newInstance();
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public final void aggregate(Object source, Map<String, Double> outcome) {
		if(outcome.isEmpty())
			return;
		boolean updatePlotSeries = false;
		for(String seriesName : outcome.keySet()) {
			XYSeries series = serii.get(seriesName);
			if(series==null) {
				series = new XYSeries(seriesName);
				serii.put(seriesName, series);
				dataset.addSeries(series);
				updatePlotSeries = true;
			}
			double value = register(seriesName, source, outcome.get(seriesName));
			series.add(time, value);
			//System.out.println(seriesName+" "+value);
		}
		if(updatePlotSeries && frame!=null) {
		    JFreeChart chart = ChartFactory.createXYLineChart(
		    	frame.getTitle(),
		        "t",
		        "",
		        dataset,
		        PlotOrientation.VERTICAL,
		        true, true, true);
		    ChartPanel panel = new ChartPanel(chart);
		    frame.setContentPane(panel);
			frame.setVisible(true);
		}
	    time += 1;
	    if(frame!=null)
	    	frame.repaint();
	}
}
