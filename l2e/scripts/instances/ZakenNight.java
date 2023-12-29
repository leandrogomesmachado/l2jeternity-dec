package l2e.scripts.instances;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class ZakenNight extends AbstractReflection {
   private boolean _teleported = false;
   private final int[][] SPAWNS = new int[][]{
      {54240, 220133, -3498},
      {54240, 218073, -3498},
      {55265, 219095, -3498},
      {56289, 220133, -3498},
      {56289, 218073, -3498},
      {54240, 220133, -3226},
      {54240, 218073, -3226},
      {55265, 219095, -3226},
      {56289, 220133, -3226},
      {56289, 218073, -3226},
      {54240, 220133, -2954},
      {54240, 218073, -2954},
      {55265, 219095, -2954},
      {56289, 220133, -2954},
      {56289, 218073, -2954}
   };

   public ZakenNight(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32713);
      this.addTalkId(32713);
      this.addKillId(29022);
      this.addAttackId(29022);
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
   protected boolean checkConditions(Player player, Npc npc, ReflectionTemplate template) {
      boolean checkTime = template.getParams().getBool("checkValidTime");
      if (!checkTime || this.getTimeHour() <= 4 && this.getTimeHour() >= 24) {
         return super.checkConditions(player, npc, template);
      } else {
         player.sendMessage(new ServerMessage("Zaken.INVALID_TIME", player.getLang()).toString());
         return false;
      }
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new ZakenNight.ZakenNightWorld(), 114)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         addSpawn(29022, 55312, 219168, -3223, 0, false, 0L, false, world.getReflectionId());
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      int i = getRandom(this.SPAWNS.length);
      if (npc.getId() == 29022 && !npc.isDead()) {
         if (event.equalsIgnoreCase("teleport")) {
            ((Attackable)npc).reduceHate(player, 9999);
            ((Attackable)npc).abortAttack();
            ((Attackable)npc).abortCast();
            npc.broadcastPacket(new MagicSkillUse(npc, 4222, 1, 1000, 0));
            this.startQuestTimer("finish_teleport", 1500L, npc, player);
         } else if (event.equalsIgnoreCase("finish_teleport")) {
            npc.teleToLocation(this.SPAWNS[i][0], this.SPAWNS[i][1], this.SPAWNS[i][2], true);
            npc.getSpawn().setX(this.SPAWNS[i][0]);
            npc.getSpawn().setY(this.SPAWNS[i][1]);
            npc.getSpawn().setZ(this.SPAWNS[i][2]);
            this._teleported = false;
         }
      }

      return event;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.getId() == 29022 && !this._teleported) {
         this.startQuestTimer("teleport", 300000L, npc, attacker);
         this._teleported = true;
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ReflectionWorld tmpWorld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpWorld instanceof ZakenNight.ZakenNightWorld) {
         ZakenNight.ZakenNightWorld world = (ZakenNight.ZakenNightWorld)tmpWorld;
         int npcId = npc.getId();
         if (npcId == 29022) {
            this.finishInstance(world, true);
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npcId == 32713) {
         this.enterInstance(player, npc);
      }

      return "";
   }

   private int getTimeHour() {
      return GameTimeController.getInstance().getGameTime() / 60 % 24;
   }

   public static void main(String[] args) {
      new ZakenNight(ZakenNight.class.getSimpleName(), "instances");
   }

   private class ZakenNightWorld extends ReflectionWorld {
      public ZakenNightWorld() {
      }
   }
}
