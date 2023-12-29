package com.mysql.cj.protocol;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Session;

public interface ResultsetRowsOwner {
   void closeOwner(boolean var1);

   MysqlConnection getConnection();

   Session getSession();

   Object getSyncMutex();

   long getConnectionId();

   String getPointOfOrigin();

   int getOwnerFetchSize();

   String getCurrentCatalog();

   int getOwningStatementId();

   int getOwningStatementMaxRows();

   int getOwningStatementFetchSize();

   long getOwningStatementServerId();
}
