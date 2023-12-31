package org.eclipse.jdt.internal.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

public class ReadManager implements Runnable {
   ICompilationUnit[] units;
   int nextFileToRead;
   ICompilationUnit[] filesRead;
   char[][] contentsRead;
   int readyToReadPosition;
   int nextAvailablePosition;
   Thread[] readingThreads;
   char[] readInProcessMarker = new char[0];
   int sleepingThreadCount;
   private Throwable caughtException;
   static final int START_CUSHION = 5;
   public static final int THRESHOLD = 10;
   static final int CACHE_SIZE = 15;

   public ReadManager(ICompilationUnit[] files, int length) {
      int threadCount = 0;

      try {
         Class runtime = Class.forName("java.lang.Runtime");
         Method m = runtime.getDeclaredMethod("availableProcessors");
         if (m != null) {
            Integer result = (Integer)m.invoke(Runtime.getRuntime(), null);
            threadCount = result + 1;
            if (threadCount < 2) {
               threadCount = 0;
            } else if (threadCount > 15) {
               threadCount = 15;
            }
         }
      } catch (IllegalAccessException var7) {
      } catch (ClassNotFoundException var8) {
      } catch (SecurityException var9) {
      } catch (NoSuchMethodException var10) {
      } catch (IllegalArgumentException var11) {
      } catch (InvocationTargetException var12) {
      }

      if (threadCount > 0) {
         synchronized(this) {
            this.units = new ICompilationUnit[length];
            System.arraycopy(files, 0, this.units, 0, length);
            this.nextFileToRead = 5;
            this.filesRead = new ICompilationUnit[15];
            this.contentsRead = new char[15][];
            this.readyToReadPosition = 0;
            this.nextAvailablePosition = 0;
            this.sleepingThreadCount = 0;
            this.readingThreads = new Thread[threadCount];
            int i = threadCount;

            while(--i >= 0) {
               this.readingThreads[i] = new Thread(this, "Compiler Source File Reader");
               this.readingThreads[i].setDaemon(true);
               this.readingThreads[i].start();
            }
         }
      }
   }

   public char[] getContents(ICompilationUnit unit) throws Error {
      if (this.readingThreads != null && this.units.length != 0) {
         boolean yield = false;
         char[] result = null;
         synchronized(this) {
            if (unit == this.filesRead[this.readyToReadPosition]) {
               for(result = this.contentsRead[this.readyToReadPosition];
                  result == this.readInProcessMarker || result == null;
                  result = this.contentsRead[this.readyToReadPosition]
               ) {
                  this.contentsRead[this.readyToReadPosition] = null;

                  try {
                     this.wait(250L);
                  } catch (InterruptedException var7) {
                  }

                  if (this.caughtException != null) {
                     if (this.caughtException instanceof Error) {
                        throw (Error)this.caughtException;
                     }

                     throw (RuntimeException)this.caughtException;
                  }
               }

               this.filesRead[this.readyToReadPosition] = null;
               this.contentsRead[this.readyToReadPosition] = null;
               if (++this.readyToReadPosition >= this.contentsRead.length) {
                  this.readyToReadPosition = 0;
               }

               if (this.sleepingThreadCount > 0) {
                  this.notify();
                  yield = this.sleepingThreadCount == this.readingThreads.length;
               }
            } else {
               int unitIndex = 0;
               int l = this.units.length;

               while(unitIndex < l && this.units[unitIndex] != unit) {
                  ++unitIndex;
               }

               if (unitIndex == this.units.length) {
                  this.units = new ICompilationUnit[0];
               } else if (unitIndex >= this.nextFileToRead) {
                  this.nextFileToRead = unitIndex + 5;
                  this.readyToReadPosition = 0;
                  this.nextAvailablePosition = 0;
                  this.filesRead = new ICompilationUnit[15];
                  this.contentsRead = new char[15][];
                  this.notifyAll();
               }
            }
         }

         if (yield) {
            Thread.yield();
         }

         return result != null ? result : unit.getContents();
      } else if (this.caughtException != null) {
         if (this.caughtException instanceof Error) {
            throw (Error)this.caughtException;
         } else {
            throw (RuntimeException)this.caughtException;
         }
      } else {
         return unit.getContents();
      }
   }

   @Override
   public void run() {
      try {
         while(this.readingThreads != null && this.nextFileToRead < this.units.length) {
            ICompilationUnit unit = null;
            int position = -1;
            synchronized(this) {
               if (this.readingThreads == null) {
                  return;
               }

               while(this.filesRead[this.nextAvailablePosition] != null) {
                  ++this.sleepingThreadCount;

                  try {
                     this.wait(250L);
                  } catch (InterruptedException var8) {
                  }

                  --this.sleepingThreadCount;
                  if (this.readingThreads == null) {
                     return;
                  }
               }

               if (this.nextFileToRead >= this.units.length) {
                  return;
               }

               unit = this.units[this.nextFileToRead++];
               position = this.nextAvailablePosition;
               if (++this.nextAvailablePosition >= this.contentsRead.length) {
                  this.nextAvailablePosition = 0;
               }

               this.filesRead[position] = unit;
               this.contentsRead[position] = this.readInProcessMarker;
            }

            char[] result = unit.getContents();
            synchronized(this) {
               if (this.filesRead[position] == unit) {
                  if (this.contentsRead[position] == null) {
                     this.notifyAll();
                  }

                  this.contentsRead[position] = result;
               }
            }
         }
      } catch (Error var10) {
         Error e = var10;
         synchronized(this) {
            this.caughtException = e;
            this.shutdown();
         }
      } catch (RuntimeException var11) {
         RuntimeException e = var11;
         synchronized(this) {
            this.caughtException = e;
            this.shutdown();
         }
      }
   }

   public synchronized void shutdown() {
      this.readingThreads = null;
      this.notifyAll();
   }
}
