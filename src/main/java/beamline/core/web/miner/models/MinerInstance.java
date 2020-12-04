package beamline.core.web.miner.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import beamline.core.miner.AbstractMiner;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class MinerInstance {

	@Getter
	private String id;
	@Getter
	private Miner miner;
	@Getter
	private MinerInstanceConfiguration configuration;
	@Getter @Setter
	@JsonIgnore
	private AbstractMiner minerObject;
	
	public MinerInstance(Miner miner, MinerInstanceConfiguration configuration) {
		this.id = UUID.randomUUID().toString();
		this.miner = miner;
		this.configuration = configuration;
	}
}
