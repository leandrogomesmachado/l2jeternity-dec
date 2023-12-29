package l2e.scripts.instances;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.quests._727_HopeWithinTheDarkness;

public final class RimPailaka727 extends AbstractReflection {
   private final Map<Integer, Integer> _castleReflections = new HashMap<>(9);

   private RimPailaka727() {
      super(RimPailaka727.class.getSimpleName(), "instances");
      this._castleReflections.put(36403, 101);
      this._castleReflections.put(36404, 102);
      this._castleReflections.put(36405, 103);
      this._castleReflections.put(36406, 104);
      this._castleReflections.put(36407, 105);
      this._castleReflections.put(36408, 106);
      this._castleReflections.put(36409, 107);
      this._castleReflections.put(36410, 108);
      this._castleReflections.put(36411, 109);

      for(int i : this._castleReflections.keySet()) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      this.addStartNpc(new int[]{36562, 36563, 36564, 36565});
      this.addTalkId(new int[]{36562, 36563, 36564, 36565});
      this.addFirstTalkId(new int[]{36562, 36563, 36564, 36565});
      this.addSpawnId(new int[]{25653, 25654, 25655, 36562, 36563, 36564, 36565});
      this.addKillId(new int[]{25653, 25654, 25655, 25656, 25657, 25658, 36562, 36563, 36564, 36565});
      this.addAttackId(new int[]{25653, 25654, 25655, 25656, 25657, 25658, 36562, 36563, 36564, 36565});
   }

   private final synchronized void enterInstance(Player player, Npc npc, int reflectionId) {
      if (this.enterInstance(player, npc, new RimPailaka727.Pailaka727World(), reflectionId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (inst != null) {
            long delay = inst.getParams().getLong("firstWaveDelay");
            ((RimPailaka727.Pailaka727World)world).firstStageSpawn = ThreadPoolManager.getInstance()
               .schedule(new RimPailaka727.FirstStage((RimPailaka727.Pailaka727World)world), delay);
         }
      }
   }

   @Override
   protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template) {
      Castle castle = npc.getCastle();
      boolean checkConds = template.getParams().getBool("checkContractConditions");
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && castle != null) {
         if (checkConds) {
            boolean haveContract = false;

            for(Fort fort : FortManager.getInstance().getForts()) {
               if (fort.getContractedCastleId() == castle.getId()) {
                  haveContract = true;
                  break;
               }
            }

            if (!haveContract) {
               html.setFile(
                  player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-11.htm"
               );
               player.sendPacket(html);
               return false;
            }
         }

         QuestState st = player.getQuestState(_727_HopeWithinTheDarkness.class.getSimpleName());
         if (st == null || st.getCond() < 1) {
            html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-07.htm");
            player.sendPacket(html);
            return false;
         } else if (player.getClan() != null && player.getClan().getCastleId() == castle.getId()) {
            return super.checkSoloType(player, npc, template);
         } else {
            html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-08.htm");
            player.sendPacket(html);
            return false;
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-03.htm");
         player.sendPacket(html);
         return false;
      }
   }

   @Override
   protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template) {
      Castle castle = npc.getCastle();
      boolean checkConds = template.getParams().getBool("checkContractConditions");
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && castle != null) {
         Party party = player.getParty();
         if (party == null) {
            player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
            return false;
         } else if (party.getLeader() != player) {
            player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
            return false;
         } else {
            if (checkConds) {
               boolean haveContract = false;

               for(Fort fort : FortManager.getInstance().getForts()) {
                  if (fort.getContractedCastleId() == castle.getId()) {
                     haveContract = true;
                     break;
                  }
               }

               if (!haveContract) {
                  html.setFile(
                     player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-11.htm"
                  );
                  player.sendPacket(html);
                  return false;
               }
            }

            for(Player partyMember : party.getMembers()) {
               if (partyMember != null) {
                  QuestState st = partyMember.getQuestState(_727_HopeWithinTheDarkness.class.getSimpleName());
                  if (st != null && st.getCond() >= 1) {
                     if (partyMember.getClan() != null && partyMember.getClan().getCastleId() == castle.getId()) {
                        continue;
                     }

                     html.setFile(
                        player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-08.htm"
                     );
                     player.sendPacket(html);
                     return false;
                  }

                  html.setFile(
                     player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-07.htm"
                  );
                  player.sendPacket(html);
                  return false;
               }
            }

            return super.checkPartyType(player, npc, template);
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-03.htm");
         player.sendPacket(html);
         return false;
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
   public String onFirstTalk(Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(player.getReflectionId());
      if (tmpworld != null && tmpworld instanceof RimPailaka727.Pailaka727World) {
         RimPailaka727.Pailaka727World world = (RimPailaka727.Pailaka727World)tmpworld;
         NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
         if (world.underAttack) {
            html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/Victim-02.htm");
         } else if (world.isStatus(4)) {
            html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/Victim-03.htm");
         } else {
            html.setFile(player, "data/scripts/quests/" + _727_HopeWithinTheDarkness.class.getSimpleName() + "/" + player.getLang() + "/Victim-01.htm");
         }

         player.sendPacket(html);
      }

      return null;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("enter")) {
         this.enterInstance(player, npc, this._castleReflections.get(npc.getId()));
      } else if (event.equalsIgnoreCase("leave") && npc.getId() >= 36562 && npc.getId() <= 36565) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(player.getReflectionId());
         if (tmpworld != null && tmpworld instanceof RimPailaka727.Pailaka727World) {
            RimPailaka727.Pailaka727World world = (RimPailaka727.Pailaka727World)tmpworld;
            world.removeAllowed(player.getObjectId());
            Reflection ref = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
            if (ref != null) {
               player.setReflectionId(0);
               player.teleToLocation(ref.getReturnLoc(), true);
               if (ref.getPlayers().isEmpty()) {
                  ReflectionManager.getInstance().destroyReflection(npc.getReflectionId());
               }
            }

            return null;
         }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon) {
      if (npc.getId() >= 36562 && npc.getId() <= 36565) {
         if (npc.getCurrentHp() <= npc.getMaxHp() * 0.1) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOUR_MIND_IS_GOING_BLANK), 2000);
         } else if (npc.getCurrentHp() <= npc.getMaxHp() * 0.4) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOUR_MIND_IS_GOING_BLANK), 2000);
         }

         return null;
      } else {
         if (player != null) {
            Playable attacker = (Playable)(isSummon ? player.getSummon() : player);
            if (attacker.getLevel() - npc.getLevel() >= 9) {
               if (attacker.getBuffCount() > 0 || attacker.getDanceCount() > 0) {
                  npc.setTarget(attacker);
                  npc.doSimultaneousCast(new SkillHolder(5456, 1).getSkill());
               } else if (player.getParty() != null) {
                  for(Player pmember : player.getParty().getMembers()) {
                     if (pmember.getBuffCount() > 0 || pmember.getDanceCount() > 0) {
                        npc.setTarget(pmember);
                        npc.doSimultaneousCast(new SkillHolder(5456, 1).getSkill());
                     }
                  }
               }
            }
         }

         return super.onAttack(npc, player, damage, isSummon);
      }
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (npc.getId() == 36562 || npc.getId() == 36563 || npc.getId() == 36564 || npc.getId() == 36565) {
         npc.setIsHasNoChatWindow(false);
         if (npc.getId() == 36562) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE), 2000);
         }
      } else if (npc.getId() == 25653) {
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES), 2000);
      } else if (npc.getId() == 25654) {
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES), 2000);
      } else if (npc.getId() == 25655) {
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE), 2000);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof RimPailaka727.Pailaka727World) {
         RimPailaka727.Pailaka727World world = (RimPailaka727.Pailaka727World)tmpworld;
         if (npc.getId() == 25653 || npc.getId() == 25654 || npc.getId() == 25655) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.HOW_DARE_YOU), 2000);
         }

         switch(npc.getId()) {
            case 25655:
            case 25657:
            case 25658:
               if (world.isStatus(3) && this.checkAliveNpc(world)) {
                  world.incStatus();
                  world.underAttack = false;
                  this.finishInstance(world, false);
                  Reflection reflection = ReflectionManager.getInstance().getReflection(world.getReflectionId());
                  if (reflection != null) {
                     for(Npc knight : reflection.getNpcs()) {
                        if (knight != null && knight.getReflectionId() == world.getReflectionId() && knight.getId() == 36562) {
                           knight.broadcastPacket(
                              new NpcSay(
                                 knight.getObjectId(),
                                 1,
                                 knight.getId(),
                                 NpcStringId.YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME
                              ),
                              2000
                           );
                        }
                     }
                  }

                  if (player != null) {
                     Party party = player.getParty();
                     if (party == null) {
                        QuestState st = player.getQuestState(_727_HopeWithinTheDarkness.class.getSimpleName());
                        if (st != null && st.isCond(2)) {
                           st.setCond(3, true);
                        }
                     } else {
                        for(Player partyMember : party.getMembers()) {
                           if (partyMember != null && partyMember.getReflectionId() == player.getReflectionId()) {
                              QuestState st = partyMember.getQuestState(_727_HopeWithinTheDarkness.class.getSimpleName());
                              if (st != null && st.isCond(2)) {
                                 st.setCond(3, true);
                              }
                           }
                        }
                     }
                  }
               }
               break;
            case 36562:
            case 36563:
            case 36564:
            case 36565:
               world.setStatus(5);
               this.doCleanup(world);
               if (npc.getId() == 36562) {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH), 2000);
               } else if (npc.getId() == 36563) {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH), 2000);
               } else if (npc.getId() == 36564) {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.KYAAAK), 2000);
               } else if (npc.getId() == 36565) {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GASP_HOW_CAN_THIS_BE), 2000);
               }

               Reflection ref = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
               if (ref != null) {
                  for(Npc npcs : ref.getNpcs()) {
                     if (npcs != null && npcs.getReflectionId() == world.getReflectionId() && npcs.getId() >= 36562 && npcs.getId() <= 36565 && !npcs.isDead()
                        )
                      {
                        npcs.doDie(null);
                     }
                  }
               }

               this.finishInstance(world, false);
         }
      }

      return super.onKill(npc, player, isPet);
   }

   private boolean checkAliveNpc(RimPailaka727.Pailaka727World world) {
      boolean check = false;
      if (world != null && world.isStatus(3)) {
         Reflection reflection = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (reflection != null) {
            check = true;

            for(Npc npc : reflection.getNpcs()) {
               if (npc != null
                  && npc.getReflectionId() == world.getReflectionId()
                  && npc.getId() != 36562
                  && npc.getId() != 36563
                  && npc.getId() != 36564
                  && npc.getId() != 36565
                  && !npc.isDead()) {
                  check = false;
                  break;
               }
            }
         }
      }

      return check;
   }

   protected void doCleanup(RimPailaka727.Pailaka727World world) {
      if (world != null) {
         if (world.firstStageSpawn != null) {
            world.firstStageSpawn.cancel(true);
         }

         if (world.secondStageSpawn != null) {
            world.secondStageSpawn.cancel(true);
         }

         if (world.thirdStageSpawn != null) {
            world.thirdStageSpawn.cancel(true);
         }

         if (world.firstCommonSpawn != null) {
            world.firstCommonSpawn.cancel(true);
         }

         if (world.secondCommonSpawn != null) {
            world.secondCommonSpawn.cancel(true);
         }

         if (world.thirdCommonSpawn != null) {
            world.thirdCommonSpawn.cancel(true);
         }
      }
   }

   public static void main(String[] args) {
      new RimPailaka727();
   }

   private class FirstCommonSpawn implements Runnable {
      private final RimPailaka727.Pailaka727World _world;

      public FirstCommonSpawn(RimPailaka727.Pailaka727World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  Quest.addSpawn(25656, 50343, -12552, -9388, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50344, -12340, -9380, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50341, -12134, -9381, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50342, -11917, -9389, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50476, -12461, -9392, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50481, -12021, -9390, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50605, -12407, -9392, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50602, -12239, -9380, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25656, 50606, -12054, -9390, 32768, false, 0L, false, this._world.getReflectionId());
                  this._world.underAttack = true;
               }
            }
         }
      }
   }

   private class FirstStage implements Runnable {
      private final RimPailaka727.Pailaka727World _world;

      public FirstStage(RimPailaka727.Pailaka727World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  Quest.addSpawn(36565, 49093, -12077, -9395, 0, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(36563, 49094, -12238, -9386, 0, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(36564, 49093, -12401, -9388, 0, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(36562, 49232, -12239, -9386, 0, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25653, 50943, -12224, -9321, 32768, false, 0L, false, this._world.getReflectionId());
                  this._world.incStatus();
                  long stageDelay = inst.getParams().getLong("secondWaveDelay");
                  long spawnDelay = inst.getParams().getLong("firstSpawnDelay");
                  this._world.firstCommonSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka727.this.new FirstCommonSpawn(this._world), spawnDelay);
                  this._world.secondStageSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka727.this.new SecondStage(this._world), stageDelay);
               }
            }
         }
      }
   }

   private class Pailaka727World extends ReflectionWorld {
      public ScheduledFuture<?> firstStageSpawn;
      public ScheduledFuture<?> secondStageSpawn;
      public ScheduledFuture<?> thirdStageSpawn;
      public ScheduledFuture<?> firstCommonSpawn;
      public ScheduledFuture<?> secondCommonSpawn;
      public ScheduledFuture<?> thirdCommonSpawn;
      public boolean underAttack = false;

      private Pailaka727World() {
      }
   }

   private class SecondCommonSpawn implements Runnable {
      private final RimPailaka727.Pailaka727World _world;

      public SecondCommonSpawn(RimPailaka727.Pailaka727World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  Quest.addSpawn(25657, 50343, -12552, -9388, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50344, -12340, -9380, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50341, -12134, -9381, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50342, -11917, -9389, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50476, -12461, -9392, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50481, -12021, -9390, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50605, -12407, -9392, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50602, -12239, -9380, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50606, -12054, -9390, 32768, false, 0L, false, this._world.getReflectionId());
               }
            }
         }
      }
   }

   private class SecondStage implements Runnable {
      private final RimPailaka727.Pailaka727World _world;

      public SecondStage(RimPailaka727.Pailaka727World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  Quest.addSpawn(25654, 50943, -12224, -9321, 32768, false, 0L, false, this._world.getReflectionId());
                  this._world.incStatus();
                  long stageDelay = inst.getParams().getLong("thirdWaveDelay");
                  long spawnDelay = inst.getParams().getLong("secondSpawnDelay");
                  this._world.secondCommonSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka727.this.new SecondCommonSpawn(this._world), spawnDelay);
                  this._world.thirdStageSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka727.this.new ThirdStage(this._world), stageDelay);
               }
            }
         }
      }
   }

   private class ThirdCommonSpawn implements Runnable {
      private final RimPailaka727.Pailaka727World _world;

      public ThirdCommonSpawn(RimPailaka727.Pailaka727World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  Quest.addSpawn(25657, 50343, -12552, -9388, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50344, -12340, -9380, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50341, -12134, -9381, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25657, 50342, -11917, -9389, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25658, 50476, -12461, -9392, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25658, 50481, -12021, -9390, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25658, 50605, -12407, -9392, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25658, 50602, -12239, -9380, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25658, 50606, -12054, -9390, 32768, false, 0L, false, this._world.getReflectionId());
               }
            }
         }
      }
   }

   private class ThirdStage implements Runnable {
      private final RimPailaka727.Pailaka727World _world;

      public ThirdStage(RimPailaka727.Pailaka727World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  Quest.addSpawn(25655, 50943, -12004, -9321, 32768, false, 0L, false, this._world.getReflectionId());
                  Quest.addSpawn(25655, 50943, -12475, -9321, 32768, false, 0L, false, this._world.getReflectionId());
                  this._world.incStatus();
                  long spawnDelay = inst.getParams().getLong("thirdSpawnDelay");
                  this._world.secondCommonSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka727.this.new ThirdCommonSpawn(this._world), spawnDelay);
               }
            }
         }
      }
   }
}
