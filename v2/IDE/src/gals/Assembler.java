package gals;

/**
 * Assembler Class
 * Used to construct BIP assembly code from PreLang code
 */
public class Assembler {

  // Data section (variables declaration)
  private String data;
  
  // Text section (commands)
  private String text;

  /**
   * Default constructor
   */
  public Assembler() {
    this.data = "";
    this.text = ".text\n";
    addToText("JMP", "_main");
  }

  /**
   * Joins and returns the full assemly code (data + text)
   * @return The full assembly code
   */
  public String getCode() {
    return data + "\n" + text + "    " + "HLT\t0";
  }

  /**
   * Adds a line to the data section
   * @param a Left side of the command (variable name)
   * @param b  Right side of the command (initial value)
   */
  public void addToData(String a, String b) {
    if (data.equals(""))
      data = ".data\n";
    this.data += "    " + removePrefixes(a) + " : " + b + "\n";
  }
  
  /**
   * Adds a line to the text section
   * @param a Left side of the command (mnemonic)
   * @param b Right side of the command
   */
  public void addToText(String a, String b) {
    this.text += "    " + a + "\t" + removePrefixes(b) + "\n";
  }
  
  /**
   * Adds a label to the text section
   * @param label Label name
   */
  public void addLabel(String label) {
    this.text += label + ":\n";
  }
  
  /**
   * Removes @ and $ prefixes from a string
   * @param string String to remove prefixes
   * @return The formatted string
   */
  private String removePrefixes(String string) {
    // If it's one of the BIP ports, don't remove $
    if (string.equals("$in_port") || string.equals("$out_port"))
      return string;
    
    return string.replaceAll("[$|@]", "");
  }
}
