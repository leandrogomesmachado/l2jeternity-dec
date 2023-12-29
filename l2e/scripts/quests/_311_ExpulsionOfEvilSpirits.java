package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class _311_ExpulsionOfEvilSpirits extends Quest {
   private Npc _varangka;
   private Npc _varangkaMinion1;
   private Npc _varangkaMinion2;
   protected Npc _altar;
   private long respawnTime = 0L;

   public _311_ExpulsionOfEvilSpirits(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32655);
      this.addTalkId(32655);
      this.addKillId(new int[]{22691, 22692, 22693, 22694, 22695, 22696, 22697, 22698, 22699, 22701, 22702, 18808, 18809, 18810});
      this.questItemIds = new int[]{14881, 14882};
      this.addEnterZoneId(new int[]{20201});
      this.addAttackId(18811);

      try {
         this.respawnTime = Long.valueOf(this.loadGlobalQuestVar("VarangkaRespawn"));
      } catch (Exception var5) {
      }

      this.saveGlobalQuestVar("VarangkaRespawn", String.valueOf(this.respawnTime));
      if (this.respawnTime != 0L && this.respawnTime - System.currentTimeMillis() >= 0L) {
         this.startQuestTimer("altarSpawn", this.respawnTime - System.currentTimeMillis(), null, null);
      } else {
         this.startQuestTimer("altarSpawn", 5000L, null, null);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("altarSpawn")) {
         if (!this.checkIfSpawned(18811)) {
            this._altar = addSpawn(18811, 74120, -101920, -960, 32760, false, 0L);
            this._altar.setIsInvul(true);
            this.saveGlobalQuestVar("VarangkaRespawn", String.valueOf(0));

            for(Player pc : World.getInstance().getAroundPlayers(this._altar, 1200, 200)) {
               ThreadPoolManager.getInstance().schedule(new _311_ExpulsionOfEvilSpirits.zoneCheck(pc), 1000L);
            }
         }

         return null;
      } else if (event.equalsIgnoreCase("minion1") && this.checkIfSpawned(18808)) {
         if (!this.checkIfSpawned(18809) && this.checkIfSpawned(18808)) {
            this._varangkaMinion1 = addSpawn(18809, player.getX() + Rnd.get(10, 50), player.getY() + Rnd.get(10, 50), -967, 0, false, 0L);
            this._varangkaMinion1.setRunning();
            ((Attackable)this._varangkaMinion1).addDamageHate(this._varangka.getTarget().getActingPlayer(), 1, 99999);
            this._varangkaMinion1.getAI().setIntention(CtrlIntention.ATTACK, this._varangka.getTarget().getActingPlayer());
         }

         return null;
      } else if (event.equalsIgnoreCase("minion2")) {
         if (!this.checkIfSpawned(18810) && this.checkIfSpawned(18808)) {
            this._varangkaMinion2 = addSpawn(18810, player.getX() + Rnd.get(10, 50), player.getY() + Rnd.get(10, 50), -967, 0, false, 0L);
            this._varangkaMinion2.setRunning();
            ((Attackable)this._varangkaMinion2).addDamageHate(this._varangka.getTarget().getActingPlayer(), 1, 99999);
            this._varangkaMinion2.getAI().setIntention(CtrlIntention.ATTACK, this._varangka.getTarget().getActingPlayer());
         }

         return null;
      } else {
         QuestState qs = this.getQuestState(player, false);
         if (qs == null) {
            return null;
         } else {
            String htmltext = null;
            if (player.getLevel() < 80) {
               return null;
            } else {
               switch(event) {
                  case "32655-03.htm":
                  case "32655-15.htm":
                     htmltext = event;
                     break;
                  case "32655-04.htm":
                     qs.startQuest();
                     htmltext = event;
                     break;
                  case "32655-11.htm":
                     if (getQuestItemsCount(player, 14881) >= 10L) {
                        takeItems(player, 14881, 10L);
                        giveItems(player, 14848, 1L);
                        htmltext = event;
                     } else {
                        htmltext = "32655-12.htm";
                     }
                     break;
                  case "32655-13.htm":
                     if (!hasQuestItems(player, 14881) && getQuestItemsCount(player, 14882) >= 10L) {
                        qs.exitQuest(true, true);
                        htmltext = event;
                     } else {
                        htmltext = "32655-14.htm";
                     }
               }

               return htmltext;
            }
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState qs = this.getRandomPartyMemberState(killer, 1, 2, npc);
      if (qs != null) {
         if (npc.getId() == 18808) {
            if (qs.getCond() != 1) {
               return null;
            }

            qs.takeItems(14848, 1L);
            this._altar.doDie(killer);
            this._altar = null;
            this._varangka = null;
            if (this.checkIfSpawned(18809)) {
               this._varangkaMinion1.doDie(killer);
            }

            if (this.checkIfSpawned(18810)) {
               this._varangkaMinion2.doDie(killer);
            }

            this.cancelQuestTimers("minion1");
            this.cancelQuestTimers("minion2");
            this._varangkaMinion1 = null;
            this._varangkaMinion2 = null;
            long respawn = (long)Rnd.get(14400000, 28800000);
            this.saveGlobalQuestVar("VarangkaRespawn", String.valueOf(System.currentTimeMillis() + respawn));
            this.startQuestTimer("altarSpawn", respawn, null, null);
            return super.onKill(npc, killer, isSummon);
         }

         if (npc.getId() == 18809) {
            this._varangkaMinion1 = null;
            this.startQuestTimer("minion1", (long)Rnd.get(60000, 120000), npc, killer);
            return super.onKill(npc, killer, isSummon);
         }

         if (npc.getId() == 18810) {
            this._varangkaMinion2 = null;
            this.startQuestTimer("minion2", (long)Rnd.get(60000, 120000), npc, killer);
            return super.onKill(npc, killer, isSummon);
         }

         int count = qs.getMemoStateEx(1) + 1;
         if (count >= 100 && getRandom(20) < count % 100 + 1) {
            qs.setMemoStateEx(1, 0);
            qs.giveItems(14881, 1L);
            qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
         } else {
            qs.setMemoStateEx(1, count);
         }

         qs.calcDoDropItems(this.getId(), 14882, npc.getId(), Integer.MAX_VALUE);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      if (qs == null) {
         return htmltext;
      } else {
         if (qs.isCreated()) {
            htmltext = player.getLevel() >= 80 ? "32655-01.htm" : "32655-02.htm";
         } else if (qs.isStarted()) {
            htmltext = !hasQuestItems(player, new int[]{14881, 14882}) ? "32655-05.htm" : "32655-06.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isPet) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (st.getQuestItemsCount(14848) > 0L && Rnd.get(100) < 20) {
            if (this._varangka == null && !this.checkIfSpawned(18808)) {
               this._varangka = addSpawn(18808, 74914, -101922, -967, 0, false, 0L);
               if (this._varangkaMinion1 == null && !this.checkIfSpawned(18809)) {
                  this._varangkaMinion1 = addSpawn(18809, 74914 + Rnd.get(10, 50), -101922 + Rnd.get(10, 50), -967, 0, false, 0L);
               }

               if (this._varangkaMinion2 == null && !this.checkIfSpawned(18810)) {
                  this._varangkaMinion2 = addSpawn(18810, 74914 + Rnd.get(10, 50), -101922 + Rnd.get(10, 50), -967, 0, false, 0L);
               }

               ZoneType zone = ZoneManager.getInstance().getZoneById(20201);

               for(Creature c : zone.getCharactersInside()) {
                  if (c instanceof Attackable && c.getId() >= 18808 && c.getId() <= 18810) {
                     c.setRunning();
                     ((Attackable)c).addDamageHate(player, 1, 99999);
                     c.getAI().setIntention(CtrlIntention.ATTACK, player);
                  }
               }
            }
         } else if (st.getQuestItemsCount(14848) == 0L) {
            ThreadPoolManager.getInstance().schedule(new _311_ExpulsionOfEvilSpirits.zoneCheck(player), 1000L);
         }

         return super.onAttack(npc, player, damage, isPet);
      }
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character.isPlayer()) {
         ThreadPoolManager.getInstance().schedule(new _311_ExpulsionOfEvilSpirits.zoneCheck(character.getActingPlayer()), 1000L);
      }

      return super.onEnterZone(character, zone);
   }

   private boolean checkIfSpawned(int npcId) {
      ZoneType zone = ZoneManager.getInstance().getZoneById(20201);

      for(Creature c : zone.getCharactersInside()) {
         if (c.getId() == npcId) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _311_ExpulsionOfEvilSpirits(311, _311_ExpulsionOfEvilSpirits.class.getSimpleName(), "");
   }

   private class zoneCheck implements Runnable {
      private final Player _player;

      protected zoneCheck(Player player) {
         this._player = player;
      }

      @Override
      public void run() {
         if (_311_ExpulsionOfEvilSpirits.this._altar != null) {
            ZoneType zone = ZoneManager.getInstance().getZoneById(20201);
            if (zone.isCharacterInZone(this._player)) {
               QuestState st = this._player.getQuestState(_311_ExpulsionOfEvilSpirits.this.getName());
               if (st == null) {
                  this.castDebuff(this._player);
                  ThreadPoolManager.getInstance().schedule(_311_ExpulsionOfEvilSpirits.this.new zoneCheck(this._player), 3000L);
               } else if (st.getQuestItemsCount(14848) == 0L) {
                  this.castDebuff(this._player);
                  ThreadPoolManager.getInstance().schedule(_311_ExpulsionOfEvilSpirits.this.new zoneCheck(this._player), 3000L);
               }
            }
         }
      }

      private void castDebuff(Player player) {
         int skillId = 6148;
         int skillLevel = 1;
         if (player.getFirstEffect(6148) != null) {
            player.stopSkillEffects(6148);
         }

         Skill skill = SkillsParser.getInstance().getInfo(6148, 1);
         skill.getEffects(_311_ExpulsionOfEvilSpirits.this._altar, player, false);
         _311_ExpulsionOfEvilSpirits.this._altar.broadcastPacket(new MagicSkillUse(_311_ExpulsionOfEvilSpirits.this._altar, player, 6148, 1, 1000, 0));
      }
   }
}
