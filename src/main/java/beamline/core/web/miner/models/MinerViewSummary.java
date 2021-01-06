package beamline.core.web.miner.models;

import java.text.DateFormat;

public class MinerViewSummary extends MinerViewRaw {

	private MinerInstance mi;
	
	public MinerViewSummary(MinerInstance mi) {
		super("Instance summary", "");
		
		this.mi = mi;
		
		configure();
	}

	private void configure() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<h4>Status summary</h4>");
		sb.append("<table class=\"table table-bordered table-hover\">");
		sb.append(getTableRow("Instance start date", DateFormat.getDateTimeInstance().format(mi.getMinerObject().getStartDate())));
		sb.append(getTableRow("Observed events", String.valueOf(mi.getMinerObject().getEvents())));
		sb.append("</table>");
		
		sb.append("<h4>Miner instance configuration</h4>");
		sb.append("<table class=\"table table-bordered table-hover\">");
		for(MinerParameterValue para : mi.getConfiguration().getParameterValues()) {
			sb.append(getTableRow(para.getName(), para.getValue().toString()));
		}
		sb.append("</table>");
		
		this.value = sb.toString();
	}
	
	private static String getTableRow(String head, String value) {
		return "<tr><th>" + head + "</th><td>" + value + "</td></tr>";
	}
}
