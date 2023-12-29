package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class DelegatingDatabaseMetaData extends AbandonedTrace implements DatabaseMetaData {
   protected DatabaseMetaData _meta;
   protected DelegatingConnection _conn = null;

   public DelegatingDatabaseMetaData(DelegatingConnection c, DatabaseMetaData m) {
      super(c);
      this._conn = c;
      this._meta = m;
   }

   public DatabaseMetaData getDelegate() {
      return this._meta;
   }

   @Override
   public boolean equals(Object obj) {
      DatabaseMetaData delegate = this.getInnermostDelegate();
      if (delegate == null) {
         return false;
      } else if (obj instanceof DelegatingDatabaseMetaData) {
         DelegatingDatabaseMetaData s = (DelegatingDatabaseMetaData)obj;
         return delegate.equals(s.getInnermostDelegate());
      } else {
         return delegate.equals(obj);
      }
   }

   @Override
   public int hashCode() {
      Object obj = this.getInnermostDelegate();
      return obj == null ? 0 : obj.hashCode();
   }

   public DatabaseMetaData getInnermostDelegate() {
      DatabaseMetaData m = this._meta;

      while(m != null && m instanceof DelegatingDatabaseMetaData) {
         m = ((DelegatingDatabaseMetaData)m).getDelegate();
         if (this == m) {
            return null;
         }
      }

      return m;
   }

   protected void handleException(SQLException e) throws SQLException {
      if (this._conn != null) {
         this._conn.handleException(e);
      } else {
         throw e;
      }
   }

   @Override
   public boolean allProceduresAreCallable() throws SQLException {
      try {
         return this._meta.allProceduresAreCallable();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean allTablesAreSelectable() throws SQLException {
      try {
         return this._meta.allTablesAreSelectable();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
      try {
         return this._meta.dataDefinitionCausesTransactionCommit();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
      try {
         return this._meta.dataDefinitionIgnoredInTransactions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean deletesAreDetected(int type) throws SQLException {
      try {
         return this._meta.deletesAreDetected(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
      try {
         return this._meta.doesMaxRowSizeIncludeBlobs();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getBestRowIdentifier(catalog, schema, table, scope, nullable));
      } catch (SQLException var7) {
         this.handleException(var7);
         throw new AssertionError();
      }
   }

   @Override
   public String getCatalogSeparator() throws SQLException {
      try {
         return this._meta.getCatalogSeparator();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getCatalogTerm() throws SQLException {
      try {
         return this._meta.getCatalogTerm();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getCatalogs() throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getCatalogs());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getColumnPrivileges(catalog, schema, table, columnNamePattern));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this._conn;
   }

   @Override
   public ResultSet getCrossReference(
      String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable
   ) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(
            this._conn, this._meta.getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable)
         );
      } catch (SQLException var8) {
         this.handleException(var8);
         throw new AssertionError();
      }
   }

   @Override
   public int getDatabaseMajorVersion() throws SQLException {
      try {
         return this._meta.getDatabaseMajorVersion();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getDatabaseMinorVersion() throws SQLException {
      try {
         return this._meta.getDatabaseMinorVersion();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public String getDatabaseProductName() throws SQLException {
      try {
         return this._meta.getDatabaseProductName();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getDatabaseProductVersion() throws SQLException {
      try {
         return this._meta.getDatabaseProductVersion();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public int getDefaultTransactionIsolation() throws SQLException {
      try {
         return this._meta.getDefaultTransactionIsolation();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getDriverMajorVersion() {
      return this._meta.getDriverMajorVersion();
   }

   @Override
   public int getDriverMinorVersion() {
      return this._meta.getDriverMinorVersion();
   }

   @Override
   public String getDriverName() throws SQLException {
      try {
         return this._meta.getDriverName();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getDriverVersion() throws SQLException {
      try {
         return this._meta.getDriverVersion();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getExportedKeys(catalog, schema, table));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public String getExtraNameCharacters() throws SQLException {
      try {
         return this._meta.getExtraNameCharacters();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getIdentifierQuoteString() throws SQLException {
      try {
         return this._meta.getIdentifierQuoteString();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getImportedKeys(catalog, schema, table));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getIndexInfo(catalog, schema, table, unique, approximate));
      } catch (SQLException var7) {
         this.handleException(var7);
         throw new AssertionError();
      }
   }

   @Override
   public int getJDBCMajorVersion() throws SQLException {
      try {
         return this._meta.getJDBCMajorVersion();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getJDBCMinorVersion() throws SQLException {
      try {
         return this._meta.getJDBCMinorVersion();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxBinaryLiteralLength() throws SQLException {
      try {
         return this._meta.getMaxBinaryLiteralLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxCatalogNameLength() throws SQLException {
      try {
         return this._meta.getMaxCatalogNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxCharLiteralLength() throws SQLException {
      try {
         return this._meta.getMaxCharLiteralLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxColumnNameLength() throws SQLException {
      try {
         return this._meta.getMaxColumnNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxColumnsInGroupBy() throws SQLException {
      try {
         return this._meta.getMaxColumnsInGroupBy();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxColumnsInIndex() throws SQLException {
      try {
         return this._meta.getMaxColumnsInIndex();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxColumnsInOrderBy() throws SQLException {
      try {
         return this._meta.getMaxColumnsInOrderBy();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxColumnsInSelect() throws SQLException {
      try {
         return this._meta.getMaxColumnsInSelect();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxColumnsInTable() throws SQLException {
      try {
         return this._meta.getMaxColumnsInTable();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxConnections() throws SQLException {
      try {
         return this._meta.getMaxConnections();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxCursorNameLength() throws SQLException {
      try {
         return this._meta.getMaxCursorNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxIndexLength() throws SQLException {
      try {
         return this._meta.getMaxIndexLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxProcedureNameLength() throws SQLException {
      try {
         return this._meta.getMaxProcedureNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxRowSize() throws SQLException {
      try {
         return this._meta.getMaxRowSize();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxSchemaNameLength() throws SQLException {
      try {
         return this._meta.getMaxSchemaNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxStatementLength() throws SQLException {
      try {
         return this._meta.getMaxStatementLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxStatements() throws SQLException {
      try {
         return this._meta.getMaxStatements();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxTableNameLength() throws SQLException {
      try {
         return this._meta.getMaxTableNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxTablesInSelect() throws SQLException {
      try {
         return this._meta.getMaxTablesInSelect();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public int getMaxUserNameLength() throws SQLException {
      try {
         return this._meta.getMaxUserNameLength();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public String getNumericFunctions() throws SQLException {
      try {
         return this._meta.getNumericFunctions();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getPrimaryKeys(catalog, schema, table));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }

   @Override
   public String getProcedureTerm() throws SQLException {
      try {
         return this._meta.getProcedureTerm();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getProcedures(catalog, schemaPattern, procedureNamePattern));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public int getResultSetHoldability() throws SQLException {
      try {
         return this._meta.getResultSetHoldability();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public String getSQLKeywords() throws SQLException {
      try {
         return this._meta.getSQLKeywords();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public int getSQLStateType() throws SQLException {
      try {
         return this._meta.getSQLStateType();
      } catch (SQLException var2) {
         this.handleException(var2);
         return 0;
      }
   }

   @Override
   public String getSchemaTerm() throws SQLException {
      try {
         return this._meta.getSchemaTerm();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getSchemas() throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getSchemas());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getSearchStringEscape() throws SQLException {
      try {
         return this._meta.getSearchStringEscape();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getStringFunctions() throws SQLException {
      try {
         return this._meta.getStringFunctions();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getSuperTables(catalog, schemaPattern, tableNamePattern));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getSuperTypes(catalog, schemaPattern, typeNamePattern));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public String getSystemFunctions() throws SQLException {
      try {
         return this._meta.getSystemFunctions();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getTablePrivileges(catalog, schemaPattern, tableNamePattern));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getTableTypes() throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getTableTypes());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getTables(catalog, schemaPattern, tableNamePattern, types));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }

   @Override
   public String getTimeDateFunctions() throws SQLException {
      try {
         return this._meta.getTimeDateFunctions();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getTypeInfo() throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getTypeInfo());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getUDTs(catalog, schemaPattern, typeNamePattern, types));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }

   @Override
   public String getURL() throws SQLException {
      try {
         return this._meta.getURL();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public String getUserName() throws SQLException {
      try {
         return this._meta.getUserName();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getVersionColumns(catalog, schema, table));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public boolean insertsAreDetected(int type) throws SQLException {
      try {
         return this._meta.insertsAreDetected(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean isCatalogAtStart() throws SQLException {
      try {
         return this._meta.isCatalogAtStart();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean isReadOnly() throws SQLException {
      try {
         return this._meta.isReadOnly();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean locatorsUpdateCopy() throws SQLException {
      try {
         return this._meta.locatorsUpdateCopy();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean nullPlusNonNullIsNull() throws SQLException {
      try {
         return this._meta.nullPlusNonNullIsNull();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean nullsAreSortedAtEnd() throws SQLException {
      try {
         return this._meta.nullsAreSortedAtEnd();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean nullsAreSortedAtStart() throws SQLException {
      try {
         return this._meta.nullsAreSortedAtStart();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean nullsAreSortedHigh() throws SQLException {
      try {
         return this._meta.nullsAreSortedHigh();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean nullsAreSortedLow() throws SQLException {
      try {
         return this._meta.nullsAreSortedLow();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean othersDeletesAreVisible(int type) throws SQLException {
      try {
         return this._meta.othersDeletesAreVisible(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean othersInsertsAreVisible(int type) throws SQLException {
      try {
         return this._meta.othersInsertsAreVisible(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean othersUpdatesAreVisible(int type) throws SQLException {
      try {
         return this._meta.othersUpdatesAreVisible(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean ownDeletesAreVisible(int type) throws SQLException {
      try {
         return this._meta.ownDeletesAreVisible(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean ownInsertsAreVisible(int type) throws SQLException {
      try {
         return this._meta.ownInsertsAreVisible(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean ownUpdatesAreVisible(int type) throws SQLException {
      try {
         return this._meta.ownUpdatesAreVisible(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean storesLowerCaseIdentifiers() throws SQLException {
      try {
         return this._meta.storesLowerCaseIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
      try {
         return this._meta.storesLowerCaseQuotedIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean storesMixedCaseIdentifiers() throws SQLException {
      try {
         return this._meta.storesMixedCaseIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
      try {
         return this._meta.storesMixedCaseQuotedIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean storesUpperCaseIdentifiers() throws SQLException {
      try {
         return this._meta.storesUpperCaseIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
      try {
         return this._meta.storesUpperCaseQuotedIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsANSI92EntryLevelSQL() throws SQLException {
      try {
         return this._meta.supportsANSI92EntryLevelSQL();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsANSI92FullSQL() throws SQLException {
      try {
         return this._meta.supportsANSI92FullSQL();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsANSI92IntermediateSQL() throws SQLException {
      try {
         return this._meta.supportsANSI92IntermediateSQL();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsAlterTableWithAddColumn() throws SQLException {
      try {
         return this._meta.supportsAlterTableWithAddColumn();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsAlterTableWithDropColumn() throws SQLException {
      try {
         return this._meta.supportsAlterTableWithDropColumn();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsBatchUpdates() throws SQLException {
      try {
         return this._meta.supportsBatchUpdates();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsCatalogsInDataManipulation() throws SQLException {
      try {
         return this._meta.supportsCatalogsInDataManipulation();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
      try {
         return this._meta.supportsCatalogsInIndexDefinitions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
      try {
         return this._meta.supportsCatalogsInPrivilegeDefinitions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsCatalogsInProcedureCalls() throws SQLException {
      try {
         return this._meta.supportsCatalogsInProcedureCalls();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsCatalogsInTableDefinitions() throws SQLException {
      try {
         return this._meta.supportsCatalogsInTableDefinitions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsColumnAliasing() throws SQLException {
      try {
         return this._meta.supportsColumnAliasing();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsConvert() throws SQLException {
      try {
         return this._meta.supportsConvert();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsConvert(int fromType, int toType) throws SQLException {
      try {
         return this._meta.supportsConvert(fromType, toType);
      } catch (SQLException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public boolean supportsCoreSQLGrammar() throws SQLException {
      try {
         return this._meta.supportsCoreSQLGrammar();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsCorrelatedSubqueries() throws SQLException {
      try {
         return this._meta.supportsCorrelatedSubqueries();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
      try {
         return this._meta.supportsDataDefinitionAndDataManipulationTransactions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
      try {
         return this._meta.supportsDataManipulationTransactionsOnly();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsDifferentTableCorrelationNames() throws SQLException {
      try {
         return this._meta.supportsDifferentTableCorrelationNames();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsExpressionsInOrderBy() throws SQLException {
      try {
         return this._meta.supportsExpressionsInOrderBy();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsExtendedSQLGrammar() throws SQLException {
      try {
         return this._meta.supportsExtendedSQLGrammar();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsFullOuterJoins() throws SQLException {
      try {
         return this._meta.supportsFullOuterJoins();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsGetGeneratedKeys() throws SQLException {
      try {
         return this._meta.supportsGetGeneratedKeys();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsGroupBy() throws SQLException {
      try {
         return this._meta.supportsGroupBy();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsGroupByBeyondSelect() throws SQLException {
      try {
         return this._meta.supportsGroupByBeyondSelect();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsGroupByUnrelated() throws SQLException {
      try {
         return this._meta.supportsGroupByUnrelated();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsIntegrityEnhancementFacility() throws SQLException {
      try {
         return this._meta.supportsIntegrityEnhancementFacility();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsLikeEscapeClause() throws SQLException {
      try {
         return this._meta.supportsLikeEscapeClause();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsLimitedOuterJoins() throws SQLException {
      try {
         return this._meta.supportsLimitedOuterJoins();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsMinimumSQLGrammar() throws SQLException {
      try {
         return this._meta.supportsMinimumSQLGrammar();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsMixedCaseIdentifiers() throws SQLException {
      try {
         return this._meta.supportsMixedCaseIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
      try {
         return this._meta.supportsMixedCaseQuotedIdentifiers();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsMultipleOpenResults() throws SQLException {
      try {
         return this._meta.supportsMultipleOpenResults();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsMultipleResultSets() throws SQLException {
      try {
         return this._meta.supportsMultipleResultSets();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsMultipleTransactions() throws SQLException {
      try {
         return this._meta.supportsMultipleTransactions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsNamedParameters() throws SQLException {
      try {
         return this._meta.supportsNamedParameters();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsNonNullableColumns() throws SQLException {
      try {
         return this._meta.supportsNonNullableColumns();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
      try {
         return this._meta.supportsOpenCursorsAcrossCommit();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
      try {
         return this._meta.supportsOpenCursorsAcrossRollback();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
      try {
         return this._meta.supportsOpenStatementsAcrossCommit();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
      try {
         return this._meta.supportsOpenStatementsAcrossRollback();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsOrderByUnrelated() throws SQLException {
      try {
         return this._meta.supportsOrderByUnrelated();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsOuterJoins() throws SQLException {
      try {
         return this._meta.supportsOuterJoins();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsPositionedDelete() throws SQLException {
      try {
         return this._meta.supportsPositionedDelete();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsPositionedUpdate() throws SQLException {
      try {
         return this._meta.supportsPositionedUpdate();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
      try {
         return this._meta.supportsResultSetConcurrency(type, concurrency);
      } catch (SQLException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public boolean supportsResultSetHoldability(int holdability) throws SQLException {
      try {
         return this._meta.supportsResultSetHoldability(holdability);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean supportsResultSetType(int type) throws SQLException {
      try {
         return this._meta.supportsResultSetType(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean supportsSavepoints() throws SQLException {
      try {
         return this._meta.supportsSavepoints();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSchemasInDataManipulation() throws SQLException {
      try {
         return this._meta.supportsSchemasInDataManipulation();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSchemasInIndexDefinitions() throws SQLException {
      try {
         return this._meta.supportsSchemasInIndexDefinitions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
      try {
         return this._meta.supportsSchemasInPrivilegeDefinitions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSchemasInProcedureCalls() throws SQLException {
      try {
         return this._meta.supportsSchemasInProcedureCalls();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSchemasInTableDefinitions() throws SQLException {
      try {
         return this._meta.supportsSchemasInTableDefinitions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSelectForUpdate() throws SQLException {
      try {
         return this._meta.supportsSelectForUpdate();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsStatementPooling() throws SQLException {
      try {
         return this._meta.supportsStatementPooling();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsStoredProcedures() throws SQLException {
      try {
         return this._meta.supportsStoredProcedures();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSubqueriesInComparisons() throws SQLException {
      try {
         return this._meta.supportsSubqueriesInComparisons();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSubqueriesInExists() throws SQLException {
      try {
         return this._meta.supportsSubqueriesInExists();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSubqueriesInIns() throws SQLException {
      try {
         return this._meta.supportsSubqueriesInIns();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsSubqueriesInQuantifieds() throws SQLException {
      try {
         return this._meta.supportsSubqueriesInQuantifieds();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsTableCorrelationNames() throws SQLException {
      try {
         return this._meta.supportsTableCorrelationNames();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
      try {
         return this._meta.supportsTransactionIsolationLevel(level);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean supportsTransactions() throws SQLException {
      try {
         return this._meta.supportsTransactions();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsUnion() throws SQLException {
      try {
         return this._meta.supportsUnion();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsUnionAll() throws SQLException {
      try {
         return this._meta.supportsUnionAll();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean updatesAreDetected(int type) throws SQLException {
      try {
         return this._meta.updatesAreDetected(type);
      } catch (SQLException var3) {
         this.handleException(var3);
         return false;
      }
   }

   @Override
   public boolean usesLocalFilePerTable() throws SQLException {
      try {
         return this._meta.usesLocalFilePerTable();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean usesLocalFiles() throws SQLException {
      try {
         return this._meta.usesLocalFiles();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return iface.isAssignableFrom(this.getClass()) || this._meta.isWrapperFor(iface);
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (iface.isAssignableFrom(this.getClass())) {
         return iface.cast(this);
      } else {
         return (T)(iface.isAssignableFrom(this._meta.getClass()) ? iface.cast(this._meta) : this._meta.unwrap(iface));
      }
   }

   @Override
   public RowIdLifetime getRowIdLifetime() throws SQLException {
      try {
         return this._meta.getRowIdLifetime();
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getSchemas(catalog, schemaPattern));
      } catch (SQLException var4) {
         this.handleException(var4);
         throw new AssertionError();
      }
   }

   @Override
   public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
      try {
         return this._meta.autoCommitFailureClosesAllResultSets();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
      try {
         return this._meta.supportsStoredFunctionsUsingCallSyntax();
      } catch (SQLException var2) {
         this.handleException(var2);
         return false;
      }
   }

   @Override
   public ResultSet getClientInfoProperties() throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getClientInfoProperties());
      } catch (SQLException var2) {
         this.handleException(var2);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getFunctions(catalog, schemaPattern, functionNamePattern));
      } catch (SQLException var5) {
         this.handleException(var5);
         throw new AssertionError();
      }
   }

   @Override
   public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
      this._conn.checkOpen();

      try {
         return DelegatingResultSet.wrapResultSet(this._conn, this._meta.getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern));
      } catch (SQLException var6) {
         this.handleException(var6);
         throw new AssertionError();
      }
   }
}
