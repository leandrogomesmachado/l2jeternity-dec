package l2e.scripts.instances;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.quests._511_AwlUnderFoot;

public final class Dungeon511 extends AbstractReflection {
   private final Map<Integer, Integer> _fortReflections = new HashMap<>(21);
   private static final int[] RAIDS1 = new int[]{25572, 25575, 25578};
   private static final int[] RAIDS2 = new int[]{25579, 25582, 25585, 25588};
   private static final int[] RAIDS3 = new int[]{25589, 25592, 25593};

   private Dungeon511() {
      super(Dungeon511.class.getSimpleName(), "instances");
      this._fortReflections.put(35666, 22);
      this._fortReflections.put(35698, 23);
      this._fortReflections.put(35735, 24);
      this._fortReflections.put(35767, 25);
      this._fortReflections.put(35804, 26);
      this._fortReflections.put(35835, 27);
      this._fortReflections.put(35867, 28);
      this._fortReflections.put(35904, 29);
      this._fortReflections.put(35936, 30);
      this._fortReflections.put(35974, 31);
      this._fortReflections.put(36011, 32);
      this._fortReflections.put(36043, 33);
      this._fortReflections.put(36081, 34);
      this._fortReflections.put(36118, 35);
      this._fortReflections.put(36149, 36);
      this._fortReflections.put(36181, 37);
      this._fortReflections.put(36219, 38);
      this._fortReflections.put(36257, 39);
      this._fortReflections.put(36294, 40);
      this._fortReflections.put(36326, 41);
      this._fortReflections.put(36364, 42);

      for(int i : this._fortReflections.keySet()) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      for(int i = 25572; i <= 25595; ++i) {
         this.addAttackId(i);
      }

      this.addKillId(new int[]{25572, 25575, 25578, 25579, 25582, 25585, 25588, 25589, 25592, 25593});
   }

   private final synchronized void enterInstance(Player player, Npc npc, int reflectionId) {
      if (this.enterInstance(player, npc, new Dungeon511.Dungeon511World(), reflectionId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (ref != null) {
            long delay = ref.getParams().getLong("stageSpawnDelay");
            ((Dungeon511.Dungeon511World)world).stageSpawnDelay = ThreadPoolManager.getInstance()
               .schedule(new Dungeon511.StageSpawn((Dungeon511.Dungeon511World)world), delay);
         }
      }
   }

   @Override
   protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template) {
      Fort fortress = npc.getFort();
      boolean checkConds = template.getParams().getBool("checkFortConditions");
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && fortress != null) {
         if (player.getClan() == null || player.getClan().getFortId() != fortress.getId()) {
            html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-01.htm");
            player.sendPacket(html);
            return false;
         } else if (fortress.getFortState() == 0 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-02.htm");
            player.sendPacket(html);
            return false;
         } else if (fortress.getFortState() == 2 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-03.htm");
            player.sendPacket(html);
            return false;
         } else {
            QuestState st = player.getQuestState(_511_AwlUnderFoot.class.getSimpleName());
            if (st != null && st.getCond() >= 1) {
               return super.checkSoloType(player, npc, template);
            } else {
               html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-04.htm");
               html.replace("%player%", player.getName());
               player.sendPacket(html);
               return false;
            }
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-01.htm");
         player.sendPacket(html);
         return false;
      }
   }

   @Override
   protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template) {
      Fort fortress = npc.getFort();
      boolean checkConds = template.getParams().getBool("checkFortConditions");
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && fortress != null) {
         if (player.getClan() == null || player.getClan().getFortId() != fortress.getId()) {
            html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-01.htm");
            player.sendPacket(html);
            return false;
         } else if (fortress.getFortState() == 0 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-02.htm");
            player.sendPacket(html);
            return false;
         } else if (fortress.getFortState() == 2 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-03.htm");
            player.sendPacket(html);
            return false;
         } else {
            Party party = player.getParty();
            if (party == null) {
               player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
               return false;
            } else if (party.getLeader() != player) {
               player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
               return false;
            } else {
               for(Player partyMember : party.getMembers()) {
                  QuestState st = partyMember.getQuestState(_511_AwlUnderFoot.class.getSimpleName());
                  if (st == null
                     || st.getCond() < 1
                     || partyMember.getClan() == null
                     || partyMember.getClan().getFortId() == 0
                     || partyMember.getClan().getFortId() != fortress.getId()) {
                     html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-04.htm");
                     html.replace("%player%", partyMember.getName());
                     player.sendPacket(html);
                     return false;
                  }
               }

               return super.checkPartyType(player, npc, template);
            }
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _511_AwlUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/FortressWarden-01.htm");
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
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("Enter")) {
         this.enterInstance(player, npc, this._fortReflections.get(npc.getId()));
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon) {
      Playable attacker = (Playable)(isSummon ? player.getSummon() : player);
      if (attacker.getLevel() - npc.getLevel() >= 9) {
         if (player.getParty() == null) {
            if (attacker.getBuffCount() > 0 || attacker.getDanceCount() > 0) {
               npc.setTarget(attacker);
               npc.doSimultaneousCast(new SkillHolder(5456, 1).getSkill());
            }
         } else if (player.getParty() != null) {
            for(Player pmember : player.getParty().getMembers()) {
               if (pmember.getBuffCount() > 0 || pmember.getDanceCount() > 0) {
                  npc.setTarget(pmember);
                  npc.doSimultaneousCast(new SkillHolder(5456, 1).getSkill());
               }
            }
         }
      }

      return super.onAttack(npc, player, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof Dungeon511.Dungeon511World) {
         Dungeon511.Dungeon511World world = (Dungeon511.Dungeon511World)tmpworld;
         if (Util.contains(RAIDS3, npc.getId())) {
            if (player.getParty() != null) {
               for(Player pl : player.getParty().getMembers()) {
                  if (pl != null && pl.getReflectionId() == world.getReflectionId()) {
                     QuestState st = pl.getQuestState(_511_AwlUnderFoot.class.getSimpleName());
                     if (st != null && st.isCond(1)) {
                        st.calcReward(511);
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               }
            } else {
               QuestState st = player.getQuestState(_511_AwlUnderFoot.class.getSimpleName());
               if (st != null && st.isCond(1)) {
                  st.calcReward(511);
                  st.playSound("ItemSound.quest_itemget");
               }
            }

            this.finishInstance(world, false);
            if (world.stageSpawnDelay != null) {
               world.stageSpawnDelay.cancel(true);
            }
         } else {
            world.incStatus();
            Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
            if (ref != null) {
               long delay = ref.getParams().getLong("stageSpawnDelay");
               world.stageSpawnDelay = ThreadPoolManager.getInstance().schedule(new Dungeon511.StageSpawn(world), delay);
            }
         }
      }

      return super.onKill(npc, player, isPet);
   }

   public static void main(String[] args) {
      new Dungeon511();
   }

   private class Dungeon511World extends ReflectionWorld {
      public ScheduledFuture<?> stageSpawnDelay;

      private Dungeon511World() {
      }
   }

   private class StageSpawn implements Runnable {
      private final Dungeon511.Dungeon511World _world;

      public StageSpawn(Dungeon511.Dungeon511World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (inst != null) {
               int spawnId;
               if (this._world.isStatus(0)) {
                  spawnId = Dungeon511.RAIDS1[Quest.getRandom(Dungeon511.RAIDS1.length)];
               } else if (this._world.isStatus(1)) {
                  spawnId = Dungeon511.RAIDS2[Quest.getRandom(Dungeon511.RAIDS2.length)];
               } else {
                  spawnId = Dungeon511.RAIDS3[Quest.getRandom(Dungeon511.RAIDS3.length)];
               }

               Npc raid = Quest.addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0L, false, this._world.getReflectionId());
               if (raid instanceof RaidBossInstance) {
                  ((RaidBossInstance)raid).setUseRaidCurse(false);
               }
            }
         }
      }
   }
}
