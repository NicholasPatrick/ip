package technical;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

import functional.Deadline;
import functional.Event;
import functional.Task;
import functional.Todo;
import ui.Ui;

/**
 * class storing the list of tasks.
 * @author Nicholas Patrick
 */
public class TaskList {
    private static ArrayList<Task> taskList = new ArrayList<>();

    /**
     * Loads tasks from the save file.
     * @throws IOException
     */
    public static void loadFromSaveFile() throws IOException {
        SaveFile.loadSaveFile();
        for (SaveLine i : SaveFile.getFileData()) {
            if (i.getInfoType().equals("todo")) {
                TaskList.taskList.add(new Todo(i));
                continue;
            }
            if (i.getInfoType().equals("deadline")) {
                TaskList.taskList.add(new Deadline(i));
                continue;
            }
            if (i.getInfoType().equals("event")) {
                TaskList.taskList.add(new Event(i));
                continue;
            }
        }
    }
    /**
     * Lists the todo list.
     */
    public static String list() {
        if (taskList.isEmpty()) {
            return Ui.reply("You have no tasks in your list.");
        }
        String[] toReply = new String[taskList.size() + 1];
        toReply[0] = "Here are the tasks in your list.";
        for (int i = 0; i < taskList.size(); ++i) {
            toReply[i + 1] = String.format("%d. %s", i + 1, taskList.get(i));
        }
        return Ui.reply(toReply);
    }

    /**
     * Reply to be made after just adding a task.
     */
    public static String justAddedComment() {
        return Ui.reply(new String[] {"Successfully added the following task",
            taskList.get(taskList.size() - 1).toString(),
            String.format("You now have %d tasks in the list.",
            taskList.size())});
    }

    private static String[] readFlags(String[] arguments, String ... keys) {
        StringBuilder[] flagged = new StringBuilder[keys.length];
        for (int i = 0; i < flagged.length; ++i) {
            flagged[i] = new StringBuilder();
        }
        for (String i : arguments) {
            int mode = Arrays.asList(keys).indexOf(i) + 1;
            if (flagged[mode].length() != 0) {
                flagged[mode].append(' ');
            }
            flagged[mode].append(i);
        }
        String[] ret = new String[keys.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = flagged[i].toString();
        }
        return ret;
    }

    /**
     * Append a Todo to the todoList.
     *
     * @param arguments The command arguments.
     */
    public static String todo(String[] arguments) throws IOException {
        String[] flagged = readFlags(arguments);
        String todoName = flagged[0];
        if (todoName.length() == 0) {
            return Ui.reply("Please include a name");
        }
        taskList.add(new Todo(todoName));
        SaveFile.addData(taskList.get(taskList.size() - 1).toData());
        return justAddedComment();
    }

    /**
     * Append a Deadline to the todoList.
     *
     * @param arguments The command arguments.
     */
    public static String deadline(String[] arguments) throws IOException {
        String[] flagged = readFlags(arguments, "/by");
        String deadlineName = flagged[0];
        String deadlineDeadlineString = flagged[1];
        if (deadlineName.length() == 0 || deadlineDeadlineString.length() == 0) {
            return Ui.reply(new String[]{"Format the command as follows:",
                "deadline <deadline name> /by <deadline>"});
        }
        LocalDateTime deadlineDeadline;
        try {
            deadlineDeadline = LocalDateTime
                .parse(deadlineDeadlineString,
                    DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"));
        } catch (DateTimeParseException e) {
            return Ui.reply("Please format your date as"
                + " \"year/month/date hour/minute/second\" (24 hour format).");
        }
        taskList.add(new Deadline(deadlineName, deadlineDeadline));
        SaveFile.addData(taskList.get(taskList.size() - 1).toData());
        return justAddedComment();
    }

    /**
     * Append an Event to the todoList.
     *
     * @param arguments The command arguments.
     */
    public static String event(String[] arguments) throws IOException {
        String[] flagged = readFlags(arguments, "/from", "/to");
        String eventName = flagged[0];
        String eventStartTimeString = flagged[1];
        String eventEndTimeString = flagged[2];
        if (eventName.length() == 0 || eventStartTimeString.length() == 0
            || eventEndTimeString.length() == 0) {
            return Ui.reply(new String[]{"Format the command as follows:",
                "event <event name> /from <event start time> /to <event end time>"});
        }
        LocalDateTime eventStartTime;
        LocalDateTime eventEndTime;
        try {
            eventStartTime = LocalDateTime
                .parse(eventStartTimeString,
                    DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"));
            eventEndTime = LocalDateTime
                .parse(eventEndTimeString,
                    DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"));
        } catch (DateTimeParseException e) {
            return Ui.reply("Please format your date as"
                + " \"year/month/date hour/minute/second\" (24 hour format).");
        }
        taskList.add(new Event(eventName, eventStartTime, eventEndTime));
        SaveFile.addData(taskList.get(taskList.size() - 1).toData());
        return justAddedComment();
    }

    /**
     * Marks tasks as done. In the case of invalid arguments, it will reply
     * with the appropriate message.
     *
     * @param arguments The command arguments.
     */
    public static String mark(String[] arguments) throws IOException {
        if (taskList.isEmpty()) {
            return Ui.reply("You don't have any tasks to mark!");
        }
        int i;
        try {
            i = Integer.parseInt(arguments[1]) - 1;
        } catch (IndexOutOfBoundsException e) {
            return Ui.reply("Please enter the item ID you wish to mark");
        } catch (NumberFormatException e) {
            return Ui.reply(String.format("Invalid argument! (Please enter an integer"
                + " between 1 and %d)", taskList.size()));
        }
        try {
            taskList.get(i).doTask();
            String ret = Ui.reply(new String[]{"Ok, I'm marking this as done",
                taskList.get(i).toString()});
            SaveFile.getFileData().get(i).setKeyValue("done", "1");
            SaveFile.saveFile();
            return ret;
        } catch (IndexOutOfBoundsException e) {
            return Ui.reply(String.format("Invalid argument! (Please enter an integer"
                + " between 1 and %d)", taskList.size()));
        }
    }

    /**
     * Marks tasks as not done. In the case of invalid arguments, it will
     * reply with the appropriate message.
     *
     * @param arguments The command arguments.
     */
    public static String unmark(String[] arguments) throws IOException {
        if (taskList.isEmpty()) {
            return Ui.reply("You don't have any tasks to unmark!");
        }
        int i;
        try {
            i = Integer.parseInt(arguments[1]) - 1;
        } catch (IndexOutOfBoundsException e) {
            return Ui.reply("Please enter the item ID you wish to unmark");
        } catch (NumberFormatException e) {
            return Ui.reply(String.format("Invalid argument! (Please enter an integer"
                + " between 1 and %d)", taskList.size()));
        }
        try {
            taskList.get(i).undo();
            String ret = Ui.reply(new String[]{"Ok, I'm marking this as not done",
                taskList.get(i).toString()});
            SaveFile.getFileData().get(i).setKeyValue("done", "0");
            SaveFile.saveFile();
            return ret;
        } catch (IndexOutOfBoundsException e) {
            return Ui.reply(String.format("Invalid argument! (Please enter an integer"
                + " between 1 and %d)", taskList.size()));
        }
    }

    /**
     * Deletes a task.
     *
     * @param arguments The command arguments.
     */
    public static String delete(String[] arguments) {
        if (taskList.isEmpty()) {
            return Ui.reply("You don't have any tasks to delete!");
        }
        int i;
        try {
            i = Integer.parseInt(arguments[1]) - 1;
        } catch (IndexOutOfBoundsException e) {
            return Ui.reply("Please enter the item ID you wish to delete");
        } catch (NumberFormatException e) {
            return Ui.reply(String.format("Invalid argument! (Please enter an integer"
                + " between 1 and %d)", taskList.size()));
        }
        try {
            String ret = Ui.reply(new String[]{"Ok, I'm deleting this",
                taskList.get(i).toString()});
            taskList.remove(i);
            SaveFile.getFileData().remove(i);
            SaveFile.saveFile();
            return ret;
        } catch (IndexOutOfBoundsException e) {
            return Ui.reply(String.format("Invalid argument! (Please enter an integer"
                + " between 1 and %d)", taskList.size()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds all tasks with the searched phrase and lists them.
     *
     * @param arguments The command arguments.
     */
    public static String find(String[] arguments) {
        if (taskList.isEmpty()) {
            return Ui.reply("You have no tasks in your list.");
        }
        String searchPhrase = String.join(" ",
            Arrays.asList(arguments).subList(1, arguments.length));
        ArrayList<String> toReplyArrayList = new ArrayList<>();
        toReplyArrayList.add("Here are the tasks in your list which matches the"
            + " search phrase.");
        for (int i = 0; i < taskList.size(); ++i) {
            if (taskList.get(i).getName().contains(searchPhrase)) {
                toReplyArrayList.add(String.format("%d. %s", i + 1, taskList.get(i)));
            }
        }
        String[] toReply = new String[toReplyArrayList.size()];
        for (int i = 0; i < toReply.length; ++i) {
            toReply[i] = toReplyArrayList.get(i);
        }
        return Ui.reply(toReply);
    }
}
