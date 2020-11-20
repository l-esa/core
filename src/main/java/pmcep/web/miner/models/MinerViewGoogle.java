package pmcep.web.miner.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import pmcep.web.miner.models.MinerViewGoogle.TYPE;

public class MinerViewGoogle extends MinerView {

	public enum TYPE {
		ScatterChart, AreaChart, BarChart, BubbleChart, ColumnChart, PieChart, Histogram, LineChart, Table
	}
	
	public MinerViewGoogle(String name, List<Object> headers, List<List<Object>> values, Map<String, Object> options, TYPE type) {
		super(name, new GoogleContainer(type, headers, values, options), Type.GOOGLE);
	}
}

class GoogleContainer {
	
	@Getter
	private TYPE type;
	@Getter
	private List<List<Object>> data;
	@Getter
	private Map<String, Object> options;
	
	public GoogleContainer(TYPE type, List<Object> headers, List<List<Object>> values, Map<String, Object> options) {
		this.type = type;
		this.data = new ArrayList<List<Object>>(values);
		this.data.add(0, headers);
		this.options = options;
	}
}
