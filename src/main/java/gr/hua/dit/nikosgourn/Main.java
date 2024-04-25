package gr.hua.dit.nikosgourn;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		// Check if args are correctly defined
		check_arg(args);
		// Get the file to be watch and the command to be executed
		Path filepath = get_path_arg(args);
		String[] command = get_command_arg(args);
		
		// Check if file exists
		if (! Files.exists(filepath)) {
			System.out.printf("No such file: \"%s\"\n" , filepath);
			System.exit(1);
		}
		
		FileTime last_modified_prev = null;
		Process proc = null;
		// Test for file modification
		while (true) {
			FileTime last_modified;
			try {
				last_modified = get_last_modified(filepath);
			}
			catch (NoSuchFileException e) {
				System.out.printf("File: \"%s\" was deleted\nExiting\n" , filepath);
				return;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			
			// If file was modified
			if (last_modified_prev != null && ! last_modified_prev.equals(last_modified)) {
				System.out.printf("File was modified: %s\nModified on:      %s\nLast modified on: %s\n" , filepath ,
				                  UTC_to_local(last_modified.toString()) , UTC_to_local(last_modified_prev.toString()));
				try {
					// If process was running, kill it
					if (proc != null) {
						proc.destroy();
					}
					
					// run the command
					proc = new ProcessBuilder(command).start();
					
					// print it's output
					BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line;
					System.out.println("-".repeat(70));
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
					System.out.println("-".repeat(70));
					//					proc.waitFor();
				}
				catch (IOException e) {
					final Pattern NO_SUCH_COMMAND = Pattern.compile("Cannot run program \".+? error=2");
					if (NO_SUCH_COMMAND.matcher(e.getMessage()).find()) {
						System.out.printf("No such program exist: `%s`\n" , String.join(" " , command));
					}
					else {
						throw new RuntimeException(e);
					}
				}
			}
			// Save current "last_modified" date-time, so we can compare with the next poll
			last_modified_prev = last_modified;
			Thread.sleep(1000);
		}
	}
	
	/**
	 * @param filepath the file path to get the data from
	 * @return the date-time of last modification of file
	 * @throws IOException in case of file error
	 */
	private static FileTime get_last_modified(Path filepath) throws IOException {
		BasicFileAttributes attr = Files.readAttributes(filepath , BasicFileAttributes.class);
		return attr.lastModifiedTime();
	}
	
	/**
	 * Converts give date-time from UTC to System's local time-zone
	 *
	 * @param date_time A UTC date_time string: example "2024-04-25T17:33:24.6169298Z"
	 * @return the same time but offset to the systems time-zone + some different formating: example of above "[25-Apr-2024] 20:33:24.616"
	 */
	private static @NotNull String UTC_to_local(@NotNull String date_time) {
		Instant date_time_utc = Instant.parse(date_time);
		DateTimeFormatter localFormat = DateTimeFormatter.ofPattern("'['dd-MMM-yyyy']' HH:mm:ss.SSS");
		return date_time_utc.atZone(ZoneId.systemDefault()).format(localFormat);
	}
	
	/**
	 * Checks if arguments are correctly passed in the program
	 *
	 * @param args program arguments
	 */
	private static void check_arg(String[] args) {
		final String USAGE_MSG = "usage:\n\t OnFileUpdate <file> <command>";
		if (args == null) {
			System.out.println(USAGE_MSG);
			System.exit(1);
		}
		if (args.length < 2) {
			System.out.println(USAGE_MSG);
			System.exit(1);
		}
	}
	
	/**
	 * Extracts the absolute path of the file to be watched from the program arguments
	 *
	 * @param args program arguments
	 * @return the path of the file to watch
	 */
	private static @NotNull Path get_path_arg(String @NotNull [] args) {
		if (args[0].startsWith("/")) {
			return Path.of(args[0]);
		}
		return Path.of(System.getProperty("user.dir") , args[0]);
	}
	
	/**
	 * Parses the command to be executed when file is modified, from program args
	 *
	 * @param args program arguments
	 * @return the parsed command
	 */
	@Contract(pure = true)
	private static String @NotNull [] get_command_arg(String @NotNull [] args) {
		String[] command = new String[args.length - 1];
		
		for (int i = 0; i < command.length; i++) {
			command[i] = args[i + 1];
		}
		
		return command;
	}
}