package beamline.core.web.miner.models;

import lombok.Getter;

public class MinerParameter {

	public enum Type {
		STRING, INTEGER, DOUBLE, RANGE_0_1, FILE;
	}
	
	@Getter
	private String name;
	@Getter
	private Type type;
	
	public MinerParameter(String name, Type type) {
		this.name = name;
		this.type = type;
	}
}