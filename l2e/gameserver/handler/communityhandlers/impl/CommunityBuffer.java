package l2e.gameserver.handler.communityhandlers.impl;

import gnu.trove.list.array.TIntArrayList;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Rnd;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharSchemesHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.actor.listener.AskQuestionAnswerListener;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.service.buffer.PlayerScheme;
import l2e.gameserver.model.service.buffer.SchemeBuff;
import l2e.gameserver.model.service.buffer.SingleBuff;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CommunityBuffer extends AbstractCommunity implements ICommunityBoardHandler {
   private static CommunityBuffer _instance = new CommunityBuffer();
   private static final char[] FINE_CHARS = new char[]{
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      '0',
      'q',
      'w',
      'e',
      'r',
      't',
      'y',
      'u',
      'i',
      'o',
      'p',
      'a',
      's',
      'd',
      'f',
      'g',
      'h',
      'j',
      'k',
      'l',
      'z',
      'x',
      'c',
      'v',
      'b',
      'n',
      'm',
      'Q',
      'W',
      'E',
      'R',
      'T',
      'Y',
      'U',
      'I',
      'O',
      'P',
      'A',
      'S',
      'D',
      'F',
      'G',
      'H',
      'J',
      'K',
      'L',
      'Z',
      'X',
      'C',
      'V',
      'B',
      'N',
      'M',
      ' '
   };
   private static final String[] SCHEME_ICONS = new String[]{
      "Icon.skill1331",
      "Icon.skill1332",
      "Icon.skill1316",
      "Icon.skill1264",
      "Icon.skill1254",
      "Icon.skill1178",
      "Icon.skill1085",
      "Icon.skill957",
      "Icon.skill0928",
      "Icon.skill0793",
      "Icon.skill0787",
      "Icon.skill0490",
      "Icon.skill0487",
      "Icon.skill0452",
      "Icon.skill0453",
      "Icon.skill0440",
      "Icon.skill0409",
      "Icon.skill0405",
      "Icon.skill0061",
      "Icon.skill0072",
      "Icon.skill0219",
      "Icon.skill0208",
      "Icon.skill0210",
      "Icon.skill0254",
      "Icon.skill0228",
      "Icon.skill0222",
      "Icon.skill0181",
      "Icon.skill0078",
      "Icon.skill0091",
      "Icon.skill0076",
      "Icon.skill0025",
      "Icon.skill0018",
      "Icon.skill0019",
      "Icon.skill0007",
      "Icon.skill1391",
      "Icon.skill1373",
      "Icon.skill1388",
      "Icon.skill1409",
      "Icon.skill1457",
      "Icon.skill1501",
      "Icon.skill1520",
      "Icon.skill1506",
      "Icon.skill1527",
      "Icon.skill5016",
      "Icon.skill5860",
      "Icon.skill5661",
      "Icon.skill6302",
      "Icon.skill6171",
      "Icon.skill6286",
      "Icon.skill4106",
      "Icon.skill4270_3"
   };
   private static List<SingleBuff> _allSingleBuffs = null;
   private final Map<Integer, ArrayList<SingleBuff>> _buffs = new HashMap<>();
   private final Map<Integer, List<Integer>> _classes = new HashMap<>();

   public static CommunityBuffer getInstance() {
      if (_instance == null) {
         _instance = new CommunityBuffer();
      }

      return _instance;
   }

   public CommunityBuffer() {
      this.load();
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   protected void load() {
      this._buffs.clear();
      _allSingleBuffs = new LinkedList<>();

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT, "data/stats/services/communityBuffer.xml");
         if (!file.exists()) {
            _log.warning(this.getClass().getSimpleName() + ": Couldn't find data/stats/services/" + file.getName());
            return;
         }

         Document doc = factory.newDocumentBuilder().parse(file);

         for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling()) {
            if ("list".equalsIgnoreCase(list.getNodeName())) {
               for(Node groups = list.getFirstChild(); groups != null; groups = groups.getNextSibling()) {
                  if ("skill".equalsIgnoreCase(groups.getNodeName())) {
                     int skillId = Integer.parseInt(groups.getAttributes().getNamedItem("id").getNodeValue());
                     int skillLvl = Integer.parseInt(groups.getAttributes().getNamedItem("level").getNodeValue());
                     int premiumLvl = groups.getAttributes().getNamedItem("premiumLvl") != null
                        ? Integer.parseInt(groups.getAttributes().getNamedItem("premiumLvl").getNodeValue())
                        : skillLvl;
                     int buffTime = groups.getAttributes().getNamedItem("buffTime") != null
                        ? Integer.parseInt(groups.getAttributes().getNamedItem("buffTime").getNodeValue())
                        : 0;
                     int premiumBuffTime = groups.getAttributes().getNamedItem("premiumBuffTime") != null
                        ? Integer.parseInt(groups.getAttributes().getNamedItem("premiumBuffTime").getNodeValue())
                        : buffTime;
                     String type = groups.getAttributes().getNamedItem("type").getNodeValue();
                     boolean isDanceSlot = groups.getAttributes().getNamedItem("isDanceSlot") != null
                        ? Boolean.parseBoolean(groups.getAttributes().getNamedItem("isDanceSlot").getNodeValue())
                        : false;
                     int[][] requestItems = groups.getAttributes().getNamedItem("requestItems") != null
                        ? this.parseItemsList(groups.getAttributes().getNamedItem("requestItems").getNodeValue())
                        : (int[][])null;
                     boolean needAllItems = groups.getAttributes().getNamedItem("needAllItems") != null
                        ? Boolean.parseBoolean(groups.getAttributes().getNamedItem("needAllItems").getNodeValue())
                        : false;
                     boolean removeItems = groups.getAttributes().getNamedItem("removeItems") != null
                        ? Boolean.parseBoolean(groups.getAttributes().getNamedItem("removeItems").getNodeValue())
                        : false;
                     _allSingleBuffs.add(
                        new SingleBuff(type, skillId, skillLvl, premiumLvl, buffTime, premiumBuffTime, isDanceSlot, requestItems, needAllItems, removeItems)
                     );
                  } else if ("groups".equalsIgnoreCase(groups.getNodeName())) {
                     NamedNodeMap attrs = groups.getAttributes();
                     int groupId = Integer.parseInt(attrs.getNamedItem("groupId").getNodeValue());
                     ArrayList<SingleBuff> groupSkills = new ArrayList<>();
                     List<Integer> classes = new ArrayList<>();

                     for(Node skills = groups.getFirstChild(); skills != null; skills = skills.getNextSibling()) {
                        if ("class".equalsIgnoreCase(skills.getNodeName())) {
                           attrs = skills.getAttributes();
                           int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                           classes.add(id);
                        } else if ("skill".equalsIgnoreCase(skills.getNodeName())) {
                           attrs = skills.getAttributes();
                           int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());

                           for(SingleBuff singleBuff : _allSingleBuffs) {
                              if (singleBuff != null && singleBuff.getSkillId() == id) {
                                 groupSkills.add(singleBuff);
                              }
                           }
                        }
                     }

                     this._classes.put(groupId, classes);
                     this._buffs.put(groupId, groupSkills);
                  }
               }
            }
         }
      } catch (Exception var19) {
         _log.warning(this.getClass().getSimpleName() + ": Error while loading buffs: " + var19);
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsbuffer", "_bbsbufferbypass", "_bbsbuffs", "_bbsbuffpage"};
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      StringTokenizer st = new StringTokenizer(command, "_");
      String cmd = st.nextToken();
      if (checkCondition(player, true, false)) {
         if (player.isInSiege() && !Config.ALLOW_COMMUNITY_BUFF_IN_SIEGE) {
            player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
         } else {
            if (Config.ALLOW_BUFF_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE) && !player.isInFightEvent()) {
               boolean canUse = false;
               if (Config.ALLOW_BUFF_WITHOUT_PEACE_FOR_PREMIUM && player.hasPremiumBonus()) {
                  canUse = true;
               }

               if (!canUse) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                  return;
               }
            }

            if ("bbsbuffer".equals(cmd)) {
               this.showWindow(player);
            } else if ("bbsbuffs".equals(cmd)) {
               this.castSkill(command.substring("_bbsbuffs_".length()), player);
            } else if ("bbsbuffpage".equals(cmd)) {
               this.generatePage(command.substring("_bbsbuffpage_".length()), player);
            } else if ("bbsbufferbypass".equals(cmd)) {
               this.onBypass(command.substring("_bbsbufferbypass_".length()), player);
            }
         }
      }
   }

   private void generatePage(String command, Player player) {
      String[] eventSplit = command.split(":");
      if (eventSplit.length >= 2) {
         String eventParam0 = eventSplit[0];
         String eventParam1 = eventSplit[1];
         this.setPetBuff(player, eventParam0);
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/" + eventParam1 + ".htm");
         html = html.replace("%schemePart%", generateScheme(player));
         separateAndSend(html, player);
      }
   }

   private void castSkill(String command, Player player) {
      String[] eventSplit = command.split(" ");
      if (eventSplit.length >= 5) {
         String eventParam0 = eventSplit[0];
         String eventParam1 = eventSplit[1];
         String eventParam2 = eventSplit[2];
         String eventParam3 = eventSplit[3];
         String eventParam4 = eventSplit[4];
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/" + eventParam4 + ".htm");
         html = html.replace("%schemePart%", generateScheme(player));
         if (eventParam0.equalsIgnoreCase("skill")) {
            if (!isValidSkill(player, Integer.parseInt(eventParam1), Integer.parseInt(eventParam2))) {
               Util.handleIllegalPlayerAction(player, "" + player.getName() + " try to cheat with Community Buffer!");
               return;
            }

            if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  separateAndSend(html, player);
                  return;
               }

               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)Config.BUFF_AMOUNT) {
                  ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                  message.add(Config.BUFF_AMOUNT);
                  message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                  this.sendErrorMessageToPlayer(player, message.toString());
                  separateAndSend(html, player);
                  return;
               }

               player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)Config.BUFF_AMOUNT, player, true);
            }

            if (player.isBlocked()) {
               return;
            }

            if (isItemRemoveSkill(player, Integer.parseInt(eventParam1)) && !checkItemsForSkill(player, Integer.parseInt(eventParam1), true)) {
               separateAndSend(html, player);
               return;
            }

            boolean getpetbuff = Integer.parseInt(eventParam3) == 1;
            if (!getpetbuff) {
               Skill skill = SkillsParser.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2));
               if (skill != null) {
                  int buffTime = getBuffTime(player, skill.getId());
                  if (buffTime > 0 && skill.hasEffects()) {
                     Env env = new Env();
                     env.setCharacter(player);
                     env.setTarget(player);
                     env.setSkill(skill);

                     for(EffectTemplate et : skill.getEffectTemplates()) {
                        Effect ef = et.getEffect(env);
                        if (ef != null) {
                           ef.setAbnormalTime(buffTime * 60);
                           ef.scheduleEffect(true);
                        }
                     }
                  } else {
                     skill.getEffects(player, player, false);
                  }

                  player.broadcastPacket(new MagicSkillUse(player, player, Integer.parseInt(eventParam1), Integer.parseInt(eventParam2), 2, 0));
               }
            } else {
               if (!player.hasSummon()) {
                  this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NOT_HAVE_SERVITOR", player.getLang()).toString());
                  separateAndSend(html, player);
                  return;
               }

               Skill skill = SkillsParser.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2));
               if (skill != null) {
                  int buffTime = getBuffTime(player, skill.getId());
                  if (buffTime > 0 && skill.hasEffects()) {
                     Env env = new Env();
                     env.setCharacter(player);
                     env.setTarget(player.getSummon());
                     env.setSkill(skill);

                     for(EffectTemplate et : skill.getEffectTemplates()) {
                        Effect ef = et.getEffect(env);
                        if (ef != null) {
                           ef.setAbnormalTime(buffTime * 60);
                           ef.scheduleEffect(true);
                        }
                     }
                  } else {
                     skill.getEffects(player, player.getSummon(), false);
                  }

                  player.broadcastPacket(new MagicSkillUse(player, player.getSummon(), Integer.parseInt(eventParam1), Integer.parseInt(eventParam2), 0, 0));
               }
            }

            separateAndSend(html, player);
         }
      }
   }

   private void onBypass(String command, Player player) {
      String msg = null;
      String[] eventSplit = command.split(" ");
      if (eventSplit.length >= 4) {
         String eventParam0 = eventSplit[0];
         String eventParam1 = eventSplit[1];
         String eventParam2 = eventSplit[2];
         String eventParam3 = eventSplit[3];
         if (!eventParam0.equals("heal") && !this.canHeal(player) && !player.containsQuickVar("BackHpOn")) {
            player.addQuickVar("BackHpOn", true);
            Playable target = (Playable)(isPetBuff(player) ? player.getSummon() : player);
            if (!isPetBuff(player)) {
               ThreadPoolManager.getInstance()
                  .schedule(new CommunityBuffer.BackHp(target, target.getCurrentHp(), target.getCurrentMp(), target.getCurrentCp()), 250L);
            }

            if (player.hasSummon()) {
               ThreadPoolManager.getInstance()
                  .schedule(new CommunityBuffer.BackHp(player.getSummon(), target.getCurrentHp(), target.getCurrentMp(), target.getCurrentCp()), 250L);
            }
         }

         if (eventParam0.equalsIgnoreCase("buffpet")) {
            this.setPetBuff(player, eventParam1);
            msg = main(player);
         } else if (eventParam0.equals("redirect")) {
            if (eventParam1.equals("main")) {
               msg = main(player);
            } else if (eventParam1.equals("view_buffs")) {
               msg = this.buildHtml("buff", player);
            } else if (eventParam1.equals("view_resists")) {
               msg = this.buildHtml("resist", player);
            } else if (eventParam1.equals("view_songs")) {
               msg = this.buildHtml("song", player);
            } else if (eventParam1.equals("view_dances")) {
               msg = this.buildHtml("dance", player);
            } else if (eventParam1.equals("view_chants")) {
               msg = this.buildHtml("chant", player);
            } else if (eventParam1.equals("view_others")) {
               msg = this.buildHtml("others", player);
            } else if (eventParam1.equals("view_special")) {
               msg = this.buildHtml("special", player);
            }
         } else if (eventParam0.equalsIgnoreCase("giveBuffs")) {
            if (!isValidSkill(player, Integer.parseInt(eventParam1), Integer.parseInt(eventParam2))) {
               Util.handleIllegalPlayerAction(player, "" + player.getName() + " try to cheat with Community Buffer!");
               return;
            }

            if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  this.showCommunity(player, main(player));
                  return;
               }

               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)Config.BUFF_AMOUNT) {
                  ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                  message.add(Config.BUFF_AMOUNT);
                  message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                  this.sendErrorMessageToPlayer(player, message.toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)Config.BUFF_AMOUNT, player, true);
            }

            if (player.isBlocked()) {
               return;
            }

            if (isItemRemoveSkill(player, Integer.parseInt(eventParam1)) && !checkItemsForSkill(player, Integer.parseInt(eventParam1), true)) {
               this.showCommunity(player, main(player));
               return;
            }

            boolean getpetbuff = isPetBuff(player);
            if (!getpetbuff) {
               Skill skill = SkillsParser.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2));
               if (skill != null) {
                  int buffTime = getBuffTime(player, skill.getId());
                  if (buffTime > 0 && skill.hasEffects()) {
                     Env env = new Env();
                     env.setCharacter(player);
                     env.setTarget(player);
                     env.setSkill(skill);

                     for(EffectTemplate et : skill.getEffectTemplates()) {
                        Effect ef = et.getEffect(env);
                        if (ef != null) {
                           ef.setAbnormalTime(buffTime * 60);
                           ef.scheduleEffect(true);
                        }
                     }
                  } else {
                     skill.getEffects(player, player, false);
                  }

                  player.broadcastPacket(new MagicSkillUse(player, player, Integer.parseInt(eventParam1), Integer.parseInt(eventParam2), 2, 0));
               }
            } else {
               if (!player.hasSummon()) {
                  this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NOT_HAVE_SERVITOR", player.getLang()).toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               Skill skill = SkillsParser.getInstance().getInfo(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2));
               if (skill != null) {
                  int buffTime = getBuffTime(player, skill.getId());
                  if (buffTime > 0 && skill.hasEffects()) {
                     Env env = new Env();
                     env.setCharacter(player);
                     env.setTarget(player.getSummon());
                     env.setSkill(skill);

                     for(EffectTemplate et : skill.getEffectTemplates()) {
                        Effect ef = et.getEffect(env);
                        if (ef != null) {
                           ef.setAbnormalTime(buffTime * 60);
                           ef.scheduleEffect(true);
                        }
                     }
                  } else {
                     skill.getEffects(player, player.getSummon(), false);
                  }

                  player.broadcastPacket(new MagicSkillUse(player, player.getSummon(), Integer.parseInt(eventParam1), Integer.parseInt(eventParam2), 0, 0));
               }
            }

            msg = this.buildHtml(eventParam3, player);
         } else if (eventParam0.equalsIgnoreCase("heal")) {
            if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  this.showCommunity(player, main(player));
                  return;
               }

               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)Config.HPMPCP_BUFF_AMOUNT) {
                  ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                  message.add(Config.HPMPCP_BUFF_AMOUNT);
                  message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                  this.sendErrorMessageToPlayer(player, message.toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)Config.HPMPCP_BUFF_AMOUNT, player, true);
            }

            if (!this.canHeal(player)) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.CANT_HEAL", player.getLang()).toString());
               this.showCommunity(player, main(player));
               return;
            }

            boolean getpetbuff = isPetBuff(player);
            if (getpetbuff) {
               if (!player.hasSummon()) {
                  this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NOT_HAVE_SERVITOR", player.getLang()).toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               this.heal(player, getpetbuff);
            } else {
               this.heal(player, getpetbuff);
            }

            msg = main(player);
         } else if (eventParam0.equalsIgnoreCase("removeBuffs")) {
            if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  this.showCommunity(player, main(player));
                  return;
               }

               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)Config.CANCEL_BUFF_AMOUNT) {
                  ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                  message.add(Config.CANCEL_BUFF_AMOUNT);
                  message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                  this.sendErrorMessageToPlayer(player, message.toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)Config.CANCEL_BUFF_AMOUNT, player, true);
            }

            boolean getpetbuff = isPetBuff(player);
            if (getpetbuff) {
               if (!player.hasSummon()) {
                  this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NOT_HAVE_SERVITOR", player.getLang()).toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               player.getSummon().stopAllEffects();
            } else {
               player.stopAllEffects();
               if (player.getCubics() != null) {
                  for(CubicInstance cubic : player.getCubics().values()) {
                     cubic.stopAction();
                     cubic.cancelDisappear();
                     player.getCubics().remove(cubic.getId());
                  }
               }
            }

            msg = main(player);
         } else if (eventParam0.equalsIgnoreCase("cast")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            int schemeId = Integer.parseInt(eventParam1);
            if (player.getBuffSchemeById(schemeId) == null || player.getBuffSchemeById(schemeId).getBuffs() == null) {
               player.sendMessage(new ServerMessage("CommunityBuffer.NEED_CREATE_SCHEME", player.getLang()).toString());
               return;
            }

            TIntArrayList buffs = new TIntArrayList();
            TIntArrayList levels = new TIntArrayList();

            for(SchemeBuff buff : player.getBuffSchemeById(schemeId).getBuffs()) {
               int id = buff.getSkillId();
               int level = player.hasPremiumBonus() ? buff.getPremiumLevel() : buff.getLevel();
               if (!isValidSkill(player, id, level)) {
                  Util.handleIllegalPlayerAction(player, "" + player.getName() + " try to cheat with Community Buffer!");
                  return;
               }

               String var162 = this.getBuffType(player, id);
               switch(var162) {
                  case "buff":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "resist":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "song":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "dance":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "chant":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "others":
                     buffs.add(id);
                     levels.add(level);
                     break;
                  case "special":
                     buffs.add(id);
                     levels.add(level);
               }
            }

            boolean getpetbuff = isPetBuff(player);
            if (player.isBlocked()) {
               return;
            }

            if (buffs.size() == 0) {
               msg = this.viewAllSchemeBuffs(player, eventParam1, "0");
            } else {
               if (getpetbuff && !player.hasSummon()) {
                  this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NOT_HAVE_SERVITOR", player.getLang()).toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
                  int price = buffs.size() * Config.BUFF_AMOUNT;
                  if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     this.showCommunity(player, main(player));
                     return;
                  }

                  if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)price) {
                     ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                     message.add(price);
                     message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                     this.sendErrorMessageToPlayer(player, message.toString());
                     this.showCommunity(player, main(player));
                     return;
                  }

                  player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)price, player, true);
               }

               for(int i = 0; i < buffs.size(); ++i) {
                  if (!isItemRemoveSkill(player, buffs.get(i)) || checkItemsForSkill(player, buffs.get(i), false)) {
                     if (!getpetbuff) {
                        Skill skill = SkillsParser.getInstance().getInfo(buffs.get(i), levels.get(i));
                        if (skill != null) {
                           int buffTime = getBuffTime(player, skill.getId());
                           if (buffTime > 0 && skill.hasEffects()) {
                              Env env = new Env();
                              env.setCharacter(player);
                              env.setTarget(player);
                              env.setSkill(skill);

                              for(EffectTemplate et : skill.getEffectTemplates()) {
                                 Effect ef = et.getEffect(env);
                                 if (ef != null) {
                                    ef.setAbnormalTime(buffTime * 60);
                                    ef.scheduleEffect(true);
                                 }
                              }
                           } else {
                              skill.getEffects(player, player, false);
                           }
                        }
                     } else if (player.hasSummon()) {
                        Skill skill = SkillsParser.getInstance().getInfo(buffs.get(i), levels.get(i));
                        if (skill != null) {
                           int buffTime = getBuffTime(player, skill.getId());
                           if (buffTime > 0 && skill.hasEffects()) {
                              Env env = new Env();
                              env.setCharacter(player);
                              env.setTarget(player.getSummon());
                              env.setSkill(skill);

                              for(EffectTemplate et : skill.getEffectTemplates()) {
                                 Effect ef = et.getEffect(env);
                                 if (ef != null) {
                                    ef.setAbnormalTime(buffTime * 60);
                                    ef.scheduleEffect(true);
                                 }
                              }
                           } else {
                              skill.getEffects(player, player.getSummon(), false);
                           }
                        }
                     }
                  }
               }
            }

            msg = main(player);
         } else if (eventParam0.equalsIgnoreCase("manage_scheme_1")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            msg = this.viewAllSchemeBuffs(player, eventParam1, eventParam2);
         } else if (eventParam0.equalsIgnoreCase("remove_buff")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            String[] split = eventParam1.split("_");
            String scheme = split[0];
            String skill = split[1];
            String level = split[2];
            CharSchemesHolder.getInstance().removeBuff(scheme, skill, level);
            int skillId = Integer.parseInt(skill);

            for(SchemeBuff buff : player.getBuffSchemeById(Integer.parseInt(scheme)).getBuffs()) {
               if (buff.getSkillId() == skillId) {
                  player.getBuffSchemeById(Integer.parseInt(scheme)).getBuffs().remove(buff);
                  break;
               }
            }

            msg = this.viewAllSchemeBuffs(player, scheme, eventParam2);
         } else if (eventParam0.equalsIgnoreCase("add_buff")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            String[] split = eventParam1.split("_");
            String scheme = split[0];
            String skill = split[1];
            String level = split[2];
            String premiumLvl = split[3];
            boolean isDanceSlot = this.isDanceSlotBuff(player, Integer.parseInt(skill));
            if (!isValidSkill(player, Integer.parseInt(skill), Integer.parseInt(level))) {
               _log.warning("Player " + player.getName() + " try to cheat with Community Buffer bypass!");
               Util.handleIllegalPlayerAction(player, "" + player.getName() + " try to cheat with Community Buffer!");
               return;
            }

            CharSchemesHolder.getInstance().addBuff(scheme, skill, level, premiumLvl, isDanceSlot);
            player.getBuffSchemeById(Integer.parseInt(scheme))
               .getBuffs()
               .add(new SchemeBuff(Integer.parseInt(skill), Integer.parseInt(level), Integer.parseInt(premiumLvl), isDanceSlot));
            msg = this.viewAllSchemeBuffs(player, scheme, eventParam2);
         } else if (eventParam0.equalsIgnoreCase("create")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            String name = this.getCorrectName(eventParam2 + (eventParam3.equalsIgnoreCase("x") ? "" : " " + eventParam3));
            if (name.isEmpty() || name.equals("no_name")) {
               player.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ENTER_NAME", player.getLang()).toString());
               return;
            }

            boolean printMain = false;
            int iconId = 0;

            try {
               iconId = Integer.parseInt(eventParam1);
               if (iconId == -1) {
                  printMain = true;
                  iconId = Rnd.get(SCHEME_ICONS.length - 1);
               }

               if (iconId < 0 || iconId > SCHEME_ICONS.length - 1) {
                  throw new Exception();
               }
            } catch (Exception var80) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.WRONG_ICON", player.getLang()).toString());
               this.showCommunity(player, main(player));
               return;
            }

            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("INSERT INTO character_scheme_list (charId,scheme_name,icon) VALUES (?,?,?)", 1);
            ) {
               statement.setInt(1, player.getObjectId());
               statement.setString(2, name);
               statement.setInt(3, iconId);
               statement.executeUpdate();

               try (ResultSet rset = statement.getGeneratedKeys()) {
                  if (rset.next()) {
                     int id = rset.getInt(1);
                     player.getBuffSchemes().add(new PlayerScheme(id, name, iconId));
                     this.addAllBuffsToScheme(player, id);
                     if (!printMain) {
                        msg = this.getOptionList(player, id);
                     } else {
                        msg = main(player);
                     }
                  } else {
                     _log.log(Level.WARNING, "Couldn't get Generated Key while creating scheme!");
                  }
               }
            } catch (SQLException var79) {
               _log.log(Level.WARNING, "Error while inserting Scheme List", (Throwable)var79);
               msg = main(player);
            }
         } else if (eventParam0.equalsIgnoreCase("delete")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            int schemeId = Integer.parseInt(eventParam1);
            PlayerScheme scheme = player.getBuffSchemeById(schemeId);
            if (scheme == null) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.INVALID_SCHEME", player.getLang()).toString());
               this.showCommunity(player, main(player));
               return;
            }

            this.askQuestion(player, schemeId, scheme.getName());
            msg = main(player);
         } else if (eventParam0.equalsIgnoreCase("create_1")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            if (player.getBuffSchemes().size() >= Config.BUFF_MAX_SCHEMES) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.MAX_SCHEMES", player.getLang()).toString());
               this.showCommunity(player, main(player));
               return;
            }

            msg = this.createScheme(player, Integer.parseInt(eventParam1));
         } else if (eventParam0.equalsIgnoreCase("edit_1")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            msg = this.getEditSchemePage(player);
         } else if (eventParam0.equalsIgnoreCase("delete_1")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            msg = this.getDeleteSchemePage(player);
         } else if (eventParam0.equalsIgnoreCase("manage_scheme_select")) {
            if (Config.ALLOW_SCHEMES_FOR_PREMIUMS && !player.hasPremiumBonus()) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ONLY_FOR_PREMIUM", player.getLang()).toString());
               return;
            }

            msg = this.getOptionList(player, Integer.parseInt(eventParam1));
         } else if (eventParam0.equalsIgnoreCase("giveBuffSet")) {
            if (player.isBlocked()) {
               return;
            }

            if (!Config.FREE_ALL_BUFFS && player.getLevel() > Config.COMMUNITY_FREE_BUFF_LVL) {
               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  this.showCommunity(player, main(player));
                  return;
               }

               if (player.getInventory().getItemByItemId(Config.BUFF_ID_ITEM).getCount() < (long)Config.SET_BUFF_AMOUNT) {
                  ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                  message.add(Config.SET_BUFF_AMOUNT);
                  message.add(Util.getItemName(player, Config.BUFF_ID_ITEM));
                  this.sendErrorMessageToPlayer(player, message.toString());
                  this.showCommunity(player, main(player));
                  return;
               }

               player.destroyItemByItemId("SchemeBuffer", Config.BUFF_ID_ITEM, (long)Config.SET_BUFF_AMOUNT, player, true);
            }

            new ArrayList();
            ArrayList var89;
            switch(eventParam1) {
               case "mage":
                  var89 = this._buffs.get(2);
                  break;
               case "dagger":
                  var89 = this._buffs.get(5);
                  break;
               case "support":
                  var89 = this._buffs.get(3);
                  break;
               case "tank":
                  var89 = this._buffs.get(4);
                  break;
               case "archer":
                  var89 = this._buffs.get(6);
                  break;
               case "fighter":
               default:
                  var89 = this._buffs.get(1);
            }

            boolean getpetbuff = isPetBuff(player);
            if (var89 != null && !var89.isEmpty()) {
               if (!getpetbuff) {
                  if (BotFunctions.getInstance().isAutoBuffEnable(player)) {
                     BotFunctions.getInstance().getAutoBuffSet(player);
                     return;
                  }

                  for(SingleBuff singleBuff : var89) {
                     int skillLvl = player.hasPremiumBonus() ? singleBuff.getPremiumLevel() : singleBuff.getLevel();
                     Skill skill = SkillsParser.getInstance().getInfo(singleBuff.getSkillId(), skillLvl);
                     if (skill != null) {
                        int buffTime = getBuffTime(player, skill.getId());
                        if (buffTime > 0 && skill.hasEffects()) {
                           Env env = new Env();
                           env.setCharacter(player);
                           env.setTarget(player);
                           env.setSkill(skill);

                           for(EffectTemplate et : skill.getEffectTemplates()) {
                              Effect ef = et.getEffect(env);
                              if (ef != null) {
                                 ef.setAbnormalTime(buffTime * 60);
                                 ef.scheduleEffect(true);
                              }
                           }
                        } else {
                           skill.getEffects(player, player, false);
                        }
                     }
                  }
               } else {
                  if (!player.hasSummon()) {
                     this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NOT_HAVE_SERVITOR", player.getLang()).toString());
                     this.showCommunity(player, main(player));
                     return;
                  }

                  boolean heal = false;
                  boolean mpHeal = false;

                  for(SingleBuff singleBuff : var89) {
                     int skillLvl = player.hasPremiumBonus() ? singleBuff.getPremiumLevel() : singleBuff.getLevel();
                     Skill skill = SkillsParser.getInstance().getInfo(singleBuff.getSkillId(), skillLvl);
                     if (skill != null) {
                        int buffTime = getBuffTime(player, skill.getId());
                        if (buffTime > 0 && skill.hasEffects()) {
                           Env env = new Env();
                           env.setCharacter(player);
                           env.setTarget(player.getSummon());
                           env.setSkill(skill);

                           for(EffectTemplate et : skill.getEffectTemplates()) {
                              Effect ef = et.getEffect(env);
                              if (ef != null) {
                                 ef.setAbnormalTime(buffTime * 60);
                                 ef.scheduleEffect(true);
                              }
                           }
                        } else {
                           skill.getEffects(player, player.getSummon(), false);
                        }

                        if (skill.hasEffectType(EffectType.HEAL_PERCENT) && player.getSummon().getCurrentHpPercents() > 50.0) {
                           heal = true;
                        } else if (skill.hasEffectType(EffectType.MANAHEAL_PERCENT) && player.getSummon().getCurrentMpPercents() > 50.0) {
                           mpHeal = true;
                        }
                     }
                  }

                  if (heal) {
                     player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
                  }

                  if (mpHeal) {
                     player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
                  }
               }
            }

            msg = main(player);
         } else if (eventParam0.equalsIgnoreCase("changeName_1")) {
            String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_change_name.htm");
            if (isPetBuff(player)) {
               dialog = dialog.replace(
                  "%topbtn%",
                  player.hasSummon()
                     ? player.getSummonName(player.getSummon())
                     : ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.DONT_HAVE_PET")
               );
            } else {
               dialog = dialog.replace("%topbtn%", player.getName());
            }

            dialog = dialog.replace("%schemeId%", eventParam1);
            dialog = dialog.replace("%schemePart%", generateScheme(player));
            msg = dialog;
         } else if (eventParam0.equalsIgnoreCase("changeName")) {
            int schemeId = Integer.parseInt(eventParam1);
            PlayerScheme scheme = player.getBuffSchemeById(schemeId);
            if (scheme == null) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.INVALID_SCHEME", player.getLang()).toString());
               this.showCommunity(player, main(player));
               return;
            }

            String name = this.getCorrectName(eventParam2 + (eventParam3.equalsIgnoreCase("x") ? "" : " " + eventParam3));
            if (name.isEmpty() || name.equals("no_name")) {
               player.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ENTER_NAME", player.getLang()).toString());
               this.showCommunity(player, this.getOptionList(player, schemeId));
               return;
            }

            scheme.setName(name);
            CharSchemesHolder.getInstance().updateScheme(name, schemeId);
            this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.NAME_CHANGE_SUCCESS", player.getLang()).toString());
            msg = this.getOptionList(player, schemeId);
         } else if (eventParam0.equalsIgnoreCase("changeIcon_1")) {
            msg = this.changeSchemeIcon(player, Integer.parseInt(eventParam1));
         } else if (eventParam0.equalsIgnoreCase("changeIcon")) {
            int schemeId = Integer.parseInt(eventParam1);
            PlayerScheme scheme = player.getBuffSchemeById(schemeId);
            if (scheme == null) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.INVALID_SCHEME", player.getLang()).toString());
               this.showCommunity(player, main(player));
               return;
            }

            int iconId = 0;

            try {
               iconId = Integer.parseInt(eventParam2);
               if (iconId < 0 || iconId > SCHEME_ICONS.length - 1) {
                  throw new Exception();
               }
            } catch (Exception var81) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.WRONG_ICON", player.getLang()).toString());
               this.showCommunity(player, this.getOptionList(player, schemeId));
               return;
            }

            scheme.setIcon(iconId);
            CharSchemesHolder.getInstance().updateIcon(iconId, schemeId);
            this.sendErrorMessageToPlayer(player, new ServerMessage("CommunityBuffer.ICON_CHANGE_SUCCESS", player.getLang()).toString());
            msg = this.getOptionList(player, schemeId);
         }

         this.showCommunity(player, msg);
      }
   }

   private void setPetBuff(Player player, String eventParam1) {
      player.addQuickVar("SchemeBufferPet", Integer.valueOf(eventParam1));
   }

   private static boolean isPetBuff(Player player) {
      int value = player.getQuickVarI("SchemeBufferPet");
      return value > 0;
   }

   public void showWindow(Player player) {
      this.showCommunity(player, main(player));
   }

   public static String main(Player player) {
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_main.htm");
      if (isPetBuff(player)) {
         dialog = dialog.replace(
            "%topbtn%",
            "<button value=\""
               + (
                  player.hasSummon()
                     ? player.getSummonName(player.getSummon())
                     : ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.DONT_HAVE_PET")
               )
               + "\" action=\"bypass _bbsbufferbypass_buffpet 0 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
         );
      } else {
         dialog = dialog.replace(
            "%topbtn%",
            "<button value="
               + player.getName()
               + " action=\"bypass _bbsbufferbypass_buffpet 1 0 0\" width=200 height=30 back=\"L2UI_ct1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
         );
      }

      dialog = dialog.replace("%schemePart%", generateScheme(player));
      dialog = dialog.replace("\r\n", "");
      return dialog.replace("\t", "");
   }

   private String viewAllSchemeBuffs(Player player, String scheme, String page) {
      int pageN = Integer.parseInt(page);
      int schemeId = Integer.parseInt(scheme);
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_buffs.htm");
      int[] buffCount = this.getBuffCount(player, schemeId);
      int TOTAL_BUFF = buffCount[0];
      int BUFF_COUNT = buffCount[1];
      int DANCE_SONG = buffCount[2];
      if (isPetBuff(player)) {
         dialog = dialog.replace(
            "%topbtn%",
            player.hasSummon()
               ? player.getSummonName(player.getSummon())
               : ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.DONT_HAVE_PET")
         );
      } else {
         dialog = dialog.replace("%topbtn%", player.getName());
      }

      dialog = dialog.replace("%bcount%", String.valueOf(player.getMaxBuffCount() - BUFF_COUNT));
      dialog = dialog.replace("%dscount%", String.valueOf(Config.DANCES_MAX_AMOUNT - DANCE_SONG));
      List<SchemeBuff> schemeBuffs = new ArrayList<>();
      List<SchemeBuff> schemeDances = new ArrayList<>();

      for(SchemeBuff buff : player.getBuffSchemeById(schemeId).getBuffs()) {
         String ROW_SIZES = this.getBuffType(player, buff.getSkillId());
         switch(ROW_SIZES) {
            case "song":
            case "dance":
               schemeDances.add(buff);
               break;
            default:
               schemeBuffs.add(buff);
         }
      }

      int MAX_ROW_SIZE = 16;
      int danceAmount = Config.DANCES_MAX_AMOUNT > 12 ? 16 : 12;
      int[] ROW_SIZES = new int[]{12, 28, 28 + danceAmount};
      StringBuilder addedBuffs = new StringBuilder();
      int row = 0;

      for(int i = 0; i < ROW_SIZES[2]; ++i) {
         if (i == 0 || i + 1 - ROW_SIZES[Math.max(row - 1, 0)] == 1) {
            addedBuffs.append("<tr>");
         }

         if (row < 2 && schemeBuffs.size() > i) {
            Skill skill = SkillsParser.getInstance().getInfo(schemeBuffs.get(i).getSkillId(), schemeBuffs.get(i).getLevel());
            addedBuffs.append("<td width=34>");
            addedBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=" + skill.getIcon() + ">");
            addedBuffs.append("<tr>");
            addedBuffs.append("<td width=34>");
            addedBuffs.append(
               "<button action=\"bypass _bbsbufferbypass_remove_buff "
                  + schemeId
                  + "_"
                  + skill.getId()
                  + "_"
                  + skill.getLevel()
                  + " "
                  + pageN
                  + " x\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>"
            );
            addedBuffs.append("</td>");
            addedBuffs.append("</tr>");
            addedBuffs.append("</table>");
            addedBuffs.append("</td>");
         } else if (row >= 2 && schemeDances.size() > i - ROW_SIZES[row - 1]) {
            Skill skill = SkillsParser.getInstance()
               .getInfo(schemeDances.get(i - ROW_SIZES[row - 1]).getSkillId(), schemeDances.get(i - ROW_SIZES[row - 1]).getLevel());
            addedBuffs.append("<td width=34>");
            addedBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=" + skill.getIcon() + ">");
            addedBuffs.append("<tr>");
            addedBuffs.append("<td width=34>");
            addedBuffs.append(
               "<button action=\"bypass _bbsbufferbypass_remove_buff "
                  + schemeId
                  + "_"
                  + skill.getId()
                  + "_"
                  + skill.getLevel()
                  + " "
                  + pageN
                  + " x\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>"
            );
            addedBuffs.append("</td>");
            addedBuffs.append("</tr>");
            addedBuffs.append("</table>");
            addedBuffs.append("</td>");
         } else {
            addedBuffs.append("<td width=34>");
            addedBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=L2UI_CH3.multisell_plusicon>");
            addedBuffs.append("<tr>");
            addedBuffs.append("<td width=34>");
            addedBuffs.append("&nbsp;");
            addedBuffs.append("</td>");
            addedBuffs.append("</tr>");
            addedBuffs.append("</table>");
            addedBuffs.append("</td>");
         }

         if (ROW_SIZES[row] < 16 * (row + 1) && i + 1 > ROW_SIZES[row]) {
            for(int z = ROW_SIZES[row]; z < 16 * (row + 1); ++z) {
               addedBuffs.append("<td width=1>");
               addedBuffs.append("&nbsp;");
               addedBuffs.append("</td>");
            }
         }

         if (i + 1 - ROW_SIZES[row] == 0) {
            addedBuffs.append("</tr>");
            ++row;
         }
      }

      List<Skill> availableSkills = new ArrayList<>();

      for(SingleBuff singleBuff : _allSingleBuffs) {
         boolean hasAddedThisBuff = false;

         for(SchemeBuff buff : schemeBuffs) {
            if (buff.getSkillId() == singleBuff.getSkillId()) {
               hasAddedThisBuff = true;
               break;
            }
         }

         for(SchemeBuff buff : schemeDances) {
            if (buff.getSkillId() == singleBuff.getSkillId()) {
               hasAddedThisBuff = true;
               break;
            }
         }

         if (!hasAddedThisBuff) {
            String var49 = singleBuff.getBuffType();
            switch(var49) {
               case "song":
               case "dance":
                  if (DANCE_SONG >= Config.DANCES_MAX_AMOUNT) {
                     continue;
                  }
                  break;
               default:
                  if (BUFF_COUNT >= player.getMaxBuffCount()) {
                     continue;
                  }
            }

            availableSkills.add(SkillsParser.getInstance().getInfo(singleBuff.getSkillId(), singleBuff.getLevel()));
         }
      }

      int SKILLS_PER_ROW = 4;
      int MAX_SKILLS_ROWS = 3;
      StringBuilder availableBuffs = new StringBuilder();
      int maxPage = (int)Math.ceil((double)availableSkills.size() / 12.0 - 1.0);
      int currentPage = Math.max(Math.min(maxPage, pageN), 0);
      int startIndex = currentPage * 4 * 3;

      for(int i = startIndex; i < startIndex + 12; ++i) {
         if (i == 0 || i % 4 == 0) {
            availableBuffs.append("<tr>");
         }

         if (availableSkills.size() > i) {
            Skill skill = availableSkills.get(i);
            String skillName = player.getSkillName(skill);
            availableBuffs.append("<td>");
            availableBuffs.append("<table cellspacing=2 cellpadding=2 width=150 height=40>");
            availableBuffs.append("<tr>");
            availableBuffs.append("<td>");
            availableBuffs.append("<table border=0 cellspacing=0 cellpadding=0 width=34 height=34 background=" + skill.getIcon() + ">");
            availableBuffs.append("<tr>");
            availableBuffs.append("<td>");
            availableBuffs.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=L2UI.item_click>");
            availableBuffs.append("<tr>");
            availableBuffs.append("<td>");
            availableBuffs.append("<br>");
            availableBuffs.append("</td>");
            availableBuffs.append("<td height=34>");
            availableBuffs.append("<button action=\"bypass _bbsbufferbypass_add_buff ")
               .append(scheme)
               .append("_")
               .append(skill.getId())
               .append("_")
               .append(skill.getLevel())
               .append("_")
               .append(getPremiumLvl(skill.getId()))
               .append(" ")
               .append(currentPage)
               .append(" ")
               .append(TOTAL_BUFF)
               .append("\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\">");
            availableBuffs.append("</td>");
            availableBuffs.append("</tr>");
            availableBuffs.append("</table>");
            availableBuffs.append("</td>");
            availableBuffs.append("</tr>");
            availableBuffs.append("</table>");
            availableBuffs.append("</td>");
            availableBuffs.append("<td width=100 align=center>");
            availableBuffs.append("<font name=CREDITTEXTSMALL>" + skillName + "</font>");
            availableBuffs.append("</td>");
            availableBuffs.append("</tr>");
            availableBuffs.append("</table>");
            availableBuffs.append("</td>");
         } else {
            availableBuffs.append("<td>");
            availableBuffs.append("<table cellspacing=2 cellpadding=2 width=150 height=40>");
            availableBuffs.append("<tr>");
            availableBuffs.append("<td>");
            availableBuffs.append("&nbsp;");
            availableBuffs.append("</td>");
            availableBuffs.append("</tr>");
            availableBuffs.append("</table>");
            availableBuffs.append("</td>");
         }

         if ((i + 1) % 4 == 0 || i - startIndex >= 12) {
            availableBuffs.append("</tr>");
         }
      }

      dialog = dialog.replace("%scheme%", scheme);
      dialog = dialog.replace("%addedBuffs%", addedBuffs.toString());
      dialog = dialog.replace("%availableBuffs%", availableBuffs.toString());
      dialog = dialog.replace("%prevPage%", currentPage > 0 ? "bypass _bbsbufferbypass_manage_scheme_1 " + scheme + " " + (currentPage - 1) + " x" : "");
      dialog = dialog.replace("%nextPage%", currentPage < maxPage ? "bypass _bbsbufferbypass_manage_scheme_1 " + scheme + " " + (currentPage + 1) + " x" : "");
      dialog = dialog.replace("\r\n", "");
      return dialog.replace("\t", "");
   }

   private boolean canHeal(Player player) {
      if (player.isInFightEvent() && player.getFightEvent().getState() != AbstractFightEvent.EVENT_STATE.PREPARATION) {
         return false;
      } else {
         return player.isInFightEvent() || !Config.ALLOW_HEAL_ONLY_PEACE || player.isInsideZone(ZoneId.PEACE) || player.isInSiege();
      }
   }

   private void heal(Player player, boolean isPet) {
      if (this.canHeal(player)) {
         if (!isPet) {
            player.setCurrentHp(player.getMaxHp());
            player.setCurrentMp(player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
            player.broadcastPacket(new MagicSkillUse(player, player, 22217, 1, 0, 0));
         } else if (player.hasSummon()) {
            Summon pet = player.getSummon();
            pet.setCurrentHp(pet.getMaxHp());
            pet.setCurrentMp(pet.getMaxMp());
            pet.setCurrentCp(pet.getMaxCp());
            pet.broadcastPacket(new MagicSkillUse(pet, 22217, 1, 0, 0));
         }
      }
   }

   private String getSkillIconHtml(int id, int level) {
      String iconNumber = this.getSkillIconNumber(id, level);
      return "<img width=32 height=32 src=\"Icon.skill" + iconNumber + "\">";
   }

   private String getSkillIcon(int id, int level) {
      return "Icon.skill" + this.getSkillIconNumber(id, level);
   }

   private String getSkillIconNumber(int id, int level) {
      String formato;
      if (id == 4) {
         formato = "0004";
      } else if (id > 9 && id < 100) {
         formato = "00" + id;
      } else if (id > 99 && id < 1000) {
         formato = "0" + id;
      } else if (id == 1517) {
         formato = "1536";
      } else if (id == 1518) {
         formato = "1537";
      } else if (id == 1547) {
         formato = "0065";
      } else if (id == 2076) {
         formato = "0195";
      } else if (id > 4550 && id < 4555) {
         formato = "5739";
      } else if (id > 4698 && id < 4701) {
         formato = "1331";
      } else if (id > 4701 && id < 4704) {
         formato = "1332";
      } else if (id == 6049) {
         formato = "0094";
      } else {
         formato = String.valueOf(id);
      }

      return formato;
   }

   private String getDeleteSchemePage(Player player) {
      StringBuilder builder = new StringBuilder();
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_delete.htm");

      for(PlayerScheme scheme : player.getBuffSchemes()) {
         builder.append("<button value=\"")
            .append(scheme.getName())
            .append("\" action=\"bypass _bbsbufferbypass_delete ")
            .append(scheme.getSchemeId())
            .append(" ")
            .append(scheme.getName())
            .append(" x\" width=200 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
      }

      return dialog.replace("%schemes%", builder.toString());
   }

   private String buildHtml(String buffType, Player player) {
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_indbuffs.htm");
      String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/indbuffs-template.htm");
      String block = "";
      String list = "";
      Collection<String> availableBuffs = new ArrayList<>();

      for(SingleBuff buff : _allSingleBuffs) {
         if (buff.getBuffType().equals(buffType)) {
            String bName = player.getSkillName(SkillsParser.getInstance().getInfo(buff.getSkillId(), buff.getLevel()));
            bName = bName.replace(" ", "+");
            int skillLvl = player.hasPremiumBonus() ? buff.getPremiumLevel() : buff.getLevel();
            availableBuffs.add(bName + "_" + buff.getSkillId() + "_" + skillLvl);
         }
      }

      if (availableBuffs.isEmpty()) {
         list = list + ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.NO_BUFFS");
      } else {
         int index = 0;

         for(String buff : availableBuffs) {
            buff = buff.replace("_", " ");
            String[] buffSplit = buff.split(" ");
            String name = buffSplit[0];
            int id = Integer.parseInt(buffSplit[1]);
            int level = Integer.parseInt(buffSplit[2]);
            name = name.replace("+", " ");
            block = template.replace("%icon%", this.getSkillIconHtml(id, level));
            block = block.replace("%iconImg%", this.getSkillIcon(id, level));
            block = block.replace("%name%", name);
            block = block.replace("%id%", String.valueOf(id));
            block = block.replace("%lvl%", String.valueOf(level));
            block = block.replace("%type%", buffType);
            if (++index % 2 == 0) {
               if (index > 0) {
                  block = block + "</tr>";
               }

               block = block + "<tr>";
            }

            list = list + block;
         }
      }

      dialog = dialog.replace("%buffs%", list);
      return dialog.replace("%schemePart%", generateScheme(player));
   }

   private String getEditSchemePage(Player player) {
      StringBuilder builder = new StringBuilder();
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_menu.htm");

      for(PlayerScheme scheme : player.getBuffSchemes()) {
         builder.append("<button value=\"")
            .append(scheme.getName())
            .append("\" action=\"bypass _bbsbufferbypass_manage_scheme_select ")
            .append(scheme.getSchemeId())
            .append(" x x\" width=200 height=30 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
      }

      return dialog.replace("%schemes%", builder.toString());
   }

   private int[] getBuffCount(Player player, int schemeId) {
      int count = 0;
      int dances = 0;
      int buffs = 0;

      for(SchemeBuff buff : player.getBuffSchemeById(schemeId).getBuffs()) {
         ++count;
         if (buff.isDanceSlot()) {
            ++dances;
         } else {
            ++buffs;
         }
      }

      return new int[]{count, buffs, dances};
   }

   private String getOptionList(Player player, int schemeId) {
      PlayerScheme scheme = player.getBuffSchemeById(schemeId);
      int[] buffCount = this.getBuffCount(player, schemeId);
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_options.htm");
      if (isPetBuff(player)) {
         dialog = dialog.replace(
            "%topbtn%",
            player.hasSummon()
               ? player.getSummonName(player.getSummon())
               : ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.DONT_HAVE_PET")
         );
      } else {
         dialog = dialog.replace("%topbtn%", player.getName());
      }

      dialog = dialog.replace("%name%", scheme != null ? scheme.getName() : "");
      dialog = dialog.replace("%bcount%", String.valueOf(buffCount[1]));
      dialog = dialog.replace("%dscount%", String.valueOf(buffCount[2]));
      dialog = dialog.replace("%manageBuffs%", "bypass _bbsbufferbypass_manage_scheme_1 " + schemeId + " 0 x");
      dialog = dialog.replace("%changeName%", "bypass _bbsbufferbypass_changeName_1 " + schemeId + " x x");
      dialog = dialog.replace("%changeIcon%", "bypass _bbsbufferbypass_changeIcon_1 " + schemeId + " x x");
      dialog = dialog.replace("%deleteScheme%", "bypass _bbsbufferbypass_delete " + schemeId + " x x");
      return dialog.replace("%schemePart%", generateScheme(player));
   }

   private String createScheme(Player player, int iconId) {
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_create.htm");
      if (isPetBuff(player)) {
         dialog = dialog.replace(
            "%topbtn%",
            player.hasSummon()
               ? player.getSummonName(player.getSummon())
               : ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.DONT_HAVE_PET")
         );
      } else {
         dialog = dialog.replace("%topbtn%", player.getName());
      }

      StringBuilder icons = new StringBuilder();
      int MAX_ICONS_PER_ROW = 17;

      for(int i = 0; i < SCHEME_ICONS.length; ++i) {
         if (i == 0 || (i + 1) % 17 == 1) {
            icons.append("<tr>");
         }

         icons.append("<td width=60 align=center valign=top>");
         icons.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=" + SCHEME_ICONS[i] + ">");
         icons.append("<tr>");
         icons.append("<td width=32 height=32 align=center valign=top>");
         if (iconId == i) {
            icons.append("<table cellspacing=0 cellpadding=0 width=34 height=34 background=L2UI_CT1.ItemWindow_DF_Frame_Over>");
            icons.append("<tr><td align=left>");
            icons.append("&nbsp;");
            icons.append("</td></tr>");
            icons.append("</table>");
         } else {
            icons.append(
               "<button action=\"bypass _bbsbufferbypass_create_1 "
                  + i
                  + " x x\" width=34 height=34 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />"
            );
         }

         icons.append("</td>");
         icons.append("</tr>");
         icons.append("</table>");
         icons.append("</td>");
         if (i + 1 == SCHEME_ICONS.length || (i + 1) % 17 == 0) {
            icons.append("</tr>");
         }
      }

      dialog = dialog.replace("%iconList%", icons.toString());
      return dialog.replace("%iconId%", String.valueOf(iconId));
   }

   private String changeSchemeIcon(Player player, int schemeId) {
      String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/buffer_scheme_change_icon.htm");
      if (isPetBuff(player)) {
         dialog = dialog.replace(
            "%topbtn%",
            player.hasSummon()
               ? player.getSummonName(player.getSummon())
               : ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.DONT_HAVE_PET")
         );
      } else {
         dialog = dialog.replace("%topbtn%", player.getName());
      }

      StringBuilder icons = new StringBuilder();
      int MAX_ICONS_PER_ROW = 17;

      for(int i = 0; i < SCHEME_ICONS.length; ++i) {
         if (i == 0 || (i + 1) % 17 == 1) {
            icons.append("<tr>");
         }

         icons.append("<td width=60 align=center valign=top>");
         icons.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=" + SCHEME_ICONS[i] + ">");
         icons.append("<tr>");
         icons.append("<td width=32 height=32 align=center valign=top>");
         icons.append(
            "<button action=\"bypass _bbsbufferbypass_changeIcon "
               + schemeId
               + " "
               + i
               + " x x\" width=34 height=34 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />"
         );
         icons.append("</td>");
         icons.append("</tr>");
         icons.append("</table>");
         icons.append("</td>");
         if (i + 1 == SCHEME_ICONS.length || (i + 1) % 17 == 0) {
            icons.append("</tr>");
         }
      }

      dialog = dialog.replace("%iconList%", icons.toString());
      return dialog.replace("%schemeId%", String.valueOf(schemeId));
   }

   private static String generateScheme(Player player) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/scheme_main.htm");
      String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/buffer/scheme_template.htm");
      String block = "";
      String list = "";
      Iterator<PlayerScheme> it = player.getBuffSchemes().iterator();

      for(int i = 0; i < Config.BUFF_MAX_SCHEMES; ++i) {
         if (!it.hasNext()) {
            list = list + "<tr><td width=210 height=50 valign=top align=center></td></tr>";
         } else {
            PlayerScheme scheme = it.next();
            int buffCount = 0;

            for(SchemeBuff buff : scheme.getBuffs()) {
               if (buff != null) {
                  ++buffCount;
               }
            }

            int price = buffCount * Config.BUFF_AMOUNT;
            block = template.replace("%icon%", SCHEME_ICONS[scheme.getIconId()]);
            block = block.replace("%schemeId%", String.valueOf(scheme.getSchemeId()));
            block = block.replace("%schemeName%", scheme.getName());
            block = block.replace("%price%", String.valueOf(price));
            list = list + block;
         }
      }

      return html.replace("%list%", list);
   }

   public String getBuffType(Player player, int id) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == id) {
            return singleBuff.getBuffType();
         }
      }

      return "none";
   }

   private boolean isDanceSlotBuff(Player player, int id) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == id) {
            return singleBuff.isDanceSlot();
         }
      }

      return false;
   }

   public void sendErrorMessageToPlayer(Player player, String msg) {
      player.sendPacket(new CreatureSay(player.getObjectId(), 18, ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.ERROR"), msg));
   }

   public void showCommunity(Player player, String text) {
      if (text != null) {
         separateAndSend(text, player);
      }
   }

   private String getCorrectName(String currentName) {
      StringBuilder newNameBuilder = new StringBuilder();
      char[] chars = currentName.toCharArray();

      for(char c : chars) {
         if (isCharFine(c)) {
            newNameBuilder.append(c);
         }
      }

      return newNameBuilder.toString();
   }

   private static boolean isCharFine(char c) {
      for(char fineChar : FINE_CHARS) {
         if (fineChar == c) {
            return true;
         }
      }

      return false;
   }

   private void askQuestion(Player player, int id, String name) {
      ServerMessage message = new ServerMessage("CommunityBuffer.WANT_DELETE", player.getLang());
      message.add(name);
      player.sendConfirmDlg(new AskQuestionAnswerListener(player), 60000, message.toString());
      player.addQuickVar("schemeToDel", id);
   }

   public void deleteScheme(int eventParam1, Player player) {
      CharSchemesHolder.getInstance().deleteScheme(eventParam1);
      int realId = eventParam1;

      for(PlayerScheme scheme : player.getBuffSchemes()) {
         if (scheme.getSchemeId() == realId) {
            player.getBuffSchemes().remove(scheme);
            break;
         }
      }
   }

   public static boolean isValidSkill(Player player, int skillId, int level) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == skillId) {
            if (!player.hasPremiumBonus()) {
               return singleBuff.getLevel() == level;
            }

            return singleBuff.getPremiumLevel() == level || singleBuff.getLevel() == level;
         }
      }

      return false;
   }

   public static boolean isItemRemoveSkill(Player player, int skillId) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == skillId && singleBuff.isBuffForItems() && singleBuff.getRequestItems() != null) {
            return true;
         }
      }

      return false;
   }

   public static boolean checkItemsForSkill(Player player, int skillId, boolean printMsg) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == skillId && singleBuff.getRequestItems() != null) {
            if (singleBuff.needAllItems()) {
               for(int[] it : singleBuff.getRequestItems()) {
                  if (it != null
                     && it.length == 2
                     && (player.getInventory().getItemByItemId(it[0]) == null || player.getInventory().getItemByItemId(it[0]).getCount() < (long)it[1])) {
                     if (printMsg) {
                        ServerMessage message = new ServerMessage("CommunityBuffer.NECESSARY_ITEMS", player.getLang());
                        message.add(it[1]);
                        message.add(Util.getItemName(player, it[0]));
                        player.sendPacket(
                           new CreatureSay(
                              player.getObjectId(), 18, ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.ERROR"), message.toString()
                           )
                        );
                     }

                     return false;
                  }
               }

               if (singleBuff.isRemoveItems()) {
                  for(int[] it : singleBuff.getRequestItems()) {
                     if (it != null && it.length == 2) {
                        player.destroyItemByItemId("RequestItemsBuff", it[0], (long)it[1], player, true);
                     }
                  }
               }

               return true;
            }

            boolean foundItem = false;
            boolean needRemove = singleBuff.isRemoveItems();

            for(int[] it : singleBuff.getRequestItems()) {
               if (it != null
                  && it.length == 2
                  && player.getInventory().getItemByItemId(it[0]) != null
                  && player.getInventory().getItemByItemId(it[0]).getCount() >= (long)it[1]) {
                  foundItem = true;
                  if (needRemove) {
                     player.destroyItemByItemId("RequestItemsBuff", it[0], (long)it[1], player, true);
                  }
                  break;
               }
            }

            if (!foundItem) {
               if (printMsg) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               }

               return false;
            }

            return true;
         }
      }

      return false;
   }

   private static int getPremiumLvl(int skillId) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == skillId) {
            return singleBuff.getPremiumLevel();
         }
      }

      return 1;
   }

   public static int getBuffTime(Player player, int skillId) {
      for(SingleBuff singleBuff : _allSingleBuffs) {
         if (singleBuff.getSkillId() == skillId) {
            return player.hasPremiumBonus() ? singleBuff.getPremiumBuffTime() : singleBuff.getBuffTime();
         }
      }

      return 0;
   }

   private void addAllBuffsToScheme(Player player, int schemeId) {
      for(Effect ef : player.getAllEffects()) {
         if (ef != null) {
            int skillId = ef.getSkill().getId();
            int level = ef.getSkill().getLevel();
            int premiumLvl = getPremiumLvl(skillId);
            boolean isDanceSlot = this.isDanceSlotBuff(player, skillId);
            if (isValidSkill(player, skillId, level) && !haveSkillInScheme(player, schemeId, skillId)) {
               CharSchemesHolder.getInstance()
                  .addBuff(String.valueOf(schemeId), String.valueOf(skillId), String.valueOf(level), String.valueOf(premiumLvl), isDanceSlot);
               player.getBuffSchemeById(schemeId).getBuffs().add(new SchemeBuff(skillId, level, premiumLvl, isDanceSlot));
            }
         }
      }
   }

   private static boolean haveSkillInScheme(Player player, int schemeId, int skillId) {
      for(SchemeBuff buff : player.getBuffSchemeById(schemeId).getBuffs()) {
         if (buff != null && buff.getSkillId() == skillId) {
            return true;
         }
      }

      return false;
   }

   public Map<Integer, List<Integer>> getBuffClasses() {
      return this._classes;
   }

   public ArrayList<SingleBuff> getSetBuffs(int groupId) {
      return this._buffs.get(groupId);
   }

   private int[][] parseItemsList(String line) {
      if (line != null && !line.isEmpty()) {
         String[] propertySplit = line.split(";");
         if (propertySplit.length == 0) {
            return (int[][])null;
         } else {
            int i = 0;
            int[][] result = new int[propertySplit.length][];

            for(String value : propertySplit) {
               String[] valueSplit = value.split(",");
               if (valueSplit.length != 2) {
                  _log.warning(StringUtil.concat("CommunityBuffer: parseItemsList invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
                  return (int[][])null;
               }

               result[i] = new int[2];

               try {
                  result[i][0] = Integer.parseInt(valueSplit[0]);
               } catch (NumberFormatException var12) {
                  _log.warning(StringUtil.concat("CommunityBuffer: parseItemsList invalid itemId -> \"", valueSplit[0], "\""));
                  return (int[][])null;
               }

               try {
                  result[i][1] = Integer.parseInt(valueSplit[1]);
               } catch (NumberFormatException var11) {
                  _log.warning(StringUtil.concat("CommunityBuffer: parseItemsList invalid item number -> \"", valueSplit[1], "\""));
                  return (int[][])null;
               }

               ++i;
            }

            return result;
         }
      } else {
         return (int[][])null;
      }
   }

   @Override
   public void onWriteCommand(String command, String s, String s1, String s2, String s3, String s4, Player Player) {
   }

   private class BackHp implements Runnable {
      private final Playable _playable;
      private final double _hp;
      private final double _mp;
      private final double _cp;

      private BackHp(Playable playable, double hp, double mp, double cp) {
         this._playable = playable;
         this._hp = hp;
         this._mp = mp;
         this._cp = cp;
      }

      @Override
      public void run() {
         this._playable.getActingPlayer().deleteQuickVar("BackHpOn");
         this._playable.setCurrentHp(this._hp);
         this._playable.setCurrentMp(this._mp);
         this._playable.setCurrentCp(this._cp);
      }
   }
}
