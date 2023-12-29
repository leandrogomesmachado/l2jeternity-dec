package l2e.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowDominionRegistry;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CastleChamberlainInstance extends MerchantInstance {
   protected static final int COND_ALL_FALSE = 0;
   protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
   protected static final int COND_OWNER = 2;

   public CastleChamberlainInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.CastleChamberlainInstance);
   }

   private void sendHtmlMessage(Player player, NpcHtmlMessage html) {
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcId%", String.valueOf(this.getId()));
      player.sendPacket(html);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player.getLastFolkNPC().getObjectId() == this.getObjectId()) {
         SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
         int condition = this.validateCondition(player);
         if (condition > 0) {
            if (condition != 1) {
               if (condition == 2) {
                  StringTokenizer st = new StringTokenizer(command, " ");
                  String actualCommand = st.nextToken();
                  String val = "";
                  if (st.hasMoreTokens()) {
                     val = st.nextToken();
                  }

                  if (actualCommand.equalsIgnoreCase("banish_foreigner")) {
                     if (!this.validatePrivileges(player, 1048576)) {
                        return;
                     }

                     if (this.siegeBlocksFunction(player)) {
                        return;
                     }

                     this.getCastle().banishForeigners();
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-banishafter.htm");
                     html.replace("%objectId%", String.valueOf(this.getObjectId()));
                     player.sendPacket(html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("banish_foreigner_show")) {
                     if (!this.validatePrivileges(player, 1048576)) {
                        return;
                     }

                     if (this.siegeBlocksFunction(player)) {
                        return;
                     }

                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-banishfore.htm");
                     html.replace("%objectId%", String.valueOf(this.getObjectId()));
                     player.sendPacket(html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("list_siege_clans")) {
                     if ((player.getClanPrivileges() & 262144) == 262144) {
                        this.getCastle().getSiege().listRegisterClan(player);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("list_territory_clans")) {
                     if ((player.getClanPrivileges() & 262144) == 262144) {
                        player.sendPacket(new ExShowDominionRegistry(this.getCastle().getId(), player));
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("receive_report")) {
                     if (player.isClanLeader()) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-report.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        Clan clan = ClanHolder.getInstance().getClan(this.getCastle().getOwnerId());
                        html.replace("%clanname%", clan.getName());
                        html.replace("%clanleadername%", clan.getLeaderName());
                        html.replace("%castlename%", this.getCastle().getName());
                        int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
                        switch(currentPeriod) {
                           case 0:
                              html.replace("%ss_event%", "Quest Event Initialization");
                              break;
                           case 1:
                              html.replace("%ss_event%", "Competition (Quest Event)");
                              break;
                           case 2:
                              html.replace("%ss_event%", "Quest Event Results");
                              break;
                           case 3:
                              html.replace("%ss_event%", "Seal Validation");
                        }

                        int sealOwner1 = SevenSigns.getInstance().getSealOwner(1);
                        switch(sealOwner1) {
                           case 0:
                              html.replace("%ss_avarice%", "Not in Possession");
                              break;
                           case 1:
                              html.replace("%ss_avarice%", "Revolutionaries of Dusk");
                              break;
                           case 2:
                              html.replace("%ss_avarice%", "Lords of Dawn");
                        }

                        int sealOwner2 = SevenSigns.getInstance().getSealOwner(2);
                        switch(sealOwner2) {
                           case 0:
                              html.replace("%ss_gnosis%", "Not in Possession");
                              break;
                           case 1:
                              html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
                              break;
                           case 2:
                              html.replace("%ss_gnosis%", "Lords of Dawn");
                        }

                        int sealOwner3 = SevenSigns.getInstance().getSealOwner(3);
                        switch(sealOwner3) {
                           case 0:
                              html.replace("%ss_strife%", "Not in Possession");
                              break;
                           case 1:
                              html.replace("%ss_strife%", "Revolutionaries of Dusk");
                              break;
                           case 2:
                              html.replace("%ss_strife%", "Lords of Dawn");
                        }

                        player.sendPacket(html);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("items")) {
                     if ((player.getClanPrivileges() & 524288) == 524288) {
                        if (val.isEmpty()) {
                           return;
                        }

                        if (Config.DEBUG) {
                           _log.fine("Showing chamberlain buylist");
                        }

                        this.showBuyWindow(player, Integer.parseInt(val + "1"));
                        player.sendActionFailed();
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("manage_siege_defender")) {
                     if ((player.getClanPrivileges() & 262144) == 262144) {
                        this.getCastle().getSiege().listRegisterClan(player);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("manage_vault")) {
                     if ((player.getClanPrivileges() & 2097152) == 2097152) {
                        String filename = "data/html/chamberlain/chamberlain-vault.htm";
                        long amount = 0L;
                        if (val.equalsIgnoreCase("deposit")) {
                           try {
                              amount = Long.parseLong(st.nextToken());
                           } catch (NoSuchElementException var15) {
                           }

                           if (amount > 0L && this.getCastle().getTreasury() + amount < PcInventory.MAX_ADENA) {
                              if (player.reduceAdena("Castle", amount, this, true)) {
                                 this.getCastle().addToTreasuryNoTax(amount);
                              } else {
                                 this.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                              }
                           }
                        } else if (val.equalsIgnoreCase("withdraw")) {
                           try {
                              amount = Long.parseLong(st.nextToken());
                           } catch (NoSuchElementException var14) {
                           }

                           if (amount > 0L) {
                              if (this.getCastle().getTreasury() < amount) {
                                 filename = "data/html/chamberlain/chamberlain-vault-no.htm";
                              } else if (this.getCastle().addToTreasuryNoTax(-1L * amount)) {
                                 player.addAdena("Castle", amount, this, true);
                              }
                           }
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), filename);
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        html.replace("%npcname%", this.getName());
                        html.replace("%tax_income%", Util.formatAdena(this.getCastle().getTreasury()));
                        html.replace("%withdraw_amount%", Util.formatAdena(amount));
                        player.sendPacket(html);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("operate_door")) {
                     if ((player.getClanPrivileges() & 65536) == 65536) {
                        if (!val.isEmpty()) {
                           boolean open = Integer.parseInt(val) == 1;

                           while(st.hasMoreTokens()) {
                              this.getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                           String file = "data/html/chamberlain/doors-close.htm";
                           if (open) {
                              file = "data/html/chamberlain/doors-open.htm";
                           }

                           html.setFile(player, player.getLang(), file);
                           html.replace("%objectId%", String.valueOf(this.getObjectId()));
                           player.sendPacket(html);
                           return;
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/" + this.getTemplate().getId() + "-d.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        html.replace("%npcname%", this.getName());
                        player.sendPacket(html);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("tax_set")) {
                     if ((player.getClanPrivileges() & 2097152) == 2097152) {
                        if (!val.isEmpty()) {
                           this.getCastle().setTaxPercent(player, Integer.parseInt(val));
                        }

                        String msg = StringUtil.concat(
                           "<html><body>",
                           this.getName(),
                           ":<br>Current tax rate: ",
                           String.valueOf(this.getCastle().getTaxPercent()),
                           "%<br><table><tr><td>Change tax rate to:</td><td><edit var=\"value\" width=40><br><button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center></body></html>"
                        );
                        this.sendHtmlMessage(player, msg);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-tax.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        html.replace("%tax%", String.valueOf(this.getCastle().getTaxPercent()));
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("manage_functions")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-manage.htm");
                     html.replace("%objectId%", String.valueOf(this.getObjectId()));
                     player.sendPacket(html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("products")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-products.htm");
                     html.replace("%objectId%", String.valueOf(this.getObjectId()));
                     html.replace("%npcId%", String.valueOf(this.getId()));
                     player.sendPacket(html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("functions")) {
                     if (val.equalsIgnoreCase("tele")) {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        if (this.getCastle().getFunction(1) == null) {
                           html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-nac.htm");
                        } else {
                           html.setFile(
                              player, player.getLang(), "data/html/chamberlain/" + this.getId() + "-t" + this.getCastle().getFunction(1).getLvl() + ".htm"
                           );
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("support")) {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        if (this.getCastle().getFunction(5) == null) {
                           html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-nac.htm");
                        } else {
                           html.setFile(player, player.getLang(), "data/html/chamberlain/support" + this.getCastle().getFunction(5).getLvl() + ".htm");
                           html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("back")) {
                        this.showChatWindow(player);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-functions.htm");
                        if (this.getCastle().getFunction(4) != null) {
                           html.replace("%xp_regen%", String.valueOf(this.getCastle().getFunction(4).getLvl()));
                        } else {
                           html.replace("%xp_regen%", "0");
                        }

                        if (this.getCastle().getFunction(2) != null) {
                           html.replace("%hp_regen%", String.valueOf(this.getCastle().getFunction(2).getLvl()));
                        } else {
                           html.replace("%hp_regen%", "0");
                        }

                        if (this.getCastle().getFunction(3) != null) {
                           html.replace("%mp_regen%", String.valueOf(this.getCastle().getFunction(3).getLvl()));
                        } else {
                           html.replace("%mp_regen%", "0");
                        }

                        this.sendHtmlMessage(player, html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("manage")) {
                     if ((player.getClanPrivileges() & 8388608) == 8388608) {
                        if (val.equalsIgnoreCase("recovery")) {
                           if (st.countTokens() >= 1) {
                              if (this.getCastle().getOwnerId() == 0) {
                                 player.sendMessage("This castle have no owner, you cannot change configuration");
                                 return;
                              }

                              val = st.nextToken();
                              if (val.equalsIgnoreCase("hp_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel.htm");
                                 html.replace("%apply%", "recovery hp 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("mp_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel.htm");
                                 html.replace("%apply%", "recovery mp 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("exp_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel.htm");
                                 html.replace("%apply%", "recovery exp 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("edit_hp")) {
                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply.htm");
                                 html.replace("%name%", "Fireplace (HP Recovery Device)");
                                 int percent = Integer.parseInt(val);
                                 int cost;
                                 switch(percent) {
                                    case 80:
                                       cost = Config.CS_HPREG1_FEE;
                                       break;
                                    case 120:
                                       cost = Config.CS_HPREG2_FEE;
                                       break;
                                    case 180:
                                       cost = Config.CS_HPREG3_FEE;
                                       break;
                                    case 240:
                                       cost = Config.CS_HPREG4_FEE;
                                       break;
                                    default:
                                       cost = Config.CS_HPREG5_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.CS_HPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Provides additional HP recovery for clan members in the castle.<font color=\"00FFFF\">"
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
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply.htm");
                                 html.replace("%name%", "Carpet (MP Recovery)");
                                 int percent = Integer.parseInt(val);
                                 int cost;
                                 switch(percent) {
                                    case 5:
                                       cost = Config.CS_MPREG1_FEE;
                                       break;
                                    case 15:
                                       cost = Config.CS_MPREG2_FEE;
                                       break;
                                    case 30:
                                       cost = Config.CS_MPREG3_FEE;
                                       break;
                                    default:
                                       cost = Config.CS_MPREG4_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.CS_MPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Provides additional MP recovery for clan members in the castle.<font color=\"00FFFF\">"
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
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply.htm");
                                 html.replace("%name%", "Chandelier (EXP Recovery Device)");
                                 int percent = Integer.parseInt(val);
                                 int cost;
                                 switch(percent) {
                                    case 15:
                                       cost = Config.CS_EXPREG1_FEE;
                                       break;
                                    case 25:
                                       cost = Config.CS_EXPREG2_FEE;
                                       break;
                                    case 35:
                                       cost = Config.CS_EXPREG3_FEE;
                                       break;
                                    default:
                                       cost = Config.CS_EXPREG4_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.CS_EXPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Restores the Exp of any clan member who is resurrected in the castle.<font color=\"00FFFF\">"
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
                                       _log.warning("Hp editing invoked");
                                    }

                                    val = st.nextToken();
                                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                                    html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
                                    if (this.getCastle().getFunction(2) != null && this.getCastle().getFunction(2).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/functions-used.htm");
                                       html.replace("%val%", val + "%");
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int percent = Integer.parseInt(val);
                                    int fee;
                                    switch(percent) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
                                          break;
                                       case 80:
                                          fee = Config.CS_HPREG1_FEE;
                                          break;
                                       case 120:
                                          fee = Config.CS_HPREG2_FEE;
                                          break;
                                       case 180:
                                          fee = Config.CS_HPREG3_FEE;
                                          break;
                                       case 240:
                                          fee = Config.CS_HPREG4_FEE;
                                          break;
                                       default:
                                          fee = Config.CS_HPREG5_FEE;
                                    }

                                    if (!this.getCastle()
                                       .updateFunctions(player, 2, percent, fee, Config.CS_HPREG_FEE_RATIO, this.getCastle().getFunction(2) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
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
                                    html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
                                    if (this.getCastle().getFunction(3) != null && this.getCastle().getFunction(3).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/functions-used.htm");
                                       html.replace("%val%", val + "%");
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int percent = Integer.parseInt(val);
                                    int fee;
                                    switch(percent) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
                                          break;
                                       case 5:
                                          fee = Config.CS_MPREG1_FEE;
                                          break;
                                       case 15:
                                          fee = Config.CS_MPREG2_FEE;
                                          break;
                                       case 30:
                                          fee = Config.CS_MPREG3_FEE;
                                          break;
                                       default:
                                          fee = Config.CS_MPREG4_FEE;
                                    }

                                    if (!this.getCastle()
                                       .updateFunctions(player, 3, percent, fee, Config.CS_MPREG_FEE_RATIO, this.getCastle().getFunction(3) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
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
                                    html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
                                    if (this.getCastle().getFunction(4) != null && this.getCastle().getFunction(4).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/functions-used.htm");
                                       html.replace("%val%", val + "%");
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int percent = Integer.parseInt(val);
                                    int fee;
                                    switch(percent) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
                                          break;
                                       case 15:
                                          fee = Config.CS_EXPREG1_FEE;
                                          break;
                                       case 25:
                                          fee = Config.CS_EXPREG2_FEE;
                                          break;
                                       case 35:
                                          fee = Config.CS_EXPREG3_FEE;
                                          break;
                                       default:
                                          fee = Config.CS_EXPREG4_FEE;
                                    }

                                    if (!this.getCastle()
                                       .updateFunctions(player, 4, percent, fee, Config.CS_EXPREG_FEE_RATIO, this.getCastle().getFunction(4) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
                                    }

                                    this.sendHtmlMessage(player, html);
                                 }

                                 return;
                              }
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           html.setFile(player, player.getLang(), "data/html/chamberlain/edit_recovery.htm");
                           String hp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";
                           String exp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
                           String mp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";
                           if (this.getCastle().getFunction(2) != null) {
                              html.replace(
                                 "%hp_recovery%",
                                 this.getCastle().getFunction(2).getLvl()
                                    + "%</font> (<font color=\"FFAABB\">"
                                    + this.getCastle().getFunction(2).getLease()
                                    + "</font>Adena /"
                                    + Config.CS_HPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%hp_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getCastle().getFunction(2).getEndTime()))
                              );
                              html.replace(
                                 "%change_hp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]"
                              );
                           } else {
                              html.replace("%hp_recovery%", "none");
                              html.replace("%hp_period%", "none");
                              html.replace(
                                 "%change_hp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]"
                              );
                           }

                           if (this.getCastle().getFunction(4) != null) {
                              html.replace(
                                 "%exp_recovery%",
                                 this.getCastle().getFunction(4).getLvl()
                                    + "%</font> (<font color=\"FFAABB\">"
                                    + this.getCastle().getFunction(4).getLease()
                                    + "</font>Adena /"
                                    + Config.CS_EXPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%exp_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getCastle().getFunction(4).getEndTime()))
                              );
                              html.replace(
                                 "%change_exp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
                              );
                           } else {
                              html.replace("%exp_recovery%", "none");
                              html.replace("%exp_period%", "none");
                              html.replace(
                                 "%change_exp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
                              );
                           }

                           if (this.getCastle().getFunction(3) != null) {
                              html.replace(
                                 "%mp_recovery%",
                                 this.getCastle().getFunction(3).getLvl()
                                    + "%</font> (<font color=\"FFAABB\">"
                                    + this.getCastle().getFunction(3).getLease()
                                    + "</font>Adena /"
                                    + Config.CS_MPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%mp_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getCastle().getFunction(3).getEndTime()))
                              );
                              html.replace(
                                 "%change_mp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]"
                              );
                           } else {
                              html.replace("%mp_recovery%", "none");
                              html.replace("%mp_period%", "none");
                              html.replace(
                                 "%change_mp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]"
                              );
                           }

                           this.sendHtmlMessage(player, html);
                        } else if (val.equalsIgnoreCase("other")) {
                           if (st.countTokens() >= 1) {
                              if (this.getCastle().getOwnerId() == 0) {
                                 player.sendMessage("This castle have no owner, you cannot change configuration");
                                 return;
                              }

                              val = st.nextToken();
                              if (val.equalsIgnoreCase("tele_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel.htm");
                                 html.replace("%apply%", "other tele 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("support_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel.htm");
                                 html.replace("%apply%", "other support 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("edit_support")) {
                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply.htm");
                                 html.replace("%name%", "Insignia (Supplementary Magic)");
                                 int stage = Integer.parseInt(val);
                                 int cost;
                                 switch(stage) {
                                    case 1:
                                       cost = Config.CS_SUPPORT1_FEE;
                                       break;
                                    case 2:
                                       cost = Config.CS_SUPPORT2_FEE;
                                       break;
                                    case 3:
                                       cost = Config.CS_SUPPORT3_FEE;
                                       break;
                                    default:
                                       cost = Config.CS_SUPPORT4_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.CS_SUPPORT_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace("%use%", "Enables the use of supplementary magic.");
                                 html.replace("%apply%", "other support " + String.valueOf(stage));
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("edit_tele")) {
                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply.htm");
                                 html.replace("%name%", "Mirror (Teleportation Device)");
                                 int stage = Integer.parseInt(val);
                                 int cost;
                                 switch(stage) {
                                    case 1:
                                       cost = Config.CS_TELE1_FEE;
                                       break;
                                    default:
                                       cost = Config.CS_TELE2_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.CS_TELE_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Teleports clan members in a castle to the target <font color=\"00FFFF\">Stage "
                                       + String.valueOf(stage)
                                       + "</font> staging area"
                                 );
                                 html.replace("%apply%", "other tele " + String.valueOf(stage));
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("tele")) {
                                 if (st.countTokens() >= 1) {
                                    if (Config.DEBUG) {
                                       _log.warning("Tele editing invoked");
                                    }

                                    val = st.nextToken();
                                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                                    html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
                                    if (this.getCastle().getFunction(1) != null && this.getCastle().getFunction(1).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/functions-used.htm");
                                       html.replace("%val%", "Stage " + val);
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int lvl = Integer.parseInt(val);
                                    int fee;
                                    switch(lvl) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
                                          break;
                                       case 1:
                                          fee = Config.CS_TELE1_FEE;
                                          break;
                                       default:
                                          fee = Config.CS_TELE2_FEE;
                                    }

                                    if (!this.getCastle()
                                       .updateFunctions(player, 1, lvl, fee, Config.CS_TELE_FEE_RATIO, this.getCastle().getFunction(1) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
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
                                    html.setFile(player, player.getLang(), "data/html/chamberlain/functions-apply_confirmed.htm");
                                    if (this.getCastle().getFunction(5) != null && this.getCastle().getFunction(5).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/functions-used.htm");
                                       html.replace("%val%", "Stage " + val);
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int lvl = Integer.parseInt(val);
                                    int fee;
                                    switch(lvl) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/chamberlain/functions-cancel_confirmed.htm");
                                          break;
                                       case 1:
                                          fee = Config.CS_SUPPORT1_FEE;
                                          break;
                                       case 2:
                                          fee = Config.CS_SUPPORT2_FEE;
                                          break;
                                       case 3:
                                          fee = Config.CS_SUPPORT3_FEE;
                                          break;
                                       default:
                                          fee = Config.CS_SUPPORT4_FEE;
                                    }

                                    if (!this.getCastle()
                                       .updateFunctions(player, 5, lvl, fee, Config.CS_SUPPORT_FEE_RATIO, this.getCastle().getFunction(5) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/chamberlain/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
                                    } else {
                                       this.sendHtmlMessage(player, html);
                                    }
                                 }

                                 return;
                              }
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           html.setFile(player, player.getLang(), "data/html/chamberlain/edit_other.htm");
                           String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
                           String support = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";
                           if (this.getCastle().getFunction(1) != null) {
                              html.replace(
                                 "%tele%",
                                 "Stage "
                                    + String.valueOf(this.getCastle().getFunction(1).getLvl())
                                    + "</font> (<font color=\"FFAABB\">"
                                    + this.getCastle().getFunction(1).getLease()
                                    + "</font>Adena /"
                                    + Config.CS_TELE_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%tele_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getCastle().getFunction(1).getEndTime()))
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

                           if (this.getCastle().getFunction(5) != null) {
                              html.replace(
                                 "%support%",
                                 "Stage "
                                    + String.valueOf(this.getCastle().getFunction(5).getLvl())
                                    + "</font> (<font color=\"FFAABB\">"
                                    + this.getCastle().getFunction(5).getLease()
                                    + "</font>Adena /"
                                    + Config.CS_SUPPORT_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%support_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getCastle().getFunction(5).getEndTime()))
                              );
                              html.replace(
                                 "%change_support%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]"
                              );
                           } else {
                              html.replace("%support%", "none");
                              html.replace("%support_period%", "none");
                              html.replace(
                                 "%change_support%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]"
                              );
                           }

                           this.sendHtmlMessage(player, html);
                        } else if (val.equalsIgnoreCase("back")) {
                           this.showChatWindow(player);
                        } else {
                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           html.setFile(player, player.getLang(), "data/html/chamberlain/manage.htm");
                           this.sendHtmlMessage(player, html);
                        }
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                        this.sendHtmlMessage(player, html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("support")) {
                     this.setTarget(player);
                     if (val.isEmpty()) {
                        return;
                     }

                     try {
                        int skill_id = Integer.parseInt(val);

                        try {
                           if (this.getCastle().getFunction(5) == null) {
                              return;
                           }

                           if (this.getCastle().getFunction(5).getLvl() == 0) {
                              return;
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           int skill_lvl = 0;
                           if (st.countTokens() >= 1) {
                              skill_lvl = Integer.parseInt(st.nextToken());
                           }

                           Skill skill = SkillsParser.getInstance().getInfo(skill_id, skill_lvl);
                           if (skill.getSkillType() == SkillType.SUMMON) {
                              player.doSimultaneousCast(skill);
                           } else {
                              if ((double)(skill.getMpConsume() + skill.getMpInitialConsume()) > this.getCurrentMp()) {
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/support-no_mana.htm");
                                 html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              this.doCast(skill);
                           }

                           html.setFile(player, player.getLang(), "data/html/chamberlain/support-done.htm");
                           html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                           this.sendHtmlMessage(player, html);
                        } catch (Exception var16) {
                           player.sendMessage("Invalid skill level, contact your admin!");
                        }
                     } catch (Exception var17) {
                        player.sendMessage("Invalid skill level, contact your admin!");
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("support_back")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(1);
                     if (this.getCastle().getFunction(5).getLvl() == 0) {
                        return;
                     }

                     html.setFile(player, player.getLang(), "data/html/chamberlain/support" + this.getCastle().getFunction(5).getLvl() + ".htm");
                     html.replace("%mp%", String.valueOf((int)this.getStatus().getCurrentMp()));
                     this.sendHtmlMessage(player, html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("goto")) {
                     int whereTo = Integer.parseInt(val);
                     this.doTeleport(player, whereTo);
                     return;
                  }

                  if (actualCommand.equals("give_crown")) {
                     if (this.siegeBlocksFunction(player)) {
                        return;
                     }

                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     if (player.isClanLeader()) {
                        if (player.getInventory().getItemByItemId(6841) == null) {
                           ItemInstance crown = player.getInventory().addItem("Castle Crown", 6841, 1L, player, this);
                           SystemMessage ms = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                           ms.addItemName(crown);
                           player.sendPacket(ms);
                           html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-gavecrown.htm");
                           html.replace("%CharName%", String.valueOf(player.getName()));
                           html.replace("%FeudName%", String.valueOf(this.getCastle().getName()));
                        } else {
                           html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-hascrown.htm");
                        }
                     } else {
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                     }

                     player.sendPacket(html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("manors_cert")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     if (this.isMyLord(player) || this.validatePrivileges(player, 5) && this.validateCondition(player) == 2) {
                        if (this.getCastle().getSiege().getIsInProgress()) {
                           html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-busy.htm");
                           html.replace("%npcname%", String.valueOf(this.getName()));
                        } else {
                           int cabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
                           if (cabal == 2 && SevenSigns.getInstance().isCompetitionPeriod()) {
                              int ticketCount = this.getCastle().getTicketBuyCount();
                              if (ticketCount < Config.SSQ_DAWN_TICKET_QUANTITY / Config.SSQ_DAWN_TICKET_BUNDLE) {
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/ssq_selldawnticket.htm");
                                 html.replace(
                                    "%DawnTicketLeft%", String.valueOf(Config.SSQ_DAWN_TICKET_QUANTITY - ticketCount * Config.SSQ_DAWN_TICKET_BUNDLE)
                                 );
                                 html.replace("%DawnTicketBundle%", String.valueOf(Config.SSQ_DAWN_TICKET_BUNDLE));
                                 html.replace("%DawnTicketPrice%", String.valueOf(Config.SSQ_DAWN_TICKET_PRICE * Config.SSQ_DAWN_TICKET_BUNDLE));
                              } else {
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/ssq_notenoughticket.htm");
                              }
                           } else {
                              html.setFile(player, player.getLang(), "data/html/chamberlain/ssq_notdawnorevent.htm");
                           }
                        }
                     } else {
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                     }

                     html.replace("%objectId%", String.valueOf(this.getObjectId()));
                     player.sendPacket(html);
                  } else if (actualCommand.equalsIgnoreCase("manors_cert_confirm")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     if (this.isMyLord(player) || this.validatePrivileges(player, 5) && this.validateCondition(player) == 2) {
                        if (this.getCastle().getSiege().getIsInProgress()) {
                           html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-busy.htm");
                           html.replace("%npcname%", String.valueOf(this.getName()));
                        } else {
                           int cabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
                           if (cabal == 2 && SevenSigns.getInstance().isCompetitionPeriod()) {
                              int ticketCount = this.getCastle().getTicketBuyCount();
                              if (ticketCount < Config.SSQ_DAWN_TICKET_QUANTITY / Config.SSQ_DAWN_TICKET_BUNDLE) {
                                 long totalCost = (long)(Config.SSQ_DAWN_TICKET_PRICE * Config.SSQ_DAWN_TICKET_BUNDLE);
                                 if (player.getAdena() >= totalCost) {
                                    player.reduceAdena(actualCommand, totalCost, this, true);
                                    player.addItem(actualCommand, Config.SSQ_MANORS_AGREEMENT_ID, (long)Config.SSQ_DAWN_TICKET_BUNDLE, this, true);
                                    this.getCastle().setTicketBuyCount(ticketCount + 1);
                                    return;
                                 }

                                 html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain_noadena.htm");
                              } else {
                                 html.setFile(player, player.getLang(), "data/html/chamberlain/ssq_notenoughticket.htm");
                              }
                           } else {
                              html.setFile(player, player.getLang(), "data/html/chamberlain/ssq_notdawnorevent.htm");
                           }
                        }
                     } else {
                        html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
                     }

                     html.replace("%objectId%", String.valueOf(this.getObjectId()));
                     player.sendPacket(html);
                  } else {
                     super.onBypassFeedback(player, command);
                  }
               }
            }
         }
      }
   }

   private void sendHtmlMessage(Player player, String htmlMessage) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setHtml(player, htmlMessage);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcname%", this.getName());
      player.sendPacket(html);
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      String filename = "data/html/chamberlain/chamberlain-no.htm";
      int condition = this.validateCondition(player);
      if (condition > 0) {
         if (condition == 1) {
            filename = "data/html/chamberlain/chamberlain-busy.htm";
         } else if (condition == 2) {
            filename = "data/html/chamberlain/chamberlain.htm";
         }
      }

      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcname%", this.getName());
      player.sendPacket(html);
   }

   private void doTeleport(Player player, int val) {
      if (Config.DEBUG) {
         _log.warning("doTeleport(Player player, int val) is called");
      }

      TeleportTemplate list = TeleLocationParser.getInstance().getTemplate(val);
      if (list != null) {
         if (player.destroyItemByItemId("Teleport", list.getId(), (long)list.getPrice(), this, true)) {
            if (Config.DEBUG) {
               _log.warning(
                  "Teleporting player " + player.getName() + " for Castle to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ()
               );
            }

            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
         }
      } else {
         _log.warning("No teleport destination with id:" + val);
      }

      player.sendActionFailed();
   }

   protected int validateCondition(Player player) {
      if (this.getCastle() != null && this.getCastle().getId() > 0 && player.getClan() != null) {
         if (this.getCastle().getZone().isActive()) {
            return 1;
         }

         if (this.getCastle().getOwnerId() == player.getClanId()) {
            return 2;
         }
      }

      return 0;
   }

   private boolean validatePrivileges(Player player, int privilege) {
      if ((player.getClanPrivileges() & privilege) != privilege) {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-noprivs.htm");
         player.sendPacket(html);
         return false;
      } else {
         return true;
      }
   }

   private boolean siegeBlocksFunction(Player player) {
      if (this.getCastle().getSiege().getIsInProgress()) {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), "data/html/chamberlain/chamberlain-busy.htm");
         html.replace("%npcname%", String.valueOf(this.getName()));
         player.sendPacket(html);
         return true;
      } else {
         return false;
      }
   }
}
