package hu.evosoft.eo.downloadstats.client;

import static com.google.gwt.i18n.client.DateTimeFormat.getFormat;

import java.util.Date;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.VAxis;

public class StatisticsByTimePanel extends DockLayoutPanel {
	
	//------------------------- constants ------------------------------------------------------------------------------
	private static final String JSON_URL = DownloadStatistics.API_DOMAIN +  "/byTime";
	private static final int REFRESH_INTERVAL = 5000; // ms

	//------------------------- fields ---------------------------------------------------------------------------------
	private ColumnChart chart;
	private Label errorMsgLabel;
	private Label lastUpdatedLabel;
	
	public StatisticsByTimePanel() {
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
				
			    lastUpdatedLabel = new Label();
			    addSouth(lastUpdatedLabel, 24);
			    
				add(getChart());
				// Create the table to store the data
				final DataTable dataTable = DataTable.create();
				dataTable.addColumn(ColumnType.DATETIME, "Time");
				dataTable.addColumn(ColumnType.NUMBER, "Downloads");
				draw(dataTable);
			    // Setup timer to refresh content automatically.
			    Timer refreshTimer = new Timer() {
			      @Override
			      public void run() {
			        loadData(dataTable);
			      }
			    };
			    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
			}
		});
	}

	private ColumnChart getChart() {
		if (chart == null) {
			chart = new ColumnChart();
		}
		return chart;
	}

	private void draw(final DataTable dataTable) {
		// Set chart options
		ColumnChartOptions chartOptions = ColumnChartOptions.create();
		chartOptions.setLegend(Legend.create(LegendPosition.NONE));
		chartOptions.setTitle("Download statistics by time");
		chartOptions.setVAxis(VAxis.create("Downloads"));
		
		loadData(dataTable);
		
		// Draw the chart
		chart.draw(dataTable, chartOptions);
	}

	/** 
	 * Send request to server and catch any errors.
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
		for (int row = 0; row < stats.length(); row++) {
			StatByTimeData statByTimeData = stats.get(row);
			Date date = new Date((long) statByTimeData.getTimeStamp());
			long count = (long) statByTimeData.getCount();
			dataTable.setValue(row, 0, date);
			dataTable.setValue(row, 1, count);
		}
	    // Display timestamp showing last refresh.
	    lastUpdatedLabel.setText("Last update : " + getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()));
	    // Clear any errors.
	    errorMsgLabel.setVisible(false);
	    chart.redraw();
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
}
