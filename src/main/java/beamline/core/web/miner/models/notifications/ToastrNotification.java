package beamline.core.web.miner.models.notifications;

public class ToastrNotification extends Notification {

	public ToastrNotification(String message) {
		super(Type.TOASTR, message);
	}
}
