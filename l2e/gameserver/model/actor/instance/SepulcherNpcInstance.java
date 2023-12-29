package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.instancemanager.FourSepulchersManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SocialAction;

public class SepulcherNpcInstance extends Npc {
   protected Future<?> _closeTask = null;
   protected Future<?> _spawnNextMysteriousBoxTask = null;
   protected Future<?> _spawnMonsterTask = null;
   private static final String HTML_FILE_PATH = "data/html/SepulcherNpc/";
   private static final int HALLS_KEY = 7260;

   public SepulcherNpcInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.SepulcherNpcInstance);
      this.setShowSummonAnimation(true);
      if (this._closeTask != null) {
         this._closeTask.cancel(true);
      }

      if (this._spawnNextMysteriousBoxTask != null) {
         this._spawnNextMysteriousBoxTask.cancel(true);
      }

      if (this._spawnMonsterTask != null) {
         this._spawnMonsterTask.cancel(true);
      }

      this._closeTask = null;
      this._spawnNextMysteriousBoxTask = null;
      this._spawnMonsterTask = null;
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this.setShowSummonAnimation(false);
   }

   @Override
   public void deleteMe() {
      if (this._closeTask != null) {
         this._closeTask.cancel(true);
         this._closeTask = null;
      }

      if (this._spawnNextMysteriousBoxTask != null) {
         this._spawnNextMysteriousBoxTask.cancel(true);
         this._spawnNextMysteriousBoxTask = null;
      }

      if (this._spawnMonsterTask != null) {
         this._spawnMonsterTask.cancel(true);
         this._spawnMonsterTask = null;
      }

      super.deleteMe();
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         if (this != player.getTarget()) {
            if (Config.DEBUG) {
               _log.info("new target selected:" + this.getObjectId());
            }

            player.setTarget(this);
         } else if (interact) {
            if (this.isAutoAttackable(player) && !this.isAlikeDead()) {
               if (Math.abs(player.getZ() - this.getZ()) < 400) {
                  player.getAI().setIntention(CtrlIntention.ATTACK, this);
               } else {
                  player.sendActionFailed();
               }
            }

            if (!this.isAutoAttackable(player)) {
               if (!this.canInteract(player)) {
                  player.getAI().setIntention(CtrlIntention.INTERACT, this);
               } else {
                  SocialAction sa = new SocialAction(this.getObjectId(), Rnd.get(8));
                  this.broadcastPacket(sa);
                  this.doAction(player);
               }
            }

            player.sendActionFailed();
         }
      }
   }

   private void doAction(Player player) {
      if (this.isDead()) {
         player.sendActionFailed();
      } else {
         switch(this.getId()) {
            case 31455:
            case 31456:
            case 31457:
            case 31458:
            case 31459:
            case 31460:
            case 31461:
            case 31462:
            case 31463:
            case 31464:
            case 31465:
            case 31466:
            case 31467:
               this.setIsInvul(false);
               this.reduceCurrentHp(this.getMaxHp() + 1.0, player, null);
               if (player.getParty() != null && !player.getParty().isLeader(player)) {
                  player = player.getParty().getLeader();
               }

               player.addItem("Quest", 7260, 1L, player, true);
               break;
            case 31468:
            case 31469:
            case 31470:
            case 31471:
            case 31472:
            case 31473:
            case 31474:
            case 31475:
            case 31476:
            case 31477:
            case 31478:
            case 31479:
            case 31480:
            case 31481:
            case 31482:
            case 31483:
            case 31484:
            case 31485:
            case 31486:
            case 31487:
               this.setIsInvul(false);
               this.reduceCurrentHp(this.getMaxHp() + 1.0, player, null);
               if (this._spawnMonsterTask != null) {
                  this._spawnMonsterTask.cancel(true);
               }

               this._spawnMonsterTask = ThreadPoolManager.getInstance().schedule(new SepulcherNpcInstance.SpawnMonster(this.getId()), 3500L);
               break;
            default:
               List<Quest> qlsa = this.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
               List<Quest> qlst = this.getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
               if (qlsa != null && !qlsa.isEmpty()) {
                  player.setLastQuestNpcObject(this.getObjectId());
               }

               if (qlst != null && qlst.size() == 1) {
                  qlst.get(0).notifyFirstTalk(this, player);
               } else {
                  this.showChatWindow(player, 0);
               }
         }

         player.sendActionFailed();
      }
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/SepulcherNpc/" + pom + ".htm";
   }

   @Override
   public void showChatWindow(Player player, int val) {
      String filename = this.getHtmlPath(this.getId(), val);
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
      player.sendActionFailed();
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (this.isBusy()) {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), "data/html/npcbusy.htm");
         html.replace("%busymessage%", this.getBusyMessage());
         html.replace("%npcname%", this.getName());
         html.replace("%playername%", player.getName());
         player.sendPacket(html);
      } else if (command.startsWith("Chat")) {
         int val = 0;

         try {
            val = Integer.parseInt(command.substring(5));
         } catch (IndexOutOfBoundsException var6) {
         } catch (NumberFormatException var7) {
         }

         this.showChatWindow(player, val);
      } else if (command.startsWith("open_gate")) {
         ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
         if (hallsKey == null) {
            this.showHtmlFile(player, "Gatekeeper-no.htm");
         } else if (FourSepulchersManager.getInstance().isAttackTime()) {
            switch(this.getId()) {
               case 31929:
               case 31934:
               case 31939:
               case 31944:
                  FourSepulchersManager.getInstance().spawnShadow(this.getId());
            }

            this.openNextDoor(this.getId());
            if (player.getParty() != null) {
               for(Player mem : player.getParty().getMembers()) {
                  if (mem != null && mem.getInventory().getItemByItemId(7260) != null) {
                     mem.destroyItemByItemId("Quest", 7260, mem.getInventory().getItemByItemId(7260).getCount(), mem, true);
                  }
               }
            } else {
               player.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), player, true);
            }
         }
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   public void openNextDoor(int npcId) {
      int doorId = FourSepulchersManager.getInstance().getHallGateKeepers().get(npcId);
      DoorParser _DoorParser = DoorParser.getInstance();
      _DoorParser.getDoor(doorId).openMe();
      if (this._closeTask != null) {
         this._closeTask.cancel(true);
      }

      this._closeTask = ThreadPoolManager.getInstance().schedule(new SepulcherNpcInstance.CloseNextDoor(doorId), 10000L);
      if (this._spawnNextMysteriousBoxTask != null) {
         this._spawnNextMysteriousBoxTask.cancel(true);
      }

      this._spawnNextMysteriousBoxTask = ThreadPoolManager.getInstance().schedule(new SepulcherNpcInstance.SpawnNextMysteriousBox(npcId), 0L);
   }

   public void sayInShout(NpcStringId msg, int time) {
      if (msg != null) {
         if (msg.getId() == 1000456) {
            for(Player player : World.getInstance().getAllPlayers()) {
               if (player != null && Util.checkIfInRange(15000, player, this, true)) {
                  ServerMessage msgs = new ServerMessage("SepulcherNpc.MIN_PASSED", player.getLang());
                  msgs.add(time);
                  player.sendPacket(
                     new CreatureSay(
                        0, 23, player.getLang() != null && !player.getLang().equalsIgnoreCase("en") ? this.getNameRu() : this.getName(), msgs.toString()
                     )
                  );
               }
            }
         } else {
            for(Player player : World.getInstance().getAllPlayers()) {
               if (player != null && Util.checkIfInRange(15000, player, this, true)) {
                  player.sendPacket(
                     new CreatureSay(0, 23, player.getLang() != null && !player.getLang().equalsIgnoreCase("en") ? this.getNameRu() : this.getName(), msg)
                  );
               }
            }
         }
      }
   }

   public void showHtmlFile(Player player, String file) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), "data/html/SepulcherNpc/" + file);
      html.replace("%npcname%", player.getLang() != null && !player.getLang().equalsIgnoreCase("en") ? this.getNameRu() : this.getName());
      player.sendPacket(html);
   }

   private static class CloseNextDoor implements Runnable {
      final DoorParser _DoorParser = DoorParser.getInstance();
      private final int _DoorId;

      public CloseNextDoor(int doorId) {
         this._DoorId = doorId;
      }

      @Override
      public void run() {
         try {
            this._DoorParser.getDoor(this._DoorId).closeMe();
         } catch (Exception var2) {
            Creature._log.warning(var2.getMessage());
         }
      }
   }

   private static class SpawnMonster implements Runnable {
      private final int _NpcId;

      public SpawnMonster(int npcId) {
         this._NpcId = npcId;
      }

      @Override
      public void run() {
         FourSepulchersManager.getInstance().spawnMonster(this._NpcId);
      }
   }

   private static class SpawnNextMysteriousBox implements Runnable {
      private final int _NpcId;

      public SpawnNextMysteriousBox(int npcId) {
         this._NpcId = npcId;
      }

      @Override
      public void run() {
         FourSepulchersManager.getInstance().spawnMysteriousBox(this._NpcId);
      }
   }
}
