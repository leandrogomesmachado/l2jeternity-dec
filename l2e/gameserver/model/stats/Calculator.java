package l2e.gameserver.model.stats;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.skills.funcs.Func;

public final class Calculator {
   private static final Func[] EMPTY_FUNCS = new Func[0];
   private Func[] _functions;

   public Calculator() {
      this._functions = EMPTY_FUNCS;
   }

   public Calculator(Calculator c) {
      this._functions = c._functions;
   }

   public static boolean equalsCals(Calculator c1, Calculator c2) {
      if (c1 == c2) {
         return true;
      } else if (c1 != null && c2 != null) {
         Func[] funcs1 = c1._functions;
         Func[] funcs2 = c2._functions;
         if (funcs1 == funcs2) {
            return true;
         } else if (funcs1.length != funcs2.length) {
            return false;
         } else if (funcs1.length == 0) {
            return true;
         } else {
            for(int i = 0; i < funcs1.length; ++i) {
               if (funcs1[i] != funcs2[i]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int size() {
      return this._functions.length;
   }

   public synchronized void addFunc(Func f) {
      Func[] funcs = this._functions;
      Func[] tmp = new Func[funcs.length + 1];
      int order = f.order;

      int i;
      for(i = 0; i < funcs.length && order >= funcs[i].order; ++i) {
         tmp[i] = funcs[i];
      }

      for(tmp[i] = f; i < funcs.length; ++i) {
         tmp[i + 1] = funcs[i];
      }

      this._functions = tmp;
   }

   public synchronized void removeFunc(Func f) {
      Func[] funcs = this._functions;
      Func[] tmp = new Func[funcs.length - 1];

      int i;
      for(i = 0; i < funcs.length && f != funcs[i]; ++i) {
         tmp[i] = funcs[i];
      }

      if (i != funcs.length) {
         ++i;

         while(i < funcs.length) {
            tmp[i - 1] = funcs[i];
            ++i;
         }

         if (tmp.length == 0) {
            this._functions = EMPTY_FUNCS;
         } else {
            this._functions = tmp;
         }
      }
   }

   public synchronized List<Stats> removeOwner(Object owner) {
      List<Stats> modifiedStats = new ArrayList<>();

      for(Func func : this._functions) {
         if (func.funcOwner == owner) {
            modifiedStats.add(func.stat);
            this.removeFunc(func);
         }
      }

      return modifiedStats;
   }

   public void calc(Env env) {
      for(Func func : this._functions) {
         func.calc(env);
      }
   }
}
