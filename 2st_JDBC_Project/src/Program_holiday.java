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

		
		Holiday_Dao hd = new Holiday_Dao();
		//Restday_Holiday rh = new Restday_Holiday();
		Emp emp = new Emp();
		Scanner sc = new Scanner(System.in);
		int rowcnt =0;
		
		public void run() {
			
			while(true) {
				System.out.println("***********************");
				System.out.println("원하시는 메뉴를 선택해주세요");
				System.out.println("[1. 휴가신청현황 조회]  [2. 휴가신청 등록]  [3. 휴가 승인하기 ( 반려 / 승인 )]   [4. 반려리스트 삭제]");
				System.out.print(">> ");
				String menu =sc.nextLine();
				switch(menu) {
				case "1":
					getholiday(); //휴가현황 조회
					break;
				case "2":
					insertholiday(); //휴가 등록
					break;
				case "3":
					signVacation();//휴가 결재
					break;
				case "4":
					deletevaction(); // 휴가 반려리스트 삭제
					break;
				default:
					break;
					}
				}
			}
		
		//1. 휴가현황 조회 
		public void getholiday() {
			List<HolidayList> hlist = hd.getList(); 
			
			System.out.println("휴가신청현황입니다.");
			ListPrint(hlist);
		}
		
		//2. 휴가 등록
		public void insertholiday() {
			Eapply insertapply = new Eapply();
			
			
			System.out.println("휴가신청조회 등록을 선택하셨습니다.");
			System.out.println("사번을 입력해주세요");
			System.out.print(">> ");
			insertapply.setEmpno(Integer.parseInt(sc.nextLine()));
			System.out.println("휴가 유형을 선택해주세요");
			System.out.println("1. 공가  2. 병가  3. 경조사");
			System.out.print(">> ");
			insertapply.setHolidayno(Integer.parseInt(sc.nextLine()));
			System.out.print("휴가 시작일을 입력해주세요 (yyyy-mm-dd) : ");
			insertapply.setStart_date(Date.valueOf(sc.nextLine()));
			System.out.print("휴가 종료일을 입력해주세요 (yyyy-mm-dd) : ");
			insertapply.setEnd_date(Date.valueOf(sc.nextLine()));
			System.out.print("휴가 사유를 적어주세요: ");
			insertapply.setReason(sc.nextLine());
			
			 //휴가 상신일수 구하기 
			int vacationdays = hd.getVacationDay(insertapply.getEmpno(),insertapply.getEnd_date(),insertapply.getStart_date());
			System.out.println("신청한 휴가일수는 "+vacationdays +"입니다.");//오류 해결
			 
			//기존 잔여 휴가일수 
			int restVacationDay = hd.getRestDay(insertapply.getEmpno()); 
			//System.out.println(restVacationDay);
			
			if(restVacationDay-vacationdays >0) { 
				hd.restdayupdate(vacationdays,insertapply.getEmpno()); // 휴가일수 반영 
				
				 rowcnt=hd.holiydayInsert(insertapply); //2. 휴가등록
				 System.out.println("insert "+rowcnt +" 건이 정상적으로 등록되었습니다.");
			}else {
				System.out.println("잔여 휴가 일수가 부족합니다.");
			}
		}
		
		//3. 휴가 승인, 내역조회
		public void signVacation() {
			System.out.println("결재하실 휴가신청번호를 입력해주세요");
			System.out.print(">> ");
			int applyno = Integer.parseInt(sc.nextLine());
			System.out.println("휴가신청 상태를 변경해주세요");
			System.out.println("0. 대기  1. 승인  2. 반려");
			System.out.print(">> ");
			int stateno = Integer.parseInt(sc.nextLine());
			hd.changeStateno(applyno, stateno); // 수정
			
			// 3-1 휴가일수 차감할 신청번호 내역 조회 
			HolidayList epdto = hd.getSignList(applyno); 
			System.out.println(epdto.getStateno());
			int estateno =epdto.getStateno();
			//승인일 경우 휴가 차감, 반영된 내역 확인 
			System.out.println(estateno);
			if(estateno == 1) {
				restVacation(applyno); 
			}
			
		}
 
		//3-2. 휴가일수 차감하기
		public void restVacation(int applyno) {
			
			HolidayList hl = hd.getSignList(applyno);
			
			long vacationDay = hd.getVacationDay(hl.getEmpno(),hl.getStart_date(),hl.getEnd_date());
			hd.restdayupdate((int)vacationDay,hl.getEmpno()); // 휴가 잔여일수 - 휴가 신청일수 업데이트
			System.out.println("[반영된 내역 확인]");
			hl= hd.getSignList(hl.getApplyno()); 
			ListPrint(hl);
		}
		
		//4. 휴가 반려리스트 삭제
		public void deletevaction() {
			System.out.println("반려리스트 내역입니다.");
			List<HolidayList> rejectlist = hd.RejectList();
			
			if (rejectlist != null) {
				ListPrint(rejectlist);
			}else {
				System.out.println("반려 내역이 존재하지 않습니다.");
				return;
				}
			System.out.print("삭제할 휴가신청번호를 입력해주세요 : ");
			int applyno = Integer.parseInt(sc.nextLine());
			int deleterow = hd.holidayDelete(applyno);
			if (deleterow > 0) {
				System.out.println(deleterow+"건 삭제되었습니다. ");
			} else {
				System.out.println("DELETE FAIL");
				}
			System.out.println("[반영된 내역 확인]");
			//반영된 내역 확인
			rejectlist = hd.RejectList();
			if (rejectlist != null) {
				ListPrint(rejectlist);
			}else {
				System.out.println("반려된 내역이 존재하지 않습니다.");
				}
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
 
 
	}
