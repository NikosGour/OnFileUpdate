package gr.hua.dit.nikosgourn;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main
{
	public static void main(String[] args) throws InterruptedException
	{
		Path filepath = check_arg(args);
		//		System.out.println(filepath);
		FileTime last_modified_prev = null;
		if (! Files.exists(filepath))
		{
			System.out.printf("No such file: \"%s\"\n" , filepath);
			System.exit(1);
		}
		while (true)
		{
			FileTime last_modified;
			try
			{
				last_modified = get_last_modified(filepath);
			} catch (NoSuchFileException e)
			{
				System.out.printf("File: \"%s\" was deleted\nExiting\n" , filepath);
				return;
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			
			if (last_modified_prev != null && ! last_modified_prev.equals(last_modified))
			{
				System.out.printf("File was modified!\nModified on:      %s\nLast modified on: %s\n" ,
				                  UTC_to_local(last_modified.toString()) , UTC_to_local(last_modified_prev.toString()));
			}
			//			System.out.println("File: \"" + filepath + "\"\nLast Modified On: " + last_modified);
			//			System.out.println("-".repeat(50));
			last_modified_prev = last_modified;
			Thread.sleep(1000);
		}
	}
	
	private static FileTime get_last_modified(Path filepath) throws IOException
	{
		BasicFileAttributes attr = Files.readAttributes(filepath , BasicFileAttributes.class);
		FileTime last_modified = attr.lastModifiedTime();
		//		System.out.println(last_modified);
		//		String last_modified_local = UTC_to_local(last_modified.toString());
		//		System.out.println("File: \"" + filepath + "\"\nLast Modified On: " + last_modified_local);
		return last_modified;
	}
	
	/**
	 * @param date_time A UTC date_time string: example "2024-04-25T17:33:24.6169298Z"
	 * @return the same time but offset to the systems time-zone + some different formating: example of above "[25-Apr-2024] 20:33:24.616"
	 */
	private static @NotNull String UTC_to_local(@NotNull String date_time)
	{
		Instant date_time_utc = Instant.parse(date_time);
		DateTimeFormatter localFormat = DateTimeFormatter.ofPattern("'['dd-MMM-yyyy']' HH:mm:ss.SSS");
		return date_time_utc.atZone(ZoneId.systemDefault()).format(localFormat);
	}
	
	private static Path check_arg(String[] args)
	{
		final String USAGE_MSG = "usage:\n\t OnFileUpdate <file> <command>";
		if (args == null)
		{
			System.out.println(USAGE_MSG);
			System.exit(1);
		}
		if (args.length != 2)
		{
			System.out.println(USAGE_MSG);
			System.exit(1);
		}
		if (args[0].startsWith("/"))
		{
			return Path.of(args[0]);
		}
		
		return Path.of(System.getProperty("user.dir") , args[0]);
	}
}