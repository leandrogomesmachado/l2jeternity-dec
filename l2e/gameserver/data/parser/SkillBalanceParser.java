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
import l2e.gameserver.model.base.SkillChangeType;
import l2e.gameserver.model.holders.SkillBalanceHolder;
import org.w3c.dom.Node;

public class SkillBalanceParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(SkillBalanceParser.class.getName());
   private final Map<String, SkillBalanceHolder> _skills = new ConcurrentHashMap<>();
   private int _balanceSize = 0;
   private int _olyBalanceSize = 0;
   private boolean _hasModify = false;

   public SkillBalanceParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("./data/stats/services/balancer/skillBalance.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._skills.size() + " balanced skill(s).");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      this._balanceSize = 0;
      this._olyBalanceSize = 0;
      this._hasModify = false;

      for(Node o = this.getCurrentDocument().getFirstChild(); o != null; o = o.getNextSibling()) {
         if ("list".equalsIgnoreCase(o.getNodeName())) {
            for(Node d = o.getFirstChild(); d != null; d = d.getNextSibling()) {
               if (d.getNodeName().equals("balance")) {
                  int skillId = Integer.parseInt(d.getAttributes().getNamedItem("skillId").getNodeValue());
                  int target = Integer.parseInt(d.getAttributes().getNamedItem("target").getNodeValue());
                  SkillBalanceHolder cbh = new SkillBalanceHolder(skillId, target);

                  for(Node set = d.getFirstChild(); set != null; set = set.getNextSibling()) {
                     if (set.getNodeName().equals("set")) {
                        double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                        SkillChangeType atkType = SkillChangeType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                        cbh.addSkillBalance(atkType, val);
                        ++this._balanceSize;
                     } else if (set.getNodeName().equals("olyset")) {
                        double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                        SkillChangeType atkType = SkillChangeType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                        cbh.addOlySkillBalance(atkType, val);
                        ++this._olyBalanceSize;
                     }
                  }

                  this._skills.put(skillId + ";" + target, cbh);
               }
            }
         }
      }
   }

   public void removeSkillBalance(String key, SkillChangeType type, boolean isOly) {
      if (!this._hasModify) {
         this._hasModify = true;
      }

      if (this._skills.containsKey(key)) {
         if (isOly) {
            this._skills.get(key).removeOly(type);
            --this._olyBalanceSize;
            return;
         }

         this._skills.get(key).remove(type);
         --this._balanceSize;
      }
   }

   public void addSkillBalance(String skill, SkillBalanceHolder sbh, boolean isEdit) {
      if (!this._hasModify) {
         this._hasModify = true;
      }

      this._skills.put(skill, sbh);
      if (!isEdit) {
         if (!sbh.getOlyBalance().isEmpty()) {
            ++this._olyBalanceSize;
         } else {
            ++this._balanceSize;
         }
      }
   }

   public Map<String, SkillBalanceHolder> getAllBalances() {
      Map<String, SkillBalanceHolder> map = new TreeMap<>(new SkillBalanceParser.SkillComparator());
      map.putAll(this._skills);
      return map;
   }

   public List<SkillBalanceHolder> getSkillBalances(int skillId) {
      List<SkillBalanceHolder> list = new ArrayList<>();

      for(Entry<String, SkillBalanceHolder> data : this._skills.entrySet()) {
         if (Integer.valueOf(data.getKey().split(";")[0]) == skillId) {
            list.add(data.getValue());
         }
      }

      return list;
   }

   public int getSkillBalanceSize(int skillId, boolean olysize) {
      int size = 0;

      for(SkillBalanceHolder data : this.getSkillBalances(skillId)) {
         size += !olysize ? data.getNormalBalance().size() : data.getOlyBalance().size();
      }

      return size;
   }

   public double getSkillValue(String sk, SkillChangeType sct, Creature victim) {
      if (Config.BALANCER_ALLOW && (this._skills.containsKey(sk) || this._skills.containsKey(sk.split(";")[0] + ";-2"))) {
         if (!sk.split(";")[1].equals("-2") && !this._skills.containsKey(sk)) {
            sk = sk.split(";")[0] + ";-2";
         }

         if (victim != null || sct.isForceCheck()) {
            if (victim instanceof Player
               && victim.getActingPlayer().isOlympiadStart()
               && victim.getActingPlayer().getOlympiadGameId() != -1
               && this._skills.containsKey(sk)) {
               return this._skills.get(sk).getOlyBalanceValue(sct);
            }

            return this._skills.get(sk).getValue(sct);
         }
      }

      return 1.0;
   }

   public int getSize(boolean olysize) {
      return olysize ? this._olyBalanceSize : this._balanceSize;
   }

   public SkillBalanceHolder getSkillHolder(String key) {
      return this._skills.get(key);
   }

   public void store(Player player) {
      if (!this._hasModify) {
         if (player != null) {
            player.sendMessage("SkillBalance: Nothing for saving!");
         }
      } else {
         try {
            File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/balancer/skillBalance.xml");
            if (file.exists()
               && !file.renameTo(
                  new File(
                     Config.DATAPACK_ROOT
                        + "/data/stats/services/balancer/skillBalance_Backup_["
                        + new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss").format(Long.valueOf(Calendar.getInstance().getTimeInMillis()))
                        + "].xml"
                  )
               )
               && player != null) {
               player.sendMessage("SkillBalance: can't save backup file!");
            }

            file = new File(Config.DATAPACK_ROOT + "/data/stats/services/balancer/skillBalance.xml");
            file.createNewFile();
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"../../../xsd/skillBalance.xsd\">\n");

            for(SkillBalanceHolder cbh : this._skills.values()) {
               if (!cbh.getNormalBalance().isEmpty() || !cbh.getOlyBalance().isEmpty()) {
                  String xml = "\t<balance skillId=\"" + cbh.getSkillId() + "\" target=\"" + cbh.getTarget() + "\">\n";

                  for(Entry<SkillChangeType, Double> info : cbh.getNormalBalance().entrySet()) {
                     xml = xml + "\t\t<set type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n";
                  }

                  for(Entry<SkillChangeType, Double> info : cbh.getOlyBalance().entrySet()) {
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
            player.sendMessage("SkillBalance: Modified data was saved!");
         }

         this._hasModify = false;
      }
   }

   public static final SkillBalanceParser getInstance() {
      return SkillBalanceParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SkillBalanceParser _instance = new SkillBalanceParser();
   }

   private class SkillComparator implements Comparator<String> {
      public SkillComparator() {
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
}
