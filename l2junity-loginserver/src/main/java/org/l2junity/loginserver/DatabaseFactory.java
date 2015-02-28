/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.loginserver;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2junity.loginserver.db.AccountDAO;
import org.l2junity.loginserver.db.AccountLoginDAO;
import org.l2junity.loginserver.db.AccountOTPDAO;
import org.l2junity.loginserver.db.dto.Account;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException;
import org.skife.jdbi.v2.tweak.HandleCallback;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This class manages the database connections.
 */
public class DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(DatabaseFactory.class.getName());
	
	private static DatabaseFactory _instance;
	
	private final ComboPooledDataSource _source = new ComboPooledDataSource();
	private final DBI _dbi;
	
	/**
	 * Creates a database factory instance.
	 * @throws SQLException the SQL exception
	 */
	public DatabaseFactory() throws SQLException
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				_log.warning("A minimum of " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}
			
			_source.setAutoCommitOnClose(true);
			
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			
			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection
			// if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.
			
			// this "connection_test_table" is automatically created if not already there
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);
			
			// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout
			
			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
			_source.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour
			
			// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(100);
			
			_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass("com.mysql.jdbc.Driver");
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);
			
			/* Test the connection */
			_source.getConnection().close();
			
			_dbi = new DBI(_source);
			
			try
			{
				AccountDAO dao = _dbi.open(AccountDAO.class);
				
				// System.out.println(dao.insert("nos6", "password"));
				System.out.println(dao.updateLastServerId(new Account(12, null, null, (short) 15, null)));
				System.out.println(dao.findById(1));
				System.out.println(dao.findByName("nos6"));
				
				AccountOTPDAO dao2 = _dbi.open(AccountOTPDAO.class);
				dao2.insert(1, "tablet", "abc");
				dao2.insert(1, "mobile", "def");
				System.out.println(dao2.findByAccountId(12));
				dao2.delete(1);
				System.out.println(dao2.findByAccountId(dao.findById(1)));
				
				AccountLoginDAO dao3 = _dbi.open(AccountLoginDAO.class);
				dao3.insert(1, "127.0.0.1");
				
				System.out.println(dao3.findByAccountId(1));
				
				System.exit(0);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println(e.getClass().getSimpleName());
				System.exit(0);
			}
		}
		catch (SQLException x)
		{
			// re-throw the exception
			throw x;
		}
		catch (Exception e)
		{
			throw new SQLException("Could not init DB connection:" + e.getMessage());
		}
	}
	
	/**
	 * Shutdown.
	 */
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
	}
	
	/**
	 * Gets the single instance of L2DatabaseFactory.
	 * @return single instance of L2DatabaseFactory
	 * @throws SQLException the SQL exception
	 */
	public static DatabaseFactory getInstance() throws SQLException
	{
		synchronized (DatabaseFactory.class)
		{
			if (_instance == null)
			{
				_instance = new DatabaseFactory();
			}
		}
		return _instance;
	}
	
	/**
	 * Gets the handle.
	 * @return the handle
	 */
	public Handle getHandle()
	{
		Handle con = null;
		while (con == null)
		{
			try
			{
				con = _dbi.open();
			}
			catch (UnableToObtainConnectionException e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": getHandle() failed, trying again", e);
			}
		}
		return _dbi.open();
	}
	
	public <R> R withHandle(HandleCallback<R> callback)
	{
		return _dbi.withHandle(callback);
	}
	
	/**
	 * Gets the busy connection count.
	 * @return the busy connection count
	 * @throws SQLException the SQL exception
	 */
	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}
	
	/**
	 * Gets the idle connection count.
	 * @return the idle connection count
	 * @throws SQLException the SQL exception
	 */
	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
	}
}
