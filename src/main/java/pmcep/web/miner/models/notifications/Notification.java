package pmcep.web.miner.models.notifications;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Notification {

	public enum Type {
		TOASTR, REFRESH;
	}
	
	@Getter
	private Type type;
	@Getter
	private String text;
	
	public Notification(Type type, String text) {
		this.type = type;
		this.text = text;
	}
}
