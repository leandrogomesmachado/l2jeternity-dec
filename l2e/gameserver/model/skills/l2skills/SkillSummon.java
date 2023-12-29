package l2e.gameserver.model.skills.l2skills;

import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ServitorInstance;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;

public class SkillSummon extends Skill {
   private final float _expPenalty;
   private final int _summonTotalLifeTime;
   private final int _summonTimeLostIdle;
   private final int _summonTimeLostActive;
   private final int _itemConsumeTime;
   private final int _itemConsumeOT;
   private final int _itemConsumeIdOT;
   private final int _itemConsumeSteps;
   private final boolean _inheritElementals;
   private final double _elementalSharePercent;

   public SkillSummon(StatsSet set) {
      super(set);
      this._expPenalty = set.getFloat("expPenalty", 0.0F);
      this._summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
      this._summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
      this._summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
      this._itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
      this._itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
      this._itemConsumeTime = set.getInteger("itemConsumeTime", 0);
      this._itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
      this._inheritElementals = set.getBool("inheritElementals", false);
      this._elementalSharePercent = set.getDouble("inheritPercent", 1.0);
   }

   @Override
   public void useSkill(Creature caster, GameObject[] targets) {
      if (caster != null && !caster.isAlikeDead() && caster.isPlayer()) {
         Player activeChar = caster.getActingPlayer();
         if (this.getNpcId() <= 0) {
            activeChar.sendMessage("Summon skill " + this.getId() + " not implemented yet.");
         } else {
            NpcTemplate summonTemplate = NpcsParser.getInstance().getTemplate(this.getNpcId());
            if (summonTemplate == null) {
               _log.warning("Summon attempt for nonexisting NPC ID:" + this.getNpcId() + ", skill ID:" + this.getId());
            } else {
               int id = IdFactory.getInstance().getNextId();
               ServitorInstance summon;
               if (summonTemplate.isType("SiegeSummon")) {
                  summon = new SiegeSummonInstance(id, summonTemplate, activeChar, this);
               } else {
                  summon = new ServitorInstance(id, summonTemplate, activeChar, this);
               }

               summon.setName(summonTemplate.getName());
               summon.setTitle(activeChar.getName());
               summon.setExpPenalty(this._expPenalty);
               summon.setSharedElementals(this._inheritElementals);
               summon.setSharedElementalsValue(this._elementalSharePercent);
               if (summon.getLevel() >= ExperienceParser.getInstance().getMaxPetLevel()) {
                  summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(ExperienceParser.getInstance().getMaxPetLevel() - 1));
                  _log.warning(
                     "Summon ("
                        + summon.getName()
                        + ") NpcID: "
                        + summon.getId()
                        + " has a level above "
                        + ExperienceParser.getInstance().getMaxPetLevel()
                        + ". Please rectify."
                  );
               } else {
                  summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(summon.getLevel() % ExperienceParser.getInstance().getMaxPetLevel()));
               }

               summon.setCurrentHp(summon.getMaxHp());
               summon.setCurrentMp(summon.getMaxMp());
               summon.setHeading(activeChar.getHeading());
               summon.setRunning();
               activeChar.setPet(summon);
               summon.spawnMe(activeChar.getX() + 20, activeChar.getY() + 20, activeChar.getZ());
            }
         }
      }
   }

   public final int getTotalLifeTime() {
      return this._summonTotalLifeTime;
   }

   public final int getTimeLostIdle() {
      return this._summonTimeLostIdle;
   }

   public final int getTimeLostActive() {
      return this._summonTimeLostActive;
   }

   public final int getItemConsumeOT() {
      return this._itemConsumeOT;
   }

   public final int getItemConsumeIdOT() {
      return this._itemConsumeIdOT;
   }

   public final int getItemConsumeSteps() {
      return this._itemConsumeSteps;
   }

   public final int getItemConsumeTime() {
      return this._itemConsumeTime;
   }

   public final float getExpPenalty() {
      return this._expPenalty;
   }

   public final NpcTemplate getSummonTemplate() {
      NpcTemplate summonTemplate = NpcsParser.getInstance().getTemplate(this.getNpcId());
      if (summonTemplate == null) {
         _log.warning("Summon attempt for nonexisting NPC ID:" + this.getNpcId() + ", skill ID:" + this.getId());
      }

      return summonTemplate;
   }

   public final boolean getInheritElementals() {
      return this._inheritElementals;
   }

   public final double getElementalSharePercent() {
      return this._elementalSharePercent;
   }
}
