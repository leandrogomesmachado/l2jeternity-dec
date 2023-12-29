package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _723_FortheSakeoftheTerritoryGoddard extends TerritoryWarSuperClass {
   public static String qn1 = "_723_FortheSakeoftheTerritoryGoddard";
   public static int qnu = 723;
   public static String qna = "";

   public _723_FortheSakeoftheTerritoryGoddard() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36505;
      this.TERRITORY_ID = 87;
      this.LEADER_IDS = new int[]{36544, 36546, 36549, 36597};
      this.GUARD_IDS = new int[]{36545, 36547, 36548};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_GODDARD_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
