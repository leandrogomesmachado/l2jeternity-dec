package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.scripts.ai.AbstractNpcAI;

public final class WyvernManager extends AbstractNpcAI {
   private static final int CRYSTAL_B_GRADE = 1460;
   private static final int WYVERN = 12621;
   private static final int WYVERN_FEE = 25;
   private static final int STRIDER_LVL = 55;
   private static final int[] STRIDERS = new int[]{12526, 12527, 12528, 16038, 16039, 16040, 16068, 13197};
   private static final Map<Integer, WyvernManager.ManagerType> MANAGERS = new HashMap<>();

   private WyvernManager(String name, String descr) {
      super(name, descr);

      for(int npcId : MANAGERS.keySet()) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
         this.addFirstTalkId(npcId);
      }
   }

   private String mountWyvern(Npc npc, Player player) {
      if (!player.isMounted() || player.getMountLevel() < 55 || !Util.contains(STRIDERS, player.getMountNpcId())) {
         return this.replacePart(player, player.getLang(), "wyvernmanager-05.htm");
      } else if (this.isOwnerClan(npc, player) && getQuestItemsCount(player, 1460) >= 25L) {
         takeItems(player, 1460, 25L);
         player.dismount();
         player.mount(12621, 0, true);
         return "wyvernmanager-04.htm";
      } else {
         return this.replacePart(player, player.getLang(), "wyvernmanager-06.htm");
      }
   }

   private boolean isOwnerClan(Npc npc, Player player) {
      switch((WyvernManager.ManagerType)MANAGERS.get(npc.getId())) {
         case CASTLE:
            if (player.getClan() != null && npc.getCastle() != null) {
               return player.isClanLeader() && player.getId() == npc.getCastle().getOwnerId();
            } else {
               return false;
            }
         case CLAN_HALL:
            if (player.getClan() != null && npc.getConquerableHall() != null) {
               return player.isClanLeader() && player.getId() == npc.getConquerableHall().getOwnerId();
            }

            return false;
         case FORT:
            Fort fort = npc.getFort();
            if (player.getClan() != null && fort != null && fort.getOwnerClan() != null) {
               return player.isClanLeader() && player.getId() == npc.getFort().getOwnerClan().getId();
            }

            return false;
         default:
            return false;
      }
   }

   private boolean isInSiege(Npc npc) {
      switch((WyvernManager.ManagerType)MANAGERS.get(npc.getId())) {
         case CASTLE:
            return npc.getCastle().getZone().isActive();
         case CLAN_HALL:
            SiegableHall hall = npc.getConquerableHall();
            return hall != null ? hall.isInSiege() : npc.getCastle().getSiege().getIsInProgress();
         case FORT:
            return npc.getFort().getZone().isActive();
         default:
            return false;
      }
   }

   private String getResidenceName(Player player, Npc npc) {
      switch((WyvernManager.ManagerType)MANAGERS.get(npc.getId())) {
         case CASTLE:
            return npc.getCastle().getName();
         case CLAN_HALL:
            return Util.clanHallName(player, npc.getConquerableHall().getId());
         case FORT:
            return npc.getFort().getName();
         default:
            return null;
      }
   }

   private String replaceAll(Player player, Npc npc, String lang) {
      return this.replacePart(player, lang, "wyvernmanager-01.htm").replace("%residence_name%", this.getResidenceName(player, npc));
   }

   private String replacePart(Player player, String lang, String htmlFile) {
      return this.getHtm(player, lang, htmlFile).replace("%wyvern_fee%", String.valueOf(25)).replace("%strider_level%", String.valueOf(55));
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "Return":
            if (!this.isOwnerClan(npc, player)) {
               htmltext = "wyvernmanager-02.htm";
            } else if (Config.ALLOW_WYVERN_ALWAYS) {
               htmltext = this.replaceAll(player, npc, player.getLang());
            } else if (MANAGERS.get(npc.getId()) == WyvernManager.ManagerType.CASTLE
               && SevenSigns.getInstance().isSealValidationPeriod()
               && SevenSigns.getInstance().getSealOwner(3) == 1) {
               htmltext = "wyvernmanager-dusk.htm";
            } else {
               htmltext = this.replaceAll(player, npc, player.getLang());
            }
            break;
         case "Help":
            htmltext = MANAGERS.get(npc.getId()) == WyvernManager.ManagerType.CASTLE
               ? this.replacePart(player, player.getLang(), "wyvernmanager-03.htm")
               : this.replacePart(player, player.getLang(), "wyvernmanager-03b.htm");
            break;
         case "RideWyvern":
            if (!Config.ALLOW_WYVERN_ALWAYS) {
               if (!Config.ALLOW_WYVERN_DURING_SIEGE && (this.isInSiege(npc) || player.isInSiege())) {
                  player.sendMessage("You cannot summon wyvern while in siege.");
                  return null;
               }

               if (MANAGERS.get(npc.getId()) == WyvernManager.ManagerType.CASTLE
                  && SevenSigns.getInstance().isSealValidationPeriod()
                  && SevenSigns.getInstance().getSealOwner(3) == 1) {
                  htmltext = "wyvernmanager-dusk.htm";
               } else {
                  htmltext = this.mountWyvern(npc, player);
               }
            } else {
               htmltext = this.mountWyvern(npc, player);
            }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = null;
      if (!this.isOwnerClan(npc, player)) {
         htmltext = "wyvernmanager-02.htm";
      } else if (Config.ALLOW_WYVERN_ALWAYS) {
         htmltext = this.replaceAll(player, npc, player.getLang());
      } else if (MANAGERS.get(npc.getId()) == WyvernManager.ManagerType.CASTLE
         && SevenSigns.getInstance().isSealValidationPeriod()
         && SevenSigns.getInstance().getSealOwner(3) == 1) {
         htmltext = "wyvernmanager-dusk.htm";
      } else {
         htmltext = this.replaceAll(player, npc, player.getLang());
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new WyvernManager(WyvernManager.class.getSimpleName(), "custom");
   }

   static {
      MANAGERS.put(35101, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35143, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35185, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35227, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35275, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35317, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35364, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35510, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35556, WyvernManager.ManagerType.CASTLE);
      MANAGERS.put(35419, WyvernManager.ManagerType.CLAN_HALL);
      MANAGERS.put(35638, WyvernManager.ManagerType.CLAN_HALL);
      MANAGERS.put(36457, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36458, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36459, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36460, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36461, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36462, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36463, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36464, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36465, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36466, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36467, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36468, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36469, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36470, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36471, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36472, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36473, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36474, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36475, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36476, WyvernManager.ManagerType.FORT);
      MANAGERS.put(36477, WyvernManager.ManagerType.FORT);
   }

   private static enum ManagerType {
      CASTLE,
      CLAN_HALL,
      FORT;
   }
}
