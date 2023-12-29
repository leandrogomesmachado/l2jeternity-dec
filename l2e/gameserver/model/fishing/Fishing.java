package l2e.gameserver.model.fishing;

import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.FishMonstersParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExFishingHpRegen;
import l2e.gameserver.network.serverpackets.ExFishingStartCombat;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Fishing implements Runnable {
   private Player _fisher;
   private int _time;
   private int _stop = 0;
   private int _goodUse = 0;
   private int _anim = 0;
   private int _mode = 0;
   private int _deceptiveMode = 0;
   private Future<?> _fishAiTask;
   private boolean _thinking;
   private final int _fishId;
   private final int _fishMaxHp;
   private int _fishCurHp;
   private final double _regenHp;
   private final boolean _isUpperGrade;
   private int _lureType;

   @Override
   public void run() {
      if (this._fisher != null) {
         if (this._fishCurHp >= this._fishMaxHp * 2) {
            this._fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH);
            this.doDie(false);
         } else if (this._time <= 0) {
            this._fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK);
            this.doDie(false);
         } else {
            this.aiTask();
         }
      }
   }

   public Fishing(Player Fisher, Fish fish, boolean isNoob, boolean isUpperGrade) {
      this._fisher = Fisher;
      this._fishMaxHp = fish.getFishHp();
      this._fishCurHp = this._fishMaxHp;
      this._regenHp = fish.getHpRegen();
      this._fishId = fish.getId();
      this._time = fish.getCombatDuration();
      this._isUpperGrade = isUpperGrade;
      if (isUpperGrade) {
         this._deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
         this._lureType = 2;
      } else {
         this._deceptiveMode = 0;
         this._lureType = isNoob ? 0 : 1;
      }

      this._mode = Rnd.get(100) >= 80 ? 1 : 0;
      this._fisher.broadcastPacket(new ExFishingStartCombat(this._fisher, this._time, this._fishMaxHp, this._mode, this._lureType, this._deceptiveMode));
      this._fisher.sendPacket(new PlaySound(1, "SF_S_01", 0, 0, 0, 0, 0));
      this._fisher.sendPacket(SystemMessageId.GOT_A_BITE);
      if (this._fishAiTask == null) {
         this._fishAiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
      }
   }

   public void changeHp(int hp, int pen) {
      this._fishCurHp -= hp;
      if (this._fishCurHp < 0) {
         this._fishCurHp = 0;
      }

      ExFishingHpRegen efhr = new ExFishingHpRegen(this._fisher, this._time, this._fishCurHp, this._mode, this._goodUse, this._anim, pen, this._deceptiveMode);
      this._fisher.broadcastPacket(efhr);
      this._anim = 0;
      if (this._fishCurHp > this._fishMaxHp * 2) {
         this._fishCurHp = this._fishMaxHp * 2;
         this.doDie(false);
      } else if (this._fishCurHp == 0) {
         this.doDie(true);
      }
   }

   public synchronized void doDie(boolean win) {
      if (this._fishAiTask != null) {
         this._fishAiTask.cancel(false);
         this._fishAiTask = null;
      }

      if (this._fisher != null) {
         if (win) {
            FishingMonster fishingMonster = FishMonstersParser.getInstance().getFishingMonster(this._fisher.getLevel());
            if (fishingMonster != null) {
               if (Rnd.get(100) <= fishingMonster.getProbability()) {
                  this._fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
                  this.spawnMonster(fishingMonster.getFishingMonsterId());
               } else {
                  this._fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
                  long amount = (long)(
                     1
                        * (int)(
                           Config.RATE_DROP_FISHING
                              * (
                                 this._fisher.isInParty() && Config.PREMIUM_PARTY_RATE
                                    ? this._fisher.getParty().getFishingRate()
                                    : this._fisher.getPremiumBonus().getFishingRate()
                              )
                        )
                  );
                  this._fisher.addItem("Fishing", this._fishId, amount, null, true);
                  this._fisher.getCounters().addAchivementInfo("fishCaughts", 0, amount, false, false, false);
               }
            }
         }

         this._fisher.endFishing(win);
         this._fisher = null;
      }
   }

   protected void aiTask() {
      if (!this._thinking) {
         this._thinking = true;
         --this._time;

         try {
            if (this._mode == 1) {
               if (this._deceptiveMode == 0) {
                  this._fishCurHp += (int)this._regenHp;
               }
            } else if (this._deceptiveMode == 1) {
               this._fishCurHp += (int)this._regenHp;
            }

            if (this._stop == 0) {
               this._stop = 1;
               int check = Rnd.get(100);
               if (check >= 70) {
                  this._mode = this._mode == 0 ? 1 : 0;
               }

               if (this._isUpperGrade) {
                  check = Rnd.get(100);
                  if (check >= 90) {
                     this._deceptiveMode = this._deceptiveMode == 0 ? 1 : 0;
                  }
               }
            } else {
               --this._stop;
            }
         } finally {
            this._thinking = false;
            ExFishingHpRegen efhr = new ExFishingHpRegen(this._fisher, this._time, this._fishCurHp, this._mode, 0, this._anim, 0, this._deceptiveMode);
            if (this._anim != 0) {
               this._fisher.broadcastPacket(efhr);
            } else {
               this._fisher.sendPacket(efhr);
            }
         }
      }
   }

   public void useReeling(int dmg, int pen) {
      this._anim = 2;
      if (Rnd.get(100) > 90) {
         this._fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
         this._goodUse = 0;
         this.changeHp(0, pen);
      } else if (this._fisher != null) {
         if (this._mode == 1) {
            if (this._deceptiveMode == 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
               sm.addNumber(dmg);
               this._fisher.sendPacket(sm);
               if (pen > 0) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
                  sm.addNumber(pen);
                  this._fisher.sendPacket(sm);
               }

               this._goodUse = 1;
               this.changeHp(dmg, pen);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
               sm.addNumber(dmg);
               this._fisher.sendPacket(sm);
               this._goodUse = 2;
               this.changeHp(-dmg, pen);
            }
         } else if (this._deceptiveMode == 0) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
            sm.addNumber(dmg);
            this._fisher.sendPacket(sm);
            this._goodUse = 2;
            this.changeHp(-dmg, pen);
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
            sm.addNumber(dmg);
            this._fisher.sendPacket(sm);
            if (pen > 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
               sm.addNumber(pen);
               this._fisher.sendPacket(sm);
            }

            this._goodUse = 1;
            this.changeHp(dmg, pen);
         }
      }
   }

   public void usePumping(int dmg, int pen) {
      this._anim = 1;
      if (Rnd.get(100) > 90) {
         this._fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
         this._goodUse = 0;
         this.changeHp(0, pen);
      } else if (this._fisher != null) {
         if (this._mode == 0) {
            if (this._deceptiveMode == 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
               sm.addNumber(dmg);
               this._fisher.sendPacket(sm);
               if (pen > 0) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
                  sm.addNumber(pen);
                  this._fisher.sendPacket(sm);
               }

               this._goodUse = 1;
               this.changeHp(dmg, pen);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
               sm.addNumber(dmg);
               this._fisher.sendPacket(sm);
               this._goodUse = 2;
               this.changeHp(-dmg, pen);
            }
         } else if (this._deceptiveMode == 0) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
            sm.addNumber(dmg);
            this._fisher.sendPacket(sm);
            this._goodUse = 2;
            this.changeHp(-dmg, pen);
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
            sm.addNumber(dmg);
            this._fisher.sendPacket(sm);
            if (pen > 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
               sm.addNumber(pen);
               this._fisher.sendPacket(sm);
            }

            this._goodUse = 1;
            this.changeHp(dmg, pen);
         }
      }
   }

   private void spawnMonster(int npcId) {
      NpcTemplate monster = NpcsParser.getInstance().getTemplate(npcId);
      if (monster != null) {
         try {
            Spawner spawn = new Spawner(monster);
            spawn.setX(this._fisher.getX());
            spawn.setY(this._fisher.getY());
            spawn.setZ(this._fisher.getZ());
            spawn.setAmount(1);
            spawn.setHeading(this._fisher.getHeading());
            spawn.stopRespawn();
            spawn.doSpawn();
            spawn.getLastSpawn().setTarget(this._fisher);
         } catch (Exception var4) {
         }
      }
   }
}
