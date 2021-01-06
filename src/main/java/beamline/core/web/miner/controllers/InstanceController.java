package beamline.core.web.miner.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import beamline.core.logger.Logger;
import beamline.core.miner.AbstractMiner;
import beamline.core.miner.exceptions.MinerException;
import beamline.core.web.miner.models.Miner;
import beamline.core.web.miner.models.MinerInstance;
import beamline.core.web.miner.models.MinerInstanceConfiguration;
import beamline.core.web.miner.models.MinerInstanceStatus;
import beamline.core.web.miner.models.MinerParameter;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.MinerViewSummary;
import beamline.core.web.miner.models.MinerParameter.Type;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin
public class InstanceController {

	@Autowired
	private MinerController minerController;
	@Autowired
	private UploadController uploadController;
	@Autowired
	private NotificationController notificationController;
	private Map<String, MinerInstance> instances = new HashMap<String, MinerInstance>();
	private Map<String, MinerInstanceStatus> instancesStatus = new HashMap<String, MinerInstanceStatus>();
	
	@GetMapping(
		value = "/instances",
		produces = { "application/json" })
	public ResponseEntity<Collection<MinerInstance>> getInstances() {
		return new ResponseEntity<Collection<MinerInstance>>(instances.values(), HttpStatus.OK);
	}
	
	@PostMapping("/instances/{minerId}")
	public ResponseEntity<MinerInstance> createInstance(@PathVariable("minerId") String minerId, @RequestBody MinerInstanceConfiguration configuration) {
		if (!minerController.minerExists(minerId)) {
			return ResponseEntity.notFound().build();
		}
		// create an instance of the miner
		MinerInstance mi = null;
		try {
			Miner miner = minerController.getById(minerId);
			Class<AbstractMiner> clazz = miner.getMinerClass();
			
			Collection<MinerParameterValue> parameterValues = configuration.getParameterValues();
			Collection<MinerParameterValue> parameterValuesTyped = new ArrayList<MinerParameterValue>(parameterValues.size());
			for (MinerParameterValue v : parameterValues) {
				MinerParameterValue vNew = new MinerParameterValue(v.getName(), v.getValue());
				for (MinerParameter p : minerController.getById(minerId).getConfigurationParameters()) {
					if (p.getName().equals(vNew.getName())) {
						vNew.setType(p.getType());
						if (p.getType().equals(Type.FILE)) {
							vNew.setValue(uploadController.get((String) vNew.getValue()));
						}
					}
				}
				parameterValuesTyped.add(vNew);
			}
			
			AbstractMiner minerObject = clazz.getDeclaredConstructor().newInstance();
			minerObject.setNotificationController(notificationController);
			minerObject.setStream(configuration.getStream());
			
			mi = new MinerInstance(miner, configuration);
			mi.setMinerObject(minerObject);
			
			minerObject.setInstance(mi);
			
			instances.put(mi.getId(), mi);
			instancesStatus.put(mi.getId(), MinerInstanceStatus.CONFIGURING);
			
			configureInstance(mi.getId(), minerObject, parameterValuesTyped);
			
		} catch (Exception e) {
			Logger.instance().error(e);
		}
		
		return new ResponseEntity<MinerInstance>(mi, HttpStatus.OK);
	}
	
	@GetMapping("/instances/{instanceId}/start")
	public ResponseEntity<Boolean> instanceStart(@PathVariable("instanceId") String instanceId) {
		if (!instances.containsKey(instanceId)) {
			return ResponseEntity.notFound().build();
		}
		try {
			instances.get(instanceId).getMinerObject().start();
			instancesStatus.put(instanceId, MinerInstanceStatus.MINING);
		} catch (MinerException e) {
			Logger.instance().error(e);
			return new ResponseEntity<Boolean>(false, HttpStatus.OK);
		}
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@DeleteMapping("/instances/{instanceId}/delete")
	public ResponseEntity<Void> instanceDelete(@PathVariable("instanceId") String instanceId) {
		if (!instances.containsKey(instanceId)) {
			return ResponseEntity.notFound().build();
		}
		try{
			instances.remove(instanceId);
			instancesStatus.remove(instanceId);
		}catch (Exception e) {
			Logger.instance().error(e);
		}
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/instances/{instanceId}/stop")
	public ResponseEntity<Boolean> instanceStop(@PathVariable("instanceId") String instanceId) {
		if (!instances.containsKey(instanceId)) {
			return ResponseEntity.notFound().build();
		}
		
		try {
			instances.get(instanceId).getMinerObject().stop();
			instancesStatus.put(instanceId, MinerInstanceStatus.NOT_MINING);
		} catch (MinerException e) {
			Logger.instance().error(e);
			return new ResponseEntity<Boolean>(false, HttpStatus.OK);
		}
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@GetMapping("/instances/{instanceId}/status")
	public ResponseEntity<MinerInstanceStatus> instanceStatus(@PathVariable("instanceId") String instanceId) {
		if (!instances.containsKey(instanceId)) {
			return ResponseEntity.notFound().build();
		}
		
		return new ResponseEntity<MinerInstanceStatus>(instancesStatus.get(instanceId), HttpStatus.OK);
	}
	
	@PostMapping(
		value = "/instances/{instanceId}/views",
		produces = { "application/json" })
	public ResponseEntity<Collection<MinerView>> instanceView(@PathVariable("instanceId") String instanceId, @RequestBody List<MinerParameterValue> configuration) {
		if (!instances.containsKey(instanceId)) {
			return ResponseEntity.notFound().build();
		}
		
		MinerInstance mi = instances.get(instanceId);
		List<MinerView> views = mi.getMinerObject().getViews(configuration);
		views.add(0, new MinerViewSummary(mi));
		return new ResponseEntity<Collection<MinerView>>(views, HttpStatus.OK);
	}

	private void configureInstance(String instanceId, AbstractMiner minerObject, Collection<MinerParameterValue> parameterValuesTyped) {
		new Thread() {
			@Override
			public void run() {
				minerObject.configure(parameterValuesTyped);
				instancesStatus.put(instanceId, MinerInstanceStatus.NOT_MINING);
			}
		}.start();
	}
}
