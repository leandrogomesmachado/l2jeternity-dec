package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _722_FortheSakeoftheTerritoryInnadril extends TerritoryWarSuperClass {
   public static String qn1 = "_722_FortheSakeoftheTerritoryInnadril";
   public static int qnu = 722;
   public static String qna = "";

   public _722_FortheSakeoftheTerritoryInnadril() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36504;
      this.TERRITORY_ID = 86;
      this.LEADER_IDS = new int[]{36538, 36540, 36543, 36596};
      this.GUARD_IDS = new int[]{36539, 36541, 36542};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_INNADRIL_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
