package l2e.scripts.quests;

public class _731_ProtecttheMilitaryAssociationLeader extends TerritoryWarSuperClass {
   public static String qn1 = "_731_ProtecttheMilitaryAssociationLeader";
   public static int qnu = 731;
   public static String qna = "";

   public _731_ProtecttheMilitaryAssociationLeader() {
      super(qnu, qn1, qna);
      this.NPC_IDS = new int[]{36508, 36514, 36520, 36526, 36532, 36538, 36544, 36550, 36556};
      qn = qn1;
      this.registerAttackIds();
   }

   @Override
   public int getTerritoryIdForThisNPCId(int npcid) {
      return 81 + (npcid - 36508) / 6;
   }
}
