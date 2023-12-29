package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _421_LittleWingAdventures extends Quest {
   private static final String qn = "_421_LittleWingAdventures";
   private static final Map<Integer, Integer> killedTrees = new HashMap<>();
   private static final int[] MOBS = new int[]{27185, 27186, 27187, 27188};
   private static final int CRONOS = 30610;
   private static final int MIMYU = 30747;
   private static final int FT_LEAF = 4325;

   public _421_LittleWingAdventures(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30610);
      this.addTalkId(30610);
      this.addTalkId(30747);

      for(int i : Util.getRange(27185, 27189)) {
         this.addAttackId(i);
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         Summon summon = player.getSummon();
         if (event.equalsIgnoreCase("30610-05.htm")) {
            if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) == 1L) {
               if (st.hasQuestItems(3500)) {
                  ItemInstance item = player.getInventory().getItemByItemId(3500);
                  if (item.getEnchantLevel() < 55) {
                     htmltext = "30610-06.htm";
                     st.exitQuest(true);
                  } else {
                     st.setState((byte)1);
                     st.set("summonOid", "" + item.getObjectId());
                     st.set("cond", "1");
                     st.set("id", "1");
                     st.playSound("ItemSound.quest_accept");
                  }
               } else if (st.hasQuestItems(3501)) {
                  ItemInstance item = player.getInventory().getItemByItemId(3501);
                  if (item.getEnchantLevel() < 55) {
                     htmltext = "30610-06.htm";
                     st.exitQuest(true);
                  } else {
                     st.setState((byte)1);
                     st.set("summonOid", "" + item.getObjectId());
                     st.set("cond", "1");
                     st.set("id", "1");
                     st.playSound("ItemSound.quest_accept");
                  }
               } else if (st.hasQuestItems(3502)) {
                  ItemInstance item = player.getInventory().getItemByItemId(3502);
                  if (item.getEnchantLevel() < 55) {
                     htmltext = "30610-06.htm";
                     st.exitQuest(true);
                  } else {
                     st.setState((byte)1);
                     st.set("summonOid", "" + item.getObjectId());
                     st.set("cond", "1");
                     st.set("id", "1");
                     st.playSound("ItemSound.quest_accept");
                  }
               }
            } else {
               htmltext = "30610-06.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("30747-02.htm")) {
            if (summon != null) {
               if (summon.getControlObjectId() == st.getInt("summonOid")) {
                  htmltext = "30747-04.htm";
               } else {
                  htmltext = "30747-03.htm";
               }
            }
         } else if (event.equalsIgnoreCase("30747-05.htm")) {
            if (summon != null) {
               if (summon.getControlObjectId() == st.getInt("summonOid")) {
                  htmltext = "30747-05.htm";
                  st.giveItems(4325, 4L);
                  st.set("cond", "2");
                  st.set("id", "0");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  htmltext = "30747-06.htm";
               }
            } else {
               htmltext = "30747-06.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_421_LittleWingAdventures");
      if (st == null) {
         st = this.newQuestState(player);
      }

      int npcId = npc.getId();
      Summon summon = player.getSummon();
      switch(st.getState()) {
         case 0:
            if (npcId == 30610) {
               if (player.getLevel() >= 45 || !st.hasQuestItems(3500) && !st.hasQuestItems(3501) && !st.hasQuestItems(3502)) {
                  if (player.getLevel() >= 45 && st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) > 1L) {
                     htmltext = "30610-02.htm";
                     st.exitQuest(true);
                  } else if (player.getLevel() >= 45 && st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) == 1L) {
                     if (st.hasQuestItems(3500)) {
                        if (player.getInventory().getItemByItemId(3500).getEnchantLevel() < 55) {
                           htmltext = "30610-03.htm";
                        } else {
                           htmltext = "30610-04.htm";
                        }
                     } else if (st.hasQuestItems(3501)) {
                        if (player.getInventory().getItemByItemId(3501).getEnchantLevel() < 55) {
                           htmltext = "30610-03.htm";
                        } else {
                           htmltext = "30610-04.htm";
                        }
                     } else if (st.hasQuestItems(3502)) {
                        if (player.getInventory().getItemByItemId(3502).getEnchantLevel() < 55) {
                           htmltext = "30610-03.htm";
                        } else {
                           htmltext = "30610-04.htm";
                        }
                     }
                  }
               } else {
                  htmltext = "30610-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            switch(npcId) {
               case 30610:
                  htmltext = "30610-07.htm";
                  break;
               case 30747:
                  int id = st.getInt("id");
                  if (id == 1) {
                     st.set("id", "2");
                     htmltext = "30747-01.htm";
                  } else if (id == 2) {
                     if (summon != null) {
                        if (summon.getControlObjectId() == st.getInt("summonOid")) {
                           htmltext = "30747-04.htm";
                        } else {
                           htmltext = "30747-03.htm";
                        }
                     } else {
                        htmltext = "30747-02.htm";
                     }
                  } else if (id == 0) {
                     htmltext = "30747-07.htm";
                  } else if (id > 0 && id < 15 && st.hasQuestItems(4325)) {
                     htmltext = "30747-11.htm";
                  } else if (id == 15 && !st.hasQuestItems(4325)) {
                     if (summon != null) {
                        if (summon.getControlObjectId() == st.getInt("summonOid")) {
                           st.set("id", "16");
                           htmltext = "30747-13.htm";
                        } else {
                           htmltext = "30747-14.htm";
                        }
                     } else {
                        htmltext = "30747-12.htm";
                     }
                  } else if (id == 16) {
                     if (summon == null) {
                        htmltext = "30747-15.htm";
                     } else if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) == 1L) {
                        if (st.hasQuestItems(3500)) {
                           ItemInstance item = player.getInventory().getItemByItemId(3500);
                           if (item.getObjectId() == st.getInt("summonOid")) {
                              st.takeItems(3500, 1L);
                              st.giveItems(4422, 1L);
                              htmltext = "30747-16.htm";
                              st.exitQuest(true);
                              st.playSound("ItemSound.quest_finish");
                           } else {
                              npc.setTarget(player);
                              Skill skill = SkillsParser.getInstance().getInfo(4167, 1);
                              if (skill != null) {
                                 skill.getEffects(npc, player, false);
                              }

                              htmltext = "30747-18.htm";
                           }
                        } else if (st.hasQuestItems(3501)) {
                           ItemInstance item = player.getInventory().getItemByItemId(3501);
                           if (item.getObjectId() == st.getInt("summonOid")) {
                              st.takeItems(3501, 1L);
                              st.giveItems(4423, 1L);
                              htmltext = "30747-16.htm";
                              st.exitQuest(true);
                              st.playSound("ItemSound.quest_finish");
                           } else {
                              npc.setTarget(player);
                              Skill skill = SkillsParser.getInstance().getInfo(4167, 1);
                              if (skill != null) {
                                 skill.getEffects(npc, player, false);
                              }

                              htmltext = "30747-18.htm";
                           }
                        } else if (st.hasQuestItems(3502)) {
                           ItemInstance item = player.getInventory().getItemByItemId(3502);
                           if (item.getObjectId() == st.getInt("summonOid")) {
                              st.takeItems(3502, 1L);
                              st.giveItems(4424, 1L);
                              htmltext = "30747-16.htm";
                              st.exitQuest(true);
                              st.playSound("ItemSound.quest_finish");
                           } else {
                              npc.setTarget(player);
                              Skill skill = SkillsParser.getInstance().getInfo(4167, 1);
                              if (skill != null) {
                                 skill.getEffects(npc, player, false);
                              }

                              htmltext = "30747-18.htm";
                           }
                        }
                     } else if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) > 1L) {
                        htmltext = "30747-17.htm";
                     }
                  }
            }
      }

      return htmltext;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      QuestState st = attacker.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();

         for(int id : killedTrees.keySet()) {
            if (id == attacker.getObjectId() && killedTrees.get(id) == npcId) {
               return null;
            }
         }

         if (isSummon && st.getInt("id") < 16 && st.getRandom(100) <= 2 && st.hasQuestItems(4325)) {
            st.takeItems(4325, 1L);
            st.playSound("ItemSound.quest_middle");
            npc.broadcastPacket(new NpcSay(npcId, 0, npcId, NpcStringId.GIVE_ME_A_FAIRY_LEAF), 2000);
            killedTrees.put(attacker.getObjectId(), npcId);
            if (st.getQuestItemsCount(4325) == 0L) {
               st.set("id", "15");
               st.set("cond", "3");
            }
         }

         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      int npcId = npc.getId();
      if (Util.contains(MOBS, npcId)) {
         for(int i = 0; i < 20; ++i) {
            Location loc = Location.findPointToStay(npc, 50, 200, true);
            Attackable newNpc = (Attackable)addSpawn(27189, loc.getX(), loc.getY(), loc.getZ(), 0, false, 30000L);
            Creature originalKiller = (Creature)(isSummon ? killer.getSummon() : killer);
            newNpc.setRunning();
            newNpc.addDamageHate(originalKiller, 0, 999);
            newNpc.getAI().setIntention(CtrlIntention.ATTACK, originalKiller);
            if (getRandomBoolean()) {
               Skill skill = SkillsParser.getInstance().getInfo(4243, 1);
               if (skill != null && originalKiller != null) {
                  skill.getEffects(newNpc, originalKiller, false);
               }
            }
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _421_LittleWingAdventures(421, "_421_LittleWingAdventures", "");
   }
}
