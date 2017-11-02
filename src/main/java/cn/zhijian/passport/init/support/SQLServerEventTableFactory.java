package cn.zhijian.passport.init.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLServerEventTableFactory implements EventTableFactory {

	final static Logger logger = LoggerFactory.getLogger(SQLServerEventTableFactory.class);

	/**
	 * Singleton SQLServerEventTableFactory instance
	 */
	public static final SQLServerEventTableFactory INSTANCE = new SQLServerEventTableFactory();

	/**
	 * Default constructor of the factory
	 */
	protected SQLServerEventTableFactory() {
	}

	@Override
	public PreparedStatement createDomainEventTable(Connection connection, EventSchema schema) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES " //
				+ "WHERE TABLE_SCHEMA = 'dbo' " //
				+ "AND  TABLE_NAME = '" + schema.domainEventTable() + "'");
		ResultSet rs = ps.executeQuery();
		String sql = "SELECT 1";
		try {
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count == 0) {
					sql = "CREATE TABLE [dbo].[" + schema.domainEventTable() //
							+ "] (" //
							+ schema.globalIndexColumn() + " BIGINT IDENTITY(0,1) PRIMARY KEY, " //
							+ schema.aggregateIdentifierColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.sequenceNumberColumn() + " BIGINT NOT NULL, " //
							+ schema.typeColumn() + " VARCHAR(255), " //
							+ schema.eventIdentifierColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.metaDataColumn() + " VARBINARY(MAX), " //
							+ schema.payloadColumn() + " VARBINARY(MAX) NOT NULL, " //
							+ schema.payloadRevisionColumn() + " VARCHAR(255), " //
							+ schema.payloadTypeColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.timestampColumn() + " VARCHAR(255) NOT NULL " //
							+ "UNIQUE (" //
							+ schema.aggregateIdentifierColumn() + ", " + schema.sequenceNumberColumn() //
							+ "), " //
							+ "UNIQUE (" + schema.eventIdentifierColumn() + ") " //
							+ ")"; //
					logger.debug("EventStore init SQL: {}", sql);
				} else {
					logger.debug("EventStore already exists");
				}
			} else {
				logger.error("Somethings wrong while searching for event table");
				return null;
			}
		} finally {
			rs.close();
			ps.close();
		}
		return connection.prepareStatement(sql);
	}

	@Override
	public PreparedStatement createSnapshotEventTable(Connection connection, EventSchema schema) throws SQLException {

		PreparedStatement ps = connection.prepareStatement("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES " //
				+ "WHERE TABLE_SCHEMA = 'dbo' " //
				+ "AND  TABLE_NAME = '" + schema.snapshotTable() + "'");
		ResultSet rs = ps.executeQuery();
		String sql = "SELECT 1";
		try {
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count == 0) {
					sql = "CREATE TABLE [dbo].[" + schema.snapshotTable() //
							+ "] (" //
							+ schema.aggregateIdentifierColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.sequenceNumberColumn() + " BIGINT NOT NULL, " //
							+ schema.typeColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.eventIdentifierColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.metaDataColumn() + " VARBINARY(MAX), " //
							+ schema.payloadColumn() + " VARBINARY(MAX) NOT NULL, " //
							+ schema.payloadRevisionColumn() + " VARCHAR(255), " //
							+ schema.payloadTypeColumn() + " VARCHAR(255) NOT NULL, " //
							+ schema.timestampColumn() + " VARCHAR(255) NOT NULL " //
							+ "PRIMARY KEY (" + schema.aggregateIdentifierColumn() + ", "
							+ schema.sequenceNumberColumn() + "), " //
							+ "UNIQUE (" + schema.eventIdentifierColumn() + ")\n" //
							+ ")"; //
					logger.debug("SnapshotEventStore init SQL: {}", sql);
					return connection.prepareStatement(sql); //
				} else {
					logger.debug("SnapshotEventStore already exists");
				}
			} else {
				logger.error("Somethings wrong while searching for snapshot event table");
				return null;
			}
		} finally {
			rs.close();
			ps.close();
		}
		return connection.prepareStatement(sql);
	}
}
