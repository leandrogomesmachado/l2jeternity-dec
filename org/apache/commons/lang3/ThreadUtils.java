package org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ThreadUtils {
   public static final ThreadUtils.AlwaysTruePredicate ALWAYS_TRUE_PREDICATE = new ThreadUtils.AlwaysTruePredicate();

   public static Thread findThreadById(long threadId, ThreadGroup threadGroup) {
      Validate.isTrue(threadGroup != null, "The thread group must not be null");
      Thread thread = findThreadById(threadId);
      return thread != null && threadGroup.equals(thread.getThreadGroup()) ? thread : null;
   }

   public static Thread findThreadById(long threadId, String threadGroupName) {
      Validate.isTrue(threadGroupName != null, "The thread group name must not be null");
      Thread thread = findThreadById(threadId);
      return thread != null && thread.getThreadGroup() != null && thread.getThreadGroup().getName().equals(threadGroupName) ? thread : null;
   }

   public static Collection<Thread> findThreadsByName(String threadName, ThreadGroup threadGroup) {
      return findThreads(threadGroup, false, new ThreadUtils.NamePredicate(threadName));
   }

   public static Collection<Thread> findThreadsByName(String threadName, String threadGroupName) {
      Validate.isTrue(threadName != null, "The thread name must not be null");
      Validate.isTrue(threadGroupName != null, "The thread group name must not be null");
      Collection<ThreadGroup> threadGroups = findThreadGroups(new ThreadUtils.NamePredicate(threadGroupName));
      if (threadGroups.isEmpty()) {
         return Collections.emptyList();
      } else {
         Collection<Thread> result = new ArrayList<>();
         ThreadUtils.NamePredicate threadNamePredicate = new ThreadUtils.NamePredicate(threadName);

         for(ThreadGroup group : threadGroups) {
            result.addAll(findThreads(group, false, threadNamePredicate));
         }

         return Collections.unmodifiableCollection(result);
      }
   }

   public static Collection<ThreadGroup> findThreadGroupsByName(String threadGroupName) {
      return findThreadGroups(new ThreadUtils.NamePredicate(threadGroupName));
   }

   public static Collection<ThreadGroup> getAllThreadGroups() {
      return findThreadGroups(ALWAYS_TRUE_PREDICATE);
   }

   public static ThreadGroup getSystemThreadGroup() {
      ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

      while(threadGroup.getParent() != null) {
         threadGroup = threadGroup.getParent();
      }

      return threadGroup;
   }

   public static Collection<Thread> getAllThreads() {
      return findThreads(ALWAYS_TRUE_PREDICATE);
   }

   public static Collection<Thread> findThreadsByName(String threadName) {
      return findThreads(new ThreadUtils.NamePredicate(threadName));
   }

   public static Thread findThreadById(long threadId) {
      Collection<Thread> result = findThreads(new ThreadUtils.ThreadIdPredicate(threadId));
      return result.isEmpty() ? null : result.iterator().next();
   }

   public static Collection<Thread> findThreads(ThreadUtils.ThreadPredicate predicate) {
      return findThreads(getSystemThreadGroup(), true, predicate);
   }

   public static Collection<ThreadGroup> findThreadGroups(ThreadUtils.ThreadGroupPredicate predicate) {
      return findThreadGroups(getSystemThreadGroup(), true, predicate);
   }

   public static Collection<Thread> findThreads(ThreadGroup group, boolean recurse, ThreadUtils.ThreadPredicate predicate) {
      Validate.isTrue(group != null, "The group must not be null");
      Validate.isTrue(predicate != null, "The predicate must not be null");
      int count = group.activeCount();

      Thread[] threads;
      do {
         threads = new Thread[count + count / 2 + 1];
         count = group.enumerate(threads, recurse);
      } while(count >= threads.length);

      List<Thread> result = new ArrayList<>(count);

      for(int i = 0; i < count; ++i) {
         if (predicate.test(threads[i])) {
            result.add(threads[i]);
         }
      }

      return Collections.unmodifiableCollection(result);
   }

   public static Collection<ThreadGroup> findThreadGroups(ThreadGroup group, boolean recurse, ThreadUtils.ThreadGroupPredicate predicate) {
      Validate.isTrue(group != null, "The group must not be null");
      Validate.isTrue(predicate != null, "The predicate must not be null");
      int count = group.activeGroupCount();

      ThreadGroup[] threadGroups;
      do {
         threadGroups = new ThreadGroup[count + count / 2 + 1];
         count = group.enumerate(threadGroups, recurse);
      } while(count >= threadGroups.length);

      List<ThreadGroup> result = new ArrayList<>(count);

      for(int i = 0; i < count; ++i) {
         if (predicate.test(threadGroups[i])) {
            result.add(threadGroups[i]);
         }
      }

      return Collections.unmodifiableCollection(result);
   }

   private static final class AlwaysTruePredicate implements ThreadUtils.ThreadPredicate, ThreadUtils.ThreadGroupPredicate {
      private AlwaysTruePredicate() {
      }

      @Override
      public boolean test(ThreadGroup threadGroup) {
         return true;
      }

      @Override
      public boolean test(Thread thread) {
         return true;
      }
   }

   public static class NamePredicate implements ThreadUtils.ThreadPredicate, ThreadUtils.ThreadGroupPredicate {
      private final String name;

      public NamePredicate(String name) {
         Validate.isTrue(name != null, "The name must not be null");
         this.name = name;
      }

      @Override
      public boolean test(ThreadGroup threadGroup) {
         return threadGroup != null && threadGroup.getName().equals(this.name);
      }

      @Override
      public boolean test(Thread thread) {
         return thread != null && thread.getName().equals(this.name);
      }
   }

   public interface ThreadGroupPredicate {
      boolean test(ThreadGroup var1);
   }

   public static class ThreadIdPredicate implements ThreadUtils.ThreadPredicate {
      private final long threadId;

      public ThreadIdPredicate(long threadId) {
         if (threadId <= 0L) {
            throw new IllegalArgumentException("The thread id must be greater than zero");
         } else {
            this.threadId = threadId;
         }
      }

      @Override
      public boolean test(Thread thread) {
         return thread != null && thread.getId() == this.threadId;
      }
   }

   public interface ThreadPredicate {
      boolean test(Thread var1);
   }
}
