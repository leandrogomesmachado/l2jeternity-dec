package l2e.scripts.quests;

public class _732_ProtecttheReligiousAssociationLeader extends TerritoryWarSuperClass {
   public static String qn1 = "_732_ProtecttheReligiousAssociationLeader";
   public static int qnu = 732;
   public static String qna = "";

   public _732_ProtecttheReligiousAssociationLeader() {
      super(qnu, qn1, qna);
      this.NPC_IDS = new int[]{36510, 36516, 36522, 36528, 36534, 36540, 36546, 36552, 36558};
      qn = qn1;
      this.registerAttackIds();
   }

   @Override
   public int getTerritoryIdForThisNPCId(int npcid) {
      return 81 + (npcid - 36510) / 6;
   }
}
