package DAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import DTO.Eapply;
import DTO.HolidayList;
import utils.ConnHelper;
 
public class Holiday_Dao {
	
	//1. 휴가신청 내역 조회(전체)
	public List<HolidayList> getList(){
		List<HolidayList> holiydaylist = new ArrayList<>();
		 
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		
		try {
			conn = ConnHelper.getConnection();
			String sql= "select b.applyno"
					+ ",a.empno"
					+ ",a.ename"
					+ ",b.start_date"
					+ ",b.end_date"
					+ ",c.hname"
					+ ",b.stateno"
					+ ",d.sinfo"
					+ ",e.restday"
					+ " from emp a join eapply b "
					+ "on(a.empno = b.empno) "
					+ "join holiday c "
					+ "on(b.holidayno = c.holidayno) "
					+ "join estate d "
					+ "on(b.stateno = d.stateno) "
					+ "join rest_holiday e "
					+ "on(b.empno = e.empno)"
					+ "order by applyno asc";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				HolidayList hl =new HolidayList();
				hl.setApplyno(rs.getInt("applyno"));
				hl.setEmpno(rs.getInt("empno"));
				hl.setEname(rs.getString("ename"));
				hl.setStart_date(rs.getDate("start_date"));
				hl.setEnd_date(rs.getDate("end_date"));
				hl.setHname(rs.getString("hname"));
				hl.setStateno(rs.getInt("stateno"));
				hl.setSinfo(rs.getString("sinfo"));
				hl.setRestday(rs.getInt("restday"));
			 
				holiydaylist.add(hl);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(rs);
			ConnHelper.close(pstmt);
		}
		return holiydaylist;
		
	}
 
	// 휴가 잔여 일수 얻기
	public int getRestDay(int empno) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int dateCal = 0;
		
		try {
			conn = ConnHelper.getConnection();
			String sql = "select restday from rest_holiday where empno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, empno);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				dateCal = rs.getInt(1);
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(rs);
			ConnHelper.close(pstmt);
		}
		return dateCal;
	}
	
	// 2.데이터 추가(휴가신청) 신청번호 시퀀스
		public int holiydayInsert(Eapply eapply) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			int row = 0;
			
			try {
				conn = ConnHelper.getConnection();
				String sql = "insert into "
						+ "eapply(applyno, empno, holidayno, stateno, start_date,end_date,reason) "
						+ "values(eapply_num.nextval, ?, ?, 0, ?, ?, ?)";
						
				pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, eapply.getEmpno());
				pstmt.setInt(2, eapply.getHolidayno());
				pstmt.setDate(3, eapply.getStart_date());
				pstmt.setDate(4, eapply.getEnd_date());
				pstmt.setString(5, eapply.getReason());
				
				row = pstmt.executeUpdate();
				
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}finally {
				ConnHelper.close(pstmt);
			}		
			return row;
		}
 
	// 3. 휴가 상태 변경 (대기 -> 승인 / 대기->반려)
	public void changeStateno(int applyno, int stateno) {
		
		Eapply ep = new Eapply();
		Connection conn =null;
		PreparedStatement pstmt =null;
		int rowcnt =0;
		
		try {
			conn = ConnHelper.getConnection();
			String sql ="update eapply "
					+ "set stateno = ? "
					+ "where applyno =?";// 0 : 대기 1 승인 2 반려 중 0 대기를 승인으로 바꾸기  
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, stateno);
			pstmt.setInt(2, applyno);
			rowcnt = pstmt.executeUpdate();
			
			if(rowcnt > 0) {
				System.out.println("update row count : "+ rowcnt);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	//3-1. 휴가일수 차감할 신청번호 내역 조회 
	public HolidayList getSignList(int applyno) {
		
		HolidayList hl = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		 
		
		try {
			conn = ConnHelper.getConnection();
			String sql = "select b.applyno"
					+ ",a.empno"
					+ ",a.ename"
					+ ",b.start_date"
					+ ",b.end_date"
					+ ",c.hname"
					+",d.stateno"
					+ ",d.sinfo"
					+ ",e.restday"
					+ " from emp a join eapply b "
					+ "on(a.empno = b.empno) "
					+ "join holiday c "
					+ "on(b.holidayno = c.holidayno) "
					+ "join estate d "
					+ "on(b.stateno = d.stateno) "
					+ "join rest_holiday e "
					+ "on(b.empno = e.empno)"
					+ " where b.applyno =?"
					+ "order by b.applyno asc";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, applyno);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				do {
					hl= new HolidayList();
					hl.setApplyno(rs.getInt("applyno"));
					hl.setEmpno(rs.getInt("empno"));
					hl.setEname(rs.getString("ename"));
					hl.setStart_date(rs.getDate("start_date"));
					hl.setEnd_date(rs.getDate("end_date"));
					hl.setHname(rs.getString("hname"));
					hl.setStateno(rs.getInt("stateno"));
					hl.setSinfo(rs.getString("sinfo"));
					hl.setRestday(rs.getInt("restday"));
				  
				}while(rs.next());
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(rs);
			ConnHelper.close(pstmt);
		}		
		return hl;
	}
 
	
	
	// 3-2. 휴가일수 얻기 -> 사용
	public int getVacationDay(int empno,  Date end_date, Date start_date) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int dateCal = 0;
		
		try {
			conn = ConnHelper.getConnection();
			String sql = "select ?-? " //종료일 - 시작일 연산
					+ "from eapply "
					+ "where empno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1, end_date);
			pstmt.setDate(2, start_date);
			pstmt.setInt(3, empno);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				dateCal = rs.getInt(1); //select 해서 나온 값 전달 
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(rs);
			ConnHelper.close(pstmt);
		}
		return dateCal;
	}
	
 
	
	
	//2-1, 3-2. 휴가 잔여일수 - 휴가 신청일수 업데이트
	public void  restdayupdate(int day,int empno) {
		Connection conn = null;
		PreparedStatement pstmt = null;
	
		try {
			conn = ConnHelper.getConnection();
			String sql = "update rest_holiday "
					+ "set restday =  restday+ ?"
					+ "where empno =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, day);
			pstmt.setInt(2, empno);
			pstmt.executeUpdate();
			 
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(pstmt);
		}
	}
 
	// 4. 반려리스트 조회
	public List<HolidayList> RejectList() {
		
		List<HolidayList> rejectlist  = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
	
		try {
			conn = ConnHelper.getConnection();
			String sql= "select b.applyno "
					+ ",a.empno"
					+ ",a.ename"
					+ ",b.start_date"
					+ ",b.end_date"
					+ ",c.hname"
					+ ",d.sinfo"
					+ ",e.restday "
					+ "from emp a join eapply b "
					+ "on(a.empno = b.empno) "
					+ "join holiday c "
					+ "on(b.holidayno = c.holidayno) "
					+ "join estate d "
					+ "on(b.stateno = d.stateno) "
					+ "join rest_holiday e "
					+ "on(b.empno = e.empno) "
					+ "where d.sinfo ='반려'";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				HolidayList hl =new HolidayList();
				
				hl.setApplyno(rs.getInt("applyno"));
				hl.setEmpno(rs.getInt("empno"));
				hl.setEname(rs.getString("ename"));
				hl.setStart_date(rs.getDate("start_date"));
				hl.setEnd_date(rs.getDate("end_date"));
				hl.setHname(rs.getString("hname"));
				hl.setSinfo(rs.getString("sinfo"));
				hl.setRestday(rs.getInt("restday"));
			 
				rejectlist.add(hl);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(pstmt);
			ConnHelper.close(rs);
		}
		 return rejectlist;
	}
	
	//4.  반려내역 삭제 
	public int holidayDelete(int applyno) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		int rowcnt =0;
		
		try {
			conn = ConnHelper.getConnection();
			String sql = "delete from eapply "
					+ "where applyno =? and stateno = 2";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, applyno);
			rowcnt = pstmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			ConnHelper.close(pstmt);
		}		
		return rowcnt;
	}

}