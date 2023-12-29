package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _721_FortheSakeoftheTerritoryAden extends TerritoryWarSuperClass {
   public static String qn1 = "_721_FortheSakeoftheTerritoryAden";
   public static int qnu = 721;
   public static String qna = "";

   public _721_FortheSakeoftheTerritoryAden() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36503;
      this.TERRITORY_ID = 85;
      this.LEADER_IDS = new int[]{36532, 36534, 36537, 36595};
      this.GUARD_IDS = new int[]{36533, 36535, 36536};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_ADEN_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
