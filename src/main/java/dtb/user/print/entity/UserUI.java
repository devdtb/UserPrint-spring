package dtb.user.print.entity;

public class UserUI {
	private UserData userData;
	private UserPeriod userPeriod;
	public UserUI() {
		super();
	}
	public UserUI(UserData userData, UserPeriod userPeriod) {
		super();
		this.userData = userData;
		this.userPeriod = userPeriod;
	}
	public UserData getUserData() {
		return userData;
	}
	public UserPeriod getUserPeriod() {
		return userPeriod;
	}
	public void setUserData(UserData userData) {
		this.userData = userData;
	}
	public void setUserPeriod(UserPeriod userPeriod) {
		this.userPeriod = userPeriod;
	}
	@Override
	public String toString() {
		return "UserUI [userData=" + userData + ", userPeriod=" + userPeriod + "]";
	}
	
}
