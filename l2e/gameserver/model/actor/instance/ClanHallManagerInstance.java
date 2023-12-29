package l2e.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AgitDecoInfo;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanHallManagerInstance extends MerchantInstance {
   protected static final int COND_OWNER_FALSE = 0;
   protected static final int COND_ALL_FALSE = 1;
   protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;
   protected static final int COND_OWNER = 3;
   private int _clanHallId = -1;

   public ClanHallManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ClanHallManagerInstance);
   }

   @Override
   public boolean isWarehouse() {
      return true;
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (!this.getClanHall().isSiegableHall() || !((SiegableHall)this.getClanHall()).isInSiege()) {
         int condition = this.validateCondition(player);
         if (condition > 1) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            if (condition == 3) {
               StringTokenizer st = new StringTokenizer(command, " ");
               String actualCommand = st.nextToken();
               String val = "";
               if (st.countTokens() >= 1) {
                  val = st.nextToken();
               }

               if (actualCommand.equalsIgnoreCase("banish_foreigner")) {
                  NpcHtmlMessage html = new NpcHtmlMessage(1);
                  if ((player.getClanPrivileges() & 16384) == 16384) {
                     if (val.equalsIgnoreCase("list")) {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/banish-list.htm");
                     } else if (val.equalsIgnoreCase("banish")) {
                        this.getClanHall().banishForeigners();
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/banish.htm");
                     }
                  } else {
                     html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
                  }

                  this.sendHtmlMessage(player, html);
                  return;
               }

               if (actualCommand.equalsIgnoreCase("manage_vault")) {
                  NpcHtmlMessage html = new NpcHtmlMessage(1);
                  if ((player.getClanPrivileges() & 8) == 8) {
                     if (this.getClanHall().getLease() <= 0) {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/vault-chs.htm");
                     } else {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/vault.htm");
                        html.replace("%rent%", String.valueOf(this.getClanHall().getLease()));
                        html.replace("%date%", format.format(Long.valueOf(this.getClanHall().getPaidUntil())));
                     }

                     this.sendHtmlMessage(player, html);
                  } else {
                     html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
                     this.sendHtmlMessage(player, html);
                  }

                  return;
               }

               if (actualCommand.equalsIgnoreCase("door")) {
                  NpcHtmlMessage html = new NpcHtmlMessage(1);
                  if ((player.getClanPrivileges() & 2048) == 2048) {
                     if (val.equalsIgnoreCase("open")) {
                        this.getClanHall().openCloseDoors(true);
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/door-open.htm");
                     } else if (val.equalsIgnoreCase("close")) {
                        this.getClanHall().openCloseDoors(false);
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/door-close.htm");
                     } else {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/door.htm");
                     }

                     this.sendHtmlMessage(player, html);
                  } else {
                     html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
                     this.sendHtmlMessage(player, html);
                  }

                  return;
               }

               if (actualCommand.equalsIgnoreCase("functions")) {
                  if (val.equalsIgnoreCase("tele")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(1);
                     if (this.getClanHall().getFunction(1) == null) {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/chamberlain-nac.htm");
                     } else {
                        int hallid = this.getClanHall().getId();
                        switch(hallid) {
                           case 21:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleResistance" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 22:
                           case 23:
                           case 24:
                           case 25:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleGludio" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 26:
                           case 27:
                           case 28:
                           case 29:
                           case 30:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleGludin" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 31:
                           case 32:
                           case 33:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleDion" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 34:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleDevastated" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 35:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleBandit" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 36:
                           case 37:
                           case 38:
                           case 39:
                           case 40:
                           case 41:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleAden" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 42:
                           case 43:
                           case 44:
                           case 45:
                           case 46:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleGiran" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 47:
                           case 48:
                           case 49:
                           case 50:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleGoddard" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 51:
                           case 52:
                           case 53:
                           case 54:
                           case 55:
                           case 56:
                           case 57:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleRune" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 58:
                           case 59:
                           case 60:
                           case 61:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleSchuttgart" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 62:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleRainbow" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 63:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleBeast" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                              break;
                           case 64:
                              html.setFile(
                                 player, player.getLang(), "data/html/clanHallManager/teleFortress" + this.getClanHall().getFunction(1).getLvl() + ".htm"
                              );
                        }
                     }

                     this.sendHtmlMessage(player, html);
                  } else if (val.equalsIgnoreCase("item_creation")) {
                     if (this.getClanHall().getFunction(2) == null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/chamberlain-nac.htm");
                        this.sendHtmlMessage(player, html);
                        return;
                     }

                     if (st.countTokens() < 1) {
                        return;
                     }

                     int valbuy = Integer.parseInt(st.nextToken()) + this.getClanHall().getFunction(2).getLvl() * 100000;
                     this.showBuyWindow(player, valbuy);
                  } else if (val.equalsIgnoreCase("support")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(1);
                     if (this.getClanHall().getFunction(6) == null) {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/chamberlain-nac.htm");
                     } else {
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/support" + this.getClanHall().getFunction(6).getLvl() + ".htm");
                        html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                     }

                     this.sendHtmlMessage(player, html);
                  } else if (val.equalsIgnoreCase("back")) {
                     this.showChatWindow(player);
                  } else {
                     NpcHtmlMessage html = new NpcHtmlMessage(1);
                     html.setFile(player, player.getLang(), "data/html/clanHallManager/functions.htm");
                     if (this.getClanHall().getFunction(5) != null) {
                        html.replace("%xp_regen%", String.valueOf(this.getClanHall().getFunction(5).getLvl()));
                     } else {
                        html.replace("%xp_regen%", "0");
                     }

                     if (this.getClanHall().getFunction(3) != null) {
                        html.replace("%hp_regen%", String.valueOf(this.getClanHall().getFunction(3).getLvl()));
                     } else {
                        html.replace("%hp_regen%", "0");
                     }

                     if (this.getClanHall().getFunction(4) != null) {
                        html.replace("%mp_regen%", String.valueOf(this.getClanHall().getFunction(4).getLvl()));
                     } else {
                        html.replace("%mp_regen%", "0");
                     }

                     this.sendHtmlMessage(player, html);
                  }

                  return;
               }

               if (actualCommand.equalsIgnoreCase("manage")) {
                  if ((player.getClanPrivileges() & 32768) == 32768) {
                     if (val.equalsIgnoreCase("recovery")) {
                        if (st.countTokens() >= 1) {
                           if (this.getClanHall().getOwnerId() == 0) {
                              player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                              return;
                           }

                           val = st.nextToken();
                           if (val.equalsIgnoreCase("hp_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "recovery hp 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("mp_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "recovery mp 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("exp_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "recovery exp 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_hp")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Fireplace (HP Recovery Device)");
                              int percent = Integer.parseInt(val);
                              int cost;
                              switch(percent) {
                                 case 20:
                                    cost = Config.CH_HPREG1_FEE;
                                    break;
                                 case 40:
                                    cost = Config.CH_HPREG2_FEE;
                                    break;
                                 case 80:
                                    cost = Config.CH_HPREG3_FEE;
                                    break;
                                 case 100:
                                    cost = Config.CH_HPREG4_FEE;
                                    break;
                                 case 120:
                                    cost = Config.CH_HPREG5_FEE;
                                    break;
                                 case 140:
                                    cost = Config.CH_HPREG6_FEE;
                                    break;
                                 case 160:
                                    cost = Config.CH_HPREG7_FEE;
                                    break;
                                 case 180:
                                    cost = Config.CH_HPREG8_FEE;
                                    break;
                                 case 200:
                                    cost = Config.CH_HPREG9_FEE;
                                    break;
                                 case 220:
                                    cost = Config.CH_HPREG10_FEE;
                                    break;
                                 case 240:
                                    cost = Config.CH_HPREG11_FEE;
                                    break;
                                 case 260:
                                    cost = Config.CH_HPREG12_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_HPREG13_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_HPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace(
                                 "%use%",
                                 "Provides additional HP recovery for clan members in the clan hall.<font color=\"00FFFF\">"
                                    + String.valueOf(percent)
                                    + "%</font>"
                              );
                              html.replace("%apply%", "recovery hp " + String.valueOf(percent));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_mp")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Carpet (MP Recovery)");
                              int percent = Integer.parseInt(val);
                              int cost;
                              switch(percent) {
                                 case 5:
                                    cost = Config.CH_MPREG1_FEE;
                                    break;
                                 case 10:
                                    cost = Config.CH_MPREG2_FEE;
                                    break;
                                 case 15:
                                    cost = Config.CH_MPREG3_FEE;
                                    break;
                                 case 30:
                                    cost = Config.CH_MPREG4_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_MPREG5_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_MPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace(
                                 "%use%",
                                 "Provides additional MP recovery for clan members in the clan hall.<font color=\"00FFFF\">"
                                    + String.valueOf(percent)
                                    + "%</font>"
                              );
                              html.replace("%apply%", "recovery mp " + String.valueOf(percent));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_exp")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Chandelier (EXP Recovery Device)");
                              int percent = Integer.parseInt(val);
                              int cost;
                              switch(percent) {
                                 case 5:
                                    cost = Config.CH_EXPREG1_FEE;
                                    break;
                                 case 10:
                                    cost = Config.CH_EXPREG2_FEE;
                                    break;
                                 case 15:
                                    cost = Config.CH_EXPREG3_FEE;
                                    break;
                                 case 25:
                                    cost = Config.CH_EXPREG4_FEE;
                                    break;
                                 case 35:
                                    cost = Config.CH_EXPREG5_FEE;
                                    break;
                                 case 40:
                                    cost = Config.CH_EXPREG6_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_EXPREG7_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_EXPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace(
                                 "%use%",
                                 "Restores the Exp of any clan member who is resurrected in the clan hall.<font color=\"00FFFF\">"
                                    + String.valueOf(percent)
                                    + "%</font>"
                              );
                              html.replace("%apply%", "recovery exp " + String.valueOf(percent));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("hp")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Mp editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(3) != null && this.getClanHall().getFunction(3).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", val + "%");
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int percent = Integer.parseInt(val);
                                 int fee;
                                 switch(percent) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 20:
                                       fee = Config.CH_HPREG1_FEE;
                                       break;
                                    case 40:
                                       fee = Config.CH_HPREG2_FEE;
                                       break;
                                    case 80:
                                       fee = Config.CH_HPREG3_FEE;
                                       break;
                                    case 100:
                                       fee = Config.CH_HPREG4_FEE;
                                       break;
                                    case 120:
                                       fee = Config.CH_HPREG5_FEE;
                                       break;
                                    case 140:
                                       fee = Config.CH_HPREG6_FEE;
                                       break;
                                    case 160:
                                       fee = Config.CH_HPREG7_FEE;
                                       break;
                                    case 180:
                                       fee = Config.CH_HPREG8_FEE;
                                       break;
                                    case 200:
                                       fee = Config.CH_HPREG9_FEE;
                                       break;
                                    case 220:
                                       fee = Config.CH_HPREG10_FEE;
                                       break;
                                    case 240:
                                       fee = Config.CH_HPREG11_FEE;
                                       break;
                                    case 260:
                                       fee = Config.CH_HPREG12_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_HPREG13_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 3, percent, fee, Config.CH_HPREG_FEE_RATIO, this.getClanHall().getFunction(3) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }

                           if (val.equalsIgnoreCase("mp")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Mp editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(4) != null && this.getClanHall().getFunction(4).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", val + "%");
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int percent = Integer.parseInt(val);
                                 int fee;
                                 switch(percent) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 5:
                                       fee = Config.CH_MPREG1_FEE;
                                       break;
                                    case 10:
                                       fee = Config.CH_MPREG2_FEE;
                                       break;
                                    case 15:
                                       fee = Config.CH_MPREG3_FEE;
                                       break;
                                    case 30:
                                       fee = Config.CH_MPREG4_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_MPREG5_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 4, percent, fee, Config.CH_MPREG_FEE_RATIO, this.getClanHall().getFunction(4) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }

                           if (val.equalsIgnoreCase("exp")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Exp editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(5) != null && this.getClanHall().getFunction(5).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", val + "%");
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int percent = Integer.parseInt(val);
                                 int fee;
                                 switch(percent) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 5:
                                       fee = Config.CH_EXPREG1_FEE;
                                       break;
                                    case 10:
                                       fee = Config.CH_EXPREG2_FEE;
                                       break;
                                    case 15:
                                       fee = Config.CH_EXPREG3_FEE;
                                       break;
                                    case 25:
                                       fee = Config.CH_EXPREG4_FEE;
                                       break;
                                    case 35:
                                       fee = Config.CH_EXPREG5_FEE;
                                       break;
                                    case 40:
                                       fee = Config.CH_EXPREG6_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_EXPREG7_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 5, percent, fee, Config.CH_EXPREG_FEE_RATIO, this.getClanHall().getFunction(5) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/edit_recovery.htm");
                        String hp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]";
                        String hp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]";
                        String hp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]";
                        String hp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
                        String exp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]";
                        String exp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]";
                        String exp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]";
                        String exp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
                        String mp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
                        String mp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";
                        String mp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]";
                        String mp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
                        if (this.getClanHall().getFunction(3) != null) {
                           html.replace(
                              "%hp_recovery%",
                              this.getClanHall().getFunction(3).getLvl()
                                 + "%</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(3).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_HPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%hp_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(3).getEndTime()))
                           );
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]"
                                 );
                           }
                        } else {
                           html.replace("%hp_recovery%", "none");
                           html.replace("%hp_period%", "none");
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_hp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]"
                                 );
                           }
                        }

                        if (this.getClanHall().getFunction(5) != null) {
                           html.replace(
                              "%exp_recovery%",
                              this.getClanHall().getFunction(5).getLvl()
                                 + "%</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(5).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_EXPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%exp_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(5).getEndTime()))
                           );
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
                                 );
                           }
                        } else {
                           html.replace("%exp_recovery%", "none");
                           html.replace("%exp_period%", "none");
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_exp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
                                 );
                           }
                        }

                        if (this.getClanHall().getFunction(4) != null) {
                           html.replace(
                              "%mp_recovery%",
                              this.getClanHall().getFunction(4).getLvl()
                                 + "%</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(4).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_MPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%mp_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(4).getEndTime()))
                           );
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]"
                                 );
                           }
                        } else {
                           html.replace("%mp_recovery%", "none");
                           html.replace("%mp_period%", "none");
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_mp%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]"
                                 );
                           }
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("other")) {
                        if (st.countTokens() >= 1) {
                           if (this.getClanHall().getOwnerId() == 0) {
                              player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                              return;
                           }

                           val = st.nextToken();
                           if (val.equalsIgnoreCase("item_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "other item 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("tele_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "other tele 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("support_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "other support 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_item")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Magic Equipment (Item Production Facilities)");
                              int stage = Integer.parseInt(val);
                              int cost;
                              switch(stage) {
                                 case 1:
                                    cost = Config.CH_ITEM1_FEE;
                                    break;
                                 case 2:
                                    cost = Config.CH_ITEM2_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_ITEM3_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_ITEM_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace("%use%", "Allow the purchase of special items at fixed intervals.");
                              html.replace("%apply%", "other item " + String.valueOf(stage));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_support")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Insignia (Supplementary Magic)");
                              int stage = Integer.parseInt(val);
                              int cost;
                              switch(stage) {
                                 case 1:
                                    cost = Config.CH_SUPPORT1_FEE;
                                    break;
                                 case 2:
                                    cost = Config.CH_SUPPORT2_FEE;
                                    break;
                                 case 3:
                                    cost = Config.CH_SUPPORT3_FEE;
                                    break;
                                 case 4:
                                    cost = Config.CH_SUPPORT4_FEE;
                                    break;
                                 case 5:
                                    cost = Config.CH_SUPPORT5_FEE;
                                    break;
                                 case 6:
                                    cost = Config.CH_SUPPORT6_FEE;
                                    break;
                                 case 7:
                                    cost = Config.CH_SUPPORT7_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_SUPPORT8_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_SUPPORT_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace("%use%", "Enables the use of supplementary magic.");
                              html.replace("%apply%", "other support " + String.valueOf(stage));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_tele")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Mirror (Teleportation Device)");
                              int stage = Integer.parseInt(val);
                              int cost;
                              switch(stage) {
                                 case 1:
                                    cost = Config.CH_TELE1_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_TELE2_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_TELE_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace(
                                 "%use%",
                                 "Teleports clan members in a clan hall to the target <font color=\"00FFFF\">Stage "
                                    + String.valueOf(stage)
                                    + "</font> staging area"
                              );
                              html.replace("%apply%", "other tele " + String.valueOf(stage));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("item")) {
                              if (st.countTokens() >= 1) {
                                 if (this.getClanHall().getOwnerId() == 0) {
                                    player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                                    return;
                                 }

                                 if (Config.DEBUG) {
                                    _log.warning("Item editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(2) != null && this.getClanHall().getFunction(2).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int lvl = Integer.parseInt(val);
                                 int fee;
                                 switch(lvl) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 1:
                                       fee = Config.CH_ITEM1_FEE;
                                       break;
                                    case 2:
                                       fee = Config.CH_ITEM2_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_ITEM3_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 2, lvl, fee, Config.CH_ITEM_FEE_RATIO, this.getClanHall().getFunction(2) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }

                           if (val.equalsIgnoreCase("tele")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Tele editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(1) != null && this.getClanHall().getFunction(1).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int lvl = Integer.parseInt(val);
                                 int fee;
                                 switch(lvl) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 1:
                                       fee = Config.CH_TELE1_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_TELE2_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 1, lvl, fee, Config.CH_TELE_FEE_RATIO, this.getClanHall().getFunction(1) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }

                           if (val.equalsIgnoreCase("support")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Support editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(6) != null && this.getClanHall().getFunction(6).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int lvl = Integer.parseInt(val);
                                 int fee;
                                 switch(lvl) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 1:
                                       fee = Config.CH_SUPPORT1_FEE;
                                       break;
                                    case 2:
                                       fee = Config.CH_SUPPORT2_FEE;
                                       break;
                                    case 3:
                                       fee = Config.CH_SUPPORT3_FEE;
                                       break;
                                    case 4:
                                       fee = Config.CH_SUPPORT4_FEE;
                                       break;
                                    case 5:
                                       fee = Config.CH_SUPPORT5_FEE;
                                       break;
                                    case 6:
                                       fee = Config.CH_SUPPORT6_FEE;
                                       break;
                                    case 7:
                                       fee = Config.CH_SUPPORT7_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_SUPPORT8_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 6, lvl, fee, Config.CH_SUPPORT_FEE_RATIO, this.getClanHall().getFunction(6) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/edit_other.htm");
                        String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
                        String support_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]";
                        String support_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";
                        String support_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]";
                        String support_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]";
                        String item = "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]";
                        if (this.getClanHall().getFunction(1) != null) {
                           html.replace(
                              "%tele%",
                              "Stage "
                                 + String.valueOf(this.getClanHall().getFunction(1).getLvl())
                                 + "</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(1).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_TELE_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%tele_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(1).getEndTime()))
                           );
                           html.replace(
                              "%change_tele%",
                              "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]"
                           );
                        } else {
                           html.replace("%tele%", "none");
                           html.replace("%tele_period%", "none");
                           html.replace(
                              "%change_tele%",
                              "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]"
                           );
                        }

                        if (this.getClanHall().getFunction(6) != null) {
                           html.replace(
                              "%support%",
                              "Stage "
                                 + String.valueOf(this.getClanHall().getFunction(6).getLvl())
                                 + "</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(6).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_SUPPORT_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%support_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(6).getEndTime()))
                           );
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]"
                                 );
                           }
                        } else {
                           html.replace("%support%", "none");
                           html.replace("%support_period%", "none");
                           int grade = this.getClanHall().getGrade();
                           switch(grade) {
                              case 0:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]"
                                 );
                                 break;
                              case 1:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]"
                                 );
                                 break;
                              case 2:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]"
                                 );
                                 break;
                              case 3:
                                 html.replace(
                                    "%change_support%",
                                    "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]"
                                 );
                           }
                        }

                        if (this.getClanHall().getFunction(2) != null) {
                           html.replace(
                              "%item%",
                              "Stage "
                                 + String.valueOf(this.getClanHall().getFunction(2).getLvl())
                                 + "</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(2).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_ITEM_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%item_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(2).getEndTime()))
                           );
                           html.replace(
                              "%change_item%",
                              "[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]"
                           );
                        } else {
                           html.replace("%item%", "none");
                           html.replace("%item_period%", "none");
                           html.replace(
                              "%change_item%",
                              "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]"
                           );
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("deco") && !this.getClanHall().isSiegableHall()) {
                        if (st.countTokens() >= 1) {
                           if (this.getClanHall().getOwnerId() == 0) {
                              player.sendMessage("This clan Hall have no owner, you cannot change configuration");
                              return;
                           }

                           val = st.nextToken();
                           if (val.equalsIgnoreCase("curtains_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "deco curtains 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("fixtures_cancel")) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel.htm");
                              html.replace("%apply%", "deco fixtures 0");
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_curtains")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Curtains (Decoration)");
                              int stage = Integer.parseInt(val);
                              int cost;
                              switch(stage) {
                                 case 1:
                                    cost = Config.CH_CURTAIN1_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_CURTAIN2_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_CURTAIN_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace("%use%", "These curtains can be used to decorate the clan hall.");
                              html.replace("%apply%", "deco curtains " + String.valueOf(stage));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("edit_fixtures")) {
                              val = st.nextToken();
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply.htm");
                              html.replace("%name%", "Front Platform (Decoration)");
                              int stage = Integer.parseInt(val);
                              int cost;
                              switch(stage) {
                                 case 1:
                                    cost = Config.CH_FRONT1_FEE;
                                    break;
                                 default:
                                    cost = Config.CH_FRONT2_FEE;
                              }

                              html.replace("%cost%", cost + "</font>Adena /" + Config.CH_FRONT_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                              html.replace("%use%", "Used to decorate the clan hall.");
                              html.replace("%apply%", "deco fixtures " + String.valueOf(stage));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           if (val.equalsIgnoreCase("curtains")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Deco curtains editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(8) != null && this.getClanHall().getFunction(8).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int lvl = Integer.parseInt(val);
                                 int fee;
                                 switch(lvl) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 1:
                                       fee = Config.CH_CURTAIN1_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_CURTAIN2_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 8, lvl, fee, Config.CH_CURTAIN_FEE_RATIO, this.getClanHall().getFunction(8) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }

                           if (val.equalsIgnoreCase("fixtures")) {
                              if (st.countTokens() >= 1) {
                                 if (Config.DEBUG) {
                                    _log.warning("Deco fixtures editing invoked");
                                 }

                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-apply_confirmed.htm");
                                 if (this.getClanHall().getFunction(7) != null && this.getClanHall().getFunction(7).getLvl() == Integer.parseInt(val)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    this.sendHtmlMessage(player, html);
                                    return;
                                 }

                                 int lvl = Integer.parseInt(val);
                                 int fee;
                                 switch(lvl) {
                                    case 0:
                                       fee = 0;
                                       html.setFile(player, player.getLang(), "data/html/clanHallManager/functions-cancel_confirmed.htm");
                                       break;
                                    case 1:
                                       fee = Config.CH_FRONT1_FEE;
                                       break;
                                    default:
                                       fee = Config.CH_FRONT2_FEE;
                                 }

                                 if (!this.getClanHall()
                                    .updateFunctions(player, 7, lvl, fee, Config.CH_FRONT_FEE_RATIO, this.getClanHall().getFunction(7) == null)) {
                                    html.setFile(player, player.getLang(), "data/html/clanHallManager/low_adena.htm");
                                    this.sendHtmlMessage(player, html);
                                 } else {
                                    this.revalidateDeco(player);
                                 }

                                 this.sendHtmlMessage(player, html);
                              }

                              return;
                           }
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/clanHallManager/deco.htm");
                        String curtains = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]";
                        String fixtures = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]";
                        if (this.getClanHall().getFunction(8) != null) {
                           html.replace(
                              "%curtain%",
                              "Stage "
                                 + String.valueOf(this.getClanHall().getFunction(8).getLvl())
                                 + "</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(8).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_CURTAIN_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%curtain_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(8).getEndTime()))
                           );
                           html.replace(
                              "%change_curtain%",
                              "[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]"
                           );
                        } else {
                           html.replace("%curtain%", "none");
                           html.replace("%curtain_period%", "none");
                           html.replace(
                              "%change_curtain%",
                              "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]"
                           );
                        }

                        if (this.getClanHall().getFunction(7) != null) {
                           html.replace(
                              "%fixture%",
                              "Stage "
                                 + String.valueOf(this.getClanHall().getFunction(7).getLvl())
                                 + "</font> (<font color=\"FFAABB\">"
                                 + this.getClanHall().getFunction(7).getLease()
                                 + "</font>Adena /"
                                 + Config.CH_FRONT_FEE_RATIO / 1000L / 60L / 60L / 24L
                                 + " Day)"
                           );
                           html.replace(
                              "%fixture_period%",
                              "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getClanHall().getFunction(7).getEndTime()))
                           );
                           html.replace(
                              "%change_fixture%",
                              "[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]"
                           );
                        } else {
                           html.replace("%fixture%", "none");
                           html.replace("%fixture_period%", "none");
                           html.replace(
                              "%change_fixture%",
                              "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]"
                           );
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("back")) {
                        this.showChatWindow(player);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(
                           player,
                           player.getLang(),
                           this.getClanHall().isSiegableHall() ? "data/html/clanHallManager/manage_siegable.htm" : "data/html/clanHallManager/manage.htm"
                        );
                        this.sendHtmlMessage(player, html);
                     }
                  } else {
                     NpcHtmlMessage html = new NpcHtmlMessage(1);
                     html.setFile(player, player.getLang(), "data/html/clanHallManager/not_authorized.htm");
                     this.sendHtmlMessage(player, html);
                  }

                  return;
               }

               if (actualCommand.equalsIgnoreCase("support")) {
                  if (player.isCursedWeaponEquipped()) {
                     player.sendMessage("The wielder of a cursed weapon cannot receive outside heals or buffs");
                     return;
                  }

                  this.setTarget(player);
                  if (val.isEmpty()) {
                     return;
                  }

                  try {
                     int skill_id = Integer.parseInt(val);

                     try {
                        int skill_lvl = 0;
                        if (st.countTokens() >= 1) {
                           skill_lvl = Integer.parseInt(st.nextToken());
                        }

                        Skill skill = SkillsParser.getInstance().getInfo(skill_id, skill_lvl);
                        if (skill.getSkillType() == SkillType.SUMMON) {
                           player.doSimultaneousCast(skill);
                        } else {
                           int mpCost = skill.getMpConsume() + skill.getMpInitialConsume();
                           if (!(this.getCurrentMp() >= (double)mpCost) && !Config.CH_BUFF_FREE) {
                              NpcHtmlMessage html = new NpcHtmlMessage(1);
                              html.setFile(player, player.getLang(), "data/html/clanHallManager/support-no_mana.htm");
                              html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                              this.sendHtmlMessage(player, html);
                              return;
                           }

                           this.doCast(skill);
                        }

                        if (this.getClanHall().getFunction(6) == null) {
                           return;
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        if (this.getClanHall().getFunction(6).getLvl() == 0) {
                           return;
                        }

                        html.setFile(player, player.getLang(), "data/html/clanHallManager/support-done.htm");
                        html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                        this.sendHtmlMessage(player, html);
                     } catch (Exception var22) {
                        player.sendMessage("Invalid skill level, contact your admin!");
                     }
                  } catch (Exception var23) {
                     player.sendMessage("Invalid skill level, contact your admin!");
                  }

                  return;
               }

               if (actualCommand.equalsIgnoreCase("list_back")) {
                  NpcHtmlMessage html = new NpcHtmlMessage(1);
                  String file = "data/html/clanHallManager/chamberlain-" + this.getId() + ".htm";
                  if (!HtmCache.getInstance().isLoadable(file)) {
                     file = "data/html/clanHallManager/chamberlain.htm";
                  }

                  html.setFile(player, player.getLang(), file);
                  html.replace("%objectId%", String.valueOf(this.getObjectId()));
                  html.replace("%npcname%", this.getName());
                  this.sendHtmlMessage(player, html);
                  return;
               }

               if (actualCommand.equalsIgnoreCase("support_back")) {
                  NpcHtmlMessage html = new NpcHtmlMessage(1);
                  if (this.getClanHall().getFunction(6).getLvl() == 0) {
                     return;
                  }

                  html.setFile(player, player.getLang(), "data/html/clanHallManager/support" + this.getClanHall().getFunction(6).getLvl() + ".htm");
                  html.replace("%mp%", String.valueOf((int)this.getStatus().getCurrentMp()));
                  this.sendHtmlMessage(player, html);
                  return;
               }

               if (actualCommand.equalsIgnoreCase("goto")) {
                  int whereTo = Integer.parseInt(val);
                  this.doTeleport(player, whereTo);
                  return;
               }
            }

            super.onBypassFeedback(player, command);
         }
      }
   }

   private void sendHtmlMessage(Player player, NpcHtmlMessage html) {
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcId%", String.valueOf(this.getId()));
      player.sendPacket(html);
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      String filename = "data/html/clanHallManager/chamberlain-no.htm";
      int condition = this.validateCondition(player);
      if (condition == 3) {
         filename = "data/html/clanHallManager/chamberlain-" + this.getId() + ".htm";
         if (!HtmCache.getInstance().isLoadable(filename)) {
            filename = "data/html/clanHallManager/chamberlain.htm";
         }
      } else if (condition == 0) {
         filename = "data/html/clanHallManager/chamberlain-of.htm";
      }

      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcId%", String.valueOf(this.getId()));
      player.sendPacket(html);
   }

   protected int validateCondition(Player player) {
      if (this.getClanHall() == null) {
         return 1;
      } else if (player.canOverrideCond(PcCondOverride.CLANHALL_CONDITIONS)) {
         return 3;
      } else if (player.getClan() != null) {
         return this.getClanHall().getOwnerId() == player.getClanId() ? 3 : 0;
      } else {
         return 1;
      }
   }

   public final ClanHall getClanHall() {
      if (this._clanHallId < 0) {
         ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(this.getX(), this.getY(), 500);
         if (temp == null) {
            temp = CHSiegeManager.getInstance().getNearbyClanHall(this);
         }

         if (temp != null) {
            this._clanHallId = temp.getId();
         }

         if (this._clanHallId < 0) {
            return null;
         }
      }

      return ClanHallManager.getInstance().getClanHallById(this._clanHallId);
   }

   private void doTeleport(Player player, int val) {
      if (Config.DEBUG) {
         _log.warning("doTeleport(Player player, int val) is called");
      }

      TeleportTemplate list = TeleLocationParser.getInstance().getTemplate(val);
      if (list != null) {
         if (player.isCombatFlagEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
            return;
         }

         if (player.destroyItemByItemId("Teleport", list.getId(), (long)list.getPrice(), this, true)) {
            if (Config.DEBUG) {
               _log.warning(
                  "Teleporting player " + player.getName() + " for CH to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ()
               );
            }

            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
         }
      } else {
         _log.warning("No teleport destination with id:" + val);
      }

      player.sendActionFailed();
   }

   private void revalidateDeco(Player player) {
      AuctionableHall ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
      if (ch != null) {
         AgitDecoInfo bl = new AgitDecoInfo(ch);
         player.sendPacket(bl);
      }
   }
}
