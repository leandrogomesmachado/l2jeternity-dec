package l2e.gameserver.model.actor.instance;

import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;

public class TreasureChestInstance extends ChestInstance {
   private static final int TREASURE_BOMB_ID = 4143;

   public TreasureChestInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void tryOpen(Player opener, Skill skill) {
      double chance = this.calcChance(opener, skill);
      if (chance > 0.0 && Rnd.chance(chance)) {
         this.addDamageHate(opener, 10000, 0);
         this.doDie(opener);
         opener.getCounters().addAchivementInfo("treasureBoxesOpened", 0, -1L, false, false, false);
      } else {
         this.fakeOpen(opener);
      }
   }

   public double calcChance(Player opener, Skill skill) {
      double chance = (double)skill.getActivateRate();
      int npcLvl = this.getLevel();
      if (!this.getTemplate().isCommonChest()) {
         double levelmod = (double)skill.getMagicLevel() - (double)npcLvl;
         chance += levelmod * (double)skill.getLevelModifier();
         if (npcLvl - opener.getLevel() >= 5) {
            chance += (double)((opener.getLevel() - npcLvl) * 10);
         }
      } else {
         int npcLvlDiff = npcLvl - opener.getLevel();
         int baseDiff = opener.getLevel() <= 77 ? 6 : 5;
         if (npcLvlDiff >= baseDiff) {
            chance = 0.0;
         }
      }

      if (chance < 0.0) {
         chance = 1.0;
      }

      return chance;
   }

   private void fakeOpen(Creature opener) {
      Skill bomb = SkillsParser.getInstance().getInfo(4143, this.getBombLvl());
      if (bomb != null) {
         this.setTarget(opener);
         this.doCast(bomb);
      }

      this.clearAggroList();
      this.onDecay();
   }

   private int getBombLvl() {
      int npcLvl = this.getLevel();
      int lvl = 1;
      if (npcLvl >= 78) {
         lvl = 10;
      } else if (npcLvl >= 72) {
         lvl = 9;
      } else if (npcLvl >= 66) {
         lvl = 8;
      } else if (npcLvl >= 60) {
         lvl = 7;
      } else if (npcLvl >= 54) {
         lvl = 6;
      } else if (npcLvl >= 48) {
         lvl = 5;
      } else if (npcLvl >= 42) {
         lvl = 4;
      } else if (npcLvl >= 36) {
         lvl = 3;
      } else if (npcLvl >= 30) {
         lvl = 2;
      }

      return lvl;
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (!this.getTemplate().isCommonChest()) {
         this.fakeOpen(attacker);
      }
   }
}
