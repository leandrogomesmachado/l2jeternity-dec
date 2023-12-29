package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class ToTheMonastery extends AbstractReflection {
   private static final int[][] minions_1 = new int[][]{
      {56504, -252840, -6760, 0}, {56504, -252728, -6760, 0}, {56392, -252728, -6760, 0}, {56408, -252840, -6760, 0}
   };
   private static final int[][] minions_2 = new int[][]{
      {55672, -252728, -6760, 0}, {55752, -252840, -6760, 0}, {55768, -252840, -6760, 0}, {55752, -252712, -6760, 0}
   };
   private static final int[][] minions_3 = new int[][]{
      {55672, -252120, -6760, 0}, {55752, -252120, -6760, 0}, {55656, -252216, -6760, 0}, {55736, -252216, -6760, 0}
   };
   private static final int[][] minions_4 = new int[][]{
      {56520, -252232, -6760, 0}, {56520, -252104, -6760, 0}, {56424, -252104, -6760, 0}, {56440, -252216, -6760, 0}
   };
   private static final int[][] TELEPORTS = new int[][]{
      {120664, -86968, -3392},
      {116324, -84994, -3397},
      {85937, -249618, -8320},
      {120727, -86868, -3392},
      {85937, -249618, -8320},
      {82434, -249546, -8320},
      {85691, -252426, -8320},
      {88573, -249556, -8320},
      {85675, -246630, -8320},
      {45512, -249832, -6760},
      {120664, -86968, -3392},
      {56033, -252944, -6760},
      {56081, -250391, -6760},
      {76736, -241021, -10832},
      {76736, -241021, -10832}
   };

   public ToTheMonastery(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32815);
      this.addTalkId(
         new int[]{32815, 32792, 32803, 32804, 32805, 32806, 32807, 32816, 32817, 32818, 32819, 32793, 32820, 32837, 32842, 32843, 32838, 32839, 32840, 32841}
      );
      this.addSpawnId(new int[]{18956, 18957, 18958, 18959});
      this.addKillId(new int[]{18949, 27403, 27404, 18956, 18957, 18958, 18959});
      this.questItemIds = new int[]{17228, 17229, 17230, 17231};
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new ToTheMonastery.TMWorld(), 151)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((ToTheMonastery.TMWorld)world).support = addSpawn(32787, player.getX(), player.getY(), player.getZ(), 0, false, 0L, false, player.getReflectionId());
         this.startQuestTimer("check_follow", 3000L, ((ToTheMonastery.TMWorld)world).support, player);
         this.startQuestTimer("check_player", 3000L, ((ToTheMonastery.TMWorld)world).support, player);
         this.startQuestTimer("check_voice", 3000L, ((ToTheMonastery.TMWorld)world).support, player);
         world.getReflection().openDoor(21100001);
         world.getReflection().openDoor(21100002);
         world.getReflection().openDoor(21100003);
         world.getReflection().openDoor(21100004);
         world.getReflection().openDoor(21100005);
         world.getReflection().openDoor(21100006);
         world.getReflection().openDoor(21100007);
         world.getReflection().openDoor(21100008);
         world.getReflection().openDoor(21100009);
         world.getReflection().openDoor(21100010);
         world.getReflection().openDoor(21100011);
         world.getReflection().openDoor(21100012);
         world.getReflection().openDoor(21100013);
         world.getReflection().openDoor(21100014);
         world.getReflection().openDoor(21100015);
         world.getReflection().openDoor(21100016);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      int npcId = npc.getId();
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld != null && tmpworld instanceof ToTheMonastery.TMWorld) {
         ToTheMonastery.TMWorld world = (ToTheMonastery.TMWorld)tmpworld;
         if (npcId == 32792) {
            if (event.equalsIgnoreCase("Enter3")) {
               this.teleportPlayer(npc, player, TELEPORTS[2], player.getReflectionId());
               this.startQuestTimer("start_movie", 3000L, npc, player);
               return null;
            }

            if (event.equalsIgnoreCase("teleport_in")) {
               this.teleportPlayer(npc, player, TELEPORTS[9], player.getReflectionId());
               QuestState check = player.getQuestState("_10295_SevenSignsSolinasTomb");
               if (check != null && check.getInt("entermovie") == 0) {
                  ThreadPoolManager.getInstance().schedule(() -> player.showQuestMovie(26), 3000L);
                  check.set("entermovie", "1");
               }

               return null;
            }

            if (event.equalsIgnoreCase("start_scene")) {
               QuestState check = player.getQuestState("_10296_SevenSignsPoweroftheSeal");
               if (check != null) {
                  check.setCond(2);
               }

               ThreadPoolManager.getInstance().schedule(new ToTheMonastery.Teleport(npc, player, TELEPORTS[13], world.getReflectionId()), 60500L);
               player.showQuestMovie(29);
               return null;
            }

            if (event.equalsIgnoreCase("teleport_back")) {
               this.teleportPlayer(npc, player, TELEPORTS[14], player.getReflectionId());
               return null;
            }
         } else if (npcId == 32803) {
            if (event.equalsIgnoreCase("ReturnToEris")) {
               this.teleportPlayer(npc, player, TELEPORTS[3], player.getReflectionId());
               return null;
            }
         } else if (npcId != 32820 && npcId != 32792) {
            if (event.equalsIgnoreCase("FirstGroupSpawn")) {
               this.SpawnFirstGroup(world);
               return null;
            }

            if (event.equalsIgnoreCase("SecondGroupSpawn")) {
               this.SpawnSecondGroup(world);
               return null;
            }

            if (event.equalsIgnoreCase("ThirdGroupSpawn")) {
               this.SpawnThirdGroup(world);
               return null;
            }

            if (event.equalsIgnoreCase("FourthGroupSpawn")) {
               this.SpawnFourthGroup(world);
               return null;
            }

            if (event.equalsIgnoreCase("start_movie")) {
               player.showQuestMovie(24);
               return null;
            }

            if (event.equalsIgnoreCase("check_player")) {
               this.cancelQuestTimer("check_player", npc, player);
               if (player.getCurrentHp() < player.getMaxHp() * 0.8) {
                  Skill skill = SkillsParser.getInstance().getInfo(6724, 1);
                  if (skill != null) {
                     npc.setTarget(player);
                     npc.doCast(skill);
                  }
               }

               if (player.getCurrentMp() < player.getMaxMp() * 0.5) {
                  Skill skill = SkillsParser.getInstance().getInfo(6728, 1);
                  if (skill != null) {
                     npc.setTarget(player);
                     npc.doCast(skill);
                  }
               }

               if (player.getCurrentHp() < player.getMaxHp() * 0.1) {
                  Skill skill = SkillsParser.getInstance().getInfo(6730, 1);
                  if (skill != null) {
                     npc.setTarget(player);
                     npc.doCast(skill);
                  }
               }

               if (player.isInCombat()) {
                  Skill skill = SkillsParser.getInstance().getInfo(6725, 1);
                  if (skill != null) {
                     npc.setTarget(player);
                     npc.doCast(skill);
                  }
               }

               return "";
            }

            if (event.equalsIgnoreCase("check_voice")) {
               this.cancelQuestTimer("check_voice", npc, player);
               QuestState qs = player.getQuestState("_10294_SevenSignToTheMonastery");
               if (qs != null && !qs.isCompleted()) {
                  if (qs.isCond(2)) {
                     if (Rnd.getChance(5)) {
                        if (Rnd.getChance(10)) {
                           npc.broadcastPacket(
                              new NpcSay(
                                 npc.getObjectId(),
                                 0,
                                 npc.getId(),
                                 NpcStringId.IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK
                              ),
                              2000
                           );
                        } else {
                           npc.broadcastPacket(
                              new NpcSay(
                                 npc.getObjectId(),
                                 0,
                                 npc.getId(),
                                 NpcStringId.REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND_YOU_CANT_TAKE_THEM_OUT_WITH_YOU
                              ),
                              2000
                           );
                        }
                     }
                  } else if (qs.isCond(3) && Rnd.getChance(8)) {
                     npc.broadcastPacket(
                        new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN), 2000
                     );
                  }
               }

               QuestState qs2 = player.getQuestState("_10295_SevenSignsSolinasTomb");
               if (qs2 != null && !qs2.isCompleted() && qs2.isCond(1) && Rnd.getChance(5)) {
                  if (Rnd.getChance(10)) {
                     npc.broadcastPacket(
                        new NpcSay(
                           npc.getObjectId(),
                           0,
                           npc.getId(),
                           NpcStringId.TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE
                        ),
                        2000
                     );
                  } else if (Rnd.getChance(15)) {
                     npc.broadcastPacket(
                        new NpcSay(
                           npc.getObjectId(),
                           0,
                           npc.getId(),
                           NpcStringId.THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED
                        ),
                        2000
                     );
                  } else {
                     npc.broadcastPacket(
                        new NpcSay(
                           npc.getObjectId(),
                           0,
                           npc.getId(),
                           NpcStringId.THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER
                        ),
                        2000
                     );
                  }
               }

               this.startQuestTimer("check_voice", 100000L, npc, player);
               return null;
            }

            if (event.equalsIgnoreCase("check_follow")) {
               this.cancelQuestTimer("check_follow", npc, player);
               npc.getAI().stopFollow();
               npc.setIsRunning(true);
               npc.getAI().startFollow(player);
               QuestState qs3 = player.getQuestState("_10296_SevenSignsPoweroftheSeal");
               if (qs3 != null && !qs3.isCompleted() && player.isInCombat()) {
                  if (player.getCurrentHp() < player.getMaxHp() * 0.8) {
                     Skill skill = SkillsParser.getInstance().getInfo(6724, 1);
                     if (skill != null) {
                        npc.setTarget(player);
                        npc.doCast(skill);
                     }
                  }

                  if (player.getCurrentMp() < player.getMaxMp() * 0.5) {
                     Skill skill = SkillsParser.getInstance().getInfo(6728, 1);
                     if (skill != null) {
                        npc.setTarget(player);
                        npc.doCast(skill);
                     }
                  }

                  if (player.getCurrentHp() < player.getMaxHp() * 0.1) {
                     Skill skill = SkillsParser.getInstance().getInfo(6730, 1);
                     if (skill != null) {
                        npc.setTarget(player);
                        npc.doCast(skill);
                     }
                  }

                  Skill skill = SkillsParser.getInstance().getInfo(6725, 1);
                  if (skill != null) {
                     npc.setTarget(player);
                     npc.doCast(skill);
                  }
               }

               this.startQuestTimer("check_follow", 5000L, npc, player);
               return null;
            }
         } else if (event.equalsIgnoreCase("teleport_solina")) {
            this.teleportPlayer(npc, player, TELEPORTS[11], player.getReflectionId());
            if (npcId == 32820) {
               QuestState qs = player.getQuestState("_10295_SevenSignsSolinasTomb");
               if (qs != null) {
                  if (qs.getInt("firstgroup") == 1 && qs.getInt("secondgroup") == 1 && qs.getInt("thirdgroup") == 1 && qs.getInt("fourthgroup") == 1) {
                     world.getReflection().openDoor(21100018);
                  } else {
                     int activity = qs.getInt("activity");
                     if (activity == 1) {
                        world.getReflection().openDoor(21100101);
                        world.getReflection().openDoor(21100102);
                        world.getReflection().openDoor(21100103);
                        world.getReflection().openDoor(21100104);
                        if (world.firstgroup == null) {
                           this.SpawnFirstGroup(world);
                        }

                        if (world.secondgroup == null) {
                           this.SpawnSecondGroup(world);
                        }

                        if (world.thirdgroup == null) {
                           this.SpawnThirdGroup(world);
                        }

                        if (world.fourthgroup == null) {
                           this.SpawnFourthGroup(world);
                        }
                     }
                  }
               }
            }

            return null;
         }
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      int npcId = npc.getId();
      if (npcId == 32815) {
         if (player.getQuestState("_10294_SevenSignToTheMonastery") != null && player.getQuestState("_10294_SevenSignToTheMonastery").getState() == 1) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10294_SevenSignToTheMonastery") != null
            && player.getQuestState("_10294_SevenSignToTheMonastery").getState() == 2
            && player.getQuestState("_10295_SevenSignsSolinasTomb") == null) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10295_SevenSignsSolinasTomb") != null && player.getQuestState("_10295_SevenSignsSolinasTomb").getState() != 2) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10295_SevenSignsSolinasTomb") != null
            && player.getQuestState("_10295_SevenSignsSolinasTomb").getState() == 2
            && player.getQuestState("_10296_SevenSignsPoweroftheSeal") == null) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10296_SevenSignsPoweroftheSeal") != null && player.getQuestState("_10296_SevenSignsPoweroftheSeal").getState() != 2) {
            this.enterInstance(player, npc);
            return null;
         }

         htmltext = "32815-00.htm";
      }

      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld != null && tmpworld instanceof ToTheMonastery.TMWorld) {
         ToTheMonastery.TMWorld world = (ToTheMonastery.TMWorld)tmpworld;
         if (npcId == 32792) {
            if (world.support != null) {
               this.cancelQuestTimer("check_follow", world.support, player);
               this.cancelQuestTimer("check_player", world.support, player);
               this.cancelQuestTimer("check_voice", world.support, player);
               world.support.deleteMe();
            }

            this.teleportPlayer(npc, player, TELEPORTS[1], 0);
            return null;
         }

         if (npcId == 32816) {
            this.teleportPlayer(npc, player, TELEPORTS[5], player.getReflectionId());
            return null;
         }

         if (npcId == 32817) {
            this.teleportPlayer(npc, player, TELEPORTS[7], player.getReflectionId());
            return null;
         }

         if (npcId == 32818) {
            this.teleportPlayer(npc, player, TELEPORTS[8], player.getReflectionId());
            return null;
         }

         if (npcId == 32819) {
            this.teleportPlayer(npc, player, TELEPORTS[6], player.getReflectionId());
            return null;
         }

         if (npcId == 32804 || npcId == 32805 || npcId == 32806 || npcId == 32807) {
            this.teleportPlayer(npc, player, TELEPORTS[4], player.getReflectionId());
            return null;
         }

         if (npcId == 32793 || npcId == 32820 || npcId == 32837) {
            this.teleportPlayer(npc, player, TELEPORTS[10], player.getReflectionId());
            return null;
         }

         if (npcId == 32842) {
            this.teleportPlayer(npc, player, TELEPORTS[12], player.getReflectionId());
            player.showQuestMovie(28);
            return null;
         }

         if (npcId == 32838) {
            if (st.getQuestItemsCount(17231) > 0L) {
               st.takeItems(17231, -1L);
               this.removeInvincibility(player, 18953);
               return null;
            }

            htmltext = "no-item.htm";
         } else if (npcId == 32839) {
            if (st.getQuestItemsCount(17228) > 0L) {
               st.takeItems(17228, -1L);
               this.removeInvincibility(player, 18954);
               return null;
            }

            htmltext = "no-item.htm";
         } else if (npcId == 32840) {
            if (st.getQuestItemsCount(17230) > 0L) {
               st.takeItems(17230, -1L);
               this.removeInvincibility(player, 18955);
               return null;
            }

            htmltext = "no-item.htm";
         } else if (npcId == 32841) {
            if (st.getQuestItemsCount(17229) > 0L) {
               st.takeItems(17229, -1L);
               this.removeInvincibility(player, 18952);
               return null;
            }

            htmltext = "no-item.htm";
         } else if (npcId == 32843) {
            QuestState qs = player.getQuestState("_10295_SevenSignsSolinasTomb");
            if (qs != null) {
               int activity = qs.getInt("activity");
               if (activity == 1) {
                  htmltext = "32843-03.htm";
               } else {
                  world.getReflection().openDoor(21100101);
                  world.getReflection().openDoor(21100102);
                  world.getReflection().openDoor(21100103);
                  world.getReflection().openDoor(21100104);
                  this.SpawnFirstGroup(world);
                  this.SpawnSecondGroup(world);
                  this.SpawnThirdGroup(world);
                  this.SpawnFourthGroup(world);
                  qs.set("activity", "1");
                  htmltext = "32843-02.htm";
               }
            }
         }
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         QuestState qs = player.getQuestState("_10295_SevenSignsSolinasTomb");
         if (qs != null) {
            int npcId = npc.getId();
            int firstgroup = qs.getInt("firstgroup");
            int secondgroup = qs.getInt("secondgroup");
            int thirdgroup = qs.getInt("thirdgroup");
            int fourthgroup = qs.getInt("fourthgroup");
            ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
            if (tmpworld != null && tmpworld instanceof ToTheMonastery.TMWorld) {
               ToTheMonastery.TMWorld world = (ToTheMonastery.TMWorld)tmpworld;
               if (npcId == 27403) {
                  if (world.firstgroup != null) {
                     world.firstgroup.remove(npc);
                     if (world.firstgroup.isEmpty()) {
                        world.firstgroup = null;
                        if (firstgroup == 1) {
                           this.cancelQuestTimer("FirstGroupSpawn", npc, player);
                        } else {
                           this.startQuestTimer("FirstGroupSpawn", 10000L, npc, player);
                        }
                     }
                  }

                  if (world.secondgroup != null) {
                     world.secondgroup.remove(npc);
                     if (world.secondgroup.isEmpty()) {
                        world.secondgroup = null;
                        if (secondgroup == 1) {
                           this.cancelQuestTimer("SecondGroupSpawn", npc, player);
                        } else {
                           this.startQuestTimer("SecondGroupSpawn", 10000L, npc, player);
                        }
                     }
                  }
               }

               if (npcId == 27404) {
                  if (world.thirdgroup != null) {
                     world.thirdgroup.remove(npc);
                     if (world.thirdgroup.isEmpty()) {
                        world.thirdgroup = null;
                        if (thirdgroup == 1) {
                           this.cancelQuestTimer("ThirdGroupSpawn", npc, player);
                        } else {
                           this.startQuestTimer("ThirdGroupSpawn", 10000L, npc, player);
                        }
                     }
                  }

                  if (world.fourthgroup != null) {
                     world.fourthgroup.remove(npc);
                     if (world.fourthgroup.isEmpty()) {
                        world.fourthgroup = null;
                        if (fourthgroup == 1) {
                           this.cancelQuestTimer("FourthGroupSpawn", npc, player);
                        } else {
                           this.startQuestTimer("FourthGroupSpawn", 10000L, npc, player);
                        }
                     }
                  }
               }

               if (npcId == 18949) {
                  ThreadPoolManager.getInstance().schedule(new ToTheMonastery.Teleport(npc, player, TELEPORTS[0], world.getReflectionId()), 60500L);
                  return null;
               }

               if (npcId == 18956) {
                  qs.set("firstgroup", "1");
               }

               if (npcId == 18957) {
                  qs.set("secondgroup", "1");
               }

               if (npcId == 18958) {
                  qs.set("thirdgroup", "1");
               }

               if (npcId == 18959) {
                  qs.set("fourthgroup", "1");
               }

               if (qs.getInt("firstgroup") == 1 && qs.getInt("secondgroup") == 1 && qs.getInt("thirdgroup") == 1 && qs.getInt("fourthgroup") == 1) {
                  world.getReflection().openDoor(21100018);
               }
            }
         }

         return "";
      }
   }

   @Override
   public String onSpawn(Npc npc) {
      if (npc instanceof MonsterInstance && (npc.getId() == 18956 || npc.getId() == 18957 || npc.getId() == 18958 || npc.getId() == 18959)) {
         MonsterInstance monster = (MonsterInstance)npc;
         monster.setIsImmobilized(true);
      }

      return super.onSpawn(npc);
   }

   protected void teleportPlayer(Npc npc, Player player, int[] coords, int instanceId) {
      player.getAI().setIntention(CtrlIntention.IDLE);
      player.setReflectionId(instanceId);
      player.teleToLocation(coords[0], coords[1], coords[2], false);
      if (instanceId > 0) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof ToTheMonastery.TMWorld) {
            ToTheMonastery.TMWorld world = (ToTheMonastery.TMWorld)tmpworld;
            if (world.support != null) {
               this.cancelQuestTimer("check_follow", world.support, player);
               this.cancelQuestTimer("check_player", world.support, player);
               this.cancelQuestTimer("check_voice", world.support, player);
               world.support.deleteMe();
            }

            world.support = addSpawn(32787, player.getX(), player.getY(), player.getZ(), 0, false, 0L, false, player.getReflectionId());
            this.startQuestTimer("check_follow", 3000L, world.support, player);
            this.startQuestTimer("check_player", 3000L, world.support, player);
            this.startQuestTimer("check_voice", 3000L, world.support, player);
         }
      }
   }

   protected void SpawnFirstGroup(ToTheMonastery.TMWorld world) {
      world.firstgroup = new ArrayList<>();

      for(int[] spawn : minions_1) {
         Npc spawnedMob = addSpawn(27403, spawn[0], spawn[1], spawn[2], spawn[3], false, 0L, false, world.getReflectionId());
         world.firstgroup.add(spawnedMob);
      }
   }

   protected void SpawnSecondGroup(ToTheMonastery.TMWorld world) {
      world.secondgroup = new ArrayList<>();

      for(int[] spawn : minions_2) {
         Npc spawnedMob = addSpawn(27403, spawn[0], spawn[1], spawn[2], spawn[3], false, 0L, false, world.getReflectionId());
         world.secondgroup.add(spawnedMob);
      }
   }

   protected void SpawnThirdGroup(ToTheMonastery.TMWorld world) {
      world.thirdgroup = new ArrayList<>();

      for(int[] spawn : minions_3) {
         Npc spawnedMob = addSpawn(27404, spawn[0], spawn[1], spawn[2], spawn[3], false, 0L, false, world.getReflectionId());
         world.thirdgroup.add(spawnedMob);
      }
   }

   protected void SpawnFourthGroup(ToTheMonastery.TMWorld world) {
      world.fourthgroup = new ArrayList<>();

      for(int[] spawn : minions_4) {
         Npc spawnedMob = addSpawn(27404, spawn[0], spawn[1], spawn[2], spawn[3], false, 0L, false, world.getReflectionId());
         world.fourthgroup.add(spawnedMob);
      }
   }

   private void removeInvincibility(Player player, int mobId) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld != null && tmpworld instanceof ToTheMonastery.TMWorld) {
         ToTheMonastery.TMWorld world = (ToTheMonastery.TMWorld)tmpworld;
         Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());

         for(Npc n : inst.getNpcs()) {
            if (n != null && n.getId() == mobId) {
               for(Effect e : n.getEffectList().getAllEffects()) {
                  if (e.getSkill().getId() == 6371) {
                     e.exit();
                  }
               }
            }
         }
      }
   }

   public static void main(String[] args) {
      new ToTheMonastery(ToTheMonastery.class.getSimpleName(), "instances");
   }

   private class TMWorld extends ReflectionWorld {
      public Npc support = null;
      public List<Npc> firstgroup;
      public List<Npc> secondgroup;
      public List<Npc> thirdgroup;
      public List<Npc> fourthgroup;

      public TMWorld() {
      }
   }

   private class Teleport implements Runnable {
      private final Npc _npc;
      private final Player _player;
      private final int _instanceId;
      private final int[] _cords;

      public Teleport(Npc npc, Player player, int[] cords, int id) {
         this._npc = npc;
         this._player = player;
         this._cords = cords;
         this._instanceId = id;
      }

      @Override
      public void run() {
         try {
            ToTheMonastery.this.teleportPlayer(this._npc, this._player, this._cords, this._instanceId);
            ToTheMonastery.this.startQuestTimer("check_follow", 3000L, this._npc, this._player);
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }
}
