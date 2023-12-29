package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SignsPriestInstance extends Npc {
   public SignsPriestInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.SignsPriestInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player.getLastFolkNPC() != null && player.getLastFolkNPC().getObjectId() == this.getObjectId()) {
         if (command.startsWith("SevenSignsDesc")) {
            int val = Integer.parseInt(command.substring(15));
            this.showChatWindow(player, val, null, true);
         } else if (command.startsWith("SevenSigns")) {
            int cabal = 0;
            int stoneType = 0;
            long ancientAdenaAmount = player.getAncientAdena();
            int val = Integer.parseInt(command.substring(11, 12).trim());
            if (command.length() > 12) {
               val = Integer.parseInt(command.substring(11, 13).trim());
            }

            if (command.length() > 13) {
               try {
                  cabal = Integer.parseInt(command.substring(14, 15).trim());
               } catch (Exception var67) {
                  try {
                     cabal = Integer.parseInt(command.substring(13, 14).trim());
                  } catch (Exception var66) {
                     try {
                        StringTokenizer st = new StringTokenizer(command.trim());
                        st.nextToken();
                        cabal = Integer.parseInt(st.nextToken());
                     } catch (Exception var65) {
                        _log.warning("Failed to retrieve cabal from bypass command. NpcId: " + this.getId() + "; Command: " + command);
                     }
                  }
               }
            }

            switch(val) {
               case 2:
                  if (!player.getInventory().validateCapacity(1L)) {
                     player.sendPacket(SystemMessageId.SLOTS_FULL);
                  } else if (!player.reduceAdena("SevenSigns", 500L, this, true)) {
                     player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                  } else {
                     player.getInventory().addItem("SevenSigns", 5707, 1L, player, this);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                     sm.addItemName(5707);
                     player.sendPacket(sm);
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, val, "dawn", false);
                     } else {
                        this.showChatWindow(player, val, "dusk", false);
                     }
                  }
                  break;
               case 3:
               case 8:
                  this.showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
                  break;
               case 4:
                  int newSeal = Integer.parseInt(command.substring(15));
                  if (player.getClassId().level() >= 1) {
                     if (cabal == 1 && Config.ALT_GAME_CASTLE_DUSK && player.getClan() != null && player.getClan().getCastleId() > 0) {
                        this.showChatWindow(player, "data/html/seven_signs/signs_33_dusk_no.htm");
                        return;
                     }

                     if (Config.ALT_GAME_CASTLE_DAWN && cabal == 2) {
                        boolean allowJoinDawn = false;
                        if (player.getClan() != null && player.getClan().getCastleId() > 0) {
                           allowJoinDawn = true;
                        } else if (player.destroyItemByItemId("SevenSigns", Config.SSQ_MANORS_AGREEMENT_ID, 1L, this, true)) {
                           allowJoinDawn = true;
                        } else if (player.reduceAdena("SevenSigns", (long)Config.SSQ_JOIN_DAWN_ADENA_FEE, this, true)) {
                           allowJoinDawn = true;
                        }

                        if (!allowJoinDawn) {
                           this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn_fee.htm");
                           return;
                        }
                     }
                  }

                  SevenSigns.getInstance().setPlayerInfo(player.getObjectId(), cabal, newSeal);
                  if (cabal == 2) {
                     player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DAWN);
                  } else {
                     player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DUSK);
                  }

                  switch(newSeal) {
                     case 1:
                        player.sendPacket(SystemMessageId.FIGHT_FOR_AVARICE);
                        break;
                     case 2:
                        player.sendPacket(SystemMessageId.FIGHT_FOR_GNOSIS);
                        break;
                     case 3:
                        player.sendPacket(SystemMessageId.FIGHT_FOR_STRIFE);
                  }

                  this.showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
                  break;
               case 5:
                  if (this instanceof DawnPriestInstance) {
                     if (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()) == 0) {
                        this.showChatWindow(player, val, "dawn_no", false);
                     } else {
                        this.showChatWindow(player, val, "dawn", false);
                     }
                  } else if (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()) == 0) {
                     this.showChatWindow(player, val, "dusk_no", false);
                  } else {
                     this.showChatWindow(player, val, "dusk", false);
                  }
                  break;
               case 6:
                  stoneType = Integer.parseInt(command.substring(13));
                  ItemInstance blueStones = player.getInventory().getItemByItemId(6360);
                  ItemInstance greenStones = player.getInventory().getItemByItemId(6361);
                  ItemInstance redStones = player.getInventory().getItemByItemId(6362);
                  long blueStoneCount = blueStones == null ? 0L : blueStones.getCount();
                  long greenStoneCount = greenStones == null ? 0L : greenStones.getCount();
                  long redStoneCount = redStones == null ? 0L : redStones.getCount();
                  long contribScore = (long)SevenSigns.getInstance().getPlayerContribScore(player.getObjectId());
                  boolean stonesFound = false;
                  if (contribScore == (long)Config.ALT_MAXIMUM_PLAYER_CONTRIB) {
                     player.sendPacket(SystemMessageId.CONTRIB_SCORE_EXCEEDED);
                  } else {
                     long redContribCount = 0L;
                     long greenContribCount = 0L;
                     long blueContribCount = 0L;
                     String contribStoneColor = null;
                     String stoneColorContr = null;
                     long stoneCountContr = 0L;
                     int stoneIdContr = 0;
                     switch(stoneType) {
                        case 1:
                           contribStoneColor = "Blue";
                           stoneColorContr = "blue";
                           stoneIdContr = 6360;
                           stoneCountContr = blueStoneCount;
                           break;
                        case 2:
                           contribStoneColor = "Green";
                           stoneColorContr = "green";
                           stoneIdContr = 6361;
                           stoneCountContr = greenStoneCount;
                           break;
                        case 3:
                           contribStoneColor = "Red";
                           stoneColorContr = "red";
                           stoneIdContr = 6362;
                           stoneCountContr = redStoneCount;
                           break;
                        case 4:
                           redContribCount = ((long)Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / 10L;
                           if (redContribCount > redStoneCount) {
                              redContribCount = redStoneCount;
                           }

                           long var113 = contribScore + redContribCount * 10L;
                           greenContribCount = ((long)Config.ALT_MAXIMUM_PLAYER_CONTRIB - var113) / 5L;
                           if (greenContribCount > greenStoneCount) {
                              greenContribCount = greenStoneCount;
                           }

                           var113 += greenContribCount * 5L;
                           blueContribCount = ((long)Config.ALT_MAXIMUM_PLAYER_CONTRIB - var113) / 3L;
                           if (blueContribCount > blueStoneCount) {
                              blueContribCount = blueStoneCount;
                           }

                           if (redContribCount > 0L && player.destroyItemByItemId("SevenSigns", 6362, redContribCount, this, false)) {
                              stonesFound = true;
                              SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                              msg.addItemName(6362);
                              msg.addItemNumber(redContribCount);
                              player.sendPacket(msg);
                           }

                           if (greenContribCount > 0L && player.destroyItemByItemId("SevenSigns", 6361, greenContribCount, this, false)) {
                              stonesFound = true;
                              SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                              msg.addItemName(6361);
                              msg.addItemNumber(greenContribCount);
                              player.sendPacket(msg);
                           }

                           if (blueContribCount > 0L && player.destroyItemByItemId("SevenSigns", 6360, blueContribCount, this, false)) {
                              stonesFound = true;
                              SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                              msg.addItemName(6360);
                              msg.addItemNumber(blueContribCount);
                              player.sendPacket(msg);
                           }

                           if (!stonesFound) {
                              if (this instanceof DawnPriestInstance) {
                                 this.showChatWindow(player, val, "dawn_no_stones", false);
                              } else {
                                 this.showChatWindow(player, val, "dusk_no_stones", false);
                              }
                           } else {
                              contribScore = SevenSigns.getInstance()
                                 .addPlayerStoneContrib(player.getObjectId(), blueContribCount, greenContribCount, redContribCount);
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1);
                              sm.addItemNumber(contribScore);
                              player.sendPacket(sm);
                              if (this instanceof DawnPriestInstance) {
                                 this.showChatWindow(player, 6, "dawn", false);
                              } else {
                                 this.showChatWindow(player, 6, "dusk", false);
                              }
                           }

                           return;
                     }

                     String path;
                     if (this instanceof DawnPriestInstance) {
                        path = "data/html/seven_signs/signs_6_dawn_contribute.htm";
                     } else {
                        path = "data/html/seven_signs/signs_6_dusk_contribute.htm";
                     }

                     String contentContr = HtmCache.getInstance().getHtm(player, player.getLang(), path);
                     if (contentContr != null) {
                        contentContr = contentContr.replaceAll("%contribStoneColor%", contribStoneColor);
                        contentContr = contentContr.replaceAll("%stoneColor%", stoneColorContr);
                        contentContr = contentContr.replaceAll("%stoneCount%", String.valueOf(stoneCountContr));
                        contentContr = contentContr.replaceAll("%stoneItemId%", String.valueOf(stoneIdContr));
                        contentContr = contentContr.replaceAll("%objectId%", String.valueOf(this.getObjectId()));
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setHtml(player, contentContr);
                        player.sendPacket(html);
                     } else {
                        _log.warning("Problem with HTML text " + path);
                     }
                  }
                  break;
               case 7:
                  long ancientAdenaConvert = 0L;

                  try {
                     ancientAdenaConvert = Long.parseLong(command.substring(13).trim());
                  } catch (NumberFormatException var68) {
                     this.showChatWindow(player, "data/html/seven_signs/blkmrkt_3.htm");
                     break;
                  } catch (StringIndexOutOfBoundsException var69) {
                     this.showChatWindow(player, "data/html/seven_signs/blkmrkt_3.htm");
                     break;
                  }

                  if (ancientAdenaConvert < 1L) {
                     this.showChatWindow(player, "data/html/seven_signs/blkmrkt_3.htm");
                  } else if (ancientAdenaAmount < ancientAdenaConvert) {
                     this.showChatWindow(player, "data/html/seven_signs/blkmrkt_4.htm");
                  } else {
                     player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
                     player.addAdena("SevenSigns", ancientAdenaConvert, this, true);
                     this.showChatWindow(player, "data/html/seven_signs/blkmrkt_5.htm");
                  }
                  break;
               case 9:
                  int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
                  int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
                  if (SevenSigns.getInstance().isSealValidationPeriod() && playerCabal == winningCabal) {
                     int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player.getObjectId(), true);
                     if (ancientAdenaReward < 3) {
                        if (this instanceof DawnPriestInstance) {
                           this.showChatWindow(player, 9, "dawn_b", false);
                        } else {
                           this.showChatWindow(player, 9, "dusk_b", false);
                        }
                     } else {
                        player.addAncientAdena("SevenSigns", (long)ancientAdenaReward, this, true);
                        if (this instanceof DawnPriestInstance) {
                           this.showChatWindow(player, 9, "dawn_a", false);
                        } else {
                           this.showChatWindow(player, 9, "dusk_a", false);
                        }
                     }
                  }
                  break;
               case 10:
               case 12:
               case 13:
               case 14:
               case 15:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
               case 30:
               case 31:
               case 32:
               default:
                  this.showChatWindow(player, val, null, false);
                  break;
               case 11:
                  try {
                     String portInfo = command.substring(14).trim();
                     StringTokenizer st = new StringTokenizer(portInfo);
                     int x = Integer.parseInt(st.nextToken());
                     int y = Integer.parseInt(st.nextToken());
                     int z = Integer.parseInt(st.nextToken());
                     long ancientAdenaCost = Long.parseLong(st.nextToken());
                     if (ancientAdenaCost <= 0L || player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true)) {
                        player.teleToLocation(x, y, z, true);
                     }
                  } catch (Exception var71) {
                     _log.log(Level.WARNING, "SevenSigns: Error occurred while teleporting player: " + var71.getMessage(), (Throwable)var71);
                  }
                  break;
               case 16:
                  if (this instanceof DawnPriestInstance) {
                     this.showChatWindow(player, val, "dawn", false);
                  } else {
                     this.showChatWindow(player, val, "dusk", false);
                  }
                  break;
               case 17:
                  stoneType = Integer.parseInt(command.substring(14));
                  int stoneId = 0;
                  long stoneCount = 0L;
                  int stoneValue = 0;
                  String stoneColor = null;
                  switch(stoneType) {
                     case 1:
                        stoneColor = "blue";
                        stoneId = 6360;
                        stoneValue = 3;
                        break;
                     case 2:
                        stoneColor = "green";
                        stoneId = 6361;
                        stoneValue = 5;
                        break;
                     case 3:
                        stoneColor = "red";
                        stoneId = 6362;
                        stoneValue = 10;
                        break;
                     case 4:
                        ItemInstance blueStonesAll = player.getInventory().getItemByItemId(6360);
                        ItemInstance greenStonesAll = player.getInventory().getItemByItemId(6361);
                        ItemInstance redStonesAll = player.getInventory().getItemByItemId(6362);
                        long blueStoneCountAll = blueStonesAll == null ? 0L : blueStonesAll.getCount();
                        long greenStoneCountAll = greenStonesAll == null ? 0L : greenStonesAll.getCount();
                        long redStoneCountAll = redStonesAll == null ? 0L : redStonesAll.getCount();
                        long ancientAdenaRewardAll = 0L;
                        ancientAdenaRewardAll = SevenSigns.calcAncientAdenaReward(blueStoneCountAll, greenStoneCountAll, redStoneCountAll);
                        if (ancientAdenaRewardAll == 0L) {
                           if (this instanceof DawnPriestInstance) {
                              this.showChatWindow(player, 18, "dawn_no_stones", false);
                           } else {
                              this.showChatWindow(player, 18, "dusk_no_stones", false);
                           }

                           return;
                        }

                        if (blueStoneCountAll > 0L) {
                           player.destroyItemByItemId("SevenSigns", 6360, blueStoneCountAll, this, true);
                        }

                        if (greenStoneCountAll > 0L) {
                           player.destroyItemByItemId("SevenSigns", 6361, greenStoneCountAll, this, true);
                        }

                        if (redStoneCountAll > 0L) {
                           player.destroyItemByItemId("SevenSigns", 6362, redStoneCountAll, this, true);
                        }

                        player.addAncientAdena("SevenSigns", ancientAdenaRewardAll, this, true);
                        if (this instanceof DawnPriestInstance) {
                           this.showChatWindow(player, 18, "dawn", false);
                        } else {
                           this.showChatWindow(player, 18, "dusk", false);
                        }

                        return;
                  }

                  ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
                  if (stoneInstance != null) {
                     stoneCount = stoneInstance.getCount();
                  }

                  String path;
                  if (this instanceof DawnPriestInstance) {
                     path = "data/html/seven_signs/signs_17_dawn.htm";
                  } else {
                     path = "data/html/seven_signs/signs_17_dusk.htm";
                  }

                  String content = HtmCache.getInstance().getHtm(player, player.getLang(), path);
                  if (content != null) {
                     content = content.replaceAll("%stoneColor%", stoneColor);
                     content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
                     content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
                     content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));
                     content = content.replaceAll("%objectId%", String.valueOf(this.getObjectId()));
                     NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                     html.setHtml(player, content);
                     player.sendPacket(html);
                  } else {
                     _log.warning("Problem with HTML text data/html/seven_signs/signs_17.htm: " + path);
                  }
                  break;
               case 18:
                  int convertStoneId = Integer.parseInt(command.substring(14, 18));
                  long convertCount = 0L;

                  try {
                     convertCount = Long.parseLong(command.substring(19).trim());
                  } catch (Exception var70) {
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, 18, "dawn_failed", false);
                     } else {
                        this.showChatWindow(player, 18, "dusk_failed", false);
                     }
                     break;
                  }

                  ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
                  if (convertItem != null) {
                     long ancientAdenaReward = 0L;
                     long totalCount = convertItem.getCount();
                     if (convertCount <= totalCount && convertCount > 0L) {
                        switch(convertStoneId) {
                           case 6360:
                              ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0L, 0L);
                              break;
                           case 6361:
                              ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0L, convertCount, 0L);
                              break;
                           case 6362:
                              ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0L, 0L, convertCount);
                        }

                        if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true)) {
                           player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
                           if (this instanceof DawnPriestInstance) {
                              this.showChatWindow(player, 18, "dawn", false);
                           } else {
                              this.showChatWindow(player, 18, "dusk", false);
                           }
                        }
                     } else if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, 18, "dawn_low_stones", false);
                     } else {
                        this.showChatWindow(player, 18, "dusk_low_stones", false);
                     }
                  } else if (this instanceof DawnPriestInstance) {
                     this.showChatWindow(player, 18, "dawn_no_stones", false);
                  } else {
                     this.showChatWindow(player, 18, "dusk_no_stones", false);
                  }
                  break;
               case 19:
                  int chosenSeal = Integer.parseInt(command.substring(16));
                  String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
                  this.showChatWindow(player, val, fileSuffix, false);
                  break;
               case 20:
                  StringBuilder contentBuffer = new StringBuilder();
                  if (this instanceof DawnPriestInstance) {
                     contentBuffer.append("<html><body>Priest of Dawn:<br><font color=\"LEVEL\">[ Seal Status ]</font><br>");
                  } else {
                     contentBuffer.append("<html><body>Dusk Priestess:<br><font color=\"LEVEL\">[ Status of the Seals ]</font><br>");
                  }

                  for(int i = 1; i < 4; ++i) {
                     int sealOwner = SevenSigns.getInstance().getSealOwner(i);
                     if (sealOwner != 0) {
                        contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>");
                     } else {
                        contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": Nothingness]<br>");
                     }
                  }

                  contentBuffer.append("<a action=\"bypass -h npc_" + this.getObjectId() + "_Chat 0\">Go back.</a></body></html>");
                  NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                  html.setHtml(player, contentBuffer.toString());
                  player.sendPacket(html);
                  break;
               case 21:
                  int contribStoneId = Integer.parseInt(command.substring(14, 18));
                  ItemInstance contribBlueStones = player.getInventory().getItemByItemId(6360);
                  ItemInstance contribGreenStones = player.getInventory().getItemByItemId(6361);
                  ItemInstance contribRedStones = player.getInventory().getItemByItemId(6362);
                  long contribBlueStoneCount = contribBlueStones == null ? 0L : contribBlueStones.getCount();
                  long contribGreenStoneCount = contribGreenStones == null ? 0L : contribGreenStones.getCount();
                  long contribRedStoneCount = contribRedStones == null ? 0L : contribRedStones.getCount();
                  long score = (long)SevenSigns.getInstance().getPlayerContribScore(player.getObjectId());
                  long contributionCount = 0L;
                  boolean contribStonesFound = false;
                  long redContrib = 0L;
                  long greenContrib = 0L;
                  long blueContrib = 0L;

                  try {
                     contributionCount = Long.parseLong(command.substring(19).trim());
                  } catch (Exception var72) {
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, 6, "dawn_failure", false);
                     } else {
                        this.showChatWindow(player, 6, "dusk_failure", false);
                     }
                     break;
                  }

                  switch(contribStoneId) {
                     case 6360:
                        blueContrib = ((long)Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / 3L;
                        if (blueContrib > contribBlueStoneCount) {
                           blueContrib = contributionCount;
                        }
                        break;
                     case 6361:
                        greenContrib = ((long)Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / 5L;
                        if (greenContrib > contribGreenStoneCount) {
                           greenContrib = contributionCount;
                        }
                        break;
                     case 6362:
                        redContrib = ((long)Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / 10L;
                        if (redContrib > contribRedStoneCount) {
                           redContrib = contributionCount;
                        }
                  }

                  if (redContrib > 0L && player.destroyItemByItemId("SevenSigns", 6362, redContrib, this, false)) {
                     contribStonesFound = true;
                     SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                     msg.addItemName(6362);
                     msg.addItemNumber(redContrib);
                     player.sendPacket(msg);
                  }

                  if (greenContrib > 0L && player.destroyItemByItemId("SevenSigns", 6361, greenContrib, this, false)) {
                     contribStonesFound = true;
                     SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                     msg.addItemName(6361);
                     msg.addItemNumber(greenContrib);
                     player.sendPacket(msg);
                  }

                  if (blueContrib > 0L && player.destroyItemByItemId("SevenSigns", 6360, blueContrib, this, false)) {
                     contribStonesFound = true;
                     SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                     msg.addItemName(6360);
                     msg.addItemNumber(blueContrib);
                     player.sendPacket(msg);
                  }

                  if (!contribStonesFound) {
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, 6, "dawn_low_stones", false);
                     } else {
                        this.showChatWindow(player, 6, "dusk_low_stones", false);
                     }
                  } else {
                     score = SevenSigns.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContrib, greenContrib, redContrib);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1);
                     sm.addItemNumber(score);
                     player.sendPacket(sm);
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, 6, "dawn", false);
                     } else {
                        this.showChatWindow(player, 6, "dusk", false);
                     }
                  }
                  break;
               case 33:
                  int oldCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
                  if (oldCabal != 0) {
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, val, "dawn_member", false);
                     } else {
                        this.showChatWindow(player, val, "dusk_member", false);
                     }

                     return;
                  }

                  if (player.getClassId().level() == 0) {
                     if (this instanceof DawnPriestInstance) {
                        this.showChatWindow(player, val, "dawn_firstclass", false);
                     } else {
                        this.showChatWindow(player, val, "dusk_firstclass", false);
                     }

                     return;
                  }

                  if (cabal == 1 && Config.ALT_GAME_CASTLE_DUSK) {
                     if (player.getClan() != null && player.getClan().getCastleId() > 0) {
                        this.showChatWindow(player, "data/html/seven_signs/signs_33_dusk_no.htm");
                        break;
                     }
                  } else if (cabal == 2 && Config.ALT_GAME_CASTLE_DAWN && (player.getClan() == null || player.getClan().getCastleId() == 0)) {
                     this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn_fee.htm");
                     break;
                  }

                  if (this instanceof DawnPriestInstance) {
                     this.showChatWindow(player, val, "dawn", false);
                  } else {
                     this.showChatWindow(player, val, "dusk", false);
                  }
                  break;
               case 34:
                  if (player.getClassId().level() <= 0
                     || player.getAdena() < (long)Config.SSQ_JOIN_DAWN_ADENA_FEE
                        && player.getInventory().getInventoryItemCount(Config.SSQ_MANORS_AGREEMENT_ID, -1) <= 0L) {
                     this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn_no.htm");
                  } else {
                     this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn.htm");
                  }
            }
         } else {
            super.onBypassFeedback(player, command);
         }
      }
   }

   private void showChatWindow(Player player, int val, String suffix, boolean isDescription) {
      String filename = "data/html/seven_signs/";
      filename = filename + (isDescription ? "desc_" + val : "signs_" + val);
      filename = filename + (suffix != null ? "_" + suffix + ".htm" : ".htm");
      this.showChatWindow(player, filename);
   }
}
