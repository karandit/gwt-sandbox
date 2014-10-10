package hu.evosoft.eo.downloadstats.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.BarChart;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.VAxis;

public class StatisticsByDomain extends DockLayoutPanel {
	private BarChart mChart;
	private StatsByDomainServiceAsync mStatsByDomainSvc;
	
	public StatisticsByDomain() {
		super(Unit.PX);
		initialize();
	}

	private void initialize() {
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {

			@Override
			public void run() {
				// Create and attach the chart
				mChart = new BarChart();
				add(mChart);
				draw();
			}
		});
	}

	public StatsByDomainServiceAsync getStatsByDomainSvc() {
		if (mStatsByDomainSvc == null) {
			mStatsByDomainSvc = GWT.create(StatsByDomainService.class);
		}
		return mStatsByDomainSvc;
	}


	private void draw() {
		// Prepare the data
		final DataTable dataTable = DataTable.create();
		dataTable.addColumn(ColumnType.STRING, "Domain");
		dataTable.addColumn(ColumnType.NUMBER, "Count");

		// Set options
		BarChartOptions options = BarChartOptions.create();
		options.setFontName("Tahoma");
		options.setTitle("Download statistics by domain");
		options.setHAxis(HAxis.create("Downloads"));
		options.setVAxis(VAxis.create("Domain"));

		// Draw the chart
		mChart.draw(dataTable, options);
		
		AsyncCallback<StatsByDomain[]> callback = new AsyncCallback<StatsByDomain[]>() {

			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(StatsByDomain[] result) {
				updateTableContent(result, dataTable);
			}
		};
		StatsByDomainServiceAsync srvc = getStatsByDomainSvc();
		srvc.getStats(callback);
	}
	
	private void updateTableContent(StatsByDomain[] stats, DataTable dataTable) {
		System.out.println("StatisticsByDomain.updateTableContent()");
		dataTable.removeRows(0, dataTable.getNumberOfRows());
		dataTable.addRows(stats.length);
		for (int row = 0; row < stats.length; row++) {
			dataTable.setValue(row, 0, stats[row].getDomain());
			dataTable.setValue(row, 1, stats[row].getCount());
		}
	}
}