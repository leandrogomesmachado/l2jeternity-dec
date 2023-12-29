package l2e.scripts.instances;

import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.quests._693_DefeatingDragonkinRemnants;
import org.apache.commons.lang3.ArrayUtils;

public class SoDMountedTroop extends AbstractReflection {
   private static final int[] ENTRANCE_ROOM_DOORS = new int[]{12240001, 12240002};
   private static final int[] _templates = new int[]{123, 124, 125, 126};

   public SoDMountedTroop(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32527);
      this.addTalkId(32527);
      this.addKillId(18703);
      this.addKillId(new int[]{18784, 18785, 18786, 18787, 18788, 18789, 18790});
   }

   private final synchronized void enterInstance(Player player, Npc npc, int templateId) {
      if (this.enterInstance(player, npc, new SoDMountedTroop.MTWorld(), templateId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((SoDMountedTroop.MTWorld)world).startTime = System.currentTimeMillis();
         ((SoDMountedTroop.MTWorld)world).reflectionId = templateId;
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
   protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template) {
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (SoDManager.isAttackStage()) {
         html.setFile(player, "data/scripts/quests/" + _693_DefeatingDragonkinRemnants.class.getSimpleName() + "/" + player.getLang() + "/32527-15.htm");
         player.sendPacket(html);
         return false;
      } else {
         return super.checkSoloType(player, npc, template);
      }
   }

   @Override
   protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template) {
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (SoDManager.isAttackStage()) {
         html.setFile(player, "data/scripts/quests/" + _693_DefeatingDragonkinRemnants.class.getSimpleName() + "/" + player.getLang() + "/32527-15.htm");
         player.sendPacket(html);
         return false;
      } else {
         return super.checkPartyType(player, npc, template);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (npc.getId() == 32527 && Util.isDigit(event) && ArrayUtils.contains(_templates, Integer.valueOf(event).intValue())) {
         this.enterInstance(player, npc, Integer.valueOf(event));
         return null;
      } else {
         return super.onAdvEvent(event, npc, player);
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld != null && tmpworld instanceof SoDMountedTroop.MTWorld) {
         SoDMountedTroop.MTWorld world = (SoDMountedTroop.MTWorld)tmpworld;
         if (world != null) {
            if (npc.getId() == 18786 && world.getStatus() == 0) {
               world.incStatus();

               for(int i : ENTRANCE_ROOM_DOORS) {
                  world.getReflection().openDoor(i);
               }
            }

            if (this.checkNpcsStatus(npc, world)) {
               Reflection inst = ReflectionManager.getInstance().getReflection(tmpworld.getReflectionId());
               if (inst != null) {
                  inst.setDuration(300000);
                  if (world.getAllowed() != null) {
                     long timeDiff = (System.currentTimeMillis() - world.startTime) / 60000L;

                     for(int playerId : world.getAllowed()) {
                        Player pl = World.getInstance().getPlayer(playerId);
                        if (pl != null && pl.isOnline() && pl.getReflectionId() == world.getReflectionId()) {
                           QuestState qst = pl.getQuestState(_693_DefeatingDragonkinRemnants.class.getSimpleName());
                           if (qst != null) {
                              qst.setCond(2, true);
                              qst.set("timeDiff", String.valueOf(timeDiff));
                              qst.set("reflectionId", world.reflectionId);
                           }
                        }
                     }
                  }
               }
            }
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return super.onKill(npc, player, isSummon);
      }
   }

   private boolean checkNpcsStatus(Npc npc, SoDMountedTroop.MTWorld wrld) {
      Reflection inst = ReflectionManager.getInstance().getReflection(wrld.getReflectionId());
      if (inst != null) {
         for(Npc n : inst.getNpcs()) {
            if (n != null && n.getReflectionId() == wrld.getReflectionId() && !n.isDead()) {
               return false;
            }
         }
      }

      return true;
   }

   public static void main(String[] args) {
      new SoDMountedTroop(SoDMountedTroop.class.getSimpleName(), "instances");
   }

   private class MTWorld extends ReflectionWorld {
      private long startTime;
      private int reflectionId;

      public MTWorld() {
      }
   }
}
