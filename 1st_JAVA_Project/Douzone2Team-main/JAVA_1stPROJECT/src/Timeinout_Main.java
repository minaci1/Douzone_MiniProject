import java.util.Scanner;

public class Timeinout_Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.println("***********************");
		System.out.println("1. 학생 메뉴");
		System.out.println("2. 관리자 메뉴");
		System.out.println("원하시는 메뉴를 선택해 주세요");

		int select = Integer.parseInt(sc.nextLine());

		switch (select) {
		case 1:
			System.out.println("학생 메뉴를 실행합니다.");
			Student_Menu sm = new Student_Menu();
			sm.MenuRun();
			break;
		case 2:
			System.out.println("관리자 메뉴를 실행합니다.");
			Admin_Menu am = new Admin_Menu();
			am.MenuRun();
			break;
		}
		sc.close();
	}
}