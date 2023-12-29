package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.skills.options.Options;
import l2e.gameserver.model.skills.options.OptionsSkillHolder;
import l2e.gameserver.model.skills.options.OptionsSkillType;
import l2e.gameserver.model.stats.Stats;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class OptionsParser extends DocumentParser {
   private final Map<Integer, Options> _data = new HashMap<>();

   protected OptionsParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDirectory("data/stats/skills/options", false);
      this._log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._data.size() + " options.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("option".equalsIgnoreCase(d.getNodeName())) {
                  int id = parseInt(d.getAttributes(), "id");
                  Options op = new Options(id);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     String var6 = cd.getNodeName();
                     switch(var6) {
                        case "for":
                           for(Node fd = cd.getFirstChild(); fd != null; fd = fd.getNextSibling()) {
                              String var9 = fd.getNodeName();
                              switch(var9) {
                                 case "add":
                                    this.parseFuncs(fd.getAttributes(), "Add", op);
                                    break;
                                 case "mul":
                                    this.parseFuncs(fd.getAttributes(), "Mul", op);
                                    break;
                                 case "basemul":
                                    this.parseFuncs(fd.getAttributes(), "BaseMul", op);
                                    break;
                                 case "sub":
                                    this.parseFuncs(fd.getAttributes(), "Sub", op);
                                    break;
                                 case "div":
                                    this.parseFuncs(fd.getAttributes(), "Div", op);
                                    break;
                                 case "set":
                                    this.parseFuncs(fd.getAttributes(), "Set", op);
                              }
                           }
                           break;
                        case "active_skill":
                           op.setActiveSkill(new SkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level")));
                           break;
                        case "passive_skill":
                           op.setPassiveSkill(new SkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level")));
                           break;
                        case "attack_skill":
                           op.addActivationSkill(
                              new OptionsSkillHolder(
                                 parseInt(cd.getAttributes(), "id"),
                                 parseInt(cd.getAttributes(), "level"),
                                 parseDouble(cd.getAttributes(), "chance"),
                                 OptionsSkillType.ATTACK
                              )
                           );
                           break;
                        case "magic_skill":
                           op.addActivationSkill(
                              new OptionsSkillHolder(
                                 parseInt(cd.getAttributes(), "id"),
                                 parseInt(cd.getAttributes(), "level"),
                                 parseDouble(cd.getAttributes(), "chance"),
                                 OptionsSkillType.MAGIC
                              )
                           );
                           break;
                        case "critical_skill":
                           op.addActivationSkill(
                              new OptionsSkillHolder(
                                 parseInt(cd.getAttributes(), "id"),
                                 parseInt(cd.getAttributes(), "level"),
                                 parseDouble(cd.getAttributes(), "chance"),
                                 OptionsSkillType.CRITICAL
                              )
                           );
                     }
                  }

                  this._data.put(op.getId(), op);
               }
            }
         }
      }
   }

   private void parseFuncs(NamedNodeMap attrs, String func, Options op) {
      Stats stat = Stats.valueOfXml(parseString(attrs, "stat"));
      int ord = Integer.decode(parseString(attrs, "order"));
      double val = parseDouble(attrs, "val");
      op.addFunc(new FuncTemplate(null, null, func, stat, ord, new LambdaConst(val)));
   }

   public Options getOptions(int id) {
      return this._data.get(id);
   }

   public Collection<Options> getUniqueOptions(Options.AugmentationFilter filter) {
      switch(filter) {
         case ACTIVE_SKILL:
            Map<Integer, Options> options = new HashMap<>();

            for(Options option : this._data.values()) {
               if (!option.hasActivationSkills() && !option.hasPassiveSkill() && option.hasActiveSkill()) {
                  for(int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST) {
                     if (id == option.getId()) {
                     }
                  }

                  if (!options.containsKey(option.getActiveSkill().getId())
                     || options.get(option.getActiveSkill().getId()).getActiveSkill().getLvl() < option.getActiveSkill().getLvl()) {
                     options.put(option.getActiveSkill().getId(), option);
                  }
               }
            }

            List<Options> augs = new ArrayList<>(options.values());
            Collections.sort(augs, new OptionsParser.ActiveSkillsComparator());
            return augs;
         case PASSIVE_SKILL:
            Map<Integer, Options> options = new HashMap<>();

            for(Options option : this._data.values()) {
               if (!option.hasActivationSkills() && !option.hasActiveSkill() && option.hasPassiveSkill()) {
                  for(int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST) {
                     if (id == option.getId()) {
                     }
                  }

                  if (!options.containsKey(option.getPassiveSkill().getId())
                     || options.get(option.getPassiveSkill().getId()).getPassiveSkill().getLvl() < option.getPassiveSkill().getLvl()) {
                     options.put(option.getPassiveSkill().getId(), option);
                  }
               }
            }

            List<Options> augs = new ArrayList<>(options.values());
            Collections.sort(augs, new OptionsParser.PassiveSkillsComparator());
            return augs;
         case CHANCE_SKILL:
            Map<Integer, Options> options = new HashMap<>();

            for(Options option : this._data.values()) {
               if (!option.hasPassiveSkill() && !option.hasActiveSkill() && option.hasActivationSkills()) {
                  for(int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST) {
                     if (id == option.getId()) {
                     }
                  }

                  if (!options.containsKey(option.getActivationsSkills().get(0).getId())
                     || options.get(option.getActivationsSkills().get(0).getId()).getActivationsSkills().get(0).getLvl()
                        < option.getActivationsSkills().get(0).getLvl()) {
                     options.put(option.getActivationsSkills().get(0).getId(), option);
                  }
               }
            }

            List<Options> augs = new ArrayList<>(options.values());
            Collections.sort(augs, new OptionsParser.ChanceSkillsComparator());
            return augs;
         case STATS:
            Map<Integer, Options> options = new HashMap<>();

            for(Options option : this._data.values()) {
               for(int id : Config.SERVICES_AUGMENTATION_DISABLED_LIST) {
                  if (id == option.getId()) {
                  }
               }

               switch(option.getId()) {
                  case 16341:
                  case 16342:
                  case 16343:
                  case 16344:
                     options.put(option.getId(), option);
               }
            }

            List<Options> augs = new ArrayList<>(options.values());
            return augs;
         default:
            return this._data.values();
      }
   }

   public Collection<Options> getUniqueAvailableOptions(Options.AugmentationFilter filter) {
      if (filter == Options.AugmentationFilter.NONE) {
         return this._data.values();
      } else {
         List<Options> augs = null;
         switch(filter) {
            case ACTIVE_SKILL:
               Map<Integer, Options> options = new HashMap<>();

               for(Options option : this._data.values()) {
                  if (!option.hasActivationSkills()
                     && !option.hasPassiveSkill()
                     && option.hasActiveSkill()
                     && (
                        !options.containsKey(option.getActiveSkill().getId())
                           || options.get(option.getActiveSkill().getId()).getActiveSkill().getLvl() < option.getActiveSkill().getLvl()
                     )) {
                     for(int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST) {
                        if (id == option.getId()) {
                           options.put(option.getActiveSkill().getId(), option);
                        }
                     }
                  }
               }

               augs = new ArrayList<>(options.values());
               Collections.sort(augs, new OptionsParser.ActiveSkillsComparator());
               break;
            case PASSIVE_SKILL:
               Map<Integer, Options> options = new HashMap<>();

               for(Options option : this._data.values()) {
                  if (!option.hasActivationSkills()
                     && !option.hasActiveSkill()
                     && option.hasPassiveSkill()
                     && (
                        !options.containsKey(option.getPassiveSkill().getId())
                           || options.get(option.getPassiveSkill().getId()).getPassiveSkill().getLvl() < option.getPassiveSkill().getLvl()
                     )) {
                     for(int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST) {
                        if (id == option.getId()) {
                           options.put(option.getPassiveSkill().getId(), option);
                        }
                     }
                  }
               }

               augs = new ArrayList<>(options.values());
               Collections.sort(augs, new OptionsParser.PassiveSkillsComparator());
               break;
            case CHANCE_SKILL:
               Map<Integer, Options> options = new HashMap<>();

               for(Options option : this._data.values()) {
                  if (!option.hasPassiveSkill()
                     && !option.hasActiveSkill()
                     && option.hasActivationSkills()
                     && (
                        !options.containsKey(option.getActivationsSkills().get(0).getId())
                           || options.get(option.getActivationsSkills().get(0).getId()).getActivationsSkills().get(0).getLvl()
                              < option.getActivationsSkills().get(0).getLvl()
                     )) {
                     for(int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST) {
                        if (id == option.getId()) {
                           options.put(option.getActivationsSkills().get(0).getId(), option);
                        }
                     }
                  }
               }

               augs = new ArrayList<>(options.values());
               Collections.sort(augs, new OptionsParser.ChanceSkillsComparator());
               break;
            case STATS:
               Map<Integer, Options> options = new HashMap<>();

               for(Options option : this._data.values()) {
                  switch(option.getId()) {
                     case 16341:
                     case 16342:
                     case 16343:
                     case 16344:
                        for(int id : Config.SERVICES_AUGMENTATION_AVAILABLE_LIST) {
                           if (id == option.getId()) {
                              options.put(option.getId(), option);
                           }
                        }
                  }
               }

               augs = new ArrayList<>(options.values());
         }

         return augs;
      }
   }

   public static final OptionsParser getInstance() {
      return OptionsParser.SingletonHolder._instance;
   }

   protected static class ActiveSkillsComparator implements Comparator<Options> {
      public int compare(Options left, Options right) {
         return left.hasActiveSkill() && right.hasActiveSkill() ? Integer.valueOf(left.getActiveSkill().getId()).compareTo(right.getActiveSkill().getId()) : 0;
      }
   }

   protected static class ChanceSkillsComparator implements Comparator<Options> {
      public int compare(Options left, Options right) {
         return left.hasActivationSkills() && right.hasActivationSkills()
            ? Integer.valueOf(left.getActivationsSkills().get(0).getId()).compareTo(right.getActivationsSkills().get(0).getId())
            : 0;
      }
   }

   protected static class PassiveSkillsComparator implements Comparator<Options> {
      public int compare(Options left, Options right) {
         return left.hasPassiveSkill() && right.hasPassiveSkill()
            ? Integer.valueOf(left.getPassiveSkill().getId()).compareTo(right.getPassiveSkill().getId())
            : 0;
      }
   }

   private static class SingletonHolder {
      protected static final OptionsParser _instance = new OptionsParser();
   }
}
