package bard.db

import java.sql.Connection
import java.sql.SQLException
 
import com.zaxxer.hikari.HikariDataSource
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.springframework.jdbc.datasource.DelegatingDataSource

class HikariDataSource extends DelegatingDataSource {
   private boolean _initialized;

   @Override
   Connection getConnection() throws SQLException {
      initialize()
      return super.getConnection()
   }
    
   @Override
   void afterPropertiesSet() {
      // override to not check for targetDataSource since it's lazily created
   }
 
   private synchronized void initialize() {
      if (_initialized) {
         return
      }
      
      def config = CH.config.dataSource
      System.out.println("Intializing "+getClass()+"..."+config.driveClassName)
      setTargetDataSource(new com.zaxxer.hikari.HikariDataSource(
            driverClassName: config.driverClassName, password: config.password,
            username: config.username, jdbcUrl: config.url,
	    maximumPoolSize: config.maxPoolSize))
 
      _initialized = true
   }
}
