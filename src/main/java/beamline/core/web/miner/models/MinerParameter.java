package beamline.core.web.miner.models;

import lombok.Getter;

public class MinerParameter {

	public enum Type {
		STRING, INTEGER, DOUBLE, RANGE_0_1, FILE, CHOICE;
	}
	
	@Getter
	private String name;
	@Getter
	private Type type;
	@Getter
	private String defaultValue;
	
	public MinerParameter(String name, Type type, String defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}
}
