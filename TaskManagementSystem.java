import java.util.Locale;
import java.util.Scanner;

public class TaskManagementSystem {
    private static Scanner input = new Scanner(System.in);
    private static TaskBoard board = new TaskBoard();
    private static boolean inputEnded = false;
    private static final int INPUT_ATTEMPTS = 3;

    public static void main(String[] args) {
        boolean running = true;

        while (running && !inputEnded) {
            board.outHeader();
            board.outBoard();
            board.outMenu();

            System.out.print("  [>] Enter your choice: ");
            String choice = readLine();
            System.out.println();

            if (!inputEnded) {
                switch (choice) {
                    case "1" -> addTask();
                    case "2" -> viewTasks();
                    case "3" -> markTaskDone();
                    case "4" -> removeTask();
                    case "5" -> {
                        System.out.println("  [i] Closing the Task Management System.\n");
                        running = false;
                    }
                    default -> {
                        System.out.println("  [!] Invalid choice. Enter 1 to 5.\n");
                        pause();
                    }
                }
            }
        }

        if (inputEnded) { System.out.println("  [i] Input ended. Closing the Task Management System.\n"); }

        input.close();
    }

    // ---------------------------------------------------------------- ADD TASK
    private static void addTask() {
        board.outSection("add task");

        if (board.isFull()) {
            System.out.println("  [!] The task board is full. Cannot add more tasks.");
            pause();
            return;
        }

        System.out.print("  [>] Enter task title: ");
        String title = readLine();

        System.out.print("  [>] Enter owner name: ");
        String ownerName = readLine();

        System.out.print("  [>] Enter project name: ");
        String projectName = readLine();

        String dueDate = readDueDate();

        if (dueDate.isEmpty()) {
            pause();
            return;
        }

        int priority = readPriority();

        if (priority == 0) {
            pause();
            return;
        }

        String problem = "";

        if (title.isEmpty()) {
            problem = "Task title cannot be empty.";
        } else if (ownerName.isEmpty()) {
            problem = "Owner name cannot be empty.";
        } else if (projectName.isEmpty()) {
            problem = "Project name cannot be empty.";
        }

        if (!problem.isEmpty()) {
            System.out.println("  [!] " + problem + " Nothing was saved.");
            pause();
            return;
        }

        User owner = new User();
        owner.setFullName(ownerName);

        Project project = new Project();
        project.setProjectName(projectName);

        Task task = new Task();
        task.setTaskTitle(title);
        task.setOwner(owner);
        task.setProject(project);
        task.setDueMonth(Integer.parseInt(dueDate.substring(0, 2)));
        task.setDueDay(Integer.parseInt(dueDate.substring(3, 5)));
        task.setDueYear(Integer.parseInt(dueDate.substring(6, 10)));
        task.setPriority(priority);

        if (board.addTask(task)) {
            System.out.println("  [OK] Task saved. " + board.getCount() + " task/s in the board.");
        } else {
            System.out.println("  [!] The task could not be saved.");
        }
        pause();
    }

    // --------------------------------------------------------------- VIEW TASKS
    private static void viewTasks() {
        board.outSection("view tasks");

        if (board.isEmpty()) {
            System.out.println("  [!] There are no tasks yet.");
            pause();
            return;
        }

        System.out.print("  [>] Search a title, owner or project (leave blank to show all): ");
        String keyword = readLine();
        System.out.println();

        int shown = board.displayMatches(keyword);

        if (shown == 0) { System.out.println("  [!] No task matched \"" + keyword + "\"."); }
        pause();
    }

    // ----------------------------------------------------------- MARK TASK DONE
    private static void markTaskDone() {
        board.outSection("mark task done");

        if (board.isEmpty()) {
            System.out.println("  [!] There are no tasks yet.");
            pause();
            return;
        }

        System.out.print("  [>] Enter the exact task title: ");
        String title = readLine();
        System.out.println();

        int slot = board.findTask(title);

        if (slot < 0) {
            System.out.println("  [!] No task is titled \"" + title + "\".");
            pause();
            return;
        }

        Task task = board.getTask(slot);

        if (task.isDone()) {
            System.out.println("  [i] \"" + task.getTaskTitle() + "\" is already done.");
        } else {
            task.setDone(true);
            System.out.println("  [OK] \"" + task.getTaskTitle() + "\" is now done.");
        }
        pause();
    }

    // ------------------------------------------------------------- REMOVE TASK
    private static void removeTask() {
        board.outSection("remove task");

        if (board.isEmpty()) {
            System.out.println("  [!] There are no tasks yet.");
            pause();
            return;
        }

        System.out.print("  [>] Search the task to remove: ");
        String keyword = readLine();
        System.out.println();

        int matches = board.countMatches(keyword);

        if (matches == 0) {
            System.out.println("  [!] No task matched \"" + keyword + "\".");
            pause();
            return;
        }

        int slot = board.findFirstMatch(keyword);

        if (matches > 1) {
            System.out.println("  [!] " + matches + " tasks matched. Type the exact task title.");

            System.out.print("  [>] Exact task title: ");
            String title = readLine();
            System.out.println();

            slot = board.findTask(title);

            if (slot < 0) {
                System.out.println("  [!] No task is titled \"" + title + "\".");
                pause();
                return;
            }
        }

        board.getTask(slot).display(1);

        System.out.print("\n  [>] Remove this task? (Y/N): ");
        String answer = readLine();
        System.out.println();

        if (!answer.equalsIgnoreCase("Y")) {
            System.out.println("  [i] Removal cancelled.");
            pause();
            return;
        }

        board.removeTask(slot);
        System.out.println("  [OK] Task removed. " + board.getCount() + " task/s left.");
        pause();
    }

    // ------------------------------------------------------------------ INPUT
    private static String readLine() {
        if (input.hasNextLine()) { return input.nextLine().trim(); }

        inputEnded = true;
        return "";
    }

    private static int readInt() {
        if (!input.hasNextLine()) {
            inputEnded = true;
            return -1;
        }

        String text = input.nextLine().trim();

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void pause() {
        System.out.println("  [INFO] Press any key to continue...");
        readLine();
        System.out.println();
    }

    // ------------------------------------------------------------ RETRY INPUT
    private static String readDueDate() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter due date (MM/DD/YYYY): ");
            String dueDate = readLine();
            System.out.println();

            if (isDate(dueDate)) { return dueDate; }

            if (inputEnded) { return ""; }

            System.out.println("  [!] Due date must be MM/DD/YYYY. Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid dates. Nothing was saved.");
        return "";
    }

    private static int readPriority() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter priority (1 to 5): ");
            int priority = readInt();
            System.out.println();

            if (priority >= 1 && priority <= 5) { return priority; }

            if (inputEnded) { return 0; }

            System.out.println("  [!] Priority must be a number from 1 to 5. Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid priorities. Nothing was saved.");
        return 0;
    }

    private static boolean isDate(String dueDate) {
        if (dueDate.length() != 10) { return false; }
        if (dueDate.charAt(2) != '/' || dueDate.charAt(5) != '/') { return false; }

        for (int i = 0; i < dueDate.length(); i++) {
            char c = dueDate.charAt(i);

            if (i != 2 && i != 5 && (c < '0' || c > '9')) { return false; }
        }

        int month = Integer.parseInt(dueDate.substring(0, 2));
        int day = Integer.parseInt(dueDate.substring(3, 5));

        return month >= 1 && month <= 12 && day >= 1 && day <= 31;
    }
}

class Task {
    private String taskTitle;
    private User owner;
    private Project project;
    private int dueMonth;
    private int dueDay;
    private int dueYear;
    private int priority;
    private boolean done;

    // fixed demo date: change these three to today's date
    private static final int TODAY_MONTH = 10;
    private static final int TODAY_DAY = 28;
    private static final int TODAY_YEAR = 2026;

    public Task() {
        taskTitle = "";
        owner = new User();
        project = new Project();
        done = false;
    }

    public void setTaskTitle(String newTitle) { taskTitle = newTitle; }
    public void setDueMonth(int newMonth) { dueMonth = newMonth; }
    public void setDueDay(int newDay) { dueDay = newDay; }
    public void setDueYear(int newYear) { dueYear = newYear; }
    public void setPriority(int newPriority) { priority = newPriority; }
    public void setDone(boolean newDone) { done = newDone; }

    public String getTaskTitle() { return taskTitle; }
    public boolean isDone() { return done; }
    public String getProjectName() { return project.getProjectName(); }
    public User getOwner() { return owner.copy(); }
    public Project getProject() { return project.copy(); }

    public void setOwner(User newOwner) {
        owner = new User();
        owner.setFullName(newOwner.getFullName());
    }

    public void setProject(Project newProject) {
        project = new Project();
        project.setProjectName(newProject.getProjectName());
    }

    public String getDueLabel() { return twoDigits(dueMonth) + "/" + twoDigits(dueDay) + "/" + dueYear; }

    public String getStatus() {
        if (done) { return "Done"; }
        if (isOverdue()) { return "Overdue"; }
        return "Pending";
    }

    public String getPriorityLabel() {
        return switch (priority) {
            case 1 -> "Low";
            case 2 -> "Normal";
            case 3 -> "High";
            case 4 -> "Urgent";
            case 5 -> "Critical";
            default -> "Unknown";
        };
    }

    public boolean matches(String keyword) {
        String text = keyword.toLowerCase(Locale.US);

        return taskTitle.toLowerCase(Locale.US).contains(text)
                || owner.getFullName().toLowerCase(Locale.US).contains(text)
                || project.getProjectName().toLowerCase(Locale.US).contains(text);
    }

    public void display(int slot) {
        System.out.println("  [" + slot + "] " + taskTitle
                + "\n      Project : " + project.getProjectName()
                + "\n      Owner   : " + owner.getFullName()
                + "\n      Due     : " + getDueLabel() + "  [" + getStatus() + "]"
                + "\n      Priority: " + getPriorityLabel() + "\n");
    }

    private boolean isOverdue() {
        if (dueYear != TODAY_YEAR) { return dueYear < TODAY_YEAR; }
        if (dueMonth != TODAY_MONTH) { return dueMonth < TODAY_MONTH; }
        return dueDay < TODAY_DAY;
    }

    private String twoDigits(int value) {
        if (value < 10) { return "0" + value; }
        return "" + value;
    }
}

class User {
    private String fullName;

    public User() { fullName = ""; }

    public void setFullName(String newFullName) { fullName = newFullName; }
    public String getFullName() { return fullName; }

    public User copy() {
        User clone = new User();
        clone.setFullName(fullName);
        return clone;
    }
}

class Project {
    private String projectName;

    public Project() { projectName = ""; }

    public void setProjectName(String newProjectName) { projectName = newProjectName; }
    public String getProjectName() { return projectName; }

    public Project copy() {
        Project clone = new Project();
        clone.setProjectName(projectName);
        return clone;
    }
}

class TaskBoard {
    private Task[] tasks = new Task[100];
    private int taskCount = 0;
    private static final int BOARD_LIMIT = 10;

    public int getCount() { return taskCount; }
    public boolean isEmpty() { return taskCount == 0; }
    public boolean isFull() { return taskCount >= tasks.length; }

    public Task getTask(int index) {
        if (index < 0 || index >= taskCount) { return null; }
        return tasks[index];
    }

    public boolean addTask(Task newTask) {
        if (isFull()) { return false; }

        tasks[taskCount] = newTask;
        taskCount++;
        return true;
    }

    public boolean removeTask(int index) {
        if (index < 0 || index >= taskCount) { return false; }

        for (int i = index; i < taskCount - 1; i++) {
            tasks[i] = tasks[i + 1];
        }

        tasks[taskCount - 1] = null;
        taskCount--;
        return true;
    }

    public int findTask(String title) {
        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].getTaskTitle().equalsIgnoreCase(title)) { return i; }
        }
        return -1;
    }

    public int findFirstMatch(String keyword) {
        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].matches(keyword)) { return i; }
        }
        return -1;
    }

    public int countMatches(String keyword) {
        int total = 0;

        for (int i = 0; i < taskCount; i++) {
            if (tasks[i].matches(keyword)) { total++; }
        }
        return total;
    }

    public int displayMatches(String keyword) {
        int shown = 0;

        for (int i = 0; i < taskCount; i++) {
            if (keyword.isEmpty() || tasks[i].matches(keyword)) {
                shown++;
                tasks[i].display(shown);
            }
        }
        return shown;
    }

    public void outBoard() {
        System.out.println("  " + taskCount + " task/s on the board");

        if (taskCount == 0) {
            System.out.println("  [i] Nothing here yet. Use [1] Add Task.\n");
            return;
        }

        System.out.println(String.format(Locale.US, "  %-4s %-22s %-14s %-10s %s",
                "#", "Title", "Project", "Due", "Status")
                + "\n  -------------------------------------------------------------");

        for (int i = 0; i < taskCount && i < BOARD_LIMIT; i++) {
            System.out.println(String.format(Locale.US, "  %-4d %-22s %-14s %-10s %s",
                    (i + 1), fit(tasks[i].getTaskTitle(), 22), fit(tasks[i].getProjectName(), 14),
                    tasks[i].getDueLabel(), tasks[i].getStatus()));
        }

        if (taskCount > BOARD_LIMIT) { System.out.println("  ... and " + (taskCount - BOARD_LIMIT) + " more. Use [2] View Tasks."); }
        System.out.println();
    }

    private String fit(String text, int width) {
        if (text.length() <= width) { return text; }
        return text.substring(0, width - 2) + "..";
    }

    public void outHeader() { System.out.println("\nTask Management System\n"); }

    public void outMenu() {
        System.out.println("  [1] Add Task\n" + "  [2] View Tasks\n" + "  [3] Mark Task Done\n"
                + "  [4] Remove Task\n" + "  [5] Exit\n");
    }

    public void outSection(String title) {
        String line = "--- " + title + " ";

        while (line.length() < 57) {
            line = line + "-";
        }
        System.out.println(line);
    }
}
