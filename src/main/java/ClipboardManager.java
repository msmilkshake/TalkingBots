import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClipboardManager {
    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private StringSelection cbData;
    
    public ClipboardManager() {
        cbData =  getClipboardString();
    }
    
    private StringSelection getClipboardString() {
        String cbStr = "";
        try {
            cbStr = (String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new StringSelection(cbStr);
    }
    
    public void putInClipboard(String str) {
        cbData = getClipboardString();
        StringSelection ss = new StringSelection(str);
        setContents(ss);
    }
    
    public void restoreClipboard() {
        setContents(cbData);
    }
    
    private void setContents(StringSelection ss) {
        clipboard.setContents(ss, null);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
