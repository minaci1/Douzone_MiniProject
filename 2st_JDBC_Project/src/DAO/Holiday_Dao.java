package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import DTO.Eapply;
import DTO.Emp;
import DTO.HolidayList;
import DTO.Restday_Holiday;
import oracle.net.aso.s;
import utils.SingletonHelper;
 
public class Holiday_Dao {
	
	//1. 휴가신청 내역 조회
	public List<HolidayList> getList(){
		List<HolidayList> holiydaylist = new ArrayList<>();
		 
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql= "select b.applyno"
					+ ",a.empno"
					+ ",a.ename"
					+ ",b.start_date"
					+ ",b.end_date"
					+ ",c.hname"
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
				hl.setSinfo(rs.getString("sinfo"));
				hl.setRestday(rs.getInt("restday"));
			 
				holiydaylist.add(hl);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		return holiydaylist;
		
	}
	
	
	
	//2. 조건조회 (경기도 주소인 사람 조회)
	public List<Emp> getSelectLike(String addr) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Emp> empList = new ArrayList<Emp>();
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select empno"
					+ ", rankno"
					+ ",deptno"
					+ ",mgr"
					+ ",ename"
					+ ",id_number"
					+ ",age"
					+ ",tel"
					+ ",hiredate"
					+ ",email "
					+ "from emp "
					+ "where addr like ?";
			pstmt = conn.prepareStatement(sql);
			String search = "%"+addr+"%";
			pstmt.setString(1, search);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				do {
					Emp edto = new Emp();
					edto.setEmpno(rs.getInt(1));
					edto.setRankno(rs.getInt(2));
					edto.setDeptno(rs.getInt(3));
					edto.setMgr(rs.getInt(4));
					edto.setEname(rs.getString(5));
					edto.setId_number(rs.getString(6));
					edto.setAge(rs.getInt(7));
					edto.setTel(rs.getString(8));
					edto.setHiredate(rs.getDate(9));
					edto.setEmail(rs.getString(10));
					
					
					empList.add(edto);
				}while(rs.next());
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}		
		return empList;
	}
	
	//3. 데이터 삽입(휴가신청) 신청번호 시퀀스
		public int holiydayInsert(Eapply eapply) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			int row = 0;
			
			try {
				conn = SingletonHelper.getConnection("oracle");
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
				SingletonHelper.close(pstmt);
			}		
			return row;
		}
		
	//4. 본인 잔여휴가 일수 조회
	public Restday_Holiday getday(int empno) {
		
		Restday_Holiday rh = new Restday_Holiday();
		Connection conn =null;
		PreparedStatement pstmt =null;
		ResultSet rs =null;
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select a.ename"
					+ ",b.restday "
					+ "from emp a join rest_holiday b "
					+ "on(a.empno = b.empno) "
					+ "where a.empno = ?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, empno);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				rh.setEname(rs.getString(1));
				rh.setRestday(rs.getInt(2));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		return rh;
	}
 
	//5. 휴가 상태 변경 (대기 -> 승인)
	public int changeStateno(int applyno) {
		
		Eapply ep = new Eapply();
		Connection conn =null;
		PreparedStatement pstmt =null;
		int rowcnt =0;
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql ="update eapply "
					+ "set stateno = 1 "
					+ "where applyno =?";// 0 : 대기 1 승인 2 반려 중 0 대기를 승인으로 바꾸기  
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, applyno);
			rowcnt = pstmt.executeUpdate();
	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return rowcnt;
	}
	
	//승인된 내역 조회하기 ,차감할 신청번호 내역 조회하기 
	public HolidayList getSignList(int applyno) {
		
		HolidayList hl = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		 
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select b.applyno"
					+ ",a.empno"
					+ ",a.ename"
					+ ",b.start_date"
					+ ",b.end_date"
					+ ",c.hname"
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
					hl.setSinfo(rs.getString("sinfo"));
					hl.setRestday(rs.getInt("restday"));
				  
				}while(rs.next());
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}		
		return hl;
	}
	
	
	// 휴가신청목록리스트 
	public List<Eapply> vacationList() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Eapply> eapplyList = new ArrayList<Eapply>();
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select applyno"
					+ ",empno"
					+ ",holidayno"
					+ ",stateno"
					+ ",start_date"
					+ ",end_date"
					+ ",reason "
					+ "from eapply";
			pstmt = conn.prepareStatement(sql);	
			rs = pstmt.executeQuery();
			while(rs.next()) {
				Eapply apdto = new Eapply();
				apdto.setApplyno(rs.getInt(1));
				apdto.setEmpno(rs.getInt(2));
				apdto.setHolidayno(rs.getInt(3));
				apdto.setStateno(rs.getInt(4));
				apdto.setStart_date(rs.getDate(5));
				apdto.setEnd_date(rs.getDate(6));
				apdto.setReason(rs.getString(7));
				eapplyList.add(apdto);
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}		
		return eapplyList; 
	}
	
	
	
	// 휴가일수 얻기 -> 사용
	public int getVacationDay(int applyno, Date start_date, Date end_date) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int dateCal = 0;
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select ?-? " //종료일 - 시작일 연산
					+ "from eapply "
					+ "where applyno = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1, end_date);
			pstmt.setDate(2, start_date);
			pstmt.setInt(3, applyno);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				dateCal = rs.getInt(1);
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		return dateCal;
	}
	
	//휴가 잔여일수 - 휴가 신청일수 업데이트
	public void  restdayupdate(int day,int empno) {
		Connection conn = null;
		PreparedStatement pstmt = null;
	
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "update rest_holiday "
					+ "set restday =  restday- ?"
					+ "where empno =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, day);
			pstmt.setInt(2, empno);
			pstmt.executeUpdate();
			 
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(pstmt);
		}
	}
	
	//차감반영된 휴가목록 확인하기 
	public int selectminusday(int empno) { 

		Connection conn =null;
		PreparedStatement pstmt =null;
		ResultSet rs =null;
		int restday =0;
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select restday"
					+ " from rest_holiday"
					+ " where empno = ?";
			
			pstmt = conn.prepareStatement(sql);	
			pstmt.setInt(1, empno);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				restday = rs.getInt(1);
				
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}		
		return restday; 
	}
	
	// 7. 반려리스트 조회
	public List<HolidayList> RejectList() {
		
		List<HolidayList> rejectlist  = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
	
		try {
			conn = SingletonHelper.getConnection("oracle");
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
			SingletonHelper.close(pstmt);
			SingletonHelper.close(rs);
		}
		 return rejectlist;
	}
	
	//8. 휴가신청번호 받아와서 반려내역 삭제 
	public int holidayDelete(int applyno) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		int rowcnt =0;
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "delete from eapply "
					+ "where applyno =? and stateno = 2";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, applyno);
			rowcnt = pstmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(pstmt);
		}		
		return rowcnt;
	}

}
