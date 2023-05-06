import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import DAO.Holiday_Dao;
import DTO.Eapply;
import DTO.Emp;
import DTO.HolidayList;
import DTO.Restday_Holiday;

public class Program_holiday {

	public static void main(String[] args) {
		Holiday_Dao hd = new Holiday_Dao();
		 
		
		Scanner sc = new Scanner(System.in);
		int applyno =0;
		
		try {
			System.out.println("1. [전제조회]");

			List<HolidayList> hlist = hd.getList();

			if (hlist != null) {
				ListPrint(hlist);
			}
			System.out.println();
			System.out.println();
			System.out.println("2. [경기도주민 조회]");
			List<Emp> emplist = hd.getSelectLike("경기도");
			if (emplist != null) {
				empPrint(emplist);
			}
			System.out.println();
			System.out.println("3. [휴가신청목록 추가]");
		
			Eapply insertapply = new Eapply();

			insertapply.setEmpno(3001);
			insertapply.setHolidayno(2);
			insertapply.setStart_date(Date.valueOf("2023-07-10"));
			insertapply.setEnd_date(Date.valueOf("2023-07-12"));
			insertapply.setReason("배가 고픕니다.");

			hd.holiydayInsert(insertapply);

			hlist = hd.getList(); // 다시 불러와야 갱신된 내역 확인할 수 있음 

			if (hlist != null) {
				ListPrint(hlist);
			}
			System.out.println();
			System.out.println("4. [본인 잔여휴가 일수 조회]");
			Restday_Holiday rh = hd.getday(1002);

			if (rh != null) {
				restdayPrint(rh);
			} else {
				System.out.println("본인 잔여휴가 일수 조회 실패");
			}
			System.out.println();
			System.out.println("5. [대기상태 ->승인상태 변경]");
			
			System.out.print("승인할 신청번호를 입력해주세요 : ");
			applyno = Integer.parseInt(sc.nextLine()); // 상태 변경하고자 하는 휴가신청번호 받아오기
			int updaterow = hd.changeStateno(applyno);
			if (updaterow > 0) {
				System.out.println(updaterow +"건 승인이 완료되었습니다.");
				System.out.println();
				System.out.println("[반영된 내역 확인]");
				
				HolidayList hl = hd.getSignList(applyno);
				if (hl != null) {
					ListPrint(hl);
				}
			} else {
				System.out.println("UPDATE FAIL");
			}
			System.out.println();
			System.out.println("6. [ 잔여휴가일수에서 휴가일수 차감 ]");
			System.out.print("차감할 신청번호를 입력해주세요 : ");
			applyno = Integer.parseInt(sc.nextLine()); // 상태 변경하고자 하는 휴가신청번호 받아오기
			
			//휴가일수 차감할 객체 1개의 정보
			HolidayList hl = hd.getSignList(applyno); 
			//휴가 종료일- 휴가 시작일 = 휴가 신청일수
			int day=hd.getVacationDay(applyno,hl.getStart_date(),hl.getEnd_date()); 
			//휴가 잔여일수 - 휴가 신청일수 반영
			hd.restdayupdate(day,hl.getEmpno());
			//잔여 휴가일수 조회 
			int restday = hd.selectminusday(hl.getEmpno()); 
			
			System.out.println("사번번호 : "+hl.getEmpno()+" 이름 :"+hl.getEname()+"님의 잔여휴가일수는 "
					+ ""+restday +""+"일 남았습니다.");
			
			System.out.println();
			System.out.println("[반영된 내역 확인]");
			HolidayList hl1 = hd.getSignList(hl.getApplyno());
			if (hl1 != null) {
				ListPrint(hl1);
			}
			System.out.println();
			System.out.println("7. [반려 리스트 확인]");
			
			List<HolidayList> rejectlist = hd.RejectList();

			if (rejectlist != null) {
				ListPrint(rejectlist);
			}else {
				System.out.println("반려 내역이 존재하지 않습니다.");
				return;
			}
			 System.out.println();
			 System.out.println("8. [반려 리스트 삭제]");
			 System.out.print("삭제할 휴가신청번호를 입력해주세요 : ");
			 applyno = Integer.parseInt(sc.nextLine());
			 
			int deleterow = hd.holidayDelete(applyno);

			if (deleterow > 0) {
				System.out.println(deleterow+"건 삭제되었습니다. ");
			} else {
				System.out.println("DELETE FAIL");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println();
		System.out.println("[반영된 내역 확인]");
		//반영된 내역 확인
		List<HolidayList> rejectlist = hd.RejectList();
		
		if (rejectlist != null) {
			ListPrint(rejectlist);
		}else {
			System.out.println("반려된 내역이 존재하지 않습니다.");
		}
		
	}
	
	//사원정보 조회
	public static void empPrint(Emp emplist) { 
		System.out.println(emplist.toString());
	}

	// 휴가 잔여일 수 확인
	public static void restdayPrint(Restday_Holiday rh) {
		System.out.println(rh.toString());
	}

	// 휴가신청확인
	public static void eapplyPrint(Eapply ep) {
		System.out.println(ep.toString());
	}
	
	//휴가신청목록 1건 조회
	public static void ListPrint(HolidayList holi) {
		System.out.println(holi.toString());
	}

	// 휴가신청목록 전체목록조회
	public static void ListPrint(List<HolidayList> list) {
		for (HolidayList data : list) {
			System.out.println(data.toString());
		}
	}

	// 휴가신청 목록조회
	public static void EapplyPrint(List<Eapply> list) {
		for (Eapply data : list) {
			System.out.println(data.toString());
		}
	}
	//사원 정보 조회
	public static void empPrint(List<Emp> list) {
		for (Emp data : list) {
			System.out.println(data.toString());
		}

	}
}