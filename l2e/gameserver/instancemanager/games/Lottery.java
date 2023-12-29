package l2e.gameserver.instancemanager.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Lottery {
   public static final long SECOND = 1000L;
   public static final long MINUTE = 60000L;
   protected static final Logger _log = Logger.getLogger(Lottery.class.getName());
   private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
   private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
   private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
   private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
   private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
   private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?";
   protected int _number = 1;
   protected long _prize = Config.ALT_LOTTERY_PRIZE;
   protected boolean _isSellingTickets = false;
   protected boolean _isStarted = false;
   protected long _enddate = System.currentTimeMillis();

   protected Lottery() {
      if (Config.ALLOW_LOTTERY) {
         new Lottery.startLottery().run();
      }
   }

   public static Lottery getInstance() {
      return Lottery.SingletonHolder._instance;
   }

   public int getId() {
      return this._number;
   }

   public long getPrize() {
      return this._prize;
   }

   public long getEndDate() {
      return this._enddate;
   }

   public void increasePrize(long count) {
      this._prize += count;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?");
         statement.setLong(1, this.getPrize());
         statement.setLong(2, this.getPrize());
         statement.setInt(3, this.getId());
         statement.execute();
         statement.close();
      } catch (SQLException var16) {
         _log.log(Level.WARNING, "Lottery: Could not increase current lottery prize: " + var16.getMessage(), (Throwable)var16);
      }
   }

   public boolean isSellableTickets() {
      return this._isSellingTickets;
   }

   public boolean isStarted() {
      return this._isStarted;
   }

   public int[] decodeNumbers(int enchant, int type2) {
      int[] res = new int[5];
      int id = 0;

      for(int nr = 1; enchant > 0; ++nr) {
         int val = enchant / 2;
         if ((long)val != Math.round((double)enchant / 2.0)) {
            res[id++] = nr;
         }

         enchant /= 2;
      }

      for(int var7 = 17; type2 > 0; ++var7) {
         int val = type2 / 2;
         if ((double)val != (double)type2 / 2.0) {
            res[id++] = var7;
         }

         type2 /= 2;
      }

      return res;
   }

   public long[] checkTicket(ItemInstance item) {
      return this.checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
   }

   public long[] checkTicket(int param1, int param2, int param3) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.IllegalStateException: Invalid switch case set: [[const(0)], [const(1), const(2), null], [const(3)], [const(4)], [const(5)]] for selector of type [J
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent.checkExprTypeBounds(SwitchHeadExprent.java:66)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExpr(VarTypeProcessor.java:135)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.checkTypeExprent(VarTypeProcessor.java:121)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.lambda$processVarTypes$1(VarTypeProcessor.java:109)
      //   at org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph.iterateExprents(DirectGraph.java:118)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.processVarTypes(VarTypeProcessor.java:109)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor.calculateVarTypes(VarTypeProcessor.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsProcessor.setVarVersions(VarVersionsProcessor.java:45)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarVersions(VarProcessor.java:43)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:204)
      //
      // Bytecode:
      // 000: bipush 2
      // 001: newarray 11
      // 003: dup
      // 004: bipush 0
      // 005: lconst_0
      // 006: lastore
      // 007: dup
      // 008: bipush 1
      // 009: lconst_0
      // 00a: lastore
      // 00b: astore 4
      // 00d: invokestatic l2e/gameserver/database/DatabaseFactory.getInstance ()Ll2e/gameserver/database/DatabaseFactory;
      // 010: invokevirtual l2e/gameserver/database/DatabaseFactory.getConnection ()Ljava/sql/Connection;
      // 013: astore 5
      // 015: aconst_null
      // 016: astore 6
      // 018: aload 5
      // 01a: ldc "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?"
      // 01c: invokeinterface java/sql/Connection.prepareStatement (Ljava/lang/String;)Ljava/sql/PreparedStatement; 2
      // 021: astore 7
      // 023: aconst_null
      // 024: astore 8
      // 026: aload 7
      // 028: bipush 1
      // 029: iload 1
      // 02a: invokeinterface java/sql/PreparedStatement.setInt (II)V 3
      // 02f: aload 7
      // 031: invokeinterface java/sql/PreparedStatement.executeQuery ()Ljava/sql/ResultSet; 1
      // 036: astore 9
      // 038: aconst_null
      // 039: astore 10
      // 03b: aload 9
      // 03d: invokeinterface java/sql/ResultSet.next ()Z 1
      // 042: ifeq 1e7
      // 045: aload 9
      // 047: ldc "number1"
      // 049: invokeinterface java/sql/ResultSet.getInt (Ljava/lang/String;)I 2
      // 04e: iload 2
      // 04f: iand
      // 050: istore 11
      // 052: aload 9
      // 054: ldc "number2"
      // 056: invokeinterface java/sql/ResultSet.getInt (Ljava/lang/String;)I 2
      // 05b: iload 3
      // 05c: iand
      // 05d: istore 12
      // 05f: iload 11
      // 061: ifne 0e5
      // 064: iload 12
      // 066: ifne 0e5
      // 069: aload 4
      // 06b: astore 13
      // 06d: aload 9
      // 06f: ifnull 094
      // 072: aload 10
      // 074: ifnull 08d
      // 077: aload 9
      // 079: invokeinterface java/sql/ResultSet.close ()V 1
      // 07e: goto 094
      // 081: astore 14
      // 083: aload 10
      // 085: aload 14
      // 087: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 08a: goto 094
      // 08d: aload 9
      // 08f: invokeinterface java/sql/ResultSet.close ()V 1
      // 094: aload 7
      // 096: ifnull 0bb
      // 099: aload 8
      // 09b: ifnull 0b4
      // 09e: aload 7
      // 0a0: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 0a5: goto 0bb
      // 0a8: astore 14
      // 0aa: aload 8
      // 0ac: aload 14
      // 0ae: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 0b1: goto 0bb
      // 0b4: aload 7
      // 0b6: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 0bb: aload 5
      // 0bd: ifnull 0e2
      // 0c0: aload 6
      // 0c2: ifnull 0db
      // 0c5: aload 5
      // 0c7: invokeinterface java/sql/Connection.close ()V 1
      // 0cc: goto 0e2
      // 0cf: astore 14
      // 0d1: aload 6
      // 0d3: aload 14
      // 0d5: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 0d8: goto 0e2
      // 0db: aload 5
      // 0dd: invokeinterface java/sql/Connection.close ()V 1
      // 0e2: aload 13
      // 0e4: areturn
      // 0e5: bipush 0
      // 0e6: istore 13
      // 0e8: bipush 1
      // 0e9: istore 14
      // 0eb: iload 14
      // 0ed: bipush 16
      // 0ef: if_icmpgt 131
      // 0f2: iload 11
      // 0f4: bipush 2
      // 0f5: idiv
      // 0f6: istore 15
      // 0f8: iload 15
      // 0fa: i2l
      // 0fb: iload 11
      // 0fd: i2d
      // 0fe: ldc2_w 2.0
      // 101: ddiv
      // 102: invokestatic java/lang/Math.round (D)J
      // 105: lcmp
      // 106: ifeq 10c
      // 109: iinc 13 1
      // 10c: iload 12
      // 10e: bipush 2
      // 10f: idiv
      // 110: istore 16
      // 112: iload 16
      // 114: i2d
      // 115: iload 12
      // 117: i2d
      // 118: ldc2_w 2.0
      // 11b: ddiv
      // 11c: dcmpl
      // 11d: ifeq 123
      // 120: iinc 13 1
      // 123: iload 15
      // 125: istore 11
      // 127: iload 16
      // 129: istore 12
      // 12b: iinc 14 1
      // 12e: goto 0eb
      // 131: iload 13
      // 133: tableswitch 107 0 5 37 107 107 84 61 40
      // 158: goto 1ac
      // 15b: aload 4
      // 15d: bipush 0
      // 15e: lconst_1
      // 15f: lastore
      // 160: aload 4
      // 162: bipush 1
      // 163: aload 9
      // 165: ldc "prize1"
      // 167: invokeinterface java/sql/ResultSet.getLong (Ljava/lang/String;)J 2
      // 16c: lastore
      // 16d: goto 1ac
      // 170: aload 4
      // 172: bipush 0
      // 173: ldc2_w 2
      // 176: lastore
      // 177: aload 4
      // 179: bipush 1
      // 17a: aload 9
      // 17c: ldc "prize2"
      // 17e: invokeinterface java/sql/ResultSet.getLong (Ljava/lang/String;)J 2
      // 183: lastore
      // 184: goto 1ac
      // 187: aload 4
      // 189: bipush 0
      // 18a: ldc2_w 3
      // 18d: lastore
      // 18e: aload 4
      // 190: bipush 1
      // 191: aload 9
      // 193: ldc "prize3"
      // 195: invokeinterface java/sql/ResultSet.getLong (Ljava/lang/String;)J 2
      // 19a: lastore
      // 19b: goto 1ac
      // 19e: aload 4
      // 1a0: bipush 0
      // 1a1: ldc2_w 4
      // 1a4: lastore
      // 1a5: aload 4
      // 1a7: bipush 1
      // 1a8: ldc2_w 200
      // 1ab: lastore
      // 1ac: getstatic l2e/gameserver/Config.DEBUG Z
      // 1af: ifeq 1e7
      // 1b2: getstatic l2e/gameserver/instancemanager/games/Lottery._log Ljava/util/logging/Logger;
      // 1b5: new java/lang/StringBuilder
      // 1b8: dup
      // 1b9: invokespecial java/lang/StringBuilder.<init> ()V
      // 1bc: ldc "count: "
      // 1be: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 1c1: iload 13
      // 1c3: invokevirtual java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
      // 1c6: ldc ", id: "
      // 1c8: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 1cb: iload 1
      // 1cc: invokevirtual java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
      // 1cf: ldc ", enchant: "
      // 1d1: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 1d4: iload 2
      // 1d5: invokevirtual java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
      // 1d8: ldc ", type2: "
      // 1da: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 1dd: iload 3
      // 1de: invokevirtual java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
      // 1e1: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 1e4: invokevirtual java/util/logging/Logger.warning (Ljava/lang/String;)V
      // 1e7: aload 9
      // 1e9: ifnull 246
      // 1ec: aload 10
      // 1ee: ifnull 207
      // 1f1: aload 9
      // 1f3: invokeinterface java/sql/ResultSet.close ()V 1
      // 1f8: goto 246
      // 1fb: astore 11
      // 1fd: aload 10
      // 1ff: aload 11
      // 201: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 204: goto 246
      // 207: aload 9
      // 209: invokeinterface java/sql/ResultSet.close ()V 1
      // 20e: goto 246
      // 211: astore 11
      // 213: aload 11
      // 215: astore 10
      // 217: aload 11
      // 219: athrow
      // 21a: astore 17
      // 21c: aload 9
      // 21e: ifnull 243
      // 221: aload 10
      // 223: ifnull 23c
      // 226: aload 9
      // 228: invokeinterface java/sql/ResultSet.close ()V 1
      // 22d: goto 243
      // 230: astore 18
      // 232: aload 10
      // 234: aload 18
      // 236: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 239: goto 243
      // 23c: aload 9
      // 23e: invokeinterface java/sql/ResultSet.close ()V 1
      // 243: aload 17
      // 245: athrow
      // 246: aload 7
      // 248: ifnull 2a5
      // 24b: aload 8
      // 24d: ifnull 266
      // 250: aload 7
      // 252: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 257: goto 2a5
      // 25a: astore 9
      // 25c: aload 8
      // 25e: aload 9
      // 260: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 263: goto 2a5
      // 266: aload 7
      // 268: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 26d: goto 2a5
      // 270: astore 9
      // 272: aload 9
      // 274: astore 8
      // 276: aload 9
      // 278: athrow
      // 279: astore 19
      // 27b: aload 7
      // 27d: ifnull 2a2
      // 280: aload 8
      // 282: ifnull 29b
      // 285: aload 7
      // 287: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 28c: goto 2a2
      // 28f: astore 20
      // 291: aload 8
      // 293: aload 20
      // 295: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 298: goto 2a2
      // 29b: aload 7
      // 29d: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 2a2: aload 19
      // 2a4: athrow
      // 2a5: aload 5
      // 2a7: ifnull 304
      // 2aa: aload 6
      // 2ac: ifnull 2c5
      // 2af: aload 5
      // 2b1: invokeinterface java/sql/Connection.close ()V 1
      // 2b6: goto 304
      // 2b9: astore 7
      // 2bb: aload 6
      // 2bd: aload 7
      // 2bf: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 2c2: goto 304
      // 2c5: aload 5
      // 2c7: invokeinterface java/sql/Connection.close ()V 1
      // 2cc: goto 304
      // 2cf: astore 7
      // 2d1: aload 7
      // 2d3: astore 6
      // 2d5: aload 7
      // 2d7: athrow
      // 2d8: astore 21
      // 2da: aload 5
      // 2dc: ifnull 301
      // 2df: aload 6
      // 2e1: ifnull 2fa
      // 2e4: aload 5
      // 2e6: invokeinterface java/sql/Connection.close ()V 1
      // 2eb: goto 301
      // 2ee: astore 22
      // 2f0: aload 6
      // 2f2: aload 22
      // 2f4: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 2f7: goto 301
      // 2fa: aload 5
      // 2fc: invokeinterface java/sql/Connection.close ()V 1
      // 301: aload 21
      // 303: athrow
      // 304: goto 334
      // 307: astore 5
      // 309: getstatic l2e/gameserver/instancemanager/games/Lottery._log Ljava/util/logging/Logger;
      // 30c: getstatic java/util/logging/Level.WARNING Ljava/util/logging/Level;
      // 30f: new java/lang/StringBuilder
      // 312: dup
      // 313: invokespecial java/lang/StringBuilder.<init> ()V
      // 316: ldc "Lottery: Could not check lottery ticket #"
      // 318: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 31b: iload 1
      // 31c: invokevirtual java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
      // 31f: ldc ": "
      // 321: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 324: aload 5
      // 326: invokevirtual java/sql/SQLException.getMessage ()Ljava/lang/String;
      // 329: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 32c: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 32f: aload 5
      // 331: invokevirtual java/util/logging/Logger.log (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
      // 334: aload 4
      // 336: areturn
   }

   private static class SingletonHolder {
      protected static final Lottery _instance = new Lottery();
   }

   private class finishLottery implements Runnable {
      protected finishLottery() {
      }

      @Override
      public void run() {
         if (Config.DEBUG) {
            Lottery._log.info("Lottery: Ending lottery #" + Lottery.this.getId() + ".");
         }

         int[] luckynums = new int[5];
         int luckynum = 0;

         for(int i = 0; i < 5; ++i) {
            boolean found = true;

            while(found) {
               luckynum = Rnd.get(20) + 1;
               found = false;

               for(int j = 0; j < i; ++j) {
                  if (luckynums[j] == luckynum) {
                     found = true;
                  }
               }
            }

            luckynums[i] = luckynum;
         }

         if (Config.DEBUG) {
            Lottery._log
               .info(
                  "Lottery: The lucky numbers are "
                     + luckynums[0]
                     + ", "
                     + luckynums[1]
                     + ", "
                     + luckynums[2]
                     + ", "
                     + luckynums[3]
                     + ", "
                     + luckynums[4]
                     + "."
               );
         }

         int enchant = 0;
         int type2 = 0;

         for(int i = 0; i < 5; ++i) {
            if (luckynums[i] < 17) {
               enchant = (int)((double)enchant + Math.pow(2.0, (double)(luckynums[i] - 1)));
            } else {
               type2 = (int)((double)type2 + Math.pow(2.0, (double)(luckynums[i] - 17)));
            }
         }

         if (Config.DEBUG) {
            Lottery._log.info("Lottery: Encoded lucky numbers are " + enchant + ", " + type2);
         }

         int count1 = 0;
         int count2 = 0;
         int count3 = 0;
         int count4 = 0;

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?");
         ) {
            statement.setInt(1, Lottery.this.getId());

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  int curenchant = rset.getInt("enchant_level") & enchant;
                  int curtype2 = rset.getInt("custom_type2") & type2;
                  if (curenchant != 0 || curtype2 != 0) {
                     int count = 0;

                     for(int i = 1; i <= 16; ++i) {
                        int val = curenchant / 2;
                        if ((long)val != Math.round((double)curenchant / 2.0)) {
                           ++count;
                        }

                        int val2 = curtype2 / 2;
                        if ((double)val2 != (double)curtype2 / 2.0) {
                           ++count;
                        }

                        curenchant = val;
                        curtype2 = val2;
                     }

                     if (count == 5) {
                        ++count1;
                     } else if (count == 4) {
                        ++count2;
                     } else if (count == 3) {
                        ++count3;
                     } else if (count > 0) {
                        ++count4;
                     }
                  }
               }
            }
         } catch (SQLException var147) {
            Lottery._log.log(Level.WARNING, "Lottery: Could restore lottery data: " + var147.getMessage(), (Throwable)var147);
         }

         long prize4 = (long)count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
         long prize1 = 0L;
         long prize2 = 0L;
         long prize3 = 0L;
         if (count1 > 0) {
            prize1 = (long)((float)(Lottery.this.getPrize() - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / (float)count1);
         }

         if (count2 > 0) {
            prize2 = (long)((float)(Lottery.this.getPrize() - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / (float)count2);
         }

         if (count3 > 0) {
            prize3 = (long)((float)(Lottery.this.getPrize() - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / (float)count3);
         }

         if (Config.DEBUG) {
            Lottery._log.info("Lottery: " + count1 + " players with all FIVE numbers each win " + prize1 + ".");
            Lottery._log.info("Lottery: " + count2 + " players with FOUR numbers each win " + prize2 + ".");
            Lottery._log.info("Lottery: " + count3 + " players with THREE numbers each win " + prize3 + ".");
            Lottery._log.info("Lottery: " + count4 + " players with ONE or TWO numbers each win " + prize4 + ".");
         }

         long newprize = Lottery.this.getPrize() - (prize1 + prize2 + prize3 + prize4);
         if (Config.DEBUG) {
            Lottery._log.info("Lottery: Jackpot for next lottery is " + newprize + ".");
         }

         if (count1 > 0) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER);
            sm.addNumber(Lottery.this.getId());
            sm.addItemNumber(Lottery.this.getPrize());
            sm.addItemNumber((long)count1);
            Announcements.getInstance().announceToAll(sm);
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER);
            sm.addNumber(Lottery.this.getId());
            sm.addItemNumber(Lottery.this.getPrize());
            Announcements.getInstance().announceToAll(sm);
         }

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
               "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?"
            );
         ) {
            statement.setLong(1, Lottery.this.getPrize());
            statement.setLong(2, newprize);
            statement.setInt(3, enchant);
            statement.setInt(4, type2);
            statement.setLong(5, prize1);
            statement.setLong(6, prize2);
            statement.setLong(7, prize3);
            statement.setInt(8, Lottery.this.getId());
            statement.execute();
         } catch (SQLException var140) {
            Lottery._log.log(Level.WARNING, "Lottery: Could not store finished lottery data: " + var140.getMessage(), (Throwable)var140);
         }

         ThreadPoolManager.getInstance().schedule(Lottery.this.new startLottery(), 60000L);
         ++Lottery.this._number;
         Lottery.this._isStarted = false;
      }
   }

   private class startLottery extends RunnableImpl {
      protected startLottery() {
      }

      @Override
      public void runImpl() throws Exception {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            label875: {
               Object statement = null;

               try (
                  Statement statementx = con.createStatement();
                  ResultSet rset = statementx.executeQuery(
                     "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1"
                  );
               ) {
                  if (!rset.next()) {
                     break label875;
                  }

                  Lottery.this._number = rset.getInt("idnr");
                  if (rset.getInt("finished") == 1) {
                     ++Lottery.this._number;
                     Lottery.this._prize = rset.getLong("newprize");
                     break label875;
                  }

                  Lottery.this._prize = rset.getLong("prize");
                  Lottery.this._enddate = rset.getLong("enddate");
                  if (Lottery.this._enddate <= System.currentTimeMillis() + 120000L) {
                     Lottery.this.new finishLottery().run();
                     return;
                  }

                  if (Lottery.this._enddate <= System.currentTimeMillis()) {
                     break label875;
                  }

                  Lottery.this._isStarted = true;
                  ThreadPoolManager.getInstance().schedule(Lottery.this.new finishLottery(), Lottery.this._enddate - System.currentTimeMillis());
                  if (Lottery.this._enddate > System.currentTimeMillis() + 720000L) {
                     Lottery.this._isSellingTickets = true;
                     ThreadPoolManager.getInstance()
                        .schedule(Lottery.this.new stopSellingTickets(), Lottery.this._enddate - System.currentTimeMillis() - 600000L);
                  }
               }

               return;
            }
         } catch (SQLException var158) {
            Lottery._log.log(Level.WARNING, "Lottery: Could not restore lottery data: " + var158.getMessage(), (Throwable)var158);
         }

         if (Config.DEBUG) {
            Lottery._log.info("Lottery: Starting ticket sell for lottery #" + Lottery.this.getId() + ".");
         }

         Lottery.this._isSellingTickets = true;
         Lottery.this._isStarted = true;
         Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + Lottery.this.getId() + ".");
         Calendar finishtime = Calendar.getInstance();
         finishtime.setTimeInMillis(Lottery.this._enddate);
         finishtime.set(12, 0);
         finishtime.set(13, 0);
         if (finishtime.get(7) == 1) {
            finishtime.set(11, 19);
            Lottery.this._enddate = finishtime.getTimeInMillis();
            Lottery.this._enddate += 604800000L;
         } else {
            finishtime.set(7, 1);
            finishtime.set(11, 19);
            Lottery.this._enddate = finishtime.getTimeInMillis();
         }

         ThreadPoolManager.getInstance().schedule(Lottery.this.new stopSellingTickets(), Lottery.this._enddate - System.currentTimeMillis() - 600000L);
         ThreadPoolManager.getInstance().schedule(Lottery.this.new finishLottery(), Lottery.this._enddate - System.currentTimeMillis());

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)");
         ) {
            statement.setInt(1, 1);
            statement.setInt(2, Lottery.this.getId());
            statement.setLong(3, Lottery.this.getEndDate());
            statement.setLong(4, Lottery.this.getPrize());
            statement.setLong(5, Lottery.this.getPrize());
            statement.execute();
         } catch (SQLException var151) {
            Lottery._log.log(Level.WARNING, "Lottery: Could not store new lottery data: " + var151.getMessage(), (Throwable)var151);
         }
      }
   }

   private class stopSellingTickets implements Runnable {
      protected stopSellingTickets() {
      }

      @Override
      public void run() {
         if (Config.DEBUG) {
            Lottery._log.info("Lottery: Stopping ticket sell for lottery #" + Lottery.this.getId() + ".");
         }

         Lottery.this._isSellingTickets = false;
         Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_TEMP_SUSPENDED));
      }
   }
}
