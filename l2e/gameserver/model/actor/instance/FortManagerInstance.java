package l2e.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2e.gameserver.network.serverpackets.WareHouseDepositList;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawList;

public class FortManagerInstance extends MerchantInstance {
   protected static final int COND_ALL_FALSE = 0;
   protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
   protected static final int COND_OWNER = 2;

   public FortManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FortManagerInstance);
   }

   @Override
   public boolean isWarehouse() {
      return true;
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
                  if (st.countTokens() >= 1) {
                     val = st.nextToken();
                  }

                  if (actualCommand.equalsIgnoreCase("expel")) {
                     if ((player.getClanPrivileges() & 1048576) == 1048576) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-expel.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        player.sendPacket(html);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-noprivs.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("banish_foreigner")) {
                     if ((player.getClanPrivileges() & 1048576) == 1048576) {
                        this.getFort().banishForeigners();
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-expeled.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        player.sendPacket(html);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-noprivs.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("receive_report")) {
                     if (this.getFort().getFortState() < 2) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-report.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        if (Config.FS_MAX_OWN_TIME > 0) {
                           int hour = (int)Math.floor((double)(this.getFort().getTimeTillRebelArmy() / 3600));
                           int minutes = (int)(Math.floor((double)(this.getFort().getTimeTillRebelArmy() - hour * 3600)) / 60.0);
                           html.replace("%hr%", String.valueOf(hour));
                           html.replace("%min%", String.valueOf(minutes));
                        } else {
                           int hour = (int)Math.floor((double)(this.getFort().getOwnedTime() / 3600));
                           int minutes = (int)(Math.floor((double)(this.getFort().getOwnedTime() - hour * 3600)) / 60.0);
                           html.replace("%hr%", String.valueOf(hour));
                           html.replace("%min%", String.valueOf(minutes));
                        }

                        player.sendPacket(html);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-castlereport.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        if (Config.FS_MAX_OWN_TIME > 0) {
                           int hour = (int)Math.floor((double)(this.getFort().getTimeTillRebelArmy() / 3600));
                           int minutes = (int)(Math.floor((double)(this.getFort().getTimeTillRebelArmy() - hour * 3600)) / 60.0);
                           html.replace("%hr%", String.valueOf(hour));
                           html.replace("%min%", String.valueOf(minutes));
                        } else {
                           int hour = (int)Math.floor((double)(this.getFort().getOwnedTime() / 3600));
                           int minutes = (int)(Math.floor((double)(this.getFort().getOwnedTime() - hour * 3600)) / 60.0);
                           html.replace("%hr%", String.valueOf(hour));
                           html.replace("%min%", String.valueOf(minutes));
                        }

                        int var80 = (int)Math.floor((double)(this.getFort().getTimeTillNextFortUpdate() / 3600L));
                        int var98 = (int)(Math.floor((double)(this.getFort().getTimeTillNextFortUpdate() - (long)(var80 * 3600))) / 60.0);
                        html.replace("%castle%", this.getFort().getContractedCastle().getName());
                        html.replace("%hr2%", String.valueOf(var80));
                        html.replace("%min2%", String.valueOf(var98));
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("operate_door")) {
                     if ((player.getClanPrivileges() & 65536) == 65536) {
                        if (!val.isEmpty()) {
                           boolean open = Integer.parseInt(val) == 1;

                           while(st.hasMoreTokens()) {
                              this.getFort().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
                           }

                           if (open) {
                              NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                              html.setFile(player, player.getLang(), "data/html/fortress/foreman-opened.htm");
                              html.replace("%objectId%", String.valueOf(this.getObjectId()));
                              player.sendPacket(html);
                           } else {
                              NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                              html.setFile(player, player.getLang(), "data/html/fortress/foreman-closed.htm");
                              html.replace("%objectId%", String.valueOf(this.getObjectId()));
                              player.sendPacket(html);
                           }
                        } else {
                           NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                           html.setFile(player, player.getLang(), "data/html/fortress/" + this.getTemplate().getId() + "-d.htm");
                           html.replace("%objectId%", String.valueOf(this.getObjectId()));
                           html.replace("%npcname%", this.getName());
                           player.sendPacket(html);
                        }
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-noprivs.htm");
                        html.replace("%objectId%", String.valueOf(this.getObjectId()));
                        player.sendPacket(html);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("manage_vault")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     if ((player.getClanPrivileges() & 8) == 8) {
                        if (val.equalsIgnoreCase("deposit")) {
                           this.showVaultWindowDeposit(player);
                        } else if (val.equalsIgnoreCase("withdraw")) {
                           if (Config.ENABLE_WAREHOUSESORTING_CLAN) {
                              String htmFile = "data/html/mods/WhSortedC.htm";
                              String htmContent = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/WhSortedC.htm");
                              if (htmContent != null) {
                                 NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(this.getObjectId());
                                 npcHtmlMessage.setHtml(player, htmContent);
                                 npcHtmlMessage.replace("%objectId%", String.valueOf(this.getObjectId()));
                                 player.sendPacket(npcHtmlMessage);
                              } else {
                                 _log.warning("Missing htm: data/html/mods/WhSortedC.htm !");
                              }
                           } else {
                              this.showVaultWindowWithdraw(player, null, (byte)0);
                           }
                        } else {
                           html.setFile(player, player.getLang(), "data/html/fortress/foreman-vault.htm");
                           this.sendHtmlMessage(player, html);
                        }
                     } else {
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-noprivs.htm");
                        this.sendHtmlMessage(player, html);
                     }

                     return;
                  }

                  if (actualCommand.startsWith("WithdrawSortedC")) {
                     String[] param = command.split("_");
                     if (param.length > 2) {
                        this.showVaultWindowWithdraw(
                           player, SortedWareHouseWithdrawalList.WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2])
                        );
                     } else if (param.length > 1) {
                        this.showVaultWindowWithdraw(player, SortedWareHouseWithdrawalList.WarehouseListType.valueOf(param[1]), (byte)1);
                     } else {
                        this.showVaultWindowWithdraw(player, SortedWareHouseWithdrawalList.WarehouseListType.ALL, (byte)1);
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("functions")) {
                     if (val.equalsIgnoreCase("tele")) {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        if (this.getFort().getFunction(1) == null) {
                           html.setFile(player, player.getLang(), "data/html/fortress/foreman-nac.htm");
                        } else {
                           html.setFile(
                              player, player.getLang(), "data/html/fortress/" + this.getId() + "-t" + this.getFort().getFunction(1).getLvl() + ".htm"
                           );
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("support")) {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        if (this.getFort().getFunction(5) == null) {
                           html.setFile(player, player.getLang(), "data/html/fortress/foreman-nac.htm");
                        } else {
                           html.setFile(player, player.getLang(), "data/html/fortress/support" + this.getFort().getFunction(5).getLvl() + ".htm");
                           html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                        }

                        this.sendHtmlMessage(player, html);
                     } else if (val.equalsIgnoreCase("back")) {
                        this.showChatWindow(player);
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-functions.htm");
                        if (this.getFort().getFunction(4) != null) {
                           html.replace("%xp_regen%", String.valueOf(this.getFort().getFunction(4).getLvl()));
                        } else {
                           html.replace("%xp_regen%", "0");
                        }

                        if (this.getFort().getFunction(2) != null) {
                           html.replace("%hp_regen%", String.valueOf(this.getFort().getFunction(2).getLvl()));
                        } else {
                           html.replace("%hp_regen%", "0");
                        }

                        if (this.getFort().getFunction(3) != null) {
                           html.replace("%mp_regen%", String.valueOf(this.getFort().getFunction(3).getLvl()));
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
                              if (this.getFort().getOwnerClan() == null) {
                                 player.sendMessage("This fortress have no owner, you cannot change configuration");
                                 return;
                              }

                              val = st.nextToken();
                              if (val.equalsIgnoreCase("hp_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel.htm");
                                 html.replace("%apply%", "recovery hp 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("mp_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel.htm");
                                 html.replace("%apply%", "recovery mp 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("exp_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel.htm");
                                 html.replace("%apply%", "recovery exp 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("edit_hp")) {
                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-apply.htm");
                                 html.replace("%name%", "(HP Recovery Device)");
                                 int percent = Integer.parseInt(val);
                                 int cost;
                                 switch(percent) {
                                    case 300:
                                       cost = Config.FS_HPREG1_FEE;
                                       break;
                                    default:
                                       cost = Config.FS_HPREG2_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.FS_HPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Provides additional HP recovery for clan members in the fortress.<font color=\"00FFFF\">"
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
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-apply.htm");
                                 html.replace("%name%", "(MP Recovery)");
                                 int percent = Integer.parseInt(val);
                                 int cost;
                                 switch(percent) {
                                    case 40:
                                       cost = Config.FS_MPREG1_FEE;
                                       break;
                                    default:
                                       cost = Config.FS_MPREG2_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.FS_MPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Provides additional MP recovery for clan members in the fortress.<font color=\"00FFFF\">"
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
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-apply.htm");
                                 html.replace("%name%", "(EXP Recovery Device)");
                                 int percent = Integer.parseInt(val);
                                 int cost;
                                 switch(percent) {
                                    case 45:
                                       cost = Config.FS_EXPREG1_FEE;
                                       break;
                                    default:
                                       cost = Config.FS_EXPREG2_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.FS_EXPREG_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Restores the Exp of any clan member who is resurrected in the fortress.<font color=\"00FFFF\">"
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
                                    html.setFile(player, player.getLang(), "data/html/fortress/functions-apply_confirmed.htm");
                                    if (this.getFort().getFunction(2) != null && this.getFort().getFunction(2).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/functions-used.htm");
                                       html.replace("%val%", val + "%");
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int percent = Integer.parseInt(val);
                                    int fee;
                                    switch(percent) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel_confirmed.htm");
                                          break;
                                       case 300:
                                          fee = Config.FS_HPREG1_FEE;
                                          break;
                                       default:
                                          fee = Config.FS_HPREG2_FEE;
                                    }

                                    if (!this.getFort()
                                       .updateFunctions(player, 2, percent, fee, Config.FS_HPREG_FEE_RATIO, this.getFort().getFunction(2) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/low_adena.htm");
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
                                    html.setFile(player, player.getLang(), "data/html/fortress/functions-apply_confirmed.htm");
                                    if (this.getFort().getFunction(3) != null && this.getFort().getFunction(3).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/functions-used.htm");
                                       html.replace("%val%", val + "%");
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int percent = Integer.parseInt(val);
                                    int fee;
                                    switch(percent) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel_confirmed.htm");
                                          break;
                                       case 40:
                                          fee = Config.FS_MPREG1_FEE;
                                          break;
                                       default:
                                          fee = Config.FS_MPREG2_FEE;
                                    }

                                    if (!this.getFort()
                                       .updateFunctions(player, 3, percent, fee, Config.FS_MPREG_FEE_RATIO, this.getFort().getFunction(3) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/low_adena.htm");
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
                                    html.setFile(player, player.getLang(), "data/html/fortress/functions-apply_confirmed.htm");
                                    if (this.getFort().getFunction(4) != null && this.getFort().getFunction(4).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/functions-used.htm");
                                       html.replace("%val%", val + "%");
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int percent = Integer.parseInt(val);
                                    int fee;
                                    switch(percent) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel_confirmed.htm");
                                          break;
                                       case 45:
                                          fee = Config.FS_EXPREG1_FEE;
                                          break;
                                       default:
                                          fee = Config.FS_EXPREG2_FEE;
                                    }

                                    if (!this.getFort()
                                       .updateFunctions(player, 4, percent, fee, Config.FS_EXPREG_FEE_RATIO, this.getFort().getFunction(4) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
                                    }

                                    this.sendHtmlMessage(player, html);
                                 }

                                 return;
                              }
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           html.setFile(player, player.getLang(), "data/html/fortress/edit_recovery.htm");
                           String hp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 400\">400%</a>]";
                           String exp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 45\">45%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";
                           String mp = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 50\">50%</a>]";
                           if (this.getFort().getFunction(2) != null) {
                              html.replace(
                                 "%hp_recovery%",
                                 this.getFort().getFunction(2).getLvl()
                                    + "%</font> (<font color=\"FFAABB\">"
                                    + this.getFort().getFunction(2).getLease()
                                    + "</font>Adena /"
                                    + Config.FS_HPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%hp_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getFort().getFunction(2).getEndTime()))
                              );
                              html.replace(
                                 "%change_hp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 400\">400%</a>]"
                              );
                           } else {
                              html.replace("%hp_recovery%", "none");
                              html.replace("%hp_period%", "none");
                              html.replace(
                                 "%change_hp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 400\">400%</a>]"
                              );
                           }

                           if (this.getFort().getFunction(4) != null) {
                              html.replace(
                                 "%exp_recovery%",
                                 this.getFort().getFunction(4).getLvl()
                                    + "%</font> (<font color=\"FFAABB\">"
                                    + this.getFort().getFunction(4).getLease()
                                    + "</font>Adena /"
                                    + Config.FS_EXPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%exp_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getFort().getFunction(4).getEndTime()))
                              );
                              html.replace(
                                 "%change_exp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 45\">45%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
                              );
                           } else {
                              html.replace("%exp_recovery%", "none");
                              html.replace("%exp_period%", "none");
                              html.replace(
                                 "%change_exp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 45\">45%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]"
                              );
                           }

                           if (this.getFort().getFunction(3) != null) {
                              html.replace(
                                 "%mp_recovery%",
                                 this.getFort().getFunction(3).getLvl()
                                    + "%</font> (<font color=\"FFAABB\">"
                                    + this.getFort().getFunction(3).getLease()
                                    + "</font>Adena /"
                                    + Config.FS_MPREG_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%mp_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getFort().getFunction(3).getEndTime()))
                              );
                              html.replace(
                                 "%change_mp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 50\">50%</a>]"
                              );
                           } else {
                              html.replace("%mp_recovery%", "none");
                              html.replace("%mp_period%", "none");
                              html.replace(
                                 "%change_mp%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 50\">50%</a>]"
                              );
                           }

                           this.sendHtmlMessage(player, html);
                        } else if (val.equalsIgnoreCase("other")) {
                           if (st.countTokens() >= 1) {
                              if (this.getFort().getOwnerClan() == null) {
                                 player.sendMessage("This fortress have no owner, you cannot change configuration");
                                 return;
                              }

                              val = st.nextToken();
                              if (val.equalsIgnoreCase("tele_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel.htm");
                                 html.replace("%apply%", "other tele 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("support_cancel")) {
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel.htm");
                                 html.replace("%apply%", "other support 0");
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("edit_support")) {
                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-apply.htm");
                                 html.replace("%name%", "Insignia (Supplementary Magic)");
                                 int stage = Integer.parseInt(val);
                                 int cost;
                                 switch(stage) {
                                    case 1:
                                       cost = Config.FS_SUPPORT1_FEE;
                                       break;
                                    default:
                                       cost = Config.FS_SUPPORT2_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.FS_SUPPORT_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace("%use%", "Enables the use of supplementary magic.");
                                 html.replace("%apply%", "other support " + String.valueOf(stage));
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              if (val.equalsIgnoreCase("edit_tele")) {
                                 val = st.nextToken();
                                 NpcHtmlMessage html = new NpcHtmlMessage(1);
                                 html.setFile(player, player.getLang(), "data/html/fortress/functions-apply.htm");
                                 html.replace("%name%", "Mirror (Teleportation Device)");
                                 int stage = Integer.parseInt(val);
                                 int cost;
                                 switch(stage) {
                                    case 1:
                                       cost = Config.FS_TELE1_FEE;
                                       break;
                                    default:
                                       cost = Config.FS_TELE2_FEE;
                                 }

                                 html.replace("%cost%", cost + "</font>Adena /" + Config.FS_TELE_FEE_RATIO / 1000L / 60L / 60L / 24L + " Day</font>)");
                                 html.replace(
                                    "%use%",
                                    "Teleports clan members in a fort to the target <font color=\"00FFFF\">Stage "
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
                                    html.setFile(player, player.getLang(), "data/html/fortress/functions-apply_confirmed.htm");
                                    if (this.getFort().getFunction(1) != null && this.getFort().getFunction(1).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/functions-used.htm");
                                       html.replace("%val%", "Stage " + val);
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int lvl = Integer.parseInt(val);
                                    int fee;
                                    switch(lvl) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel_confirmed.htm");
                                          break;
                                       case 1:
                                          fee = Config.FS_TELE1_FEE;
                                          break;
                                       default:
                                          fee = Config.FS_TELE2_FEE;
                                    }

                                    if (!this.getFort().updateFunctions(player, 1, lvl, fee, Config.FS_TELE_FEE_RATIO, this.getFort().getFunction(1) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/low_adena.htm");
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
                                    html.setFile(player, player.getLang(), "data/html/fortress/functions-apply_confirmed.htm");
                                    if (this.getFort().getFunction(5) != null && this.getFort().getFunction(5).getLvl() == Integer.parseInt(val)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/functions-used.htm");
                                       html.replace("%val%", "Stage " + val);
                                       this.sendHtmlMessage(player, html);
                                       return;
                                    }

                                    int lvl = Integer.parseInt(val);
                                    int fee;
                                    switch(lvl) {
                                       case 0:
                                          fee = 0;
                                          html.setFile(player, player.getLang(), "data/html/fortress/functions-cancel_confirmed.htm");
                                          break;
                                       case 1:
                                          fee = Config.FS_SUPPORT1_FEE;
                                          break;
                                       default:
                                          fee = Config.FS_SUPPORT2_FEE;
                                    }

                                    if (!this.getFort()
                                       .updateFunctions(player, 5, lvl, fee, Config.FS_SUPPORT_FEE_RATIO, this.getFort().getFunction(5) == null)) {
                                       html.setFile(player, player.getLang(), "data/html/fortress/low_adena.htm");
                                       this.sendHtmlMessage(player, html);
                                    } else {
                                       this.sendHtmlMessage(player, html);
                                    }
                                 }

                                 return;
                              }
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           html.setFile(player, player.getLang(), "data/html/fortress/edit_other.htm");
                           String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";
                           String support = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]";
                           if (this.getFort().getFunction(1) != null) {
                              html.replace(
                                 "%tele%",
                                 "Stage "
                                    + String.valueOf(this.getFort().getFunction(1).getLvl())
                                    + "</font> (<font color=\"FFAABB\">"
                                    + this.getFort().getFunction(1).getLease()
                                    + "</font>Adena /"
                                    + Config.FS_TELE_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%tele_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getFort().getFunction(1).getEndTime()))
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

                           if (this.getFort().getFunction(5) != null) {
                              html.replace(
                                 "%support%",
                                 "Stage "
                                    + String.valueOf(this.getFort().getFunction(5).getLvl())
                                    + "</font> (<font color=\"FFAABB\">"
                                    + this.getFort().getFunction(5).getLease()
                                    + "</font>Adena /"
                                    + Config.FS_SUPPORT_FEE_RATIO / 1000L / 60L / 60L / 24L
                                    + " Day)"
                              );
                              html.replace(
                                 "%support_period%",
                                 "Withdraw the fee for the next time at " + format.format(Long.valueOf(this.getFort().getFunction(5).getEndTime()))
                              );
                              html.replace(
                                 "%change_support%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Deactivate</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]"
                              );
                           } else {
                              html.replace("%support%", "none");
                              html.replace("%support_period%", "none");
                              html.replace(
                                 "%change_support%",
                                 "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]"
                              );
                           }

                           this.sendHtmlMessage(player, html);
                        } else if (val.equalsIgnoreCase("back")) {
                           this.showChatWindow(player);
                        } else {
                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           html.setFile(player, player.getLang(), "data/html/fortress/manage.htm");
                           this.sendHtmlMessage(player, html);
                        }
                     } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(player, player.getLang(), "data/html/fortress/foreman-noprivs.htm");
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
                           if (this.getFort().getFunction(5) == null) {
                              return;
                           }

                           if (this.getFort().getFunction(5).getLvl() == 0) {
                              return;
                           }

                           NpcHtmlMessage html = new NpcHtmlMessage(1);
                           int skill_lvl = 0;
                           if (st.countTokens() >= 1) {
                              skill_lvl = Integer.parseInt(st.nextToken());
                           }

                           Skill skill = SkillsParser.getInstance().getInfo(skill_id, skill_lvl);
                           if (skill.getSkillType() == SkillType.SUMMON) {
                              player.doCast(skill);
                           } else {
                              if ((double)(skill.getMpConsume() + skill.getMpInitialConsume()) > this.getCurrentMp()) {
                                 html.setFile(player, player.getLang(), "data/html/fortress/support-no_mana.htm");
                                 html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                                 this.sendHtmlMessage(player, html);
                                 return;
                              }

                              this.doCast(skill);
                           }

                           html.setFile(player, player.getLang(), "data/html/fortress/support-done.htm");
                           html.replace("%mp%", String.valueOf((int)this.getCurrentMp()));
                           this.sendHtmlMessage(player, html);
                        } catch (Exception var12) {
                           player.sendMessage("Invalid skill level, contact your admin!");
                        }
                     } catch (Exception var13) {
                        player.sendMessage("Invalid skill level, contact your admin!");
                     }

                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("support_back")) {
                     NpcHtmlMessage html = new NpcHtmlMessage(1);
                     if (this.getFort().getFunction(5).getLvl() == 0) {
                        return;
                     }

                     html.setFile(player, player.getLang(), "data/html/fortress/support" + this.getFort().getFunction(5).getLvl() + ".htm");
                     html.replace("%mp%", String.valueOf((int)this.getStatus().getCurrentMp()));
                     this.sendHtmlMessage(player, html);
                     return;
                  }

                  if (actualCommand.equalsIgnoreCase("goto")) {
                     int whereTo = Integer.parseInt(val);
                     this.doTeleport(player, whereTo);
                     return;
                  }

                  super.onBypassFeedback(player, command);
               }
            }
         }
      }
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      String filename = "data/html/fortress/foreman-no.htm";
      int condition = this.validateCondition(player);
      if (condition > 0) {
         if (condition == 1) {
            filename = "data/html/fortress/foreman-busy.htm";
         } else if (condition == 2) {
            filename = "data/html/fortress/foreman.htm";
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
                  "Teleporting player " + player.getName() + " for Fortress to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ()
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
      if (this.getFort() != null && this.getFort().getId() > 0 && player.getClan() != null) {
         if (this.getFort().getZone().isActive()) {
            return 1;
         }

         if (this.getFort().getOwnerClan() != null && this.getFort().getOwnerClan().getId() == player.getClanId()) {
            return 2;
         }
      }

      return 0;
   }

   private void showVaultWindowDeposit(Player player) {
      player.sendActionFailed();
      player.setActiveWarehouse(player.getClan().getWarehouse());
      player.sendPacket(new WareHouseDepositList(player, 4));
   }

   private void showVaultWindowWithdraw(Player player, SortedWareHouseWithdrawalList.WarehouseListType itemtype, byte sortorder) {
      if (!player.isClanLeader() && (player.getClanPrivileges() & 8) != 8) {
         NpcHtmlMessage html = new NpcHtmlMessage(1);
         html.setFile(player, player.getLang(), "data/html/fortress/foreman-noprivs.htm");
         this.sendHtmlMessage(player, html);
      } else {
         player.sendActionFailed();
         player.setActiveWarehouse(player.getClan().getWarehouse());
         if (itemtype != null) {
            player.sendPacket(new SortedWareHouseWithdrawalList(player, 4, itemtype, sortorder));
         } else {
            player.sendPacket(new WareHouseWithdrawList(player, 4));
         }
      }
   }
}
