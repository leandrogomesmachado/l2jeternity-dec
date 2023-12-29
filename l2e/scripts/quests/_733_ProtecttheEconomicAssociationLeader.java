package l2e.scripts.quests;

public class _733_ProtecttheEconomicAssociationLeader extends TerritoryWarSuperClass {
   public static String qn1 = "_733_ProtecttheEconomicAssociationLeader";
   public static int qnu = 733;
   public static String qna = "";

   public _733_ProtecttheEconomicAssociationLeader() {
      super(qnu, qn1, qna);
      this.NPC_IDS = new int[]{36513, 36519, 36525, 36531, 36537, 36543, 36549, 36555, 36561};
      qn = qn1;
      this.registerAttackIds();
   }

   @Override
   public int getTerritoryIdForThisNPCId(int npcid) {
      return 81 + (npcid - 36513) / 6;
   }
}
