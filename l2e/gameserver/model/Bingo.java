package l2e.gameserver.model;

import java.util.ArrayList;
import l2e.commons.util.Rnd;

public class Bingo {
   protected static final String template = "%msg%<br><br>%choices%<br><br>%board%";
   protected static final String template_final = "%msg%<br><br>%board%";
   protected static final String template_board = "For your information, below is your current selection.<br><table border=\"1\" border color=\"white\" width=100><tr><td align=\"center\">%cell1%</td><td align=\"center\">%cell2%</td><td align=\"center\">%cell3%</td></tr><tr><td align=\"center\">%cell4%</td><td align=\"center\">%cell5%</td><td align=\"center\">%cell6%</td></tr><tr><td align=\"center\">%cell7%</td><td align=\"center\">%cell8%</td><td align=\"center\">%cell9%</td></tr></table>";
   protected static final String msg_again = "You have already selected that number. Choose your %choicenum% number again.";
   protected static final String msg_begin = "I've arranged 9 numbers on the panel.<br>Now, select your %choicenum% number.";
   protected static final String msg_next = "Now, choose your %choicenum% number.";
   protected static final String msg_0lines = "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?";
   protected static final String msg_3lines = "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations!";
   protected static final String msg_lose = "Hmm... You didn't make 3 lines. Why don't you try again? The red-colored numbers on the panel are the ones you chose.";
   protected static final String[] nums = new String[]{"first", "second", "third", "fourth", "fifth", "final"};
   protected int lines;
   private final String _template_choice;
   private final ArrayList<Integer> board = new ArrayList<>();
   private final ArrayList<Integer> guesses = new ArrayList<>();

   public Bingo(String template_choice) {
      this._template_choice = template_choice;

      while(this.board.size() < 9) {
         int num = Rnd.get(1, 9);
         if (!this.board.contains(num)) {
            this.board.add(num);
         }
      }
   }

   public String Select(String s) {
      try {
         return this.Select(Integer.valueOf(s));
      } catch (Exception var3) {
         return null;
      }
   }

   public String Select(int choise) {
      if (choise >= 1 && choise <= 9) {
         if (this.guesses.contains(choise)) {
            return this.getDialog("You have already selected that number. Choose your %choicenum% number again.");
         } else {
            this.guesses.add(choise);
            return this.guesses.size() == 6 ? this.getFinal() : this.getDialog("");
         }
      } else {
         return null;
      }
   }

   protected String getBoard() {
      if (this.guesses.size() == 0) {
         return "";
      } else {
         String result = "For your information, below is your current selection.<br><table border=\"1\" border color=\"white\" width=100><tr><td align=\"center\">%cell1%</td><td align=\"center\">%cell2%</td><td align=\"center\">%cell3%</td></tr><tr><td align=\"center\">%cell4%</td><td align=\"center\">%cell5%</td><td align=\"center\">%cell6%</td></tr><tr><td align=\"center\">%cell7%</td><td align=\"center\">%cell8%</td><td align=\"center\">%cell9%</td></tr></table>";

         for(int i = 1; i <= 9; ++i) {
            String cell = "%cell" + String.valueOf(i) + "%";
            int num = this.board.get(i - 1);
            if (this.guesses.contains(num)) {
               result = result.replaceFirst(cell, "<font color=\"" + (this.guesses.size() == 6 ? "ff0000" : "ffff00") + "\">" + num + "</font>");
            } else {
               result = result.replaceFirst(cell, "?");
            }
         }

         return result;
      }
   }

   public String getDialog(String _msg) {
      String result = "<html><body>%msg%<br><br>%choices%<br><br>%board%</body></html>";
      if (this.guesses.size() == 0) {
         result = result.replaceFirst("%msg%", "I've arranged 9 numbers on the panel.<br>Now, select your %choicenum% number.");
      } else {
         result = result.replaceFirst("%msg%", _msg.equalsIgnoreCase("") ? "Now, choose your %choicenum% number." : _msg);
      }

      result = result.replaceFirst("%choicenum%", nums[this.guesses.size()]);
      StringBuilder choices = new StringBuilder();

      for(int i = 1; i <= 9; ++i) {
         if (!this.guesses.contains(i)) {
            choices.append(this._template_choice.replaceAll("%n%", String.valueOf(i)));
         }
      }

      result = result.replaceFirst("%choices%", choices.toString());
      return result.replaceFirst("%board%", this.getBoard());
   }

   protected String getFinal() {
      String result = "<html><body>%msg%<br><br>%board%</body></html>".replaceFirst("%board%", this.getBoard());
      this.calcLines();
      if (this.lines == 3) {
         result = result.replaceFirst(
            "%msg%", "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations!"
         );
      } else if (this.lines == 0) {
         result = result.replaceFirst(
            "%msg%",
            "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?"
         );
      } else {
         result = result.replaceFirst(
            "%msg%", "Hmm... You didn't make 3 lines. Why don't you try again? The red-colored numbers on the panel are the ones you chose."
         );
      }

      return result;
   }

   public int calcLines() {
      this.lines = 0;
      this.lines += this.checkLine(0, 1, 2) ? 1 : 0;
      this.lines += this.checkLine(3, 4, 5) ? 1 : 0;
      this.lines += this.checkLine(6, 7, 8) ? 1 : 0;
      this.lines += this.checkLine(0, 3, 6) ? 1 : 0;
      this.lines += this.checkLine(1, 4, 7) ? 1 : 0;
      this.lines += this.checkLine(2, 5, 8) ? 1 : 0;
      this.lines += this.checkLine(0, 4, 8) ? 1 : 0;
      this.lines += this.checkLine(2, 4, 6) ? 1 : 0;
      return this.lines;
   }

   public boolean checkLine(int idx1, int idx2, int idx3) {
      return this.guesses.contains(this.board.get(idx1)) && this.guesses.contains(this.board.get(idx2)) && this.guesses.contains(this.board.get(idx3));
   }
}
