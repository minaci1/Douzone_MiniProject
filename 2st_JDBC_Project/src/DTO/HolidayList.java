package DTO;

import java.sql.Date;

import lombok.Data;

@Data
public class HolidayList {
	private int applyno;
	private int empno;
	private String ename;
	private Date Start_date;
	private Date End_date;
	private String hname;
	private int stateno;
	private String sinfo;
	private int restday;
	
	
}
