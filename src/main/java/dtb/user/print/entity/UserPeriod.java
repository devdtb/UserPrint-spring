package dtb.user.print.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

@Entity
public class UserPeriod {
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long userPeriodId;
	private String ctr;
	private String fname;
	private String lname;
	private String cnp;
	private Date startDate;
	private long userDataId;
	
	public long getUserPeriodId() {
		return userPeriodId;
	}
	public String getCtr() {
		return ctr;
	}
	public String getFname() {
		return fname;
	}
	public String getLname() {
		return lname;
	}
	public String getCnp() {
		return cnp;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setUserPeriodId(long userPeriodId) {
		this.userPeriodId = userPeriodId;
	}
	public void setCtr(String ctr) {
		this.ctr = ctr;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	public void setCnp(String cnp) {
		this.cnp = cnp;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public long getUserDataId() {
		return userDataId;
	}
	public void setUserDataId(long userDataId) {
		this.userDataId = userDataId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userPeriodId ^ (userPeriodId >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPeriod other = (UserPeriod) obj;
		if (userPeriodId != other.userPeriodId)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "UserPeriod [userPeriodId=" + userPeriodId + ", ctr=" + ctr + ", fname=" + fname + ", lname=" + lname
				+ ", cnp=" + cnp + ", startDate=" + startDate + ", userDataId=" + userDataId + "]";
	}
}
