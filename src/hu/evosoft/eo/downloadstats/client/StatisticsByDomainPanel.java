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
import com.googlecode.gwt.charts.client.corechart.BarChart;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.VAxis;

public class StatisticsByDomainPanel extends DockLayoutPanel {
	
	private static final int TOP = 30;
	//------------------------- constants ------------------------------------------------------------------------------
	private static final String JSON_URL = DownloadStatistics.API_DOMAIN +  "/byDomain";
	private static final int REFRESH_INTERVAL = 5000; // ms

	//------------------------- fields ---------------------------------------------------------------------------------
	private BarChart chart;
	private Label errorMsgLabel;
	private Label lastUpdatedLabel;

	public StatisticsByDomainPanel() {
		super(Unit.PX);
		initialize();
	}

	private void initialize() {
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {

			@Override
			public void run() {
				errorMsgLabel = new Label();
				errorMsgLabel.setStyleName("errorMessage");
				errorMsgLabel.setVisible(false);
				addNorth(errorMsgLabel, 24);

			    lastUpdatedLabel = new Label();
			    addSouth(lastUpdatedLabel, 24);
			    
				// Create and attach the chart
				chart = new BarChart();
			    add(chart);
				// Create the table to store the data
				// Prepare the data
				final DataTable dataTable = DataTable.create();
				dataTable.addColumn(ColumnType.STRING, "Domain");
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

	private void draw(final DataTable dataTable) {
		// Set options
		BarChartOptions options = BarChartOptions.create();
		options.setLegend(Legend.create(LegendPosition.NONE));
		options.setTitle("TOP " + TOP  + " Download statistics by domain");
		options.setHAxis(HAxis.create("Downloads"));
		options.setVAxis(VAxis.create("Domains"));
		// Draw the chart
		chart.draw(dataTable, options);

		loadData(dataTable);
	}

	/** 
	 * Send request to server and catch any errors.
	 * @param dataTable the dataTable which stores the data
	 */
	private void loadData(final DataTable dataTable) {
		String url = URL.encode(JSON_URL);
		JsonpRequestBuilder builder = new JsonpRequestBuilder();
		builder.setCallbackParam("callback");
		builder.requestObject(url, new AsyncCallback<JsArray<StatByDomainData>>() {
			@Override
			public void onFailure(Throwable caught) {
				displayError("Couldn't retrieve JSON: " + caught.getMessage());
				System.err.println("StatisticsByDomainPanel Failure" + caught.getMessage());
			}

			@Override
			public void onSuccess(JsArray<StatByDomainData> result) {
				if (result != null) {
					updateTableContent(result, dataTable);
				} else {
					displayError("Could retrieve JSON, but was null");
				}
			}
		});
	}
	private void updateTableContent(JsArray<StatByDomainData> stats, DataTable dataTable) {
		dataTable.removeRows(0, dataTable.getNumberOfRows());
		int length = Math.min(TOP, stats.length());
		dataTable.addRows(length);
		for (int row = 0; row < length; row++) {
			StatByDomainData data = stats.get(row);
			dataTable.setValue(row, 0, data.getDomain());
			dataTable.setValue(row, 1, data.getCount());
		}
	    // Display timestamp showing last refresh.
	    lastUpdatedLabel.setText("Last update : " + getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()));
	    // Clear any errors.
	    errorMsgLabel.setVisible(false);
	    chart.redraw();
	}
	
	private void displayError(String error) {
		errorMsgLabel.setText("Error: " + error);
		errorMsgLabel.setVisible(true);
	}
	
    public static int min(int a, int b) {
        return (a <= b) ? a : b;
    }
}
