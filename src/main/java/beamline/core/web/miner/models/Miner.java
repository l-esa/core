package beamline.core.web.miner.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import beamline.core.miner.AbstractMiner;
import beamline.core.web.annotations.ExposedMiner;
import beamline.core.web.annotations.ExposedMinerParameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Miner {

	@Getter
	private String id;
	@Getter
	@JsonIgnore
	private Class<AbstractMiner> minerClass;
	@Getter
	private String name;
	@Getter
	private String description;
	@Getter
	@EqualsAndHashCode.Exclude
	private Set<MinerParameter> configurationParameters;
	@Getter
	@EqualsAndHashCode.Exclude
	private Set<MinerParameter> viewParameters;
	
	public Miner(ExposedMiner annotation, Class<AbstractMiner> clazz) {
		this.id = UUID.randomUUID().toString();
		this.minerClass = clazz;
		this.name = annotation.name();
		this.description = annotation.description();
		this.configurationParameters = new HashSet<MinerParameter>();
		this.viewParameters = new HashSet<MinerParameter>();
		
		for (ExposedMinerParameter p : annotation.configurationParameters()) {
			configurationParameters.add(new MinerParameter(p.name(), p.type()));
		}
		
		for (ExposedMinerParameter p : annotation.viewParameters()) {
			viewParameters.add(new MinerParameter(p.name(), p.type()));
		}
	}
}
