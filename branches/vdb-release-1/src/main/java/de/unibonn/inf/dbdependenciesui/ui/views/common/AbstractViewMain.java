package de.unibonn.inf.dbdependenciesui.ui.views.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public abstract class AbstractViewMain extends JPanel implements Observer {

	private static final long serialVersionUID = -5191433372790634609L;
	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	protected AbstractViewData data;
	protected JPanel graphArea;

	protected abstract AbstractViewData getViewData();

	protected void initialize() {
		data = getViewData();
		data.addObserver(this);
		setLayout(new BorderLayout());

		this.add(getGraphArea(), BorderLayout.CENTER);
	}

	protected JPanel getGraphArea() {
		if (graphArea == null) {
			graphArea = new JPanel();
			graphArea.setLayout(new BorderLayout());
			graphArea.setBackground(Color.white);
		}
		return graphArea;
	}

	@Override
	public void update(final Observable o, final Object arg) {
		ThreadExecutor.execute(new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if ((arg != null) && (arg instanceof AbstractViewData.Notification)) {
					final AbstractViewData.Notification notification = (AbstractViewData.Notification) arg;
					final JPanel area = getGraphArea();
					switch (notification) {
					case NEW_GRAPH:
						area.removeAll();
						final JComponent vv = data.getGraphMainView();
						if (vv instanceof VisualizationViewer) {
							final GraphZoomScrollPane scrollablePane = new GraphZoomScrollPane(
									(VisualizationViewer<DatabaseObject, Relation>) vv);
							area.add(scrollablePane, BorderLayout.CENTER);
						} else {
							area.add(vv, BorderLayout.CENTER);
						}
						area.updateUI();
						log.log(Level.INFO, "New Graph was inserted.");
						break;
					case RESIZE_GRAPH:
						data.getGraphMainView().repaint();
						break;
					}
				}
			}

		});
	}

}
