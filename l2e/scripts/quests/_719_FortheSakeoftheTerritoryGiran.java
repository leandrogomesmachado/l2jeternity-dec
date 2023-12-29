package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _719_FortheSakeoftheTerritoryGiran extends TerritoryWarSuperClass {
   public static String qn1 = "_719_FortheSakeoftheTerritoryGiran";
   public static int qnu = 719;
   public static String qna = "";

   public _719_FortheSakeoftheTerritoryGiran() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36501;
      this.TERRITORY_ID = 83;
      this.LEADER_IDS = new int[]{36520, 36522, 36525, 36593};
      this.GUARD_IDS = new int[]{36521, 36523, 36524};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_GIRAN_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
