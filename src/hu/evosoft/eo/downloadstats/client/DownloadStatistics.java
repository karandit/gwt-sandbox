package hu.evosoft.eo.downloadstats.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class DownloadStatistics implements EntryPoint {

	private static class Tuple<T1, T2> {
		public final T1 first;
		public final T2 second;

		public Tuple(T1 t1, T2 t2) {
			first = t1;
			second = t2;
		}
	}
	
	private static class DashboardBuilder {
		private List<NavBarBuilder> navbars = new LinkedList<>();
		
		public NavBarBuilder addNavBar(String label) {
			return new NavBarBuilder(this, label);
		}
		
		public DockLayoutPanel buildDashboard() {
			StackLayoutPanel navPanel = new StackLayoutPanel(Unit.PX);
			final DeckLayoutPanel contentPanel = new DeckLayoutPanel();
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					PushButton pb = (PushButton) event.getSource();
					contentPanel.showWidget((Widget) pb.getLayoutData());
				}
			};
			for (NavBarBuilder navbar : navbars) {
				FlowPanel navFlowPanel = new FlowPanel();
				for (Tuple<String, Widget> item : navbar.items) {
					Widget pushButton = new PushButton(item.first, clickHandler);
					pushButton.setLayoutData(item.second);
					navFlowPanel.add(pushButton);
					contentPanel.add(item.second);
				}
				navPanel.add(navFlowPanel, new HTML(navbar.navLabel), 24);
			}
			
			DockLayoutPanel dashboard = new SplitLayoutPanel();
			dashboard.addWest(navPanel, 140);
			dashboard.add(contentPanel);
			return dashboard;
		}
	}
	
	private static class NavBarBuilder {
		
		private DashboardBuilder mParent;
		final String navLabel;
		final List<Tuple<String, Widget>> items = new LinkedList<>();
		
		public NavBarBuilder(DashboardBuilder parent, String navLabel) {
			this.mParent = parent;
			this.navLabel = navLabel;
		}

		public NavBarBuilder addItem(String itemLabel, Widget widget) {
			items.add(new Tuple<String, Widget>(itemLabel, widget));
			return this;
		}
		
		public DashboardBuilder end() {
			mParent.navbars.add(this);
			return mParent;
		}
	}
	
	public void onModuleLoad() {
		DockLayoutPanel dashboard = new DashboardBuilder()
		.addNavBar("Statistics")
			.addItem("By domain", new StatisticsByDomain())
			.addItem("By time", new StatisticsByTime())
		.end()
		.addNavBar("Configuration")
			.addItem("Users", new HTML("User Management"))
			.addItem("Roles", new HTML("Role Management"))
		.end()
		.buildDashboard();
		RootLayoutPanel.get().add(dashboard);
	}
}
