package l2e.fake.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import l2e.commons.util.Rnd;
import l2e.fake.FakePlayer;
import l2e.fake.model.BotSkill;
import l2e.fake.model.HealingSpell;
import l2e.fake.model.OffensiveSpell;
import l2e.fake.model.SupportSpell;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.ClanHallDoormenInstance;
import l2e.gameserver.model.actor.instance.ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.TeleporterInstance;
import l2e.gameserver.model.actor.templates.player.FakeLocTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.zone.ZoneId;

public abstract class CombatAI extends FakePlayerAI {
   public CombatAI(FakePlayer character) {
      super(character, false);
   }

   protected void tryAction(boolean isMage) {
      if (this._fakePlayer.isInsideZone(ZoneId.PEACE)) {
         this.townBehavior(isMage);
      } else {
         this.areaBehavior(isMage);
      }
   }

   private void townBehavior(boolean isMage) {
      if (this._fakePlayer.getFakeAi().isTownZone()) {
         GameObject target = this._fakePlayer.getTarget();
         if (target != null && this._fakePlayer.getFakeAi().isTargetLock()) {
            double distance = this._fakePlayer.getDistance(target);
            if (distance <= 150.0) {
               this.sleep((long)Rnd.get(4, 6));
               this._fakePlayer.getFakeAi().setTargetLock(false);
               this._fakePlayer.setTarget(null);
            } else if (this._idleTime > System.currentTimeMillis()) {
               if (this._fakePlayer.getFakeAi().maybeMoveToPawn(target, 60)) {
                  return;
               }

               this._fakePlayer.getFakeAi().clientStopMoving(null);
            } else {
               this._idleTime = 0L;
               this._fakePlayer.getFakeAi().setTargetLock(false);
               this._fakePlayer.setTarget(null);
            }
         } else {
            if (Rnd.get(1000) <= 50) {
               this.sitDown(this._fakePlayer);
               return;
            }

            if (this._sitTime <= System.currentTimeMillis()) {
               if (this._fakePlayer.isSitting()) {
                  this._fakePlayer.standUp();
                  return;
               }

               GameObject newTarget = this.searchTownNpc(2000);
               if (newTarget != null) {
                  this._fakePlayer.setTarget(newTarget);
                  if (this._fakePlayer.getFakeAi().maybeMoveToPawn(target, 70)) {
                     this._fakePlayer.getFakeAi().setTargetLock(true);
                     this._idleTime = System.currentTimeMillis() + 60000L;
                  }
               }
            }
         }
      } else if (this._fakePlayer.getFakeAi().isWantToFarm()) {
         GameObject target = this._fakePlayer.getTarget();
         if (target != null && target instanceof TeleporterInstance) {
            double distance = this._fakePlayer.getDistance(target);
            if (distance <= 150.0) {
               this.sleep((long)Rnd.get(2, 3));
               this._fakePlayer.getFakeAi().setWantToFarm(false);
               Location loc = this._fakePlayer.getFakeLocation().getLocation();
               this._fakePlayer.getFakeAi().teleportToLocation(loc.getX(), loc.getY(), loc.getZ(), 0);
            } else {
               if (this._fakePlayer.getFakeAi().maybeMoveToPawn(target, 60)) {
                  return;
               }

               this._fakePlayer.getFakeAi().clientStopMoving(null);
               this._fakePlayer.getFakeAi().setWantToFarm(false);
               Location loc = this._fakePlayer.getFakeLocation().getLocation();
               this._fakePlayer.getFakeAi().teleportToLocation(loc.getX(), loc.getY(), loc.getZ(), 0);
            }
         } else {
            Creature telepoter = this.searchTeleporter(5000);
            if (telepoter != null) {
               this._fakePlayer.setTarget(telepoter);
               if (this._fakePlayer.getFakeAi().maybeMoveToPawn(target, 60)) {
                  return;
               }

               this._fakePlayer.getFakeAi().setWantToFarm(false);
               Location loc = this._fakePlayer.getFakeLocation().getLocation();
               this._fakePlayer.getFakeAi().teleportToLocation(loc.getX(), loc.getY(), loc.getZ(), 0);
            } else {
               this._fakePlayer.getFakeAi().setWantToFarm(false);
               Location loc = this._fakePlayer.getFakeLocation().getLocation();
               this._fakePlayer.getFakeAi().teleportToLocation(loc.getX(), loc.getY(), loc.getZ(), 0);
            }
         }
      } else {
         if (Rnd.chance(1)) {
            this._fakePlayer.getFakeAi().setWantToFarm(true);
            this._fakePlayer.getFakeAi().setTargetLock(false);
            this._fakePlayer.setTarget(null);
            if (this._fakePlayer.isSitting()) {
               this._fakePlayer.standUp();
            }

            this.sleep((long)Rnd.get(1, 2));
            return;
         }

         GameObject target = this._fakePlayer.getTarget();
         if (target != null && this._fakePlayer.getFakeAi().isTargetLock()) {
            double distance = this._fakePlayer.getDistance(target);
            if (distance <= 150.0) {
               this.sleep((long)Rnd.get(4, 6));
               this._fakePlayer.getFakeAi().setTargetLock(false);
               this._fakePlayer.setTarget(null);
            } else if (this._idleTime > System.currentTimeMillis()) {
               if (this._fakePlayer.getFakeAi().maybeMoveToPawn(target, 60)) {
                  return;
               }

               this._fakePlayer.getFakeAi().clientStopMoving(null);
            } else {
               this._idleTime = 0L;
               this._fakePlayer.getFakeAi().setTargetLock(false);
               this._fakePlayer.setTarget(null);
            }
         } else {
            if (Rnd.get(1000) <= 50) {
               this.sitDown(this._fakePlayer);
               return;
            }

            if (this._sitTime <= System.currentTimeMillis()) {
               if (this._fakePlayer.isSitting()) {
                  this._fakePlayer.standUp();
                  return;
               }

               GameObject newTarget = this.searchTownNpc(2000);
               if (newTarget != null) {
                  this._fakePlayer.setTarget(newTarget);
                  if (this._fakePlayer.getFakeAi().maybeMoveToPawn(target, 60)) {
                     this._fakePlayer.getFakeAi().setTargetLock(true);
                     this._idleTime = System.currentTimeMillis() + 60000L;
                  }
               }
            }
         }
      }
   }

   private void sitDown(FakePlayer fakePlayer) {
      if (!fakePlayer.isSitting()) {
         fakePlayer.sitDown();
         this._sitTime = System.currentTimeMillis() + (long)(Rnd.get(60, 120) * 1000);
      }
   }

   private Creature searchTownNpc(int range) {
      List<Npc> targets = new ArrayList<>();

      for(Npc target : World.getInstance().getAroundNpc(this._fakePlayer, range, 400)) {
         if (!target.isWalker() && !(target instanceof ClanHallDoormenInstance) && !(target instanceof ClanHallManagerInstance)) {
            targets.add(target);
         }
      }

      return targets != null && !targets.isEmpty() ? targets.get(Rnd.get(targets.size())) : null;
   }

   private Creature searchTeleporter(int range) {
      for(Npc target : World.getInstance().getAroundNpc(this._fakePlayer, range, 400)) {
         if (target instanceof TeleporterInstance) {
            return target;
         }
      }

      return null;
   }

   private void areaBehavior(boolean isMage) {
      if (Rnd.chance(50)) {
         this.checkMp(this._fakePlayer);
      }

      if (Rnd.chance(70)) {
         this.checkHp(this._fakePlayer);
      }

      GameObject target = this.tryTargetRandomCreatureByTypeInRadius(
         2000, creature -> GeoEngine.canSeeTarget(this._fakePlayer, creature, false) && !creature.isDead()
      );
      if (target != null) {
         this._fakePlayer.setTarget(target);
         if (isMage) {
            this.tryAttackingUsingMageOffensiveSkill();
         } else {
            this.tryAttackingUsingFighterOffensiveSkill();
         }
      } else {
         FakeLocTemplate tmpl = this._fakePlayer.getFakeLocation();
         if (!this._fakePlayer.isInsideRadius(tmpl.getLocation().getX(), tmpl.getLocation().getY(), tmpl.getLocation().getZ(), tmpl.getDistance(), true, true)
            )
          {
            this._fakePlayer.setRunning();
            this._fakePlayer
               .getAI()
               .setIntention(CtrlIntention.MOVING, new Location(tmpl.getLocation().getX(), tmpl.getLocation().getY(), tmpl.getLocation().getZ()));
            this.sleep((long)Rnd.get(3, 5));
         } else {
            if (Rnd.chance(30)) {
               this.rndShortWalk();
            }

            this.sleep((long)Rnd.get(1, 2));
         }
      }
   }

   protected void tryAttackingUsingMageOffensiveSkill() {
      if (this._fakePlayer.getTarget() != null) {
         BotSkill botSkill = this.getRandomAvaiableMageSpellForTarget();
         if (botSkill == null) {
            return;
         }

         Skill skill = this._fakePlayer.getKnownSkill(botSkill.getSkillId());
         if (skill != null) {
            if (!this.castSpell(skill) && !this._fakePlayer.checkDoCastConditions(skill, false)) {
               this.rndShortWalk();
               if (Rnd.chance(30)) {
                  this._fakePlayer.setCurrentMp(this._fakePlayer.getMaxMp());
               }

               this.sleep((long)Rnd.get(1, 2));
            }
         } else {
            this.rndShortWalk();
         }
      }
   }

   protected void tryAttackingUsingFighterOffensiveSkill() {
      if (this._fakePlayer.getTarget() != null) {
         if (this.getOffensiveSpells() != null && !this.getOffensiveSpells().isEmpty()) {
            Skill skill = this.getRandomAvaiableFighterSpellForTarget();
            if (skill != null && Rnd.chance(this.changeOfUsingSkill())) {
               this._fakePlayer.useMagic(skill, true, true, false);
            }
         }

         this._fakePlayer.forceAutoAttack((Creature)this._fakePlayer.getTarget());
      }
   }

   @Override
   public void thinkAndAct() {
      this.handleDeath();
   }

   protected int getArrowId() {
      ItemInstance weapon = this._fakePlayer.getInventory().getPaperdollItem(5);
      if (weapon != null) {
         switch(weapon.getItem().getItemGrade()) {
            case 0:
               return 17;
            case 1:
               return 1341;
            case 2:
               return 1342;
            case 3:
               return 1343;
            case 4:
               return 1344;
            case 5:
            case 6:
            case 7:
               return 1345;
         }
      }

      return 0;
   }

   protected int getBoltId() {
      ItemInstance weapon = this._fakePlayer.getInventory().getPaperdollItem(5);
      if (weapon != null) {
         switch(weapon.getItem().getItemGrade()) {
            case 0:
               return 9632;
            case 1:
               return 9633;
            case 2:
               return 9634;
            case 3:
               return 9635;
            case 4:
               return 9636;
            case 5:
            case 6:
            case 7:
               return 9637;
         }
      }

      return 0;
   }

   protected void handleShots() {
      if (this._shotsTime <= System.currentTimeMillis()) {
         int shotId = 0;
         ItemInstance weapon = this._fakePlayer.getInventory().getPaperdollItem(5);
         if (weapon != null) {
            switch(weapon.getItem().getItemGrade()) {
               case 0:
                  shotId = weapon.getItem().isMagicWeapon() ? 3947 : 1835;
                  break;
               case 1:
                  shotId = weapon.getItem().isMagicWeapon() ? 3948 : 1463;
                  break;
               case 2:
                  shotId = weapon.getItem().isMagicWeapon() ? 3949 : 1464;
                  break;
               case 3:
                  shotId = weapon.getItem().isMagicWeapon() ? 3950 : 1465;
                  break;
               case 4:
                  shotId = weapon.getItem().isMagicWeapon() ? 3951 : 1466;
                  break;
               case 5:
               case 6:
               case 7:
                  shotId = weapon.getItem().isMagicWeapon() ? 3952 : 1467;
            }

            if (this._fakePlayer.getInventory().getItemByItemId(shotId) != null) {
               if (this._fakePlayer.getInventory().getItemByItemId(shotId).getCount() <= 20L) {
                  this._fakePlayer.getInventory().addItem("", shotId, 500L, this._fakePlayer, null);
               }
            } else {
               this._fakePlayer.getInventory().addItem("", shotId, 500L, this._fakePlayer, null);
            }

            if (this._fakePlayer.getAutoSoulShot().isEmpty()) {
               this._fakePlayer.addAutoSoulShot(shotId);
               this._fakePlayer.rechargeShots(true, true);
            }
         }

         this._shotsTime = System.currentTimeMillis() + 180000L;
      }
   }

   protected void handleSpiritOre() {
      if (this._spiritOreTime <= System.currentTimeMillis()) {
         if (this._fakePlayer.getInventory().getItemByItemId(3031) != null) {
            if (this._fakePlayer.getInventory().getItemByItemId(3031).getCount() <= 20L) {
               this._fakePlayer.getInventory().addItem("", 3031, 500L, this._fakePlayer, null);
            }
         } else {
            this._fakePlayer.getInventory().addItem("", 3031, 500L, this._fakePlayer, null);
         }

         this._spiritOreTime = System.currentTimeMillis() + 1200000L;
      }
   }

   public HealingSpell getRandomAvaiableHealingSpellForTarget() {
      if (this.getHealingSpells().isEmpty()) {
         return null;
      } else {
         List<HealingSpell> spellsOrdered = this.getHealingSpells()
            .stream()
            .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
            .collect(Collectors.toList());
         BotSkill skill = this.waitAndPickAvailablePrioritisedSpell(spellsOrdered, spellsOrdered.size());
         return skill != null ? (HealingSpell)skill : null;
      }
   }

   protected BotSkill getRandomAvaiableMageSpellForTarget() {
      List<OffensiveSpell> spellsOrdered = this.getOffensiveSpells()
         .stream()
         .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
         .collect(Collectors.toList());
      BotSkill skill = this.waitAndPickAvailablePrioritisedSpell(spellsOrdered, spellsOrdered.size());
      return skill != null ? skill : null;
   }

   private BotSkill waitAndPickAvailablePrioritisedSpell(List<? extends BotSkill> spellsOrdered, int skillListSize) {
      int skillIndex = 0;
      BotSkill botSkill = spellsOrdered.get(skillIndex);
      Skill skill = this._fakePlayer.getKnownSkill(botSkill.getSkillId());
      if (skill == null) {
         return null;
      } else if (skill.getCastRange() > 0 && !GeoEngine.canSeeTarget(this._fakePlayer, this._fakePlayer.getTarget(), false)) {
         this.moveToPawn(this._fakePlayer.getTarget(), 100);
         return null;
      } else {
         while(!this._fakePlayer.checkUseMagicConditions(skill, true, false)) {
            this._isBusyThinking = true;
            if (!this._fakePlayer.isDead() && !this._fakePlayer.isOutOfControl()) {
               if (skillIndex >= 0 && skillIndex < skillListSize) {
                  skill = this._fakePlayer.getKnownSkill(spellsOrdered.get(skillIndex).getSkillId());
                  botSkill = spellsOrdered.get(skillIndex);
                  ++skillIndex;
                  continue;
               }

               return null;
            }

            return null;
         }

         return botSkill;
      }
   }

   protected Skill getRandomAvaiableFighterSpellForTarget() {
      List<OffensiveSpell> spellsOrdered = this.getOffensiveSpells()
         .stream()
         .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
         .collect(Collectors.toList());
      int skillIndex = 0;
      Skill skill = this._fakePlayer.getKnownSkill(spellsOrdered.get(skillIndex).getSkillId());
      if (skill == null) {
         return null;
      } else {
         while(!this._fakePlayer.checkUseMagicConditions(skill, true, false)) {
            if (skillIndex < 0 || skillIndex >= spellsOrdered.size()) {
               return null;
            }

            skill = this._fakePlayer.getKnownSkill(spellsOrdered.get(skillIndex).getSkillId());
            ++skillIndex;
         }

         if (!this._fakePlayer.checkUseMagicConditions(skill, true, false)) {
            this._fakePlayer.forceAutoAttack((Creature)this._fakePlayer.getTarget());
            return null;
         } else {
            return skill;
         }
      }
   }

   protected void selfSupportBuffs() {
      if (this.getSelfSupportSpells() != null && !this.getSelfSupportSpells().isEmpty() && !this._fakePlayer.getFakeAi().isTownZone()) {
         List<Integer> activeEffects = Arrays.stream(this._fakePlayer.getAllEffects()).map(x -> x.getSkill().getId()).collect(Collectors.toList());

         for(SupportSpell selfBuff : this.getSelfSupportSpells()) {
            if (!activeEffects.contains(selfBuff.getSkillId())) {
               Skill skill = this._fakePlayer.getKnownSkill(selfBuff.getSkillId());
               if (skill != null) {
                  skill = SkillsParser.getInstance().getInfo(selfBuff.getSkillId(), this._fakePlayer.getSkillLevel(selfBuff.getSkillId()));
                  if (this._fakePlayer.checkUseMagicConditions(skill, true, false)) {
                     switch(selfBuff.getCondition()) {
                        case LESSHPPERCENT:
                           if (Math.round(100.0 / this._fakePlayer.getMaxHp() * this._fakePlayer.getCurrentHp()) <= (long)selfBuff.getConditionValue()) {
                              this.castSelfSpell(skill);
                           }
                           break;
                        case MISSINGCP:
                           if (this.getMissingHealth() >= (double)selfBuff.getConditionValue()) {
                              this.castSelfSpell(skill);
                           }
                           break;
                        case NONE:
                           this.castSelfSpell(skill);
                     }
                  }
               }
            }
         }
      }
   }

   private double getMissingHealth() {
      return this._fakePlayer.getMaxCp() - this._fakePlayer.getCurrentCp();
   }

   protected int changeOfUsingSkill() {
      return 10;
   }

   protected void checkMp(FakePlayer fakePlayer) {
      ItemInstance mpPoint = fakePlayer.getInventory().getItemByItemId(728);
      if (mpPoint != null && fakePlayer.getCurrentMp() < 0.7 * fakePlayer.getMaxMp()) {
         IItemHandler handler = ItemHandler.getInstance().getHandler(mpPoint.getEtcItem());
         if (handler != null) {
            handler.useItem(fakePlayer, mpPoint, false);
         }
      }
   }

   protected void checkHp(FakePlayer fakePlayer) {
      ItemInstance hpPoint = fakePlayer.getInventory().getItemByItemId(1539);
      if (hpPoint != null && fakePlayer.getCurrentHp() < 0.95 * fakePlayer.getMaxHp()) {
         IItemHandler handler = ItemHandler.getInstance().getHandler(hpPoint.getEtcItem());
         if (handler != null) {
            handler.useItem(fakePlayer, hpPoint, false);
         }
      }
   }

   protected void sleep(long time) {
      try {
         Thread.sleep(time * 1000L);
      } catch (InterruptedException var4) {
      }
   }

   protected abstract List<OffensiveSpell> getOffensiveSpells();

   protected abstract List<HealingSpell> getHealingSpells();

   protected abstract List<SupportSpell> getSelfSupportSpells();
}
