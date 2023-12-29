package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _718_FortheSakeoftheTerritoryDion extends TerritoryWarSuperClass {
   public static String qn1 = "_718_FortheSakeoftheTerritoryDion";
   public static int qnu = 718;
   public static String qna = "";

   public _718_FortheSakeoftheTerritoryDion() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36500;
      this.TERRITORY_ID = 82;
      this.LEADER_IDS = new int[]{36514, 36516, 36519, 36592};
      this.GUARD_IDS = new int[]{36515, 36517, 36518};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_DION_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
