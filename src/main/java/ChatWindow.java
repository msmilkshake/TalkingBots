import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.util.Scanner;

public class ChatWindow {
    
    private int fontSize;
    
    private int lines;
    private int chars;
    
    private Scanner scanner;
    
    private volatile boolean blinkingCaret;
    
    private volatile String initial;
    private volatile StyledDocument doc;
    private volatile Style style;
    
    private JTextPane text;
    private JFrame frame;
    // color Dark Blue = "#002344"
    
    public ChatWindow() {
        
        fontSize = 36;
        
        lines = 26;
        chars = 42;
        
        int frameWidth = 22 * chars + 56;
        int frameHeight = fontSize * lines + 56;
        
        scanner = new Scanner(System.in);
        blinkingCaret = false;
        
        initial = "M";
        
        initializeWindow(frameWidth, frameHeight);
        //asyncConsole();
        callBlinkingCarret();
    }
    
    public static void main(String[] args) {
        new ChatWindow();
    }
    
    
    private Font createFont() {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/font/Monaco.ttf")).deriveFont((float) fontSize);
            GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            genv.registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void callBlinkingCarret() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                
                blinkingCaret = true;
                
                int len = doc.getLength() - 1;
                text.setCaretPosition(len);
                try {
                    while (blinkingCaret) {
                        
                        doc.remove(len, 1);
                        for (int i = 0; i < 5; ++i) {
                            if (!blinkingCaret) {
                                break;
                            }
                            Thread.sleep(100);
                        }
                        doc.insertString(len, "▯", style);
                        for (int i = 0; i < 5; ++i) {
                            if (!blinkingCaret) {
                                break;
                            }
                            Thread.sleep(100);
                        }
                    }
                    
                    if (doc.getLength() == len) {
                        doc.insertString(len, "▯", style);
                    }
                    text.setCaretPosition(len);
                } catch (InterruptedException | BadLocationException e) {
                    e.printStackTrace();
                }
                
            }
        });
        t.start();
    }
    
    private void initializeWindow(int w, int h) {
        
        Font monaco = createFont();
        monaco.deriveFont(Font.BOLD);
        
        frame = new JFrame();
        frame.getContentPane().setBackground(Color.decode("#000000"));
        frame.setSize(w, h);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("ChatWindow");
        
        text = new JTextPane();
        text.setCaretColor(Color.decode("#000000"));
        text.setBackground(Color.decode("#000000"));
        text.setBorder(BorderFactory.createEmptyBorder());
        
        text.setFont(monaco);
        text.setForeground(Color.decode("#ffffff"));
        style = text.addStyle("Style1", null);
        StyleConstants.setForeground(style, Color.decode("#1cff1c"));
        
        JScrollPane scroll = new JScrollPane(text);
        
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        frame.add(scroll);
    
        doc = text.getStyledDocument();
    
        try {
            doc.insertString(0, initial + ":▯", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        int len = doc.getLength();
        text.setCaretPosition(len);
        
        frame.setVisible(true);
    }
    
    public void printMessageInConsole(String s) {
        blinkingCaret = false;
        try {
            Thread.sleep(150);
            //I think you're a good friend and an amaz ing person as a whole.
            String response = "";
            int length = s.length();
            boolean firstLine = true;
            boolean lastLine = false;
            for (int i = 0; i < length / chars + 1; ++i) {
                int lineEnd = chars;
                if (i * chars + lineEnd > length) {
                    lineEnd = length % chars + 2;
                    lastLine = true;
                }
                if (firstLine) {
                    lineEnd -= 2;
                    firstLine = false;
                }
                response += s.substring((i * chars  == 0 ? 0 : i * chars - 2), (i * chars  == 0 ? 0 : i * chars - 2) + lineEnd);
                if (!lastLine) {
                    response += "\n";
                }
            }
        
            for (char c : response.toCharArray()) {
                doc.insertString(text.getCaretPosition(), String.valueOf(c), style);
                text.setCaretPosition(text.getCaretPosition() + 1);
                Thread.sleep(25);
            }
            if (initial.equals("M")) {
                //text.setForeground(Color.decode("#ff5e19"));
                StyleConstants.setForeground(style, Color.decode("#ff5e19"));
                initial = "F";
            } else {
                //text.setForeground(Color.decode("#0fa03a"));
                StyleConstants.setForeground(style, Color.decode("#1cff1c"));
                initial = "M";
            }
            doc.insertString(text.getCaretPosition(), "\n" + initial + ":", style);
        } catch (InterruptedException | BadLocationException e) {
            e.printStackTrace();
        }
        callBlinkingCarret();
    }
    
    public String  getInitial() {
        return initial;
    }
    
    private void asyncConsole() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("in console");
                while (true) {
                    String s = scanner.nextLine();
                    System.out.println("entered msg");
                   printMessageInConsole(s);
                }
            }
        });
        t.start();
    }
}
