package com.mysql.cj.xdevapi;

import java.util.List;

public interface Schema extends DatabaseObject {
   List<Collection> getCollections();

   List<Collection> getCollections(String var1);

   List<Table> getTables();

   List<Table> getTables(String var1);

   Collection getCollection(String var1);

   Collection getCollection(String var1, boolean var2);

   Table getCollectionAsTable(String var1);

   Table getTable(String var1);

   Table getTable(String var1, boolean var2);

   Collection createCollection(String var1);

   Collection createCollection(String var1, boolean var2);

   void dropCollection(String var1);
}
