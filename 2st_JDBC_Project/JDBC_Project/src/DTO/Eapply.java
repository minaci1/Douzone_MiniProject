package DTO;

import java.sql.Date;

import lombok.Data;

@Data
public class Eapply {
	private int applyno;
	private int empno;
	private int holidayno;
	private int stateno;
	private Date start_date;
	private Date end_date;
	private String reason;
 
}
