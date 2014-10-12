package hu.evosoft.eo.downloadstats.client;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ChartType;
import com.googlecode.gwt.charts.client.ChartWrapper;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.controls.Dashboard;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilter;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterOptions;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterState;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterStateRange;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterUi;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.options.ChartArea;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.VAxis;

public class StatisticsByTimeWithRangePanel extends DockLayoutPanel {
	
	//------------------------- constants ------------------------------------------------------------------------------
	private static final String JSON_URL = "http://alaska.cfapps.io/byTime";

	//------------------------- fields ---------------------------------------------------------------------------------
	private Dashboard dashboard;
	private ChartWrapper<ColumnChartOptions> chart;
	private ChartRangeFilter rangeFilter;
	private Label errorMsgLabel;
	
	public StatisticsByTimeWithRangePanel() {
		super(Unit.PX);
		initialize();
	}

	private void initialize() {
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART, ChartPackage.CONTROLS);
		chartLoader.loadApi(new Runnable() {

			@Override
			public void run() {
				errorMsgLabel = new Label();
			    errorMsgLabel.setStyleName("errorMessage");
			    errorMsgLabel.setVisible(false);
			    addNorth(errorMsgLabel, 24);
				
				addNorth(getDashboardWidget(), 10);
				addSouth(getNumberRangeFilter(), 100);
				add(getChart());
				// Create the table to store the data
				final DataTable dataTable = DataTable.create();
				dataTable.addColumn(ColumnType.DATETIME, "Time");
				dataTable.addColumn(ColumnType.NUMBER, "Downloads");
				draw(dataTable);
			}
		});
	}

	private Dashboard getDashboardWidget() {
		if (dashboard == null) {
			dashboard = new Dashboard();
		}
		return dashboard;
	}

	private ChartWrapper<ColumnChartOptions> getChart() {
		if (chart == null) {
			chart = new ChartWrapper<ColumnChartOptions>();
			chart.setChartType(ChartType.COLUMN);
		}
		return chart;
	}

	private ChartRangeFilter getNumberRangeFilter() {
		if (rangeFilter == null) {
			rangeFilter = new ChartRangeFilter();
		}
		return rangeFilter;
	}

	private void draw(final DataTable dataTable) {
		// Set control options
		ChartArea chartArea = ChartArea.create();
		chartArea.setWidth("90%");
		chartArea.setHeight("90%");
		
		{
			ChartRangeFilterOptions chartRangeFilterOptions = ChartRangeFilterOptions .create();
			chartRangeFilterOptions.setFilterColumnIndex(0); // Filter by the date axis
			{
				ChartRangeFilterUi chartRangeFilterUi = ChartRangeFilterUi.create();
				chartRangeFilterUi.setChartType(ChartType.AREA);
				chartRangeFilterUi.setMinRangeSize(8 * 60 * 60 * 1000); // 8 hours in milliseconds
				{
					ColumnChartOptions controlChartOptions = ColumnChartOptions.create();
					controlChartOptions.setHeight(100);
					controlChartOptions.setChartArea(chartArea);
					chartRangeFilterUi.setChartOptions(controlChartOptions);
				}
				chartRangeFilterOptions.setUi(chartRangeFilterUi);
			}
			rangeFilter.setOptions(chartRangeFilterOptions);
		}
		{
			ChartRangeFilterState controlState = ChartRangeFilterState.create();
			{
				ChartRangeFilterStateRange stateRange = ChartRangeFilterStateRange.create();
				stateRange.setStart(createDate(2014, 7, 20, 0, 0));
				stateRange.setEnd(createDate(2014, 7, 30, 18, 0));
				controlState.setRange(stateRange);
			}
			rangeFilter.setState(controlState);
		}
		// Set chart options
		{
			ColumnChartOptions chartOptions = ColumnChartOptions.create();
			chartOptions.setLegend(Legend.create(LegendPosition.NONE));
			chartOptions.setChartArea(chartArea);
			chartOptions.setTitle("Download statistics by time");
			chartOptions.setVAxis(VAxis.create("Downloads"));
			chart.setOptions(chartOptions);
		}


		// Draw the chart
		dashboard.bind(rangeFilter, chart);
		loadData(dataTable);
	}

	/** Send request to server and catch any errors.
	 * @param dataTable the dataTable which stores the data
	 */
	private void loadData(final DataTable dataTable) {
		String url = URL.encode(JSON_URL);
		JsonpRequestBuilder builder = new JsonpRequestBuilder();
		builder.setCallbackParam("callback");
		builder.requestObject(url, new AsyncCallback<JsArray<StatByTimeData>>() {
			@Override
			public void onFailure(Throwable caught) {
				displayError("Couldn't retrieve JSON: " + caught.getMessage());
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(JsArray<StatByTimeData> result) {
				if (result != null) {
					updateTableContent(result, dataTable);
				} else {
					displayError("Could retrieve JSON, but was null");
				}
			}
		});
	}

	private void updateTableContent(JsArray<StatByTimeData> stats, DataTable dataTable) {
		dataTable.removeRows(0, dataTable.getNumberOfRows());
		dataTable.addRows(stats.length());

		StatByTimeData[] statsArr = new StatByTimeData[stats.length()];
		for (int i = 0; i < stats.length(); i++) {
			statsArr[i] = stats.get(i);
		}
		
		Arrays.sort(statsArr, new StatsByTimeDataComparator());
		int row = 0;
		for (StatByTimeData data : statsArr) {
			Date date = new Date((long) data.getTimeStamp());
			long count = (long) data.getCount();
			dataTable.setValue(row, 0, date);
			dataTable.setValue(row, 1, count);
			row++;
		}
		getDashboardWidget().draw(dataTable);
		getChart().getChart().draw(dataTable);
	    // Clear any errors.
	    errorMsgLabel.setVisible(false);
	}

	@SuppressWarnings("deprecation")
	static Date createDate(int year, int month, int day, int hours, int min){
		Date date = new Date(year - 1900, month - 1, day, hours, min);
		return date;
	}
	
	private void displayError(String error) {
		errorMsgLabel.setText("Error: " + error);
		errorMsgLabel.setVisible(true);
	}
	
	private static class StatsByTimeDataComparator implements Comparator<StatByTimeData> {
		@Override
		public int compare(StatByTimeData o1, StatByTimeData o2) {
			return (int) (o1.getTimeStamp() - o2.getTimeStamp());
		}
	}
		
}