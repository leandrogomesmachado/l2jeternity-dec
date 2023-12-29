package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortLogisticsInstance extends MerchantInstance {
   private static final int[] SUPPLY_BOX_IDS = new int[]{
      35665, 35697, 35734, 35766, 35803, 35834, 35866, 35903, 35935, 35973, 36010, 36042, 36080, 36117, 36148, 36180, 36218, 36256, 36293, 36325, 36363
   };

   public FortLogisticsInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.FortLogisticsInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player.getLastFolkNPC().getObjectId() == this.getObjectId()) {
         StringTokenizer st = new StringTokenizer(command, " ");
         String actualCommand = st.nextToken();
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         if (actualCommand.equalsIgnoreCase("rewards")) {
            if (this.isMyLord(player)) {
               html.setFile(player, player.getLang(), "data/html/fortress/logistics-rewards.htm");
               html.replace("%bloodoath%", String.valueOf(player.getClan().getBloodOathCount()));
            } else {
               html.setFile(player, player.getLang(), "data/html/fortress/logistics-noprivs.htm");
            }

            html.replace("%objectId%", String.valueOf(this.getObjectId()));
            player.sendPacket(html);
         } else if (actualCommand.equalsIgnoreCase("blood")) {
            if (this.isMyLord(player)) {
               int blood = player.getClan().getBloodOathCount();
               if (blood > 0) {
                  player.addItem("Quest", 9910, (long)blood, this, true);
                  player.getClan().resetBloodOathCount();
                  html.setFile(player, player.getLang(), "data/html/fortress/logistics-blood.htm");
               } else {
                  html.setFile(player, player.getLang(), "data/html/fortress/logistics-noblood.htm");
               }
            } else {
               html.setFile(player, player.getLang(), "data/html/fortress/logistics-noprivs.htm");
            }

            html.replace("%objectId%", String.valueOf(this.getObjectId()));
            player.sendPacket(html);
         } else if (actualCommand.equalsIgnoreCase("supplylvl")) {
            if (this.getFort().getFortState() == 2) {
               if (player.isClanLeader()) {
                  html.setFile(player, player.getLang(), "data/html/fortress/logistics-supplylvl.htm");
                  html.replace("%supplylvl%", String.valueOf(this.getFort().getSupplyLvL()));
               } else {
                  html.setFile(player, player.getLang(), "data/html/fortress/logistics-noprivs.htm");
               }
            } else {
               html.setFile(player, player.getLang(), "data/html/fortress/logistics-1.htm");
            }

            html.replace("%objectId%", String.valueOf(this.getObjectId()));
            player.sendPacket(html);
         } else if (actualCommand.equalsIgnoreCase("supply")) {
            if (this.isMyLord(player)) {
               if (this.getFort().getSiege().getIsInProgress()) {
                  html.setFile(player, player.getLang(), "data/html/fortress/logistics-siege.htm");
               } else {
                  int level = this.getFort().getSupplyLvL();
                  if (level > 0) {
                     NpcTemplate BoxTemplate = NpcsParser.getInstance().getTemplate(SUPPLY_BOX_IDS[level - 1]);
                     MonsterInstance box = new MonsterInstance(IdFactory.getInstance().getNextId(), BoxTemplate);
                     box.setCurrentHp(box.getMaxHp());
                     box.setCurrentMp(box.getMaxMp());
                     box.setHeading(0);
                     box.spawnMe(this.getX() - 23, this.getY() + 41, this.getZ());
                     this.getFort().setSupplyLvL(0);
                     this.getFort().saveFortVariables();
                     html.setFile(player, player.getLang(), "data/html/fortress/logistics-supply.htm");
                  } else {
                     html.setFile(player, player.getLang(), "data/html/fortress/logistics-nosupply.htm");
                  }
               }
            } else {
               html.setFile(player, player.getLang(), "data/html/fortress/logistics-noprivs.htm");
            }

            html.replace("%objectId%", String.valueOf(this.getObjectId()));
            player.sendPacket(html);
         } else {
            super.onBypassFeedback(player, command);
         }
      }
   }

   @Override
   public void showChatWindow(Player player) {
      this.showMessageWindow(player, 0);
   }

   private void showMessageWindow(Player player, int val) {
      player.sendActionFailed();
      String filename;
      if (val == 0) {
         filename = "data/html/fortress/logistics.htm";
      } else {
         filename = "data/html/fortress/logistics-" + val + ".htm";
      }

      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcId%", String.valueOf(this.getId()));
      if (this.getFort().getOwnerClan() != null) {
         html.replace("%clanname%", this.getFort().getOwnerClan().getName());
      } else {
         html.replace("%clanname%", "NPC");
      }

      player.sendPacket(html);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "logistics";
      } else {
         pom = "logistics-" + val;
      }

      return "data/html/fortress/" + pom + ".htm";
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }
}
