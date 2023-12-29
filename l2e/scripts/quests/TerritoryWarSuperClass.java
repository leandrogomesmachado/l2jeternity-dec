package l2e.scripts.quests;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.RewardManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

public class TerritoryWarSuperClass extends Quest {
   private static Map<Integer, TerritoryWarSuperClass> _forTheSakeScripts = new HashMap<>();
   private static Map<Integer, TerritoryWarSuperClass> _protectTheScripts = new HashMap<>();
   private static Map<Integer, TerritoryWarSuperClass> _killTheScripts = new HashMap<>();
   public static String qn = "TerritoryWarSuperClass";
   public int CATAPULT_ID;
   public int TERRITORY_ID;
   public int[] LEADER_IDS;
   public int[] GUARD_IDS;
   public NpcStringId[] npcString = new NpcStringId[0];
   public int[] NPC_IDS;
   public int[] CLASS_IDS;
   public int RANDOM_MIN;
   public int RANDOM_MAX;

   public void registerKillIds() {
      this.addKillId(this.CATAPULT_ID);

      for(int mobid : this.LEADER_IDS) {
         this.addKillId(mobid);
      }

      for(int mobid : this.GUARD_IDS) {
         this.addKillId(mobid);
      }
   }

   public void registerAttackIds() {
      for(int mobid : this.NPC_IDS) {
         this.addAttackId(mobid);
      }
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (Util.contains(targets, npc) && skill.getId() == 847) {
         if (TerritoryWarManager.getInstance().getHQForClan(caster.getClan()) != npc) {
            return super.onSkillSee(npc, caster, skill, targets, isSummon);
         }

         TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(caster);
         if (ward == null) {
            return super.onSkillSee(npc, caster, skill, targets, isSummon);
         }

         if (caster.getSiegeSide() - 80 == ward.getOwnerCastleId()) {
            for(TerritoryWarManager.TerritoryNPCSpawn wardSpawn : TerritoryWarManager.getInstance().getTerritory(ward.getOwnerCastleId()).getOwnedWard()) {
               if (wardSpawn.getId() == ward.getTerritoryId()) {
                  wardSpawn.setNPC(wardSpawn.getNpc().getSpawn().doSpawn());
                  ward.unSpawnMe();
                  ward.setNpc(wardSpawn.getNpc());
               }
            }
         } else {
            ward.unSpawnMe();
            ward.setNpc(TerritoryWarManager.getInstance().addTerritoryWard(ward.getTerritoryId(), caster.getSiegeSide() - 80, ward.getOwnerCastleId(), true));
            ward.setOwnerCastleId(caster.getSiegeSide() - 80);
            TerritoryWarManager.getInstance().getTerritory(caster.getSiegeSide() - 80).getQuestDone()[1]++;
            caster.getCounters().addAchivementInfo("stealTerritoryWards", 0, -1L, false, false, true);
         }
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   public int getTerritoryIdForThisNPCId(int npcid) {
      return 0;
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon) {
      if (npc.getCurrentHp() == npc.getMaxHp() && Util.contains(this.NPC_IDS, npc.getId())) {
         int territoryId = this.getTerritoryIdForThisNPCId(npc.getId());
         if (territoryId >= 81 && territoryId <= 89) {
            for(Player pl : World.getInstance().getAllPlayers()) {
               if (pl.getSiegeSide() == territoryId) {
                  QuestState st = pl.getQuestState(this.getName());
                  if (st == null) {
                     st = this.newQuestState(pl);
                  }

                  if (st.getState() != 1) {
                     st.setCond(1);
                     st.setState((byte)1, false);
                  }
               }
            }
         }
      }

      return super.onAttack(npc, player, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == this.CATAPULT_ID) {
         TerritoryWarManager.getInstance().territoryCatapultDestroyed(this.TERRITORY_ID - 80);
         TerritoryWarManager.getInstance().giveTWPoint(killer, this.TERRITORY_ID, 4);
         TerritoryWarManager.getInstance().announceToParticipants(new ExShowScreenMessage(this.npcString[0], 2, 10000), 135000, 13500);
         handleBecomeMercenaryQuest(killer, true);
         if (killer != null) {
            killer.getCounters().addAchivementInfo("killCatapultAtTw", 0, -1L, false, false, false);
         }
      } else if (Util.contains(this.LEADER_IDS, npc.getId())) {
         TerritoryWarManager.getInstance().giveTWPoint(killer, this.TERRITORY_ID, 3);
      }

      if (killer.getSiegeSide() != this.TERRITORY_ID && TerritoryWarManager.getInstance().getTerritory(killer.getSiegeSide() - 80) != null) {
         TerritoryWarManager.getInstance().getTerritory(killer.getSiegeSide() - 80).getQuestDone()[0]++;
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (npc == null && player == null) {
         StringTokenizer st = new StringTokenizer(event, " ");
         event = st.nextToken();
         if (event.equalsIgnoreCase("setNextTWDate")) {
            Calendar startTWDate = Calendar.getInstance();
            startTWDate.set(7, 7);
            startTWDate.set(11, 20);
            startTWDate.set(12, 0);
            startTWDate.set(13, 0);
            if (startTWDate.getTimeInMillis() < System.currentTimeMillis()) {
               startTWDate.add(5, 7);
            }

            if (SiegeManager.getInstance().isCheckSevenSignStatus() && !SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate)) {
               startTWDate.add(5, 7);
            }

            ServerVariables.set("TerritoryWarDate", startTWDate.getTimeInMillis());
            TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
            _log.info("TerritoryWarManager: Next battle " + startTWDate.getTime());
         } else if (event.equalsIgnoreCase("setTWDate") && st.hasMoreTokens()) {
            Calendar startTWDate = Calendar.getInstance();
            startTWDate.setTimeInMillis(Long.parseLong(st.nextToken()));
            ServerVariables.set("TerritoryWarDate", startTWDate.getTimeInMillis());
            TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
            _log.info("TerritoryWarManager: Next battle " + startTWDate.getTime());
         }

         return null;
      } else {
         return null;
      }
   }

   private void handleKillTheQuest(Player player) {
      QuestState st = player.getQuestState(this.getName());
      int kill = 1;
      int max = 10;
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (!st.isCompleted()) {
         if (!st.isStarted()) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.set("kill", "1");
            max = getRandom(this.RANDOM_MIN, this.RANDOM_MAX);
            st.set("max", String.valueOf(max));
         } else {
            kill = st.getInt("kill") + 1;
            max = st.getInt("max");
         }

         if (kill >= max) {
            TerritoryWarManager.getInstance().giveTWQuestPoint(player);
            st.addExpAndSp(534000, 51000);
            st.set("doneDate", String.valueOf(Calendar.getInstance().get(6)));
            st.setState((byte)2);
            st.exitQuest(true);
            player.sendPacket(new ExShowScreenMessage(this.npcString[1], 2, 10000));
         } else {
            st.set("kill", String.valueOf(kill));
            player.sendPacket(new ExShowScreenMessage(this.npcString[0], 2, 10000, String.valueOf(max), String.valueOf(kill)));
         }
      } else if (st.getInt("doneDate") != Calendar.getInstance().get(6)) {
         st.setState((byte)1);
         st.set("cond", "1");
         st.set("kill", "1");
         max = getRandom(this.RANDOM_MIN, this.RANDOM_MAX);
         st.set("max", String.valueOf(max));
         player.sendPacket(new ExShowScreenMessage(this.npcString[0], 2, 10000, String.valueOf(max), String.valueOf(kill)));
      }
   }

   private static void handleBecomeMercenaryQuest(Player player, boolean catapult) {
      int enemyCount = 10;
      int catapultCount = 1;
      QuestState st = player.getQuestState(_147_PathtoBecominganEliteMercenary.class.getSimpleName());
      if (st != null && st.isCompleted()) {
         st = player.getQuestState(_148_PathtoBecominganExaltedMercenary.class.getSimpleName());
         enemyCount = 30;
         catapultCount = 2;
      }

      if (st != null && st.isStarted()) {
         if (catapult) {
            if (st.isCond(1) || st.isCond(2)) {
               int count = st.getInt("catapult");
               st.set("catapult", String.valueOf(++count));
               if (count >= catapultCount) {
                  if (st.isCond(1)) {
                     st.setCond(3);
                  } else {
                     st.setCond(4);
                  }
               }
            }
         } else if (st.isCond(1) || st.isCond(3)) {
            int _kills = st.getInt("kills");
            st.set("kills", String.valueOf(++_kills));
            if (_kills >= enemyCount) {
               if (st.isCond(1)) {
                  st.setCond(2);
               } else {
                  st.setCond(4);
               }
            }
         }
      }
   }

   private void handleStepsForHonor(Player player) {
      int kills = 0;
      int cond = 0;
      QuestState _sfh = player.getQuestState("_176_StepsForHonor");
      if (_sfh != null && _sfh.getState() == 1) {
         cond = _sfh.getInt("cond");
         if (cond == 1 || cond == 3 || cond == 5 || cond == 7) {
            kills = _sfh.getInt("kills");
            _sfh.set("kills", String.valueOf(++kills));
            if (cond == 1 && kills >= 9) {
               _sfh.set("cond", "2");
               _sfh.set("kills", "0");
            } else if (cond == 3 && kills >= 18) {
               _sfh.set("cond", "4");
               _sfh.set("kills", "0");
            } else if (cond == 5 && kills >= 27) {
               _sfh.set("cond", "6");
               _sfh.set("kills", "0");
            } else if (cond == 7 && kills >= 36) {
               _sfh.set("cond", "8");
               _sfh.unset("kills");
            }
         }
      }
   }

   @Override
   public String onDeath(Creature killer, Creature victim, QuestState qs) {
      if (killer != victim && victim.isPlayer() && victim.getLevel() >= 61) {
         Player actingPlayer = killer.getActingPlayer();
         if (actingPlayer != null && qs.getPlayer() != null) {
            if (actingPlayer.getUCState() > 0 || actingPlayer.isInFightEvent()) {
               return "";
            }

            if (actingPlayer.getParty() != null) {
               for(Player pl : actingPlayer.getParty().getMembers()) {
                  if (pl.getSiegeSide() != qs.getPlayer().getSiegeSide() && pl.getSiegeSide() != 0 && Util.checkIfInRange(2000, killer, pl, false)) {
                     if (pl == actingPlayer) {
                        this.handleStepsForHonor(actingPlayer);
                        handleBecomeMercenaryQuest(actingPlayer, false);
                     }

                     this.handleKillTheQuest(pl);
                  }
               }
            } else if (actingPlayer.getSiegeSide() != qs.getPlayer().getSiegeSide() && actingPlayer.getSiegeSide() > 0) {
               this.handleKillTheQuest(actingPlayer);
               this.handleStepsForHonor(actingPlayer);
               handleBecomeMercenaryQuest(actingPlayer, false);
            }

            TerritoryWarManager.getInstance().giveTWPoint(actingPlayer, qs.getPlayer().getSiegeSide(), 1);
            if (DoubleSessionManager.getInstance().check(actingPlayer, victim)) {
               RewardManager.getInstance().checkTerritoryWarReward(actingPlayer, victim.getActingPlayer());
               actingPlayer.getCounters().addAchivementInfo("pvpKillsAtTw", 0, -1L, false, false, false);
            }
         }

         return "";
      } else {
         return "";
      }
   }

   @Override
   public String onEnterWorld(Player player) {
      int territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
      if (territoryId > 0) {
         TerritoryWarSuperClass territoryQuest = _forTheSakeScripts.get(territoryId);
         QuestState st = player.getQuestState(territoryQuest.getName());
         if (st == null) {
            st = territoryQuest.newQuestState(player);
         }

         st.setState((byte)1, false);
         st.setCond(1);
         if (player.getLevel() >= 61) {
            TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassId().getId());
            if (killthe != null) {
               st = player.getQuestState(killthe.getName());
               if (st == null) {
                  st = killthe.newQuestState(player);
               }

               player.addNotifyQuestOfDeath(st);
            } else {
               _log.warning("TerritoryWar: Missing Kill the quest for player " + player.getName() + " whose class id: " + player.getClassId().getId());
            }
         }
      }

      return null;
   }

   @Override
   public void setOnEnterWorld(boolean val) {
      super.setOnEnterWorld(val);

      for(Player player : World.getInstance().getAllPlayers()) {
         if (player.getSiegeSide() > 0) {
            TerritoryWarSuperClass territoryQuest = _forTheSakeScripts.get(player.getSiegeSide());
            if (territoryQuest != null) {
               QuestState st = player.hasQuestState(territoryQuest.getName())
                  ? player.getQuestState(territoryQuest.getName())
                  : territoryQuest.newQuestState(player);
               if (val) {
                  st.setState((byte)1, false);
                  st.setCond(1);
                  if (player.getLevel() >= 61) {
                     TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassId().getId());
                     if (killthe != null) {
                        st = player.getQuestState(killthe.getName());
                        if (st == null) {
                           st = killthe.newQuestState(player);
                        }

                        player.addNotifyQuestOfDeath(st);
                     } else {
                        _log.warning("TerritoryWar: Missing Kill the quest for player " + player.getName() + " whose class id: " + player.getClassId().getId());
                     }
                  }
               } else {
                  st.exitQuest(false);

                  for(Quest q : _protectTheScripts.values()) {
                     st = player.getQuestState(q.getName());
                     if (st != null) {
                        st.exitQuest(false);
                     }
                  }

                  TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassIndex());
                  if (killthe != null) {
                     st = player.getQuestState(killthe.getName());
                     if (st != null) {
                        player.removeNotifyQuestOfDeath(st);
                     }
                  }
               }
            }
         }
      }
   }

   public TerritoryWarSuperClass(int questId, String name, String descr) {
      super(questId, name, descr);
      if (questId < 0) {
         this.addSkillSeeId(new int[]{36590});
      }
   }

   public static void main(String[] args) {
      new TerritoryWarSuperClass(-1, qn, "Territory_War");
      TerritoryWarSuperClass gludio = new _717_FortheSakeoftheTerritoryGludio();
      _forTheSakeScripts.put(gludio.TERRITORY_ID, gludio);
      TerritoryWarSuperClass dion = new _718_FortheSakeoftheTerritoryDion();
      _forTheSakeScripts.put(dion.TERRITORY_ID, dion);
      TerritoryWarSuperClass giran = new _719_FortheSakeoftheTerritoryGiran();
      _forTheSakeScripts.put(giran.TERRITORY_ID, giran);
      TerritoryWarSuperClass oren = new _720_FortheSakeoftheTerritoryOren();
      _forTheSakeScripts.put(oren.TERRITORY_ID, oren);
      TerritoryWarSuperClass aden = new _721_FortheSakeoftheTerritoryAden();
      _forTheSakeScripts.put(aden.TERRITORY_ID, aden);
      TerritoryWarSuperClass innadril = new _722_FortheSakeoftheTerritoryInnadril();
      _forTheSakeScripts.put(innadril.TERRITORY_ID, innadril);
      TerritoryWarSuperClass goddard = new _723_FortheSakeoftheTerritoryGoddard();
      _forTheSakeScripts.put(goddard.TERRITORY_ID, goddard);
      TerritoryWarSuperClass rune = new _724_FortheSakeoftheTerritoryRune();
      _forTheSakeScripts.put(rune.TERRITORY_ID, rune);
      TerritoryWarSuperClass schuttgart = new _725_FortheSakeoftheTerritorySchuttgart();
      _forTheSakeScripts.put(schuttgart.TERRITORY_ID, schuttgart);
      TerritoryWarSuperClass catapult = new _729_Protecttheterritorycatapult();
      _protectTheScripts.put(catapult.getId(), catapult);
      TerritoryWarSuperClass military = new _731_ProtecttheMilitaryAssociationLeader();
      _protectTheScripts.put(military.getId(), military);
      TerritoryWarSuperClass religious = new _732_ProtecttheReligiousAssociationLeader();
      _protectTheScripts.put(religious.getId(), religious);
      TerritoryWarSuperClass supplies = new _730_ProtecttheSuppliesSafe();
      _protectTheScripts.put(supplies.getId(), supplies);
      TerritoryWarSuperClass knights = new _734_Piercethroughashield();

      for(int i : knights.CLASS_IDS) {
         _killTheScripts.put(i, knights);
      }

      TerritoryWarSuperClass warriors = new _735_Makespearsdull();

      for(int i : warriors.CLASS_IDS) {
         _killTheScripts.put(i, warriors);
      }

      TerritoryWarSuperClass wizards = new _736_Weakenmagic();

      for(int i : wizards.CLASS_IDS) {
         _killTheScripts.put(i, wizards);
      }

      TerritoryWarSuperClass priests = new _737_DenyBlessings();

      for(int i : priests.CLASS_IDS) {
         _killTheScripts.put(i, priests);
      }

      TerritoryWarSuperClass keys = new _738_DestroyKeyTargets();

      for(int i : keys.CLASS_IDS) {
         _killTheScripts.put(i, keys);
      }
   }
}
