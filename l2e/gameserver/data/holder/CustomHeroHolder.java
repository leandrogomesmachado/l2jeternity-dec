package l2e.gameserver.data.holder;

import java.util.logging.Logger;
import l2e.gameserver.model.actor.Player;

public class CustomHeroHolder {
   private static final Logger _log = Logger.getLogger(CustomHeroHolder.class.getName());
   static String DATA_INSERT = "REPLACE INTO characters_custom_data (charId, char_name, hero) VALUES (?,?,?)";
   static String DATA_DELETE = "UPDATE characters_custom_data SET hero = ? WHERE charId = ?";

   public static void updateDatabase(Player var0, boolean var1) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:643)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:574)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:574)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:574)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:574)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:574)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.mergeVars(VarDefinitionHelper.java:516)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.setVarDefinitions(VarDefinitionHelper.java:222)
      //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarDefinitions(VarProcessor.java:49)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:383)
      //
      // Bytecode:
      // 000: invokestatic l2e/gameserver/database/DatabaseFactory.getInstance ()Ll2e/gameserver/database/DatabaseFactory;
      // 003: invokevirtual l2e/gameserver/database/DatabaseFactory.getConnection ()Ljava/sql/Connection;
      // 006: astore 2
      // 007: aconst_null
      // 008: astore 3
      // 009: aload 0
      // 00a: ifnonnull 030
      // 00d: aload 2
      // 00e: ifnull 02f
      // 011: aload 3
      // 012: ifnull 029
      // 015: aload 2
      // 016: invokeinterface java/sql/Connection.close ()V 1
      // 01b: goto 02f
      // 01e: astore 4
      // 020: aload 3
      // 021: aload 4
      // 023: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 026: goto 02f
      // 029: aload 2
      // 02a: invokeinterface java/sql/Connection.close ()V 1
      // 02f: return
      // 030: aload 0
      // 031: invokevirtual l2e/gameserver/model/actor/Player.getName ()Ljava/lang/String;
      // 034: astore 4
      // 036: aload 0
      // 037: invokevirtual l2e/gameserver/model/actor/Player.getObjectId ()I
      // 03a: istore 5
      // 03c: iload 1
      // 03d: istore 6
      // 03f: aload 2
      // 040: iload 6
      // 042: ifeq 04b
      // 045: getstatic l2e/gameserver/data/holder/CustomHeroHolder.DATA_INSERT Ljava/lang/String;
      // 048: goto 04e
      // 04b: getstatic l2e/gameserver/data/holder/CustomHeroHolder.DATA_DELETE Ljava/lang/String;
      // 04e: invokeinterface java/sql/Connection.prepareStatement (Ljava/lang/String;)Ljava/sql/PreparedStatement; 2
      // 053: astore 7
      // 055: iload 1
      // 056: ifeq 088
      // 059: aload 7
      // 05b: bipush 1
      // 05c: iload 5
      // 05e: invokeinterface java/sql/PreparedStatement.setInt (II)V 3
      // 063: aload 7
      // 065: bipush 2
      // 066: aload 4
      // 068: invokeinterface java/sql/PreparedStatement.setString (ILjava/lang/String;)V 3
      // 06d: aload 7
      // 06f: bipush 3
      // 070: bipush 1
      // 071: invokeinterface java/sql/PreparedStatement.setInt (II)V 3
      // 076: aload 7
      // 078: invokeinterface java/sql/PreparedStatement.execute ()Z 1
      // 07d: pop
      // 07e: aload 7
      // 080: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 085: goto 0b2
      // 088: aload 7
      // 08a: bipush 1
      // 08b: iload 1
      // 08c: ifeq 093
      // 08f: bipush 0
      // 090: goto 094
      // 093: bipush 0
      // 094: invokeinterface java/sql/PreparedStatement.setInt (II)V 3
      // 099: aload 7
      // 09b: bipush 2
      // 09c: iload 5
      // 09e: invokeinterface java/sql/PreparedStatement.setInt (II)V 3
      // 0a3: aload 7
      // 0a5: invokeinterface java/sql/PreparedStatement.execute ()Z 1
      // 0aa: pop
      // 0ab: aload 7
      // 0ad: invokeinterface java/sql/PreparedStatement.close ()V 1
      // 0b2: aload 2
      // 0b3: ifnull 106
      // 0b6: aload 3
      // 0b7: ifnull 0ce
      // 0ba: aload 2
      // 0bb: invokeinterface java/sql/Connection.close ()V 1
      // 0c0: goto 106
      // 0c3: astore 4
      // 0c5: aload 3
      // 0c6: aload 4
      // 0c8: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 0cb: goto 106
      // 0ce: aload 2
      // 0cf: invokeinterface java/sql/Connection.close ()V 1
      // 0d4: goto 106
      // 0d7: astore 4
      // 0d9: aload 4
      // 0db: astore 3
      // 0dc: aload 4
      // 0de: athrow
      // 0df: astore 8
      // 0e1: aload 2
      // 0e2: ifnull 103
      // 0e5: aload 3
      // 0e6: ifnull 0fd
      // 0e9: aload 2
      // 0ea: invokeinterface java/sql/Connection.close ()V 1
      // 0ef: goto 103
      // 0f2: astore 9
      // 0f4: aload 3
      // 0f5: aload 9
      // 0f7: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 0fa: goto 103
      // 0fd: aload 2
      // 0fe: invokeinterface java/sql/Connection.close ()V 1
      // 103: aload 8
      // 105: athrow
      // 106: goto 112
      // 109: astore 2
      // 10a: getstatic l2e/gameserver/data/holder/CustomHeroHolder._log Ljava/util/logging/Logger;
      // 10d: ldc "Error: could not update database"
      // 10f: invokevirtual java/util/logging/Logger.warning (Ljava/lang/String;)V
      // 112: return
   }
}
