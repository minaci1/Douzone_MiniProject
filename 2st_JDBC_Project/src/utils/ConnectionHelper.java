package utils;

import java.sql.SQLException;
import java.sql.Connection;

public class ConnectionHelper {

	public static void main(String[] args) {
		Connection conn =null;
		
		conn = SingletonHelper.getConnection("mariadb");
		
		try {
			System.out.println(conn.toString());
			System.out.println(conn.getMetaData().getDatabaseProductName());
			System.out.println(conn.getMetaData().getDatabaseProductVersion()); //버전 확인
			System.out.println(conn.isClosed()); //닫혔는지 확인
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

}
