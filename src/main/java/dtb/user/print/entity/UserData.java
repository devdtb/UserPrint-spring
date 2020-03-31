package dtb.user.print.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserData {
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	private String ctr;
	private String fname;
	private String lname;
	private String cnp;
	private String address;
	private String idnr;
	public long getId() {
		return id;
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
	public String getAddress() {
		return address;
	}
	public String getIdnr() {
		return idnr;
	}
	public void setId(long id) {
		this.id = id;
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
	public void setAddress(String address) {
		this.address = address;
	}
	public void setIdnr(String idnr) {
		this.idnr = idnr;
	}
	@Override
	public String toString() {
		return "UserData [id=" + id + ", ctr=" + ctr + ", fname=" + fname + ", lname=" + lname + ", cnp=" + cnp
				+ ", address=" + address + ", idnr=" + idnr + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		UserData other = (UserData) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	

}
