package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends Npc {
   public SymbolMakerInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.SymbolMakerInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      return "data/html/symbolmaker/SymbolMaker.htm";
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return false;
   }
}
