package com.fision.beans;

public class MyContact {
	private Integer id;
	private String name;
	private String phonenum;
	private String tx;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhonenum() {
		return phonenum;
	}
	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}
	public String getTx() {
		return tx;
	}
	public void setTx(String tx) {
		this.tx = tx;
	}
	public MyContact(Integer id, String name, String phonenum, String tx) {
		super();
		this.id = id;
		this.name = name;
		this.phonenum = phonenum;
		this.tx = tx;
	}
	public MyContact( String name, String phonenum, String tx) {
		super();
		this.name = name;
		this.phonenum = phonenum;
		this.tx = tx;
	}
	public MyContact() {
		super();
	}

	@Override
	public String toString() {
		return "name="+name+" phonenumber="+phonenum+" tx="+tx;
	}
}
