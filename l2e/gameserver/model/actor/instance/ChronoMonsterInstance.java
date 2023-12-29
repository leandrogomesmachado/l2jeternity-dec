package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class ChronoMonsterInstance extends MonsterInstance {
   private Player _owner;
   private int _lvlUp;

   public ChronoMonsterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ChronoMonsterInstance);
      this.setAI(new ChronoMonsterInstance.L2ChronoAI(this));
      this._lvlUp = 0;
   }

   public final Player getOwner() {
      return this._owner;
   }

   public void setOwner(Player newOwner) {
      this._owner = newOwner;
   }

   @Override
   public Creature getMostHated() {
      return null;
   }

   public void setLevelUp(int lvl) {
      this._lvlUp = lvl;
   }

   public int getLevelUp() {
      return this._lvlUp;
   }

   @Override
   public boolean isMonster() {
      return false;
   }

   class L2ChronoAI extends DefaultAI {
      public L2ChronoAI(Attackable accessor) {
         super(accessor);
      }

      @Override
      protected void onEvtThink() {
         if (!this._actor.isAllSkillsDisabled()) {
            if (this.getIntention() == CtrlIntention.ATTACK) {
               this.setIntention(CtrlIntention.ACTIVE);
            }
         }
      }
   }
}
