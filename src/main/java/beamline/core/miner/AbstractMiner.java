package beamline.core.miner;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import beamline.core.miner.exceptions.MinerException;
import beamline.core.web.annotations.ExposedMiner;
import beamline.core.web.annotations.ExposedMinerParameter;
import beamline.core.web.miner.controllers.NotificationController;
import beamline.core.web.miner.models.MinerInstance;
import beamline.core.web.miner.models.MinerParameter;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.Stream;
import beamline.core.web.miner.models.notifications.Notification;
import lombok.Getter;
import lombok.Setter;
import mqttxes.lib.XesMqttConsumer;
import mqttxes.lib.XesMqttEvent;
import mqttxes.lib.XesMqttEventCallback;

public abstract class AbstractMiner {

	private boolean running = false;
	private boolean configured = true;
	@Getter @Setter
	private Stream stream = null;
	@Getter @Setter
	private MinerInstance instance = null;
	private XesMqttConsumer client;
	@Setter
	private NotificationController notificationController;
	
	public abstract void configure(Collection<MinerParameterValue> collection);
	
	public abstract void consumeEvent(String caseID, String activityName);
	
	public abstract List<MinerView> getViews(Collection<MinerParameterValue> collection);
	
	public void notifyToClients(Notification notification) {
		notificationController.notifyToClient(getInstance(), notification);
	}

	public Collection<MinerParameter> getConfigurationParameters() {
		ExposedMiner annotation = this.getClass().getAnnotation(ExposedMiner.class);
		HashSet<MinerParameter> params = new HashSet<MinerParameter>();
		for (ExposedMinerParameter p : annotation.configurationParameters()) {
			params.add(new MinerParameter(p.name(), p.type(), p.defaultValue()));
		}
		return params;
	}
	
	public Collection<MinerParameter> getViewParameters() {
		ExposedMiner annotation = this.getClass().getAnnotation(ExposedMiner.class);
		HashSet<MinerParameter> params = new HashSet<MinerParameter>();
		for (ExposedMinerParameter p : annotation.viewParameters()) {
			params.add(new MinerParameter(p.name(), p.type(), p.defaultValue()));
		}
		return params;
	}
	
	public void start() throws MinerException {
		if (running) {
			throw new MinerException("Miner instance already running");
		}
		if (stream == null || !configured) {
			throw new MinerException("Miner instance not yet configured");
		}
		this.client = new XesMqttConsumer(stream.getBrokerHost(), stream.getTopicBase());
		client.subscribe(stream.getProcessName(), new XesMqttEventCallback() {
			@Override
			public void accept(XesMqttEvent e) {
				consumeEvent(e.getCaseId(), e.getActivityName());
			}
		});

		client.connect();
		running = true;
	}
	
	public void stop() throws MinerException {
		if (!running) {
			throw new MinerException("Miner instance not running");
		}
		client.disconnect();
		running = false;
	}
	
	public boolean isRunnning() {
		return running;
	}
}