package gr.hua.dit.nikosgourn;
import org.jetbrains.annotations.NotNull;

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
        System.out.println(last_access);
        String date_local = UTC_to_local(last_access.toString());
        System.out.println("File: \"" + filepath + "\"\nLast Modified On: " + date_local);
    }
    
    /**
     * @param date_time A UTC date_time string: example "2024-04-25T17:33:24.6169298Z"
     * @return the same time but offset to the systems time-zone + some different formating: example of above "[25-Apr-2024] 20:33:24.616"
     */
    private static @NotNull String UTC_to_local(@NotNull String date_time)
    {
        Instant last_access_utc = Instant.parse(date_time);
        DateTimeFormatter localFormat =
                DateTimeFormatter.ofPattern("'['dd-MMM-yyyy']' HH:mm:ss.SSS");
        return last_access_utc.atZone(ZoneId.systemDefault()).format(localFormat);
    }
    
}
