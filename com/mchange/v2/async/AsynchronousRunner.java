package com.mchange.v2.async;

import com.mchange.v1.util.ClosableResource;

public interface AsynchronousRunner extends ClosableResource {
   void postRunnable(Runnable var1);

   void close(boolean var1);

   @Override
   void close();
}
