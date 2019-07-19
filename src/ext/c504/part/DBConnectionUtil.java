package ext.c504.part;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import wt.method.MethodContext;
import wt.method.RemoteMethodServer;
import wt.pom.WTConnection;


/**
 * DB Connection.
 * @version 1.0
 * @author yxing
 *
 */
public class DBConnectionUtil {
	Connection connection;
    Statement statement;
    PreparedStatement preparedStatement;
    ResultSet resultSet;
    String[] columnNames = {};
    Class[] columnClasses = {};
    ResultSetMetaData   metaData;
    String errorMessage = null;
    int updateCount;
    
    static MethodContext methodcontext;
	
	/**
	 * this is the constructor to construct DatabaseUtil with OOTB connection
	 * @throws Exception
	 */
    public DBConnectionUtil() throws Exception{	 
		try {			
           MethodContext methodcontext = null;
            methodcontext = MethodContext.getContext();
            WTConnection wtconnection = (WTConnection) methodcontext.getConnection();
            connection = wtconnection.getConnection();
			//connection = MethodContext.getPomHandler().getConnection();
			statement = connection.createStatement();
		} catch (ClassNotFoundException ex) {
			errorMessage = "Cannot find the database driver classes.<br>" + ex.getMessage();
			throw ex;
		} catch (SQLException ex) {
			errorMessage = "Cannot connect to this database.<br>" + ex.getMessage();
			throw ex;
		}
    }
    
	/**
	 * this is the constructor to construct DatabaseUtil with parameter
	 * @param url
	 * @param driverName
	 * @param user
	 * @param passwd
	 * @throws ClassNotFoundException, SQLException
	 */
    public DBConnectionUtil(String url, String driverName, String user, String passwd) throws ClassNotFoundException, SQLException{
		try {			
			Class.forName(driverName);
			connection = DriverManager.getConnection(url, user, passwd);
			statement = connection.createStatement();
		} catch (ClassNotFoundException ex) {
			errorMessage = "Cannot find the database driver classes.<br>" + ex.getMessage();
			throw ex;
		} catch (SQLException ex) {
			errorMessage = "Cannot connect to this database.<br>" + ex.getMessage();
			throw ex;
		}
	}

	/**
	 * it is used to execute query sql
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @param query
	 * @return ResultSet the query result
	 * @throws SQLException
	 * 
	 *
     */
    public ResultSet executeQuery(String query) throws SQLException {
        if (connection == null || statement == null) {
            errorMessage = "There is no database to execute the query.";
            return null;
        }
        try {
            resultSet = statement.executeQuery(query);
           return resultSet;
        }
        catch (SQLException ex) {
            errorMessage = ex.getMessage();
            throw ex;
        }
    }

	/**
	 * it is used to execute update sql
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @param sqlcommand update sql
	 * @throws SQLException
	 * 
	 *
	 */
    public void executeUpdate(String sqlcommand) throws SQLException {
        if (connection == null || statement == null) {
            errorMessage = "There is no database to execute the query.";
            return;
        }
        try {
            updateCount = statement.executeUpdate(sqlcommand);
        }
        catch (SQLException ex) {
            errorMessage = ex.getMessage();
            throw ex;
        }
    }
    
	/**
	 * this is used to set not use auto commit
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @throws SQLException
	 * 
	 *
	 */
    public void start() throws SQLException{
    	connection.setAutoCommit( false );
    }

	/**
	 * this is used to execute the commit operation
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @throws SQLException
	 * 
	 *
	 */
    public void commit() throws SQLException {
		connection.commit();
	}

	
	/**
	 * this is used to execute rollback operation
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @throws SQLException
	 * 
	 *
	 */
    public void rollback() throws SQLException {
		connection.rollback();
	}    

	/**
	 * it is used to execute get meta date operation. If you want to get meta date such as:columnName,
	 * you should execute this method first
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @param query
	 * @throws SQLException
	 * 
	 *
	 */
    public void getMetaData(String query) throws SQLException {
        if (connection == null || statement == null) {
            errorMessage = "There is no database to execute the query.";
            return;
        }
        try {
            resultSet = statement.executeQuery(query);
            metaData = resultSet.getMetaData();

            int numberOfColumns =  metaData.getColumnCount();
            columnNames = new String[numberOfColumns];
            columnClasses = new Class[numberOfColumns];
            // Get the column names and cache them.
            for(int column = 0; column < numberOfColumns; column++) {
                columnNames[column] = metaData.getColumnLabel(column+1);
                columnClasses[column] = setColumnClass( column );
            }
        }
        catch (SQLException ex) {
            errorMessage = ex.getMessage();
            //System.err.println(errorMessage);
            throw ex;
        }
    }

    /**
     * it is used to set column class type
     * </p>
     *  
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
     * <b>Comment:</b>
     *
     * @param column
     * @return Class column class type
     * @throws SQLException
     * 
     *
     */
    public Class setColumnClass(int column)throws SQLException {
        int type;
       
            type = metaData.getColumnType(column+1);

        switch(type) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return String.class;

        case Types.BIT:
            return Boolean.class;

        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return Integer.class;

        case Types.BIGINT:
            return Long.class;

        case Types.FLOAT:
        case Types.DOUBLE:
        case Types.DECIMAL: 
        case Types.NUMERIC: 
            return Double.class;

        case Types.DATE:

        case Types.TIME: 
            return java.sql.Time.class;
        case Types.TIMESTAMP: 
            return java.sql.Timestamp.class;
        default: 
            return Object.class;
        }
    }
    
	/**
	 * this is used to get ResultSet of this DatabaseUtil class
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @return ResultSet 
	 * 
	 *
	 */
    public ResultSet getResutlSet(){
        return resultSet;
    }

    /**
     * it is used to close the DB connection.
     * </p>
     *  
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
     * <b>Comment:</b>
     *
     * @throws SQLException
     * 
     *
     */
    public void close() throws SQLException {
        if( statement != null ){
            statement.close(); 
        }
           
        if (connection != null) {
    		if (RemoteMethodServer.getDefault().ServerFlag) { 
    			if(methodcontext != null) {
    			    methodcontext.freeConnection();
    			}
    		}
        }
    }
    

	/**
	 * this method is used to relase the resource
	 */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

	/**
	 * this method is used to get column name
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @param column
	 * @return String the column name
	 * 
	 *
	 */
    public String getColumnName(int column) {
        if (columnNames[column] != null) {
            return columnNames[column];
        } else {
            return "";
        }
    }

	/**
	 * this method is used to get column class type
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @param column
	 * @return Class column class type
	 * 
	 *
	 */
    public Class getColumnClass(int column) {
        if (columnClasses[column] != null) {
            return columnClasses[column];
        } else {
            return null;
        }
    }

	/**
	 * this is used to get column count
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @return int column count
	 * 
	 *
	 */
    public int getColumnCount() {
        return columnNames.length;
    }

	/**
	 * it is used to get update count
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @return int the update count
	 * 
	 *
	 */
    public int getUpdateCount(){
        return updateCount;
    }

	/**
	 * it is used to get errormessage    
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @return String errormessage
	 * 
	 *
	 */
    public String getErrorMessage(){
        return errorMessage;
    }

	/**
	 * this method is used to get connection
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @return Connection 
	 * 
	 *
	 */
    public Connection getConnection(){
        return connection;
    }
    
	/**    
	 * it is used to get Row count from ResultSet
	 * </p>
	 *  
	 * <b>Revision History</b><br>
	 * <b>Rev:</b> 1.0 2014-02-12, yxing<br>
	 * <b>Comment:</b>
	 *
	 * @param resultSet
	 * @return int the row count of this ResultSet
	 * @throws SQLException
	 *
	 */
    public static int getRowCountFromResultSet(ResultSet resultSet) throws SQLException {
    	int ret = 0;
    	if(resultSet == null) {
    		return ret;
    	}
    	while(resultSet.next()) { 
    		ret++; 
    	} 
    	return ret;
    }
}
