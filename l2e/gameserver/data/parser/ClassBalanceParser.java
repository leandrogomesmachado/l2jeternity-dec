package l2e.gameserver.data.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.base.AttackType;
import l2e.gameserver.model.holders.ClassBalanceHolder;
import org.w3c.dom.Node;

public class ClassBalanceParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(ClassBalanceParser.class.getName());
   private final Map<String, ClassBalanceHolder> _classes = new ConcurrentHashMap<>();
   private int _balanceSize = 0;
   private int _olyBalanceSize = 0;
   private boolean _hasModify = false;

   public ClassBalanceParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("./data/stats/services/balancer/classBalance.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._classes.size() + " balanced classe(s).");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node o = this.getCurrentDocument().getFirstChild(); o != null; o = o.getNextSibling()) {
         if ("list".equalsIgnoreCase(o.getNodeName())) {
            for(Node d = o.getFirstChild(); d != null; d = d.getNextSibling()) {
               if (d.getNodeName().equals("balance")) {
                  int classId = Integer.parseInt(d.getAttributes().getNamedItem("classId").getNodeValue());
                  int targetClassId = Integer.parseInt(d.getAttributes().getNamedItem("targetClassId").getNodeValue());
                  ClassBalanceHolder cbh = new ClassBalanceHolder(classId, targetClassId);

                  for(Node set = d.getFirstChild(); set != null; set = set.getNextSibling()) {
                     if (set.getNodeName().equals("set")) {
                        double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                        AttackType atkType = AttackType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                        cbh.addNormalBalance(atkType, val);
                        ++this._balanceSize;
                     } else if (set.getNodeName().equals("olyset")) {
                        double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                        AttackType atkType = AttackType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                        cbh.addOlyBalance(atkType, val);
                        ++this._olyBalanceSize;
                     }
                  }

                  this._classes.put(classId + ";" + targetClassId, cbh);
               }
            }
         }
      }
   }

   public Map<String, ClassBalanceHolder> getAllBalances() {
      Map<String, ClassBalanceHolder> map = new TreeMap<>(new ClassBalanceParser.ClassComparator());
      map.putAll(this._classes);
      return map;
   }

   public List<ClassBalanceHolder> getClassBalances(int classId) {
      List<ClassBalanceHolder> list = new ArrayList<>();

      for(Entry<String, ClassBalanceHolder> data : this._classes.entrySet()) {
         if (Integer.valueOf(data.getKey().split(";")[0]) == classId) {
            list.add(data.getValue());
         }
      }

      return list;
   }

   public int getClassBalanceSize(int classId, boolean olysize) {
      int size = 0;

      for(ClassBalanceHolder data : this.getClassBalances(classId)) {
         size += !olysize ? data.getNormalBalance().size() : data.getOlyBalance().size();
      }

      return size;
   }

   public ClassBalanceHolder getBalanceHolder(String key) {
      return this._classes.get(key);
   }

   public double getBalancedClass(AttackType type, Creature attacker, Creature victim) {
      if (Config.BALANCER_ALLOW) {
         if (attacker instanceof Player && victim instanceof Player) {
            int classId = attacker.getActingPlayer().getClassId().getId();
            int targetClassId = victim.getActingPlayer().getClassId().getId();
            if (attacker.getActingPlayer().isInOlympiadMode() && victim.getActingPlayer().isInOlympiadMode()) {
               if (attacker.getActingPlayer().getOlympiadGameId() == victim.getActingPlayer().getOlympiadGameId()
                  && this._classes.containsKey(classId + ";" + targetClassId)) {
                  return this._classes.get(classId + ";" + targetClassId).getOlyBalanceValue(type);
               }

               return this._classes.containsKey(classId + ";-2") ? this._classes.get(classId + ";-2").getOlyBalanceValue(type) : 1.0;
            }

            if (this._classes.containsKey(classId + ";" + targetClassId)) {
               return this._classes.get(classId + ";" + targetClassId).getBalanceValue(type);
            }

            return this._classes.containsKey(classId + ";-2") ? this._classes.get(classId + ";-2").getBalanceValue(type) : 1.0;
         }

         if (attacker instanceof Player && victim instanceof MonsterInstance) {
            int classId = attacker.getActingPlayer().getClassId().getId();
            if (this._classes.containsKey(classId + ";-1")) {
               return this._classes.get(classId + ";-1").getBalanceValue(type);
            }
         }
      }

      return 1.0;
   }

   public void removeClassBalance(String key, AttackType type, boolean isOly) {
      if (this._classes.containsKey(key)) {
         if (!this._hasModify) {
            this._hasModify = true;
         }

         if (isOly) {
            this._classes.get(key).removeOlyBalance(type);
            --this._olyBalanceSize;
            return;
         }

         this._classes.get(key).remove(type);
         --this._balanceSize;
      }
   }

   public void addClassBalance(String key, ClassBalanceHolder cbh, boolean isEdit) {
      if (!this._hasModify) {
         this._hasModify = true;
      }

      this._classes.put(key, cbh);
      if (!isEdit) {
         if (!cbh.getOlyBalance().isEmpty()) {
            ++this._olyBalanceSize;
         } else {
            ++this._balanceSize;
         }
      }
   }

   public void store(Player player) {
      if (!this._hasModify) {
         if (player != null) {
            player.sendMessage("ClassBalance: Nothing for saving!");
         }
      } else {
         try {
            File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/balancer/classBalance.xml");
            if (file.exists()
               && !file.renameTo(
                  new File(
                     Config.DATAPACK_ROOT
                        + "/stats/services/balancer/classBalance_Backup_["
                        + new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss").format(Long.valueOf(Calendar.getInstance().getTimeInMillis()))
                        + "].xml"
                  )
               )
               && player != null) {
               player.sendMessage("ClassBalance: can't save backup file!");
            }

            file = new File(Config.DATAPACK_ROOT + "/data/stats/services/balancer/classBalance.xml");
            file.createNewFile();
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../../../xsd/classBalance.xsd\">\n");

            for(ClassBalanceHolder cbh : this._classes.values()) {
               if (!cbh.getNormalBalance().isEmpty() || !cbh.getOlyBalance().isEmpty()) {
                  String xml = "\t<balance classId=\"" + cbh.getActiveClass() + "\" targetClassId=\"" + cbh.getTargetClass() + "\">\n";

                  for(Entry<AttackType, Double> info : cbh.getNormalBalance().entrySet()) {
                     xml = xml + "\t\t<set type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n";
                  }

                  for(Entry<AttackType, Double> info : cbh.getOlyBalance().entrySet()) {
                     xml = xml + "\t\t<olyset type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n";
                  }

                  xml = xml + "\t</balance>\n";
                  out.write(xml);
               }
            }

            out.write("</list>");
            out.close();
         } catch (Exception var10) {
            var10.printStackTrace();
         }

         if (player != null) {
            player.sendMessage("ClassBalance: Modified data was saved!");
         }

         this._hasModify = false;
      }
   }

   public int getSize(boolean olysize) {
      return olysize ? this._olyBalanceSize : this._balanceSize;
   }

   public static final ClassBalanceParser getInstance() {
      return ClassBalanceParser.SingletonHolder._instance;
   }

   private class ClassComparator implements Comparator<String> {
      public ClassComparator() {
      }

      public int compare(String l, String r) {
         int left = Integer.valueOf(l.split(";")[0]);
         int right = Integer.valueOf(r.split(";")[0]);
         if (left > right) {
            return 1;
         } else if (left < right) {
            return -1;
         } else if (Integer.valueOf(l.split(";")[1]) > Integer.valueOf(r.split(";")[1])) {
            return 1;
         } else if (Integer.valueOf(r.split(";")[1]) > Integer.valueOf(l.split(";")[1])) {
            return -1;
         } else {
            Random x = new Random();
            return x.nextInt(2) == 1 ? 1 : 1;
         }
      }
   }

   private static class SingletonHolder {
      protected static final ClassBalanceParser _instance = new ClassBalanceParser();
   }
}
