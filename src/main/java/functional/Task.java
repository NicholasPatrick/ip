package functional;

import technical.SaveLine;

public abstract class Task {
    protected String name;
    protected boolean done;

    /**
     * Construct functional.Task with a fixed name.
     *
     * @param name The name of the task.
     */
    public Task(String name) {
        this.name = name;
        done = false;
    }

    public Task(SaveLine line) {
        name = line.getValue("name");
        done = line.getValue("done").equals("1");
    }

    /**
     * Get the name of the task.
     *
     * @return The name of the task.
     */
    public String getName() {
        return name;
    }

    /**
     * Get whether the task is done or not.
     *
     * @return Whether the task is done or not.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Mark a task as done.
     */
    public void doTask() {
        done = true;
    }

    /**
     * Mark a task as not done.
     */

    public void undo() {
        done = false;
    }

    /**
     * Get the character symbol of the done-ness of the task.
     *
     * @return Returns 'X' for done tasks and ' ' for not done tasks.
     */

    public char mark() {
        return done ? 'X' : ' ';
    }

    /**
     * Shows the task name and status as a String.
     *
     * @return A String with the task name and status.
     */
    @Override
    public String toString() {
        return String.format("[%c] %s", mark(), name);
    }

    public SaveLine toData() {
        return new SaveLine("task", "name", name, "done", done ? "1" : "0");
    }
}
