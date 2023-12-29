package com.mchange.v1.db.sql;

import com.mchange.v1.util.BrokenObjectException;
import com.mchange.v1.util.ClosableResource;
import java.sql.SQLException;

public interface ConnectionBundlePool extends ClosableResource {
   ConnectionBundle checkoutBundle() throws SQLException, InterruptedException, BrokenObjectException;

   void checkinBundle(ConnectionBundle var1) throws SQLException, BrokenObjectException;

   @Override
   void close() throws SQLException;
}
