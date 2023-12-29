package l2e.gameserver.model.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.base.PlayerState;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.skills.conditions.ConditionAgathionItemId;
import l2e.gameserver.model.skills.conditions.ConditionAngelCatEventActive;
import l2e.gameserver.model.skills.conditions.ConditionChangeWeapon;
import l2e.gameserver.model.skills.conditions.ConditionGameChance;
import l2e.gameserver.model.skills.conditions.ConditionGameTime;
import l2e.gameserver.model.skills.conditions.ConditionLogicAnd;
import l2e.gameserver.model.skills.conditions.ConditionLogicNot;
import l2e.gameserver.model.skills.conditions.ConditionLogicOr;
import l2e.gameserver.model.skills.conditions.ConditionMinDistance;
import l2e.gameserver.model.skills.conditions.ConditionPeaceZone;
import l2e.gameserver.model.skills.conditions.ConditionPlayerActiveEffectId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerActiveSkillId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerAgathionId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCallPc;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanEscape;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanPossessHolything;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanRefuelAirship;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanSummon;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanSummonSiegeGolem;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanSweep;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanTakePcBangPoints;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanTransform;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCanUntransform;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCharges;
import l2e.gameserver.model.skills.conditions.ConditionPlayerClassIdRestriction;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCloakStatus;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCombat;
import l2e.gameserver.model.skills.conditions.ConditionPlayerCp;
import l2e.gameserver.model.skills.conditions.ConditionPlayerEnergy;
import l2e.gameserver.model.skills.conditions.ConditionPlayerFlyMounted;
import l2e.gameserver.model.skills.conditions.ConditionPlayerGrade;
import l2e.gameserver.model.skills.conditions.ConditionPlayerHasCastle;
import l2e.gameserver.model.skills.conditions.ConditionPlayerHasClanHall;
import l2e.gameserver.model.skills.conditions.ConditionPlayerHasFort;
import l2e.gameserver.model.skills.conditions.ConditionPlayerHasPet;
import l2e.gameserver.model.skills.conditions.ConditionPlayerHasServitor;
import l2e.gameserver.model.skills.conditions.ConditionPlayerHp;
import l2e.gameserver.model.skills.conditions.ConditionPlayerInFightEvent;
import l2e.gameserver.model.skills.conditions.ConditionPlayerInsideZoneId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerInstanceId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerInvSize;
import l2e.gameserver.model.skills.conditions.ConditionPlayerIsClanLeader;
import l2e.gameserver.model.skills.conditions.ConditionPlayerIsHero;
import l2e.gameserver.model.skills.conditions.ConditionPlayerLandingZone;
import l2e.gameserver.model.skills.conditions.ConditionPlayerLevel;
import l2e.gameserver.model.skills.conditions.ConditionPlayerLevelRange;
import l2e.gameserver.model.skills.conditions.ConditionPlayerMp;
import l2e.gameserver.model.skills.conditions.ConditionPlayerPkCount;
import l2e.gameserver.model.skills.conditions.ConditionPlayerPledgeClass;
import l2e.gameserver.model.skills.conditions.ConditionPlayerRace;
import l2e.gameserver.model.skills.conditions.ConditionPlayerRangeFromNpc;
import l2e.gameserver.model.skills.conditions.ConditionPlayerReflectionEntry;
import l2e.gameserver.model.skills.conditions.ConditionPlayerServitorNpcId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerSex;
import l2e.gameserver.model.skills.conditions.ConditionPlayerSiegeSide;
import l2e.gameserver.model.skills.conditions.ConditionPlayerSouls;
import l2e.gameserver.model.skills.conditions.ConditionPlayerState;
import l2e.gameserver.model.skills.conditions.ConditionPlayerSubclass;
import l2e.gameserver.model.skills.conditions.ConditionPlayerTransformationId;
import l2e.gameserver.model.skills.conditions.ConditionPlayerVehicleMounted;
import l2e.gameserver.model.skills.conditions.ConditionPlayerWeight;
import l2e.gameserver.model.skills.conditions.ConditionSiegeZone;
import l2e.gameserver.model.skills.conditions.ConditionSlotItemId;
import l2e.gameserver.model.skills.conditions.ConditionTargetAbnormal;
import l2e.gameserver.model.skills.conditions.ConditionTargetActiveEffectId;
import l2e.gameserver.model.skills.conditions.ConditionTargetActiveSkillId;
import l2e.gameserver.model.skills.conditions.ConditionTargetAggro;
import l2e.gameserver.model.skills.conditions.ConditionTargetClassIdRestriction;
import l2e.gameserver.model.skills.conditions.ConditionTargetInvSize;
import l2e.gameserver.model.skills.conditions.ConditionTargetLevel;
import l2e.gameserver.model.skills.conditions.ConditionTargetLevelRange;
import l2e.gameserver.model.skills.conditions.ConditionTargetMyPartyExceptMe;
import l2e.gameserver.model.skills.conditions.ConditionTargetNpcId;
import l2e.gameserver.model.skills.conditions.ConditionTargetNpcType;
import l2e.gameserver.model.skills.conditions.ConditionTargetPercentCp;
import l2e.gameserver.model.skills.conditions.ConditionTargetPercentHp;
import l2e.gameserver.model.skills.conditions.ConditionTargetPercentMp;
import l2e.gameserver.model.skills.conditions.ConditionTargetPlayable;
import l2e.gameserver.model.skills.conditions.ConditionTargetRace;
import l2e.gameserver.model.skills.conditions.ConditionTargetRaceId;
import l2e.gameserver.model.skills.conditions.ConditionTargetUsesWeaponKind;
import l2e.gameserver.model.skills.conditions.ConditionTargetWard;
import l2e.gameserver.model.skills.conditions.ConditionTargetWeight;
import l2e.gameserver.model.skills.conditions.ConditionUsingItemType;
import l2e.gameserver.model.skills.conditions.ConditionUsingSkill;
import l2e.gameserver.model.skills.conditions.ConditionWithSkill;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.Lambda;
import l2e.gameserver.model.skills.funcs.LambdaCalc;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.skills.funcs.LambdaStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class DocumentBase {
   protected final Logger _log = Logger.getLogger(this.getClass().getName());
   private final File _file;
   protected Map<String, String[]> _tables = new HashMap<>();

   protected DocumentBase(File pFile) {
      this._file = pFile;
   }

   public Document parse() {
      Document doc = null;

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         doc = factory.newDocumentBuilder().parse(this._file);
         this.parseDocument(doc);
      } catch (Exception var3) {
         this._log.log(Level.SEVERE, "Error loading file " + this._file, (Throwable)var3);
      }

      return doc;
   }

   protected abstract void parseDocument(Document var1);

   protected abstract StatsSet getStatsSet();

   protected abstract String getTableValue(String var1);

   protected abstract String getTableValue(String var1, int var2);

   protected void resetTable() {
      this._tables.clear();
   }

   protected void setTable(String name, String[] table) {
      this._tables.put(name, table);
   }

   protected void parseTemplate(Node n, Object template) {
      Condition condition = null;
      n = n.getFirstChild();
      if (n != null) {
         if ("cond".equalsIgnoreCase(n.getNodeName())) {
            condition = this.parseCondition(n.getFirstChild(), template);
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if (condition != null && msg != null) {
               condition.setMessage(msg.getNodeValue());
            } else if (condition != null && msgId != null) {
               condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
               Node addName = n.getAttributes().getNamedItem("addName");
               if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                  condition.addName();
               }
            }

            n = n.getNextSibling();
         }

         for(; n != null; n = n.getNextSibling()) {
            if ("add".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Add", condition);
            } else if ("sub".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Sub", condition);
            } else if ("mul".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Mul", condition);
            } else if ("basemul".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "BaseMul", condition);
            } else if ("div".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Div", condition);
            } else if ("set".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Set", condition);
            } else if ("share".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Share", condition);
            } else if ("enchant".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "Enchant", condition);
            } else if ("enchanthp".equalsIgnoreCase(n.getNodeName())) {
               this.attachFunc(n, template, "EnchantHp", condition);
            } else if ("effect".equalsIgnoreCase(n.getNodeName())) {
               if (template instanceof EffectTemplate) {
                  throw new RuntimeException("Nested effects");
               }

               this.attachEffect(n, template, condition);
            }
         }
      }
   }

   protected void attachFunc(Node n, Object template, String name, Condition attachCond) {
      Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
      String order = n.getAttributes().getNamedItem("order").getNodeValue();
      Lambda lambda = this.getLambda(n, template);
      int ord = Integer.decode(this.getValue(order, template));
      Condition applayCond = this.parseCondition(n.getFirstChild(), template);
      FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, lambda);
      if (template instanceof Item) {
         ((Item)template).attach(ft);
      } else if (template instanceof Skill) {
         ((Skill)template).attach(ft);
      } else if (template instanceof EffectTemplate) {
         ((EffectTemplate)template).attach(ft);
      }
   }

   protected void attachLambdaFunc(Node n, Object template, LambdaCalc calc) {
      String name = n.getNodeName();
      StringBuilder sb = new StringBuilder(name);
      sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
      name = sb.toString();
      Lambda lambda = this.getLambda(n, template);
      FuncTemplate ft = new FuncTemplate(null, null, name, null, calc.funcs.length, lambda);
      calc.addFunc(ft.getFunc(new Env(), calc));
   }

   protected void attachEffect(Node n, Object template, Condition attachCond) {
      NamedNodeMap attrs = n.getAttributes();
      StatsSet set = new StatsSet();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Node att = attrs.item(i);
         set.set(att.getNodeName(), this.getValue(att.getNodeValue(), template));
      }

      StatsSet parameters = this.parseParameters(n.getFirstChild(), template);
      Lambda lambda = this.getLambda(n, template);
      Condition applayCond = this.parseCondition(n.getFirstChild(), template);
      if (template instanceof IIdentifiable) {
         set.set("id", ((IIdentifiable)template).getId());
      }

      byte abnormalLvl = 0;
      String abnormalType = "none";
      if (attrs.getNamedItem("abnormalType") != null) {
         abnormalType = attrs.getNamedItem("abnormalType").getNodeValue();
      }

      if (attrs.getNamedItem("abnormalLvl") != null) {
         abnormalLvl = Byte.parseByte(this.getValue(attrs.getNamedItem("abnormalLvl").getNodeValue(), template));
      }

      EffectTemplate effectTemplate = new EffectTemplate(attachCond, applayCond, lambda, abnormalType, abnormalLvl, set, parameters);
      this.parseTemplate(n, effectTemplate);
      if (template instanceof Item) {
         ((Item)template).attach(effectTemplate);
      } else if (template instanceof Skill) {
         Skill sk = (Skill)template;
         if (set.getInteger("self", 0) == 1) {
            sk.attachSelf(effectTemplate);
         } else if (sk.isPassive()) {
            sk.attachPassive(effectTemplate);
         } else {
            sk.attach(effectTemplate);
         }
      }
   }

   private StatsSet parseParameters(Node n, Object template) {
      StatsSet parameters;
      for(parameters = null; n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1 && "param".equals(n.getNodeName())) {
            if (parameters == null) {
               parameters = new StatsSet();
            }

            NamedNodeMap params = n.getAttributes();

            for(int i = 0; i < params.getLength(); ++i) {
               Node att = params.item(i);
               parameters.set(att.getNodeName(), this.getValue(att.getNodeValue(), template));
            }
         }
      }

      return parameters;
   }

   protected Condition parseCondition(Node n, Object template) {
      while(n != null && n.getNodeType() != 1) {
         n = n.getNextSibling();
      }

      Condition condition = null;
      if (n != null) {
         String var4 = n.getNodeName();
         switch(var4) {
            case "and":
               condition = this.parseLogicAnd(n, template);
               break;
            case "or":
               condition = this.parseLogicOr(n, template);
               break;
            case "not":
               condition = this.parseLogicNot(n, template);
               break;
            case "player":
               condition = this.parsePlayerCondition(n, template);
               break;
            case "target":
               condition = this.parseTargetCondition(n, template);
               break;
            case "using":
               condition = this.parseUsingCondition(n);
               break;
            case "game":
               condition = this.parseGameCondition(n);
         }
      }

      return condition;
   }

   protected Condition parseLogicAnd(Node n, Object template) {
      ConditionLogicAnd cond = new ConditionLogicAnd();

      for(Node var4 = n.getFirstChild(); var4 != null; var4 = var4.getNextSibling()) {
         if (var4.getNodeType() == 1) {
            cond.add(this.parseCondition(var4, template));
         }
      }

      if (cond.conditions == null || cond.conditions.length == 0) {
         this._log.severe("Empty <and> condition in " + this._file);
      }

      return cond;
   }

   protected Condition parseLogicOr(Node n, Object template) {
      ConditionLogicOr cond = new ConditionLogicOr();

      for(Node var4 = n.getFirstChild(); var4 != null; var4 = var4.getNextSibling()) {
         if (var4.getNodeType() == 1) {
            cond.add(this.parseCondition(var4, template));
         }
      }

      if (cond.conditions == null || cond.conditions.length == 0) {
         this._log.severe("Empty <or> condition in " + this._file);
      }

      return cond;
   }

   protected Condition parseLogicNot(Node n, Object template) {
      for(Node var3 = n.getFirstChild(); var3 != null; var3 = var3.getNextSibling()) {
         if (var3.getNodeType() == 1) {
            return new ConditionLogicNot(this.parseCondition(var3, template));
         }
      }

      this._log.severe("Empty <not> condition in " + this._file);
      return null;
   }

   protected Condition parsePlayerCondition(Node n, Object template) {
      Condition cond = null;
      NamedNodeMap attrs = n.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Node a = attrs.item(i);
         if ("races".equalsIgnoreCase(a.getNodeName())) {
            String[] racesVal = a.getNodeValue().split(",");
            Race[] races = new Race[racesVal.length];

            for(int r = 0; r < racesVal.length; ++r) {
               if (racesVal[r] != null) {
                  races[r] = Race.valueOf(racesVal[r]);
               }
            }

            cond = this.joinAnd(cond, new ConditionPlayerRace(races));
         } else if ("level".equalsIgnoreCase(a.getNodeName())) {
            int lvl = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerLevel(lvl));
         } else if ("levelRange".equalsIgnoreCase(a.getNodeName())) {
            String[] range = this.getValue(a.getNodeValue(), template).split(";");
            if (range.length == 2) {
               int[] lvlRange = new int[]{
                  Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[0]),
                  Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[1])
               };
               cond = this.joinAnd(cond, new ConditionPlayerLevelRange(lvlRange));
            }
         } else if ("resting".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
         } else if ("flying".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
         } else if ("moving".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
         } else if ("running".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
         } else if ("standing".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
         } else if ("behind".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
         } else if ("front".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
         } else if ("chaotic".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
         } else if ("olympiad".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
         } else if ("agathionEnergy".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerEnergy(val));
         } else if ("ishero".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerIsHero(val));
         } else if ("transformationId".equalsIgnoreCase(a.getNodeName())) {
            int id = Integer.parseInt(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerTransformationId(id));
         } else if ("hp".equalsIgnoreCase(a.getNodeName())) {
            int hp = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerHp(hp));
         } else if ("mp".equalsIgnoreCase(a.getNodeName())) {
            int hp = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerMp(hp));
         } else if ("cp".equalsIgnoreCase(a.getNodeName())) {
            int cp = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerCp(cp));
         } else if ("grade".equalsIgnoreCase(a.getNodeName())) {
            int expIndex = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerGrade(expIndex));
         } else if ("pkCount".equalsIgnoreCase(a.getNodeName())) {
            int expIndex = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerPkCount(expIndex));
         } else if ("peacezone".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPeaceZone(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("siegezone".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionSiegeZone(Boolean.parseBoolean(a.getNodeValue()), true));
         } else if ("siegeside".equalsIgnoreCase(a.getNodeName())) {
            int value = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerSiegeSide(value));
         } else if ("charges".equalsIgnoreCase(a.getNodeName())) {
            int value = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerCharges(value));
         } else if ("souls".equalsIgnoreCase(a.getNodeName())) {
            int value = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerSouls(value));
         } else if ("weight".equalsIgnoreCase(a.getNodeName())) {
            int weight = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerWeight(weight));
         } else if ("invSize".equalsIgnoreCase(a.getNodeName())) {
            int size = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerInvSize(size));
         } else if ("isClanLeader".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerIsClanLeader(val));
         } else if ("pledgeClass".equalsIgnoreCase(a.getNodeName())) {
            int pledgeClass = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
         } else if ("clanHall".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionPlayerHasClanHall(array));
         } else if ("fort".equalsIgnoreCase(a.getNodeName())) {
            int fort = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerHasFort(fort));
         } else if ("castle".equalsIgnoreCase(a.getNodeName())) {
            int castle = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerHasCastle(castle));
         } else if ("sex".equalsIgnoreCase(a.getNodeName())) {
            int sex = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionPlayerSex(sex));
         } else if ("flyMounted".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerFlyMounted(val));
         } else if ("vehicleMounted".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerVehicleMounted(val));
         } else if ("landingZone".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerLandingZone(val));
         } else if ("active_effect_id".equalsIgnoreCase(a.getNodeName())) {
            int effect_id = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
         } else if ("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName())) {
            String val = this.getValue(a.getNodeValue(), template);
            int effect_id = Integer.decode(this.getValue(val.split(",")[0], template));
            int effect_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
            cond = this.joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
         } else if ("active_skill_id".equalsIgnoreCase(a.getNodeName())) {
            int skill_id = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
         } else if ("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName())) {
            String val = this.getValue(a.getNodeValue(), template);
            int skill_id = Integer.decode(this.getValue(val.split(",")[0], template));
            int skill_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
            cond = this.joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
         } else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
         } else if ("subclass".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerSubclass(val));
         } else if ("instanceId".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionPlayerInstanceId(array));
         } else if ("agathionId".equalsIgnoreCase(a.getNodeName())) {
            int agathionId = Integer.decode(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
         } else if ("agathionItems".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionAgathionItemId(array));
         } else if ("cloakStatus".equalsIgnoreCase(a.getNodeName())) {
            int val = Integer.parseInt(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionPlayerCloakStatus(val));
         } else if ("hasPet".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionPlayerHasPet(array));
         } else if ("hasservitor".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerHasServitor());
         } else if ("servitorNpcId".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionPlayerServitorNpcId(array));
         } else if ("npcIdRadius".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            if (st.countTokens() == 3) {
               String[] ids = st.nextToken().split(";");
               int[] npcIds = new int[ids.length];

               for(int index = 0; index < ids.length; ++index) {
                  npcIds[index] = Integer.parseInt(this.getValue(ids[index], template));
               }

               int radius = Integer.parseInt(st.nextToken());
               boolean val = Boolean.parseBoolean(st.nextToken());
               cond = this.joinAnd(cond, new ConditionPlayerRangeFromNpc(npcIds, radius, val));
            }
         } else if ("callPc".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCallPc(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("canEscape".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanEscape(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("canPossessHolything".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanPossessHolything(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("canRefuelAirship".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanRefuelAirship(Integer.parseInt(a.getNodeValue())));
         } else if ("cansummon".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanSummon(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("cansummonsiegegolem".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanSummonSiegeGolem(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("canSweep".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanSweep(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("canTransform".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanTransform(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("canUntransform".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.parseBoolean(a.getNodeValue())));
         } else if (!"insideZoneId".equalsIgnoreCase(a.getNodeName())) {
            if ("reflectionEntry".equalsIgnoreCase(a.getNodeName())) {
               String val = this.getValue(a.getNodeValue(), template);
               int type = Integer.decode(this.getValue(val.split(":")[0], template));
               int attempts = Integer.decode(this.getValue(val.split(":")[1], template));
               cond = this.joinAnd(cond, new ConditionPlayerReflectionEntry(type, attempts));
            } else if ("canPcBangPoints".equalsIgnoreCase(a.getNodeName())) {
               cond = this.joinAnd(cond, new ConditionPlayerCanTakePcBangPoints(Boolean.parseBoolean(a.getNodeValue())));
            } else if ("isAngelCatEventActive".equalsIgnoreCase(a.getNodeName())) {
               cond = this.joinAnd(cond, new ConditionAngelCatEventActive(Boolean.parseBoolean(a.getNodeValue())));
            } else if ("isInFightEvent".equalsIgnoreCase(a.getNodeName())) {
               cond = this.joinAnd(cond, new ConditionPlayerInFightEvent(Boolean.parseBoolean(a.getNodeValue())));
            } else if ("isValidTarget".equalsIgnoreCase(a.getNodeName())) {
               cond = this.joinAnd(cond, new ConditionTargetWard(Boolean.parseBoolean(a.getNodeValue())));
            } else if ("isInCombat".equalsIgnoreCase(a.getNodeName())) {
               cond = this.joinAnd(cond, new ConditionPlayerCombat(Boolean.parseBoolean(a.getNodeValue())));
            }
         } else {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionPlayerInsideZoneId(array));
         }
      }

      if (cond == null) {
         this._log.severe("Unrecognized <player> condition in " + this._file);
      }

      return cond;
   }

   protected Condition parseTargetCondition(Node n, Object template) {
      Condition cond = null;
      NamedNodeMap attrs = n.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Node a = attrs.item(i);
         if ("aggro".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionTargetAggro(val));
         } else if ("siegezone".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionSiegeZone(Boolean.parseBoolean(a.getNodeValue()), false));
         } else if ("level".equalsIgnoreCase(a.getNodeName())) {
            int lvl = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionTargetLevel(lvl));
         } else if ("levelRange".equalsIgnoreCase(a.getNodeName())) {
            String[] range = this.getValue(a.getNodeValue(), template).split(";");
            if (range.length == 2) {
               int[] lvlRange = new int[]{
                  Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[0]),
                  Integer.decode(this.getValue(a.getNodeValue(), template).split(";")[1])
               };
               cond = this.joinAnd(cond, new ConditionTargetLevelRange(lvlRange));
            }
         } else if ("myPartyExceptMe".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionTargetMyPartyExceptMe(Boolean.parseBoolean(a.getNodeValue())));
         } else if ("playable".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionTargetPlayable());
         } else if ("percentHP".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionTargetPercentHp(this.parseNumber(a.getNodeValue()).intValue()));
         } else if ("percentMP".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionTargetPercentMp(this.parseNumber(a.getNodeValue()).intValue()));
         } else if ("percentCP".equalsIgnoreCase(a.getNodeName())) {
            cond = this.joinAnd(cond, new ConditionTargetPercentCp(this.parseNumber(a.getNodeValue()).intValue()));
         } else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionTargetClassIdRestriction(array));
         } else if ("active_effect_id".equalsIgnoreCase(a.getNodeName())) {
            int effect_id = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
         } else if ("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName())) {
            String val = this.getValue(a.getNodeValue(), template);
            int effect_id = Integer.decode(this.getValue(val.split(",")[0], template));
            int effect_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
            cond = this.joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
         } else if ("active_skill_id".equalsIgnoreCase(a.getNodeName())) {
            int skill_id = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
         } else if ("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName())) {
            String val = this.getValue(a.getNodeValue(), template);
            int skill_id = Integer.decode(this.getValue(val.split(",")[0], template));
            int skill_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
            cond = this.joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
         } else if ("abnormal".equalsIgnoreCase(a.getNodeName())) {
            int abnormalId = Integer.decode(this.getValue(a.getNodeValue(), template));
            cond = this.joinAnd(cond, new ConditionTargetAbnormal(abnormalId));
         } else if ("mindistance".equalsIgnoreCase(a.getNodeName())) {
            int distance = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionMinDistance(distance * distance));
         } else if ("race_id".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionTargetRaceId(array));
         } else if ("races".equalsIgnoreCase(a.getNodeName())) {
            String[] racesVal = a.getNodeValue().split(",");
            Race[] races = new Race[racesVal.length];

            for(int r = 0; r < racesVal.length; ++r) {
               if (racesVal[r] != null) {
                  races[r] = Race.valueOf(racesVal[r]);
               }
            }

            cond = this.joinAnd(cond, new ConditionTargetRace(races));
         } else if ("using".equalsIgnoreCase(a.getNodeName())) {
            int mask = 0;
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();

               for(WeaponType wt : WeaponType.values()) {
                  if (wt.toString().equals(item)) {
                     mask |= wt.mask();
                     break;
                  }
               }

               for(ArmorType at : ArmorType.values()) {
                  if (at.toString().equals(item)) {
                     mask |= at.mask();
                     break;
                  }
               }
            }

            cond = this.joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
         } else if ("npcId".equalsIgnoreCase(a.getNodeName())) {
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
            ArrayList<Integer> array = new ArrayList<>(st.countTokens());

            while(st.hasMoreTokens()) {
               String item = st.nextToken().trim();
               array.add(Integer.decode(this.getValue(item, null)));
            }

            cond = this.joinAnd(cond, new ConditionTargetNpcId(array));
         } else if (!"npcType".equalsIgnoreCase(a.getNodeName())) {
            if ("weight".equalsIgnoreCase(a.getNodeName())) {
               int weight = Integer.decode(this.getValue(a.getNodeValue(), null));
               cond = this.joinAnd(cond, new ConditionTargetWeight(weight));
            } else if ("invSize".equalsIgnoreCase(a.getNodeName())) {
               int size = Integer.decode(this.getValue(a.getNodeValue(), null));
               cond = this.joinAnd(cond, new ConditionTargetInvSize(size));
            }
         } else {
            String values = this.getValue(a.getNodeValue(), template).trim();
            String[] valuesSplit = values.split(",");
            GameObject.InstanceType[] types = new GameObject.InstanceType[valuesSplit.length];

            for(int j = 0; j < valuesSplit.length; ++j) {
               GameObject.InstanceType type = Enum.valueOf(GameObject.InstanceType.class, valuesSplit[j]);
               if (type == null) {
                  throw new IllegalArgumentException("Instance type not recognized: " + valuesSplit[j]);
               }

               types[j] = type;
            }

            cond = this.joinAnd(cond, new ConditionTargetNpcType(types));
         }
      }

      if (cond == null) {
         this._log.severe("Unrecognized <target> condition in " + this._file);
      }

      return cond;
   }

   protected Condition parseUsingCondition(Node n) {
      Condition cond = null;
      NamedNodeMap attrs = n.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Node a = attrs.item(i);
         if (!"kind".equalsIgnoreCase(a.getNodeName())) {
            if ("skill".equalsIgnoreCase(a.getNodeName())) {
               int id = Integer.parseInt(a.getNodeValue());
               cond = this.joinAnd(cond, new ConditionUsingSkill(id));
            } else if ("slotitem".equalsIgnoreCase(a.getNodeName())) {
               StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
               int id = Integer.parseInt(st.nextToken().trim());
               int slot = Integer.parseInt(st.nextToken().trim());
               int enchant = 0;
               if (st.hasMoreTokens()) {
                  enchant = Integer.parseInt(st.nextToken().trim());
               }

               cond = this.joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
            } else if ("weaponChange".equalsIgnoreCase(a.getNodeName())) {
               boolean val = Boolean.parseBoolean(a.getNodeValue());
               cond = this.joinAnd(cond, new ConditionChangeWeapon(val));
            }
         } else {
            int mask = 0;
            StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");

            while(st.hasMoreTokens()) {
               int old = mask;
               String item = st.nextToken().trim();
               if (ItemsParser._weaponTypes.containsKey(item)) {
                  mask |= ItemsParser._weaponTypes.get(item).mask();
               }

               if (ItemsParser._armorTypes.containsKey(item)) {
                  mask |= ItemsParser._armorTypes.get(item).mask();
               }

               if (old == mask) {
                  this._log.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
               }
            }

            cond = this.joinAnd(cond, new ConditionUsingItemType(mask));
         }
      }

      if (cond == null) {
         this._log.severe("Unrecognized <using> condition in " + this._file);
      }

      return cond;
   }

   protected Condition parseGameCondition(Node n) {
      Condition cond = null;
      NamedNodeMap attrs = n.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Node a = attrs.item(i);
         if ("skill".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionWithSkill(val));
         }

         if ("night".equalsIgnoreCase(a.getNodeName())) {
            boolean val = Boolean.parseBoolean(a.getNodeValue());
            cond = this.joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, val));
         }

         if ("chance".equalsIgnoreCase(a.getNodeName())) {
            int val = Integer.decode(this.getValue(a.getNodeValue(), null));
            cond = this.joinAnd(cond, new ConditionGameChance(val));
         }
      }

      if (cond == null) {
         this._log.severe("Unrecognized <game> condition in " + this._file);
      }

      return cond;
   }

   protected void parseTable(Node n) {
      NamedNodeMap attrs = n.getAttributes();
      String name = attrs.getNamedItem("name").getNodeValue();
      if (name.charAt(0) != '#') {
         throw new IllegalArgumentException("Table name must start with #");
      } else {
         StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
         List<String> array = new ArrayList<>(data.countTokens());

         while(data.hasMoreTokens()) {
            array.add(data.nextToken());
         }

         this.setTable(name, array.toArray(new String[array.size()]));
      }
   }

   protected void parseBeanSet(Node n, StatsSet set, Integer level) {
      String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
      String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
      char ch = value.isEmpty() ? 32 : value.charAt(0);
      if (ch != '#' && ch != '-' && !Character.isDigit(ch)) {
         set.set(name, value);
      } else {
         set.set(name, String.valueOf(this.getValue(value, level)));
      }
   }

   protected void setExtractableSkillData(StatsSet set, String value) {
      set.set("capsuled_items_skill", value);
   }

   protected Lambda getLambda(Node n, Object template) {
      Node nval = n.getAttributes().getNamedItem("val");
      if (nval != null) {
         String val = nval.getNodeValue();
         if (val.charAt(0) == '#') {
            return new LambdaConst(Double.parseDouble(this.getTableValue(val)));
         } else if (val.charAt(0) == '$') {
            if (val.equalsIgnoreCase("$player_level")) {
               return new LambdaStats(LambdaStats.StatsType.PLAYER_LEVEL);
            } else if (val.equalsIgnoreCase("$target_level")) {
               return new LambdaStats(LambdaStats.StatsType.TARGET_LEVEL);
            } else if (val.equalsIgnoreCase("$player_max_hp")) {
               return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_HP);
            } else if (val.equalsIgnoreCase("$player_max_mp")) {
               return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_MP);
            } else {
               StatsSet set = this.getStatsSet();
               String field = set.getString(val.substring(1));
               if (field != null) {
                  return new LambdaConst(Double.parseDouble(this.getValue(field, template)));
               } else {
                  throw new IllegalArgumentException("Unknown value " + val);
               }
            }
         } else {
            return new LambdaConst(Double.parseDouble(val));
         }
      } else {
         LambdaCalc calc = new LambdaCalc();
         n = n.getFirstChild();

         while(n != null && n.getNodeType() != 1) {
            n = n.getNextSibling();
         }

         if (n != null && "val".equals(n.getNodeName())) {
            for(Node var8 = n.getFirstChild(); var8 != null; var8 = var8.getNextSibling()) {
               if (var8.getNodeType() == 1) {
                  this.attachLambdaFunc(var8, template, calc);
               }
            }

            return calc;
         } else {
            throw new IllegalArgumentException("Value not specified");
         }
      }
   }

   protected String getValue(String value, Object template) {
      if (value.charAt(0) == '#') {
         if (template instanceof Skill) {
            return this.getTableValue(value);
         } else if (template instanceof Integer) {
            return this.getTableValue(value, (Integer)template);
         } else {
            throw new IllegalStateException();
         }
      } else {
         return value;
      }
   }

   protected Number parseNumber(String value) {
      if (value.charAt(0) == '#') {
         value = this.getTableValue(value).toString();
      }

      try {
         if (value.equalsIgnoreCase("max")) {
            return Double.POSITIVE_INFINITY;
         } else if (value.equalsIgnoreCase("min")) {
            return Double.NEGATIVE_INFINITY;
         } else if (value.indexOf(46) == -1) {
            int radix = 10;
            if (value.length() > 2 && value.substring(0, 2).equalsIgnoreCase("0x")) {
               value = value.substring(2);
               radix = 16;
            }

            return Integer.valueOf(value, radix);
         } else {
            return Double.valueOf(value);
         }
      } catch (NumberFormatException var3) {
         return null;
      }
   }

   protected Condition joinAnd(Condition cond, Condition c) {
      if (cond == null) {
         return c;
      } else if (cond instanceof ConditionLogicAnd) {
         ((ConditionLogicAnd)cond).add(c);
         return cond;
      } else {
         ConditionLogicAnd and = new ConditionLogicAnd();
         and.add(cond);
         and.add(c);
         return and;
      }
   }
}
