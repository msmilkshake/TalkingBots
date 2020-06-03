package application;

import java.io.File;
import java.io.FileWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private String fileName;
    private File file;
    
    public Log() {
        fileName = "log/" +
                LocalDate.now()
                .toString() +
                "_" +
                LocalTime.now()
                        .format(DateTimeFormatter.ofPattern("HH-mm-ss"));
        file = new File(fileName + ".txt");
    }
    
    public void log(String logText) {
        try {
            FileWriter fw = new FileWriter(file, true);
            String text = LocalDate.now().toString() +
                    "-" +
                    LocalTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))+
                    " - " +
                    logText + "\n";
            fw.write(text);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
