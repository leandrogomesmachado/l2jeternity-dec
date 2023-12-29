package l2e.commons.dao;

import java.io.Serializable;

public interface JdbcEntity extends Serializable {
   void setJdbcState(JdbcEntityState var1);

   JdbcEntityState getJdbcState();

   void save();

   void update();

   void delete();
}
