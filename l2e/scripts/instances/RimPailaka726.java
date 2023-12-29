package l2e.scripts.instances;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.quests._726_LightwithintheDarkness;

public final class RimPailaka726 extends AbstractReflection {
   private final Map<Integer, Integer> _fortReflections = new HashMap<>(21);

   private RimPailaka726() {
      super(RimPailaka726.class.getSimpleName(), "instances");
      this._fortReflections.put(35666, 80);
      this._fortReflections.put(35698, 81);
      this._fortReflections.put(35735, 82);
      this._fortReflections.put(35767, 83);
      this._fortReflections.put(35804, 84);
      this._fortReflections.put(35835, 85);
      this._fortReflections.put(35867, 86);
      this._fortReflections.put(35904, 87);
      this._fortReflections.put(35936, 88);
      this._fortReflections.put(35974, 89);
      this._fortReflections.put(36011, 90);
      this._fortReflections.put(36043, 91);
      this._fortReflections.put(36081, 92);
      this._fortReflections.put(36118, 93);
      this._fortReflections.put(36149, 94);
      this._fortReflections.put(36181, 95);
      this._fortReflections.put(36219, 96);
      this._fortReflections.put(36257, 97);
      this._fortReflections.put(36294, 98);
      this._fortReflections.put(36326, 99);
      this._fortReflections.put(36364, 100);

      for(int i : this._fortReflections.keySet()) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      this.addKillId(25661);
   }

   private final synchronized void enterInstance(Player player, Npc npc, int reflectionId) {
      if (this.enterInstance(player, npc, new RimPailaka726.Pailaka726World(), reflectionId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (inst != null) {
            long delay = inst.getParams().getLong("firstWaveDelay");
            ((RimPailaka726.Pailaka726World)world).firstStageSpawn = ThreadPoolManager.getInstance()
               .schedule(new RimPailaka726.FirstStage((RimPailaka726.Pailaka726World)world), delay);
         }
      }
   }

   @Override
   protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template) {
      Fort fort = npc.getFort();
      boolean checkConds = template.getParams().getBool("checkFortConditions");
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && fort != null) {
         if (player.getClan() == null || player.getClan().getFortId() != fort.getId()) {
            html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-01a.htm");
            player.sendPacket(html);
            return false;
         } else if (fort.getFortState() == 0 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-07.htm");
            player.sendPacket(html);
            return false;
         } else if (fort.getFortState() == 2 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-08.htm");
            player.sendPacket(html);
            return false;
         } else {
            return super.checkSoloType(player, npc, template);
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-01a.htm");
         player.sendPacket(html);
         return false;
      }
   }

   @Override
   protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template) {
      Fort fort = npc.getFort();
      boolean checkConds = template.getParams().getBool("checkFortConditions");
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player != null && fort != null) {
         Party party = player.getParty();
         if (party == null) {
            player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
            return false;
         } else if (party.getLeader() != player) {
            player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
            return false;
         } else if (player.getClan() == null || player.getClan().getFortId() != fort.getId()) {
            html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-01a.htm");
            player.sendPacket(html);
            return false;
         } else if (fort.getFortState() == 0 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-07.htm");
            player.sendPacket(html);
            return false;
         } else if (fort.getFortState() == 2 && checkConds) {
            html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-08.htm");
            player.sendPacket(html);
            return false;
         } else {
            for(Player partyMember : party.getMembers()) {
               if (partyMember.getClan() == null || partyMember.getClan().getFortId() == 0 || partyMember.getClan().getFortId() != fort.getId()) {
                  html.setFile(
                     player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-09.htm"
                  );
                  html.replace("%player%", partyMember.getName());
                  player.sendPacket(html);
                  return false;
               }
            }

            return super.checkPartyType(player, npc, template);
         }
      } else {
         html.setFile(player, "data/scripts/quests/" + _726_LightwithintheDarkness.class.getSimpleName() + "/" + player.getLang() + "/FortWarden-01a.htm");
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
   public String onKill(Npc npc, Player player, boolean isPet) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof RimPailaka726.Pailaka726World) {
         RimPailaka726.Pailaka726World world = (RimPailaka726.Pailaka726World)tmpworld;
         switch(npc.getId()) {
            case 25661:
               ++world.checkKills;
               if (world.checkKills > 1) {
                  if (player.isInParty()) {
                     for(Player partymember : player.getParty().getMembers()) {
                        if (partymember != null && !partymember.isDead()) {
                           QuestState st = partymember.getQuestState(_726_LightwithintheDarkness.class.getSimpleName());
                           if (st != null && st.isCond(1) && partymember.isInsideRadius(npc, 1000, true, false)) {
                              st.setCond(2, true);
                           }
                        }
                     }
                  } else {
                     QuestState st = player.getQuestState(_726_LightwithintheDarkness.class.getSimpleName());
                     if (st != null && st.isCond(1) && player.isInsideRadius(npc, 1000, true, false)) {
                        st.setCond(2, true);
                     }
                  }

                  this.doCleanup(world);
                  Reflection reflection = world.getReflection();
                  if (reflection != null) {
                     reflection.cleanupNpcs();
                  }

                  this.finishInstance(world, false);
               }
         }
      }

      return super.onKill(npc, player, isPet);
   }

   protected void broadCastPacket(RimPailaka726.Pailaka726World world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   protected void doCleanup(RimPailaka726.Pailaka726World world) {
      if (world.firstStageSpawn != null) {
         world.firstStageSpawn.cancel(true);
      }

      if (world.secondStageSpawn != null) {
         world.secondStageSpawn.cancel(true);
      }

      if (world.thirdStageSpawn != null) {
         world.thirdStageSpawn.cancel(true);
      }
   }

   public static void main(String[] args) {
      new RimPailaka726();
   }

   private class FirstStage implements Runnable {
      private final RimPailaka726.Pailaka726World _world;

      public FirstStage(RimPailaka726.Pailaka726World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (inst != null) {
               Quest.addSpawn(36562, 49384, -12232, -9384, 0, false, 0L, false, this._world.getReflectionId());
               Quest.addSpawn(36563, 49192, -12232, -9384, 0, false, 0L, false, this._world.getReflectionId());
               Quest.addSpawn(36564, 49192, -12456, -9392, 0, false, 0L, false, this._world.getReflectionId());
               Quest.addSpawn(36565, 49192, -11992, -9392, 0, false, 0L, false, this._world.getReflectionId());
               Quest.addSpawn(25659, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());
               RimPailaka726.this.broadCastPacket(this._world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_1, 2, 3000));

               for(int i = 0; i < 10; ++i) {
                  Quest.addSpawn(25662, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());
               }

               long delay = inst.getParams().getLong("secondWaveDelay");
               this._world.secondStageSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka726.this.new SecondStage(this._world), delay);
            }
         }
      }
   }

   private class Pailaka726World extends ReflectionWorld {
      public ScheduledFuture<?> firstStageSpawn;
      public ScheduledFuture<?> secondStageSpawn;
      public ScheduledFuture<?> thirdStageSpawn;
      public int checkKills = 0;

      private Pailaka726World() {
      }
   }

   private class SecondStage implements Runnable {
      private final RimPailaka726.Pailaka726World _world;

      public SecondStage(RimPailaka726.Pailaka726World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (inst != null) {
               RimPailaka726.this.broadCastPacket(this._world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_2, 2, 3000));
               Quest.addSpawn(25660, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());

               for(int i = 0; i < 10; ++i) {
                  Quest.addSpawn(25663, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());
               }

               long delay = inst.getParams().getLong("thirdWaveDelay");
               this._world.thirdStageSpawn = ThreadPoolManager.getInstance().schedule(RimPailaka726.this.new ThirdStage(this._world), delay);
            }
         }
      }
   }

   private class ThirdStage implements Runnable {
      private final RimPailaka726.Pailaka726World _world;

      public ThirdStage(RimPailaka726.Pailaka726World world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (inst != null) {
               RimPailaka726.this.broadCastPacket(this._world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_3, 2, 3000));
               Quest.addSpawn(25661, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());
               Quest.addSpawn(25661, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());

               for(int i = 0; i < 10; ++i) {
                  Quest.addSpawn(25664, 50536, -12232, -9384, 32768, false, 0L, false, this._world.getReflectionId());
               }
            }
         }
      }
   }
}
