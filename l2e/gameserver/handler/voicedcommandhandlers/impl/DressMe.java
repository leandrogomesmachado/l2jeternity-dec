package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.DressArmorParser;
import l2e.gameserver.data.parser.DressCloakParser;
import l2e.gameserver.data.parser.DressHatParser;
import l2e.gameserver.data.parser.DressShieldParser;
import l2e.gameserver.data.parser.DressWeaponParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.DressArmorTemplate;
import l2e.gameserver.model.actor.templates.DressCloakTemplate;
import l2e.gameserver.model.actor.templates.DressHatTemplate;
import l2e.gameserver.model.actor.templates.DressShieldTemplate;
import l2e.gameserver.model.actor.templates.DressWeaponTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ItemType;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ShopPreviewInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class DressMe implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{
      "dressme",
      "dress",
      "dressme-armor",
      "dressme-cloak",
      "dressme-shield",
      "dressme-hat",
      "dressme-weapon",
      "dress-armor",
      "dress-cloak",
      "dress-shield",
      "dress-hat",
      "dress-weapon",
      "dress-armorpage",
      "dress-cloakpage",
      "dress-shieldpage",
      "dress-hatpage",
      "dress-weaponpage",
      "dress-tryarmor",
      "dress-trycloak",
      "dress-tryshield",
      "dress-tryhat",
      "dress-tryweapon",
      "dressinfo",
      "undressme",
      "undressme-armor",
      "undressme-cloak",
      "undressme-shield",
      "undressme-hat",
      "undressme-weapon",
      "showdress",
      "hidedress"
   };
   private static Map<Integer, DressWeaponTemplate> SWORD = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> BLUNT = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> DAGGER = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> BOW = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> POLE = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> FIST = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> DUAL = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> DUALFIST = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> BIGSWORD = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> ROD = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> BIGBLUNT = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> CROSSBOW = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> RAPIER = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> ANCIENTSWORD = new HashMap<>();
   private static Map<Integer, DressWeaponTemplate> DUALDAGGER = new HashMap<>();

   @Override
   public boolean useVoicedCommand(String command, Player player, String args) {
      if (!Config.ALLOW_VISUAL_ARMOR_COMMAND) {
         return false;
      } else {
         if (command.equals("dressme")) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/index.htm");
            html = html.replace(
               "<?show_hide?>", player.getVar("showVisualChange") == null ? "Show visual equip on other player!" : "Hide visual equip on other player!"
            );
            html = html.replace("<?show_hide_b?>", player.getVar("showVisualChange") == null ? "showdress" : "hidedress");
            Util.setHtml(html, player);
         } else if (command.equals("dressme-armor")) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/index-armor.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/template-armor.htm");
            String block = "";
            String list = "";
            if (args == null) {
               args = "1";
            }

            String[] param = args.split(" ");
            int page = param[0].length() > 0 ? Integer.parseInt(param[0]) : 1;
            int perpage = 6;
            int counter = 0;
            boolean isThereNextPage = DressArmorParser.getInstance().size() > 6;

            for(int i = (page - 1) * 6; i < DressArmorParser.getInstance().size(); ++i) {
               DressArmorTemplate dress = DressArmorParser.getInstance().getArmor(i + 1);
               if (dress != null) {
                  String dress_name = dress.getName();
                  if (dress_name.length() > 25) {
                     dress_name = dress_name.substring(0, 25) + ".";
                  }

                  block = template.replace("{bypass}", "bypass .dress-armorpage " + (i + 1));
                  block = block.replace("{name}", dress_name);
                  block = block.replace("{price}", Util.formatPay(player, dress.getPriceCount(), dress.getPriceId()));
                  block = block.replace("{icon}", Util.getItemIcon(dress.getChest()));
                  list = list + block;
               }

               if (++counter >= 6) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)DressArmorParser.getInstance().size() / 6.0);
            html = html.replace("{list}", list);
            html = html.replace(
               "{navigation}", Util.getNavigationBlock(count, page, DressArmorParser.getInstance().size(), 6, isThereNextPage, ".dressme-armor %s")
            );
            Util.setHtml(html, player);
         } else if (command.equals("dressme-cloak")) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/index-cloak.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/template-cloak.htm");
            String block = "";
            String list = "";
            if (args == null) {
               args = "1";
            }

            String[] param = args.split(" ");
            int page = param[0].length() > 0 ? Integer.parseInt(param[0]) : 1;
            int perpage = 6;
            int counter = 0;
            boolean isThereNextPage = DressCloakParser.getInstance().size() > 6;

            for(int i = (page - 1) * 6; i < DressCloakParser.getInstance().size(); ++i) {
               DressCloakTemplate cloak = DressCloakParser.getInstance().getCloak(i + 1);
               if (cloak != null) {
                  String cloak_name = cloak.getName();
                  if (cloak_name.length() > 25) {
                     cloak_name = cloak_name.substring(0, 25) + ".";
                  }

                  block = template.replace("{bypass}", "bypass -h .dress-cloakpage " + (i + 1));
                  block = block.replace("{name}", cloak_name);
                  block = block.replace("{price}", Util.formatPay(player, cloak.getPriceCount(), cloak.getPriceId()));
                  block = block.replace("{icon}", Util.getItemIcon(cloak.getCloakId()));
                  list = list + block;
               } else {
                  _log.info("No Cloak!!!");
               }

               if (++counter >= 6) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)DressCloakParser.getInstance().size() / 6.0);
            html = html.replace("{list}", list);
            html = html.replace(
               "{navigation}", Util.getNavigationBlock(count, page, DressCloakParser.getInstance().size(), 6, isThereNextPage, ".dressme-cloak %s")
            );
            Util.setHtml(html, player);
         } else if (command.equals("dressme-shield")) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/index-shield.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/template-shield.htm");
            String block = "";
            String list = "";
            if (args == null) {
               args = "1";
            }

            String[] param = args.split(" ");
            int page = param[0].length() > 0 ? Integer.parseInt(param[0]) : 1;
            int perpage = 6;
            int counter = 0;
            boolean isThereNextPage = DressShieldParser.getInstance().size() > 6;

            for(int i = (page - 1) * 6; i < DressShieldParser.getInstance().size(); ++i) {
               DressShieldTemplate shield = DressShieldParser.getInstance().getShield(i + 1);
               if (shield != null) {
                  String shield_name = shield.getName();
                  if (shield_name.length() > 25) {
                     shield_name = shield_name.substring(0, 25) + ".";
                  }

                  block = template.replace("{bypass}", "bypass -h .dress-shieldpage " + (i + 1));
                  block = block.replace("{name}", shield_name);
                  block = block.replace("{price}", Util.formatPay(player, shield.getPriceCount(), shield.getPriceId()));
                  block = block.replace("{icon}", Util.getItemIcon(shield.getShieldId()));
                  list = list + block;
               }

               if (++counter >= 6) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)DressShieldParser.getInstance().size() / 6.0);
            html = html.replace("{list}", list);
            html = html.replace(
               "{navigation}", Util.getNavigationBlock(count, page, DressShieldParser.getInstance().size(), 6, isThereNextPage, ".dressme-shield %s")
            );
            Util.setHtml(html, player);
         } else if (command.equals("dressme-hat")) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/index-hat.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/template-hat.htm");
            String block = "";
            String list = "";
            if (args == null) {
               args = "1";
            }

            String[] param = args.split(" ");
            int page = param[0].length() > 0 ? Integer.parseInt(param[0]) : 1;
            int perpage = 6;
            int counter = 0;
            boolean isThereNextPage = DressHatParser.getInstance().size() > 6;

            for(int i = (page - 1) * 6; i < DressHatParser.getInstance().size(); ++i) {
               DressHatTemplate hat = DressHatParser.getInstance().getHat(i + 1);
               if (hat != null) {
                  String hat_name = hat.getName();
                  if (hat_name.length() > 25) {
                     hat_name = hat_name.substring(0, 25) + ".";
                  }

                  block = template.replace("{bypass}", "bypass -h .dress-hatpage " + (i + 1));
                  block = block.replace("{name}", hat_name);
                  block = block.replace("{price}", Util.formatPay(player, hat.getPriceCount(), hat.getPriceId()));
                  block = block.replace("{icon}", Util.getItemIcon(hat.getHatId()));
                  list = list + block;
               }

               if (++counter >= 6) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)DressHatParser.getInstance().size() / 6.0);
            html = html.replace("{list}", list);
            html = html.replace(
               "{navigation}", Util.getNavigationBlock(count, page, DressHatParser.getInstance().size(), 6, isThereNextPage, ".dressme-hat %s")
            );
            Util.setHtml(html, player);
         } else if (command.startsWith("dressme-weapon")) {
            ItemInstance slot = player.getInventory().getPaperdollItem(5);
            if (slot == null) {
               player.sendMessage(new ServerMessage("DressMe.NO_WEAPON", player.getLang()).toString());
               return false;
            }

            ItemType type = slot.getItemType();
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/index-weapon.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/template-weapon.htm");
            String block = "";
            String list = "";
            if (args == null) {
               args = "1";
            }

            String[] param = args.split(" ");
            int page = param[0].length() > 0 ? Integer.parseInt(param[0]) : 1;
            int perpage = 6;
            int counter = 0;
            Map<Integer, DressWeaponTemplate> map = new HashMap<>();
            map = this.initMap(type.toString(), map);
            if (map == null) {
               _log.warning("Dress me system: Weapon Map is null.");
               return false;
            }

            boolean isThereNextPage = map.size() > 6;

            for(int i = (page - 1) * 6; i < map.size(); ++i) {
               DressWeaponTemplate weapon = map.get(i + 1);
               if (weapon != null) {
                  String weapon_name = weapon.getName();
                  if (weapon_name.length() > 25) {
                     weapon_name = weapon_name.substring(0, 25) + ".";
                  }

                  block = template.replace("{bypass}", "bypass -h .dress-weaponpage " + weapon.getId());
                  block = block.replace("{name}", weapon_name);
                  block = block.replace("{price}", Util.formatPay(player, weapon.getPriceCount(), weapon.getPriceId()));
                  block = block.replace("{icon}", Util.getItemIcon(weapon.getId()));
                  list = list + block;
               }

               if (++counter >= 6) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)map.size() / 6.0);
            html = html.replace("{list}", list);
            html = html.replace("{navigation}", Util.getNavigationBlock(count, page, map.size(), 6, isThereNextPage, ".dressme-weapon %s"));
            Util.setHtml(html, player);
         } else if (command.equals("dress-armorpage")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressArmorTemplate dress = DressArmorParser.getInstance().getArmor(set);
            if (dress != null) {
               String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/dress-armor.htm");
               ItemInstance my_chest = player.getInventory().getPaperdollItem(6);
               html = html.replace("{my_chest_icon}", my_chest == null ? "icon.NOIMAGE" : my_chest.getItem().getIcon());
               ItemInstance my_legs = player.getInventory().getPaperdollItem(11);
               html = html.replace("{my_legs_icon}", my_legs == null ? "icon.NOIMAGE" : my_legs.getItem().getIcon());
               ItemInstance my_gloves = player.getInventory().getPaperdollItem(10);
               html = html.replace("{my_gloves_icon}", my_gloves == null ? "icon.NOIMAGE" : my_gloves.getItem().getIcon());
               ItemInstance my_feet = player.getInventory().getPaperdollItem(12);
               html = html.replace("{my_feet_icon}", my_feet == null ? "icon.NOIMAGE" : my_feet.getItem().getIcon());
               html = html.replace("{bypassBuy}", "bypass -h .dress-armor " + set);
               html = html.replace("{bypassTry}", "bypass -h .dress-tryarmor " + set);
               html = html.replace("{name}", dress.getName());
               html = html.replace("{price}", Util.formatPay(player, dress.getPriceCount(), dress.getPriceId()));
               Item chest = ItemsParser.getInstance().getTemplate(dress.getChest());
               String chest_name = player.getItemName(chest);
               if (chest_name.length() > 25) {
                  chest_name = chest_name.substring(0, 25) + ".";
               }

               html = html.replace("{chest_icon}", chest.getIcon());
               html = html.replace("{chest_name}", chest_name);
               html = html.replace("{chest_grade}", chest.getItemsGrade(chest.getCrystalType()));
               if (dress.getLegs() != -1) {
                  Item legs = ItemsParser.getInstance().getTemplate(dress.getLegs());
                  String legs_name = player.getItemName(legs);
                  if (legs_name.length() > 25) {
                     legs_name = legs_name.substring(0, 25) + ".";
                  }

                  html = html.replace("{legs_icon}", legs.getIcon());
                  html = html.replace("{legs_name}", legs_name);
                  html = html.replace("{legs_grade}", legs.getItemsGrade(legs.getCrystalType()));
               } else {
                  html = html.replace("{legs_icon}", "icon.NOIMAGE");
                  html = html.replace("{legs_name}", "<font color=FF0000>...</font>");
                  html = html.replace("{legs_grade}", "NO");
               }

               if (dress.getGloves() != -1) {
                  Item gloves = ItemsParser.getInstance().getTemplate(dress.getGloves());
                  String gloves_name = player.getItemName(gloves);
                  if (gloves_name.length() > 25) {
                     gloves_name = gloves_name.substring(0, 25) + ".";
                  }

                  html = html.replace("{gloves_icon}", gloves.getIcon());
                  html = html.replace("{gloves_name}", gloves_name);
                  html = html.replace("{gloves_grade}", gloves.getItemsGrade(gloves.getCrystalType()));
               } else {
                  html = html.replace("{gloves_icon}", "icon.NOIMAGE");
                  html = html.replace("{gloves_name}", "<font color=FF0000>...</font>");
                  html = html.replace("{gloves_grade}", "NO");
               }

               if (dress.getFeet() != -1) {
                  Item feet = ItemsParser.getInstance().getTemplate(dress.getFeet());
                  String feet_name = player.getItemName(feet);
                  if (feet_name.length() > 25) {
                     feet_name = feet_name.substring(0, 25) + ".";
                  }

                  html = html.replace("{feet_icon}", feet.getIcon());
                  html = html.replace("{feet_name}", feet_name);
                  html = html.replace("{feet_grade}", feet.getItemsGrade(feet.getCrystalType()));
               } else {
                  html = html.replace("{feet_icon}", "icon.NOIMAGE");
                  html = html.replace("{feet_name}", "<font color=FF0000>...</font>");
                  html = html.replace("{feet_grade}", "NO");
               }

               Util.setHtml(html, player);
            }
         } else if (command.equals("dress-cloakpage")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressCloakTemplate cloak = DressCloakParser.getInstance().getCloak(set);
            if (cloak != null) {
               String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/dress-cloak.htm");
               ItemInstance my_cloak = player.getInventory().getPaperdollItem(23);
               html = html.replace("{my_cloak_icon}", my_cloak == null ? "icon.NOIMAGE" : my_cloak.getItem().getIcon());
               String cloak_name = cloak.getName();
               if (cloak_name.length() > 25) {
                  cloak_name = cloak_name.substring(0, 25) + ".";
               }

               html = html.replace("{bypassBuy}", "bypass -h .dress-cloak " + cloak.getId());
               html = html.replace("{bypassTry}", "bypass -h .dress-trycloak " + cloak.getId());
               html = html.replace("{name}", cloak_name);
               html = html.replace("{price}", Util.formatPay(player, cloak.getPriceCount(), cloak.getPriceId()));
               Item item = ItemsParser.getInstance().getTemplate(cloak.getCloakId());
               String cloak_name1 = player.getItemName(item);
               if (cloak_name1.length() > 25) {
                  cloak_name1 = cloak_name1.substring(0, 25) + ".";
               }

               html = html.replace("{item_icon}", item.getIcon());
               html = html.replace("{item_name}", cloak_name1);
               html = html.replace("{item_grade}", item.getItemsGrade(item.getCrystalType()));
               Util.setHtml(html, player);
            }
         } else if (command.equals("dress-shieldpage")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressShieldTemplate shield = DressShieldParser.getInstance().getShield(set);
            if (shield != null) {
               String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/dress-shield.htm");
               ItemInstance my_shield = player.getInventory().getPaperdollItem(7);
               html = html.replace("{my_shield_icon}", my_shield == null ? "icon.NOIMAGE" : my_shield.getItem().getIcon());
               String shield_myName = shield.getName();
               if (shield_myName.length() > 25) {
                  shield_myName = shield_myName.substring(0, 25) + ".";
               }

               html = html.replace("{bypassBuy}", "bypass -h .dress-shield " + shield.getId());
               html = html.replace("{bypassTry}", "bypass -h .dress-tryshield " + shield.getId());
               html = html.replace("{name}", shield_myName);
               html = html.replace("{price}", Util.formatPay(player, shield.getPriceCount(), shield.getPriceId()));
               Item item = ItemsParser.getInstance().getTemplate(shield.getShieldId());
               String shield_name = player.getItemName(item);
               if (shield_name.length() > 25) {
                  shield_name = shield_name.substring(0, 25) + ".";
               }

               html = html.replace("{item_icon}", item.getIcon());
               html = html.replace("{item_name}", shield_name);
               html = html.replace("{item_grade}", item.getItemsGrade(item.getCrystalType()));
               Util.setHtml(html, player);
            }
         } else if (command.equals("dress-hatpage")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressHatTemplate hat = DressHatParser.getInstance().getHat(set);
            if (hat != null) {
               String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/dress-hat.htm");
               ItemInstance my_hat = hat.getSlot() == 2 ? player.getInventory().getPaperdollItem(2) : player.getInventory().getPaperdollItem(3);
               html = html.replace("{my_hat_icon}", my_hat == null ? "icon.NOIMAGE" : my_hat.getItem().getIcon());
               String my_hatName = hat.getName();
               if (my_hatName.length() > 25) {
                  my_hatName = my_hatName.substring(0, 25) + ".";
               }

               html = html.replace("{bypassBuy}", "bypass -h .dress-hat " + hat.getId());
               html = html.replace("{name}", my_hatName);
               html = html.replace("{price}", Util.formatPay(player, hat.getPriceCount(), hat.getPriceId()));
               Item item = ItemsParser.getInstance().getTemplate(hat.getHatId());
               String hat_name = player.getItemName(item);
               if (hat_name.length() > 25) {
                  hat_name = hat_name.substring(0, 25) + ".";
               }

               html = html.replace("{item_icon}", item.getIcon());
               html = html.replace("{item_name}", hat_name);
               html = html.replace("{item_grade}", item.getItemsGrade(item.getCrystalType()));
               Util.setHtml(html, player);
            }
         } else if (command.equals("dress-weaponpage")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressWeaponTemplate weapon = DressWeaponParser.getInstance().getWeapon(set);
            if (weapon != null) {
               String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/dress-weapon.htm");
               ItemInstance my_weapon = player.getInventory().getPaperdollItem(5);
               html = html.replace("{my_weapon_icon}", my_weapon == null ? "icon.NOIMAGE" : my_weapon.getItem().getIcon());
               String my_weaponName = weapon.getName();
               if (my_weaponName.length() > 25) {
                  my_weaponName = my_weaponName.substring(0, 25) + ".";
               }

               html = html.replace("{bypassBuy}", "bypass -h .dress-weapon " + weapon.getId());
               html = html.replace("{bypassTry}", "bypass -h .dress-tryweapon " + weapon.getId());
               html = html.replace("{name}", my_weaponName);
               html = html.replace("{price}", Util.formatPay(player, weapon.getPriceCount(), weapon.getPriceId()));
               Item item = ItemsParser.getInstance().getTemplate(weapon.getId());
               String weapon_name = player.getItemName(item);
               if (weapon_name.length() > 25) {
                  weapon_name = weapon_name.substring(0, 25) + ".";
               }

               html = html.replace("{item_icon}", item.getIcon());
               html = html.replace("{item_name}", weapon_name);
               html = html.replace("{item_grade}", item.getItemsGrade(item.getCrystalType()));
               Util.setHtml(html, player);
            }
         } else if (command.equals("dressinfo")) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/info.htm");
            Util.setHtml(html, player);
         } else if (command.equals("dress-armor")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressArmorTemplate dress = DressArmorParser.getInstance().getArmor(set);
            ItemInstance chest = player.getInventory().getPaperdollItem(6);
            if (chest == null) {
               player.sendMessage(new ServerMessage("DressMe.NO_CHEST", player.getLang()).toString());
               this.useVoicedCommand("dress-armorpage", player, args);
               return false;
            }

            ItemInstance legs = player.getInventory().getPaperdollItem(11);
            if (legs == null && chest.getItem().getBodyPart() != 32768) {
               player.sendMessage(new ServerMessage("DressMe.NO_LEGS", player.getLang()).toString());
               this.useVoicedCommand("dress-armorpage", player, args);
               return false;
            }

            ItemInstance gloves = player.getInventory().getPaperdollItem(10);
            if (gloves == null) {
               player.sendMessage(new ServerMessage("DressMe.NO_GLOVES", player.getLang()).toString());
               this.useVoicedCommand("dress-armorpage", player, args);
               return false;
            }

            ItemInstance feet = player.getInventory().getPaperdollItem(12);
            if (feet == null) {
               player.sendMessage(new ServerMessage("DressMe.NO_FEET", player.getLang()).toString());
               this.useVoicedCommand("dress-armorpage", player, args);
               return false;
            }

            if (dress.getShieldId() > 0 && DressShieldParser.getInstance().getShieldId(dress.getShieldId()) != -1) {
               ItemInstance shield = player.getInventory().getPaperdollItem(7);
               if (shield == null || shield != null && shield.getItem().isArrow()) {
                  player.sendMessage(new ServerMessage("DressMe.NO_SHIELD", player.getLang()).toString());
                  this.useVoicedCommand("dress-armorpage", player, args);
                  return false;
               }
            }

            if (dress.getCloakId() > 0 && DressCloakParser.getInstance().getCloakId(dress.getCloakId()) != -1) {
               ItemInstance cloak = player.getInventory().getPaperdollItem(23);
               if (cloak == null) {
                  player.sendMessage(new ServerMessage("DressMe.NO_CLOAK", player.getLang()).toString());
                  this.useVoicedCommand("dress-armorpage", player, args);
                  return false;
               }
            }

            if (dress.getHatId() > 0 && DressHatParser.getInstance().getHatId(dress.getHatId()) != -1) {
               ItemInstance hat = dress.getSlot() == 2 ? player.getInventory().getPaperdollItem(2) : player.getInventory().getPaperdollItem(3);
               if (hat == null) {
                  player.sendMessage(new ServerMessage("DressMe.NO_HAT", player.getLang()).toString());
                  this.useVoicedCommand("dress-armorpage", player, args);
                  return false;
               }
            }

            if (!dress.isForKamael() && player.getRace() == Race.Kamael) {
               player.sendMessage(new ServerMessage("DressMe.NOT_SUIT", player.getLang()).toString());
               this.useVoicedCommand("dress-armorpage", player, args);
               return false;
            }

            if (player.getInventory().getItemByItemId(dress.getPriceId()) == null) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return false;
            }

            if (player.getInventory().getItemByItemId(dress.getPriceId()).getCount() < dress.getPriceCount()) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return false;
            }

            player.destroyItemByItemId("Dress", dress.getPriceId(), dress.getPriceCount(), player, true);
            this.visuality(player, chest, dress.getChest());
            player.getInventory().unEquipItem(chest);
            player.getInventory().equipItem(chest);
            if (legs != null) {
               this.visuality(player, legs, dress.getLegs());
               player.getInventory().unEquipItem(legs);
               player.getInventory().equipItem(legs);
            }

            this.visuality(player, gloves, dress.getGloves());
            player.getInventory().unEquipItem(gloves);
            player.getInventory().equipItem(gloves);
            this.visuality(player, feet, dress.getFeet());
            player.getInventory().unEquipItem(feet);
            player.getInventory().equipItem(feet);
            if (dress.getShieldId() > 0 && DressShieldParser.getInstance().getShieldId(dress.getShieldId()) != -1) {
               ItemInstance shield = player.getInventory().getPaperdollItem(23);
               this.visuality(player, shield, DressShieldParser.getInstance().getShieldId(dress.getShieldId()));
               player.getInventory().unEquipItem(shield);
               player.getInventory().equipItem(shield);
            }

            if (dress.getCloakId() > 0 && DressCloakParser.getInstance().getCloakId(dress.getCloakId()) != -1) {
               ItemInstance cloak = player.getInventory().getPaperdollItem(23);
               this.visuality(player, cloak, DressCloakParser.getInstance().getCloakId(dress.getCloakId()));
               player.getInventory().unEquipItem(cloak);
               player.getInventory().equipItem(cloak);
            }

            if (dress.getHatId() > 0 && DressHatParser.getInstance().getHatId(dress.getHatId()) != -1) {
               ItemInstance hat = dress.getSlot() == 2 ? player.getInventory().getPaperdollItem(2) : player.getInventory().getPaperdollItem(3);
               this.visuality(
                  player,
                  dress.getSlot() == 2 ? player.getInventory().getPaperdollItem(2) : player.getInventory().getPaperdollItem(3),
                  DressHatParser.getInstance().getHatId(dress.getHatId())
               );
               player.getInventory().unEquipItem(hat);
               player.getInventory().equipItem(hat);
            }

            player.broadcastUserInfo(true);
         } else if (command.equals("dress-tryarmor")) {
            int set = Integer.parseInt(args.split(" ")[0]);
            DressArmorTemplate dress = DressArmorParser.getInstance().getArmor(set);
            if (dress == null) {
               return false;
            }

            if (!dress.isForKamael() && player.getRace() == Race.Kamael) {
               player.sendMessage(new ServerMessage("DressMe.NOT_SUIT", player.getLang()).toString());
               this.useVoicedCommand("dress-armorpage", player, args);
               return false;
            }

            if (player.canUsePreviewTask()) {
               Map<Integer, Integer> itemList = new HashMap<>();
               itemList.put(6, dress.getChest());
               itemList.put(11, dress.getLegs() > 0 ? dress.getLegs() : dress.getChest());
               itemList.put(10, dress.getGloves());
               itemList.put(12, dress.getFeet());
               if (dress.getShieldId() > 0) {
                  itemList.put(7, dress.getShieldId());
               }

               if (dress.getCloakId() > 0) {
                  itemList.put(23, dress.getCloakId());
               }

               player.sendPacket(new ShopPreviewInfo(itemList));
               player.setRemovePreviewTask();
            }

            this.useVoicedCommand("dress-armorpage", player, args);
         } else {
            if (command.equals("dress-cloak")) {
               int set = Integer.parseInt(args.split(" ")[0]);
               DressCloakTemplate cloak_data = DressCloakParser.getInstance().getCloak(set);
               ItemInstance cloak = player.getInventory().getPaperdollItem(23);
               if (cloak == null) {
                  player.sendMessage(new ServerMessage("DressMe.NO_CLOAK", player.getLang()).toString());
                  this.useVoicedCommand("dress-cloakpage", player, args);
                  return false;
               }

               if (player.getInventory().getItemByItemId(cloak_data.getPriceId()) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               if (player.getInventory().getItemByItemId(cloak_data.getPriceId()).getCount() < cloak_data.getPriceCount()) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               player.destroyItemByItemId("Dress", cloak_data.getPriceId(), cloak_data.getPriceCount(), player, true);
               this.visuality(player, cloak, cloak_data.getCloakId());
               player.getInventory().unEquipItem(cloak);
               player.getInventory().equipItem(cloak);
               player.broadcastUserInfo(true);
               return true;
            }

            if (command.equals("dress-trycloak")) {
               int set = Integer.parseInt(args.split(" ")[0]);
               DressCloakTemplate cloak_data = DressCloakParser.getInstance().getCloak(set);
               if (cloak_data == null) {
                  return false;
               }

               if (player.canUsePreviewTask()) {
                  Map<Integer, Integer> itemList = new HashMap<>();
                  itemList.put(23, cloak_data.getCloakId());
                  player.sendPacket(new ShopPreviewInfo(itemList));
                  player.setRemovePreviewTask();
               }

               this.useVoicedCommand("dress-cloakpage", player, args);
               return false;
            }

            if (command.equals("dress-shield")) {
               int shield_id = Integer.parseInt(args.split(" ")[0]);
               DressShieldTemplate shield_data = DressShieldParser.getInstance().getShield(shield_id);
               ItemInstance shield = player.getInventory().getPaperdollItem(7);
               if (shield == null || shield != null && shield.getItem().isArrow()) {
                  player.sendMessage(new ServerMessage("DressMe.NO_SHIELD", player.getLang()).toString());
                  this.useVoicedCommand("dress-shieldpage", player, args);
                  return false;
               }

               if (player.getInventory().getItemByItemId(shield_data.getPriceId()) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               if (player.getInventory().getItemByItemId(shield_data.getPriceId()).getCount() < shield_data.getPriceCount()) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               player.destroyItemByItemId("Dress", shield_data.getPriceId(), shield_data.getPriceCount(), player, true);
               this.visuality(player, shield, shield_data.getShieldId());
               player.getInventory().unEquipItem(shield);
               player.getInventory().equipItem(shield);
               player.broadcastUserInfo(true);
            } else if (command.equals("dress-tryshield")) {
               int shield_id = Integer.parseInt(args.split(" ")[0]);
               DressShieldTemplate shield_data = DressShieldParser.getInstance().getShield(shield_id);
               if (shield_data == null) {
                  return false;
               }

               if (player.canUsePreviewTask()) {
                  Map<Integer, Integer> itemList = new HashMap<>();
                  itemList.put(7, shield_data.getShieldId());
                  player.sendPacket(new ShopPreviewInfo(itemList));
                  player.setRemovePreviewTask();
               }

               this.useVoicedCommand("dress-shieldpage", player, args);
            } else if (command.equals("dress-hat")) {
               int hat_id = Integer.parseInt(args.split(" ")[0]);
               DressHatTemplate hat_data = DressHatParser.getInstance().getHat(hat_id);
               ItemInstance hat = hat_data.getSlot() == 2 ? player.getInventory().getPaperdollItem(2) : player.getInventory().getPaperdollItem(3);
               if (hat == null) {
                  player.sendMessage(new ServerMessage("DressMe.NO_HAT", player.getLang()).toString());
                  this.useVoicedCommand("dress-hatpage", player, args);
                  return false;
               }

               if (player.getInventory().getItemByItemId(hat_data.getPriceId()) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               if (player.getInventory().getItemByItemId(hat_data.getPriceId()).getCount() < hat_data.getPriceCount()) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               player.destroyItemByItemId("Dress", hat_data.getPriceId(), hat_data.getPriceCount(), player, true);
               this.visuality(player, hat, hat_data.getHatId());
               player.getInventory().unEquipItem(hat);
               player.getInventory().equipItem(hat);
               player.broadcastUserInfo(true);
            } else if (command.equals("dress-weapon")) {
               int set = Integer.parseInt(args.split(" ")[0]);
               DressWeaponTemplate weapon_data = DressWeaponParser.getInstance().getWeapon(set);
               ItemInstance weapon = player.getInventory().getPaperdollItem(5);
               if (weapon == null) {
                  player.sendMessage(new ServerMessage("DressMe.NO_WEAPON", player.getLang()).toString());
                  this.useVoicedCommand("dress-weaponpage", player, args);
                  return false;
               }

               if (!weapon.getItemType().toString().equals(weapon_data.getType())) {
                  player.sendMessage(new ServerMessage("DressMe.WRONG_TYPE", player.getLang()).toString());
                  this.useVoicedCommand("dressme-weapon", player, null);
                  return false;
               }

               if (player.getInventory().getItemByItemId(weapon_data.getPriceId()) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               if (player.getInventory().getItemByItemId(weapon_data.getPriceId()).getCount() < weapon_data.getPriceCount()) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return false;
               }

               player.destroyItemByItemId("Dress", weapon_data.getPriceId(), weapon_data.getPriceCount(), player, true);
               this.visuality(player, weapon, weapon_data.getId());
               player.getInventory().unEquipItem(weapon);
               player.getInventory().equipItem(weapon);
               player.broadcastUserInfo(true);
            } else {
               if (command.equals("dress-tryweapon")) {
                  int set = Integer.parseInt(args.split(" ")[0]);
                  DressWeaponTemplate weapon_data = DressWeaponParser.getInstance().getWeapon(set);
                  if (weapon_data == null) {
                     return false;
                  }

                  if (player.canUsePreviewTask()) {
                     Map<Integer, Integer> itemList = new HashMap<>();
                     itemList.put(5, weapon_data.getId());
                     player.sendPacket(new ShopPreviewInfo(itemList));
                     player.setRemovePreviewTask();
                  }

                  this.useVoicedCommand("dress-weaponpage", player, args);
                  return false;
               }

               if (command.equals("undressme")) {
                  String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/dressme/undressme.htm");
                  html = html.replace(
                     "<?show_hide?>", player.getVar("showVisualChange") == null ? "Show visual equip on other player!" : "Hide visual equip on other player!"
                  );
                  html = html.replace("<?show_hide_b?>", player.getVar("showVisualChange") == null ? "showdress" : "hidedress");
                  Util.setHtml(html, player);
               } else if (command.equals("undressme-armor")) {
                  ItemInstance chest = player.getInventory().getPaperdollItem(6);
                  ItemInstance legs = player.getInventory().getPaperdollItem(11);
                  ItemInstance gloves = player.getInventory().getPaperdollItem(10);
                  ItemInstance feet = player.getInventory().getPaperdollItem(12);
                  if (chest != null) {
                     this.visuality(player, chest, 0);
                     player.getInventory().unEquipItem(chest);
                     player.getInventory().equipItem(chest);
                  }

                  if (legs != null) {
                     this.visuality(player, legs, 0);
                     player.getInventory().unEquipItem(legs);
                     player.getInventory().equipItem(legs);
                  }

                  if (gloves != null) {
                     this.visuality(player, gloves, 0);
                     player.getInventory().unEquipItem(gloves);
                     player.getInventory().equipItem(gloves);
                  }

                  if (feet != null) {
                     this.visuality(player, feet, 0);
                     player.getInventory().unEquipItem(feet);
                     player.getInventory().equipItem(feet);
                  }

                  player.broadcastUserInfo(true);
                  this.useVoicedCommand("undressme", player, null);
               } else if (command.equals("undressme-cloak")) {
                  ItemInstance cloak = player.getInventory().getPaperdollItem(23);
                  if (cloak != null) {
                     this.visuality(player, cloak, 0);
                     player.getInventory().unEquipItem(cloak);
                     player.getInventory().equipItem(cloak);
                  }

                  player.broadcastUserInfo(true);
                  this.useVoicedCommand("undressme", player, null);
               } else if (command.equals("undressme-shield")) {
                  ItemInstance shield = player.getInventory().getPaperdollItem(7);
                  if (shield != null && !shield.getItem().isArrow()) {
                     this.visuality(player, shield, 0);
                     player.getInventory().unEquipItem(shield);
                     player.getInventory().equipItem(shield);
                  }

                  player.broadcastUserInfo(true);
                  this.useVoicedCommand("undressme", player, null);
               } else if (command.equals("undressme-hat")) {
                  ItemInstance hat = player.getInventory().getPaperdollItem(2);
                  ItemInstance hat2 = player.getInventory().getPaperdollItem(3);
                  if (hat != null) {
                     this.visuality(player, hat, 0);
                     player.getInventory().unEquipItem(hat);
                     player.getInventory().equipItem(hat);
                  }

                  if (hat2 != null) {
                     this.visuality(player, hat2, 0);
                     player.getInventory().unEquipItem(hat2);
                     player.getInventory().equipItem(hat2);
                  }

                  player.broadcastUserInfo(true);
                  this.useVoicedCommand("undressme", player, null);
               } else if (command.equals("undressme-weapon")) {
                  ItemInstance weapon = player.getInventory().getPaperdollItem(5);
                  if (weapon != null) {
                     this.visuality(player, weapon, 0);
                     player.getInventory().unEquipItem(weapon);
                     player.getInventory().equipItem(weapon);
                  }

                  player.broadcastUserInfo(true);
                  this.useVoicedCommand("undressme", player, null);
               } else {
                  if (command.equals("showdress")) {
                     if (player.getVar("showVisualChange") == null) {
                        player.setVar("showVisualChange", "-1");
                        player.broadcastCharInfo();
                     }

                     this.useVoicedCommand("dressme", player, null);
                     return true;
                  }

                  if (command.equals("hidedress")) {
                     if (player.getVar("showVisualChange") != null) {
                        player.unsetVar("showVisualChange");
                        player.broadcastCharInfo();
                     }

                     this.useVoicedCommand("dressme", player, null);
                  }
               }
            }
         }

         return true;
      }
   }

   private Map<Integer, DressWeaponTemplate> initMap(String type, Map<Integer, DressWeaponTemplate> map) {
      if (type.equals("Sword")) {
         map = SWORD;
         return SWORD;
      } else if (type.equals("Blunt")) {
         map = BLUNT;
         return BLUNT;
      } else if (type.equals("Dagger")) {
         map = DAGGER;
         return DAGGER;
      } else if (type.equals("Bow")) {
         map = BOW;
         return BOW;
      } else if (type.equals("Pole")) {
         map = POLE;
         return POLE;
      } else if (type.equals("Fist")) {
         map = FIST;
         return FIST;
      } else if (type.equals("Dual Sword")) {
         map = DUAL;
         return DUAL;
      } else if (type.equals("Dual Fist")) {
         map = DUALFIST;
         return DUALFIST;
      } else if (type.equals("Big Sword")) {
         map = BIGSWORD;
         return BIGSWORD;
      } else if (type.equals("Rod")) {
         map = ROD;
         return ROD;
      } else if (type.equals("Big Blunt")) {
         map = BIGBLUNT;
         return BIGBLUNT;
      } else if (type.equals("Crossbow")) {
         map = CROSSBOW;
         return CROSSBOW;
      } else if (type.equals("Rapier")) {
         map = RAPIER;
         return RAPIER;
      } else if (type.equals("Ancient")) {
         map = ANCIENTSWORD;
         return ANCIENTSWORD;
      } else if (type.equals("Dual Dagger")) {
         map = DUALDAGGER;
         return DUALDAGGER;
      } else {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Unknown type: " + type);
         return null;
      }
   }

   public static int parseWeapon() {
      int Sword = 1;
      int Blunt = 1;
      int Dagger = 1;
      int Bow = 1;
      int Pole = 1;
      int Fist = 1;
      int DualSword = 1;
      int DualFist = 1;
      int BigSword = 1;
      int Rod = 1;
      int BigBlunt = 1;
      int Crossbow = 1;
      int Rapier = 1;
      int AncientSword = 1;
      int DualDagger = 1;

      for(DressWeaponTemplate weapon : DressWeaponParser.getInstance().getAllWeapons()) {
         if (weapon.getType().equals("Sword")) {
            SWORD.put(Sword, weapon);
            ++Sword;
         } else if (weapon.getType().equals("Blunt")) {
            BLUNT.put(Blunt, weapon);
            ++Blunt;
         } else if (weapon.getType().equals("Dagger")) {
            DAGGER.put(Dagger, weapon);
            ++Dagger;
         } else if (weapon.getType().equals("Bow")) {
            BOW.put(Bow, weapon);
            ++Bow;
         } else if (weapon.getType().equals("Pole")) {
            POLE.put(Pole, weapon);
            ++Pole;
         } else if (weapon.getType().equals("Fist")) {
            FIST.put(Fist, weapon);
            ++Fist;
         } else if (weapon.getType().equals("Dual Sword")) {
            DUAL.put(DualSword, weapon);
            ++DualSword;
         } else if (weapon.getType().equals("Dual Fist")) {
            DUALFIST.put(DualFist, weapon);
            ++DualFist;
         } else if (weapon.getType().equals("Big Sword")) {
            BIGSWORD.put(BigSword, weapon);
            ++BigSword;
         } else if (weapon.getType().equals("Rod")) {
            ROD.put(Rod, weapon);
            ++Rod;
         } else if (weapon.getType().equals("Big Blunt")) {
            BIGBLUNT.put(BigBlunt, weapon);
            ++BigBlunt;
         } else if (weapon.getType().equals("Crossbow")) {
            CROSSBOW.put(Crossbow, weapon);
            ++Crossbow;
         } else if (weapon.getType().equals("Rapier")) {
            RAPIER.put(Rapier, weapon);
            ++Rapier;
         } else if (weapon.getType().equals("Ancient")) {
            ANCIENTSWORD.put(AncientSword, weapon);
            ++AncientSword;
         } else if (weapon.getType().equals("Dual Dagger")) {
            DUALDAGGER.put(DualDagger, weapon);
            ++DualDagger;
         } else {
            _log.log(Level.WARNING, "DressMe: Can't find type: " + weapon.getType());
         }
      }

      return 0;
   }

   private void visuality(Player player, ItemInstance item, int visual) {
      item.setVisualItemId(visual);
      item.updateDatabase();
      if (visual > 0) {
         ServerMessage msg = new ServerMessage("DressMe.CHANGE", player.getLang());
         msg.add(Util.getItemName(player, item.getId()));
         msg.add(Util.getItemName(player, visual));
         player.sendMessage(msg.toString());
      } else {
         ServerMessage msg = new ServerMessage("DressMe.REMOVE", player.getLang());
         msg.add(Util.getItemName(player, item.getId()));
         player.sendMessage(msg.toString());
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
