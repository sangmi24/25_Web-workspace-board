package com.kh.board.model.dao;

import static com.kh.common.JDBCTemplate.close;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import com.kh.board.model.vo.Attachment;
import com.kh.board.model.vo.Board;
import com.kh.board.model.vo.Category;
import com.kh.common.model.vo.PageInfo;

public class BoardDao {

	private Properties prop = new Properties();
	
	public BoardDao() { //기본 생성자
		
		try {
			prop.loadFromXML(new FileInputStream(BoardDao.class.getResource("/sql/board/board-mapper.xml").getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	public int selectListCount(Connection conn) {
		
		// SELECT 문 => ResultSet 객체 ( 한개의 행)
		
		int listCount = 0;
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectListCount");
		
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			rset = pstmt.executeQuery();
			
			if(rset.next()) {
				
				listCount = rset.getInt("COUNT");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			
		    close(rset);
			close(pstmt);
		}
		
		return listCount;	
	}
	public ArrayList<Board> selectList(Connection conn,PageInfo pi){
		
		// SELECT문 => ResultSet 객체 (여러 행 조회) => ArrayList
		
		ArrayList<Board> list = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectList");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			/*
			 * boardLimit가 10이라는 가정 하에
			 * currentPage : 1 => 시작값 1, 끝값 10
			 * currentPage : 2 => 시작값11, 끝값20
			 * currentPage : 3 => 시작값21, 끝값30
			 * ...
			 * 시작값 = (currentPage - 1) * boardLimit + 1
			 * 끝값 = 시작값 + boardLimit - 1 
			 */
			int startRow = (pi.getCurrentPage()-1)*pi.getBoardLimit()+1;
			int endRow = startRow +pi.getBoardLimit() -1 ;
			
			pstmt.setInt(1, startRow);
			pstmt.setInt(2, endRow);
			
			rset = pstmt.executeQuery();
            
			while(rset.next()) {
				
				list.add(new Board(rset.getInt("BOARD_NO")
						          ,rset.getString("CATEGORY_NAME")
						          ,rset.getString("BOARD_TITLE")
						          ,rset.getString("USER_ID")
						          ,rset.getInt("COUNT")
						          ,rset.getDate("CREATE_DATE")));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
		   
			close(rset);
			close(pstmt);
			
		}
		 return list;
		
		
	}
	
	public ArrayList<Category> selectCategoryList(Connection conn) {
		
		// SELECT문 => ResultSet (여러행 조회)
		
		ArrayList<Category> list = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectCategoryList");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				
				list.add(new Category(rset.getInt("CATEGORY_NO")
						             ,rset.getString("CATEGORY_NAME")));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(rset);
			close(pstmt);
		}
		return list;
	}
	
	//Board Insert 전용 메소드
	public int insertBoard(Connection conn, Board b) {
		
		//INSERT 문 =>int (처리된 행의 갯수)
		int result = 0;
		PreparedStatement pstmt = null;
		
		String sql =prop.getProperty("insertBoard");
		
		try {
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, Integer.parseInt(b.getCategory()));
			pstmt.setString(2, b.getBoardTitle());
			pstmt.setString(3, b.getBoardContent());
			pstmt.setInt(4, Integer.parseInt(b.getBoardWriter()));
			
			result = pstmt.executeUpdate();
                			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		return result;
	}
	
	//Attachment Insert 전용 메소드
	public int insertAttachment(Connection conn, Attachment at) {
		
		//INSERT 문 =>int (처리된 행의 갯수)
		int result = 0;
		PreparedStatement pstmt = null;
		
		String sql =prop.getProperty("insertAttachment");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, at.getOriginName());
			pstmt.setString(2, at.getChangeName());
			pstmt.setString(3, at.getFilePath());
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
		     
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		return result;
		
	}
	
	public int increaseCount(Connection conn, int boardNo) {
		
		// UPDATE 문 => int (처리된 행의 갯수)
		
		int result = 0;
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("increaseCount");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, boardNo);
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}	
		return result;		
	}
	public Board selectBoard(Connection conn,int boardNo) {
		
		// SELECT문=> ResultSet객체 (한행 조회)
		
		Board b = null;
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectBoard");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, boardNo);
			
			rset = pstmt.executeQuery();
			
		    if(rset.next()) {
		    	
		    	b = new Board();
		    	b.setBoardNo(rset.getInt("BOARD_NO"));
		    	b.setCategory(rset.getString("CATEGORY_NAME"));
		    	b.setBoardTitle(rset.getString("BOARD_TITLE"));
		    	b.setBoardContent(rset.getString("BOARD_CONTENT"));
		    	b.setBoardWriter(rset.getString("USER_ID"));
		    	b.setCreateDate(rset.getDate("CREATE_DATE"));
		    }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(rset);
			close(pstmt);
		}
		
		return b;
		
		
	}
	public Attachment selectAttachment(Connection conn,int boardNo) {
		
		// SELECT 문 => ResultSet 객체 (한 행 조회)
		
		Attachment at = null;
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectAttachment");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, boardNo);
			
			rset = pstmt.executeQuery();
			
			if(rset.next()) {
				at = new Attachment();
				
				at.setFileNo(rset.getInt("FILE_NO"));
				at.setOriginName(rset.getString("ORIGIN_NAME"));
				at.setChangeName(rset.getString("CHANGE_NAME"));
			    at.setFilePath(rset.getString("FILE_PATH"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(rset);
			close(pstmt);
		}
		return at;
			
	}
	
	public int updateBoard(Connection conn,Board b) {
	
		// UPDATE 문 => int(처리된 행의 갯수)
		
		int result = 0;
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("updateBoard");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, Integer.parseInt(b.getCategory()));
			pstmt.setString(2, b.getBoardTitle());
			pstmt.setString(3, b.getBoardContent());
			pstmt.setInt(4, b.getBoardNo());
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		
		return result;
		
	}
	
	public int updateAttachment(Connection conn,Attachment at) {
		
		// UPDATE 문 => int(처리된 행의 갯수)
		int result = 0;
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("updateAttachment");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, at.getOriginName());
			pstmt.setString(2, at.getChangeName());
			pstmt.setString(3, at.getFilePath());
			pstmt.setInt(4, at.getFileNo());
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		
		return result;
	}
	public int insertNewAttachment(Connection conn,Attachment at) {
		
		// INSERT 문 => int(처리된 행의 갯수)
		int result = 0;
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("insertNewAttachment");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, at.getRefNo());
			pstmt.setString(2, at.getOriginName());
			pstmt.setString(3, at.getChangeName());
			pstmt.setString(4, at.getFilePath());
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		return result;
		
	}
	public int deleteBoard(Connection conn,int boardNo) {
		
		//UPDATE 문 =>int (처리된 행의 갯수)
		int result = 0;
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("deleteBoard");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, boardNo);
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		return result;
	}
	
	public int insertThumbnailBoard(Connection conn,Board b) {
		
		//INSERT 문 =>int (처리된 행의 갯수)
		int result =0;
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("insertThumnailBoard");
		
		try {
		pstmt = conn.prepareStatement(sql);
		
		pstmt.setString(1, b.getBoardTitle());
		pstmt.setString(2, b.getBoardContent());
		pstmt.setInt(3, Integer.parseInt(b.getBoardWriter())); 
		
		result = pstmt.executeUpdate();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		return result;
	}
	public int insertAttachmentList(Connection conn, ArrayList<Attachment> list) {
		
		//INSERT문 => int (처리된 행의 갯수)
		int result = 1; 
		// insert를 반복해서 진행=> 하나라도 실패할 경우 실패처리
		// result 를 애초에 1로 셋팅해두고 누적 곱을 구할 예정
		
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("insertAttachmentList");
		
		try {
			for(Attachment at : list) {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, at.getOriginName());
			pstmt.setString(2, at.getChangeName());
			pstmt.setString(3, at.getFilePath());
			pstmt.setInt(4, at.getFileLevel());
			
			result *= pstmt.executeUpdate();
			// 하나라도 실패할 경우 0이 뜰 것임		
		  }
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(pstmt);
		}
		return result;		
	}
	public ArrayList<Board> selectThumbnailList(Connection conn) {
		
		// SELECT 문 => ResultSet (여러행) => ArrayList
		
		ArrayList<Board> list = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectThumbnailList");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				
				Board b = new Board();
				b.setBoardNo(rset.getInt("BOARD_NO"));
				b.setBoardTitle(rset.getString("BOARD_TITLE"));
				b.setCount(rset.getInt("COUNT"));
				b.setTitleImg(rset.getString("TITLEIMG"));
				
				list.add(b);
			}
			
		} catch (SQLException e) {
		
			e.printStackTrace();
		}finally {
			close(rset);
			close(pstmt);
		}
		return list;
	}
	
	public ArrayList<Attachment> selectAttachmentList(Connection conn,int boardNo){
		
		// SELECT 문 =>ResultSet (여러행 조회) =>ArrayList
		ArrayList<Attachment> list = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		//키 값이 selectAttachment 쿼리문을 재활용
		String sql = prop.getProperty("selectAttachment");
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, boardNo);
			
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
			
				Attachment at = new Attachment();
				at.setFileNo(rset.getInt("FILE_NO"));
				at.setOriginName(rset.getString("ORIGIN_NAME"));
				at.setChangeName(rset.getString("CHANGE_NAME"));
				at.setFilePath(rset.getString("FILE_PATH"));
				
				// 여기서 우리가 상세조회 시 CHANGE_NAME, FILE_PATH 값만 이용해도 충분
				// 단, FILE_NO, ORIGIN_NAME은 수정 삭제 시 필요하므로조회			
				list.add(at);				
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(rset);
			close(pstmt);
		}
		
		return list;
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
