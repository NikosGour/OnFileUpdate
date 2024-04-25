package gr.hua.dit.nikosgourn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main
{
    public static void main(String[] args) throws IOException, ParseException
    {
        Path filepath = Path.of(System.getProperty("user.dir") , "pom.xml");
        BasicFileAttributes attr = Files.readAttributes(filepath , BasicFileAttributes.class);
        FileTime last_access = attr.lastModifiedTime();
        
        String date_local = UTC_to_local(last_access.toString());
        System.out.println("File: \"" + filepath + "\"\nLast Modified On: " + date_local);
    }
    
    private static String UTC_to_local(String dataTime)
    {
        Instant last_access_utc = Instant.parse(dataTime);
        DateTimeFormatter localFormat =
                DateTimeFormatter.ofPattern("'['dd-MMM-yyyy']' HH:mm:ss.SSS");
        return last_access_utc.atZone(ZoneId.systemDefault()).format(localFormat);
    }
    
}
