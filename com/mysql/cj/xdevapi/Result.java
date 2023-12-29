package com.mysql.cj.xdevapi;

import java.util.Iterator;

public interface Result {
   long getAffectedItemsCount();

   int getWarningsCount();

   Iterator<Warning> getWarnings();
}
