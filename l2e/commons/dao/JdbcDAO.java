package l2e.commons.dao;

import java.io.Serializable;

public interface JdbcDAO<K extends Serializable, E extends JdbcEntity> {
   E load(K var1);

   void save(E var1);

   void update(E var1);

   void saveOrUpdate(E var1);

   void delete(E var1);

   JdbcEntityStats getStats();
}
