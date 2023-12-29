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
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.quests._512_BladeUnderFoot;

public final class Dungeon512 extends AbstractReflection {
   private final Map<Integer, Integer> _castleReflections = new HashMap<>(9);
   private static final int[] RAIDS1 = new int[]{25546, 25549, 25552};
   private static final int[] RAIDS2 = new int[]{25553, 25554, 25557, 25560};
   private static final int[] RAIDS3 = new int[]{25563, 25566, 25569};

   private Dungeon512() {
      super(Dungeon512.class.getSimpleName(), "instances");
      this._castleReflections.put(36403, 13);
      this._castleReflections.put(36404, 14);
      this._castleReflections.put(36405, 15);
      this._castleReflections.put(36406, 16);
      this._castleReflections.put(36407, 17);
      this._castleReflections.put(36408, 18);
      this._castleReflections.put(36409, 19);
      this._castleReflections.put(36410, 20);
      this._castleReflections.put(36411, 21);

      for(int i : this._castleReflections.keySet()) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      for(int i = 25546; i <= 25571; ++i) {
         this.addAttackId(i);
      }

      this.addKillId(new int[]{25546, 25549, 25552, 25553, 25554, 25557, 25560, 25563, 25566, 25569});
   }

   private final synchronized void enterInstance(Player player, Npc npc, int reflectionId) {
      if (this.enterInstance(player, npc, new Dungeon512.Dungeon512World(), reflectionId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (ref != null) {
            long delay = ref.getParams().getLong("stageSpawnDelay");
            ((Dungeon512.Dungeon512World)world).stageSpawnDelay = ThreadPoolManager.getInstance()
               .schedule(new Dungeon512.StageSpawn((Dungeon512.Dungeon512World)world), delay);
         }
      }
   }

   @Override
   protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template) {
      Castle castle = npc.getCastle();
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && castle != null) {
         if (player.getClan() != null && player.getClan().getCastleId() == castle.getId()) {
            QuestState st = player.getQuestState(_512_BladeUnderFoot.class.getSimpleName());
            if (st != null && st.getCond() >= 1) {
               return super.checkSoloType(player, npc, template);
            } else {
               html.setFile(player, "data/scripts/quests/" + _512_BladeUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-02.htm");
               html.replace("%player%", player.getName());
               player.sendPacket(html);
               return false;
            }
         } else {
            html.setFile(player, "data/scripts/quests/" + _512_BladeUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-01.htm");
            player.sendPacket(html);
            return false;
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _512_BladeUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-01.htm");
         player.sendPacket(html);
         return false;
      }
   }

   @Override
   protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template) {
      Castle castle = npc.getCastle();
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && castle != null) {
         if (player.getClan() != null && player.getClan().getCastleId() == castle.getId()) {
            Party party = player.getParty();
            if (party == null) {
               player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
               return false;
            } else if (party.getLeader() != player) {
               player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
               return false;
            } else {
               for(Player partyMember : party.getMembers()) {
                  QuestState st = partyMember.getQuestState(_512_BladeUnderFoot.class.getSimpleName());
                  if (st == null || st.getCond() < 1) {
                     html.setFile(player, "data/scripts/quests/" + _512_BladeUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-02.htm");
                     html.replace("%player%", partyMember.getName());
                     player.sendPacket(html);
                  }
               }

               return super.checkPartyType(player, npc, template);
            }
         } else {
            html.setFile(player, "data/scripts/quests/" + _512_BladeUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-01.htm");
            player.sendPacket(html);
            return false;
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _512_BladeUnderFoot.class.getSimpleName() + "/" + player.getLang() + "/CastleWarden-01.htm");
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
         this.enterInstance(player, npc, this._castleReflections.get(npc.getId()));
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
      if (tmpworld != null && tmpworld instanceof Dungeon512.Dungeon512World) {
         Dungeon512.Dungeon512World world = (Dungeon512.Dungeon512World)tmpworld;
         if (Util.contains(RAIDS3, npc.getId())) {
            int allowed = world.getAllowed().size();
            if (player.getParty() != null) {
               for(Player pl : player.getParty().getMembers()) {
                  if (pl != null && pl.getReflectionId() == world.getReflectionId()) {
                     QuestState st = pl.getQuestState(_512_BladeUnderFoot.class.getSimpleName());
                     if (st != null && st.isCond(1)) {
                        st.calcReward(512, allowed);
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               }
            } else {
               QuestState st = player.getQuestState(_512_BladeUnderFoot.class.getSimpleName());
               if (st != null && st.isCond(1)) {
                  st.calcReward(512, allowed);
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
               world.stageSpawnDelay = ThreadPoolManager.getInstance().schedule(new Dungeon512.StageSpawn(world), delay);
            }
         }
      }

      return super.onKill(npc, player, isPet);
   }

   public static void main(String[] args) {
      new Dungeon512();
   }

   private class Dungeon512World extends ReflectionWorld {
      public ScheduledFuture<?> stageSpawnDelay;

      private Dungeon512World() {
      }
   }

   private class StageSpawn implements Runnable {
      private final Dungeon512.Dungeon512World _world;

      public StageSpawn(Dungeon512.Dungeon512World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (inst != null) {
               int spawnId;
               switch(this._world.getStatus()) {
                  case 0:
                     spawnId = Dungeon512.RAIDS1[Quest.getRandom(Dungeon512.RAIDS1.length)];
                     break;
                  case 1:
                     spawnId = Dungeon512.RAIDS2[Quest.getRandom(Dungeon512.RAIDS2.length)];
                     break;
                  default:
                     spawnId = Dungeon512.RAIDS3[Quest.getRandom(Dungeon512.RAIDS3.length)];
               }

               Npc raid = Quest.addSpawn(spawnId, 12161, -49144, -3000, 0, false, 0L, false, this._world.getReflectionId());
               if (raid instanceof RaidBossInstance) {
                  ((RaidBossInstance)raid).setUseRaidCurse(false);
               }
            }
         }
      }
   }
}
