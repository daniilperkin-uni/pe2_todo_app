package de.unistuttgart.iste.ese.api.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestUtil {

    private static final String[] PRENAMES = {"Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy", "Kevin", "Linda", "Michael", "Nancy", "Oliver", "Patricia", "Quentin", "Rachel", "Steve", "Tina", "Ursula", "Victor", "Wendy", "Xander", "Yvonne", "Zach"};
    private static final String[] NAMES = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker"};
    private static final String[] TODO_TITLES = {"Buy groceries", "Do laundry", "Clean house", "Water plants", "Feed cat", "Study for exam", "Write paper", "Call Mom", "Pay bills", "Cook dinner", "Mow lawn", "Fix leak", "Wash car", "Pick up kids", "Go to gym", "Walk dog", "Take out trash", "Get haircut", "Book flight", "Renew passport", "Check mail", "Schedule appointment", "Get oil change", "Buy gift", "Return books"};
    private static final String[] TODO_DESCRIPTIONS = {"Buy milk eggs bread apples and cereal", "Wash dry and fold clothes", "Vacuum dust and mop all rooms", "Give each plant a cup of water", "Give cat half a can of food", "Review notes and practice problems", "Write introduction methods and results", "Ask how she is doing and tell her about your day", "Pay rent electricity and phone bill", "Make spaghetti with meatballs and garlic bread", "Cut grass trim hedges and rake leaves", "Fix leaky faucet in bathroom", "Clean car inside and out", "Pick up kids from school and drive them to soccer practice", "Lift weights run on treadmill and do sit-ups", "Walk dog around the block", "Take out trash cans and recycling bins", "Get haircut at local barber shop", "Book flight to Hawaii for vacation", "Renew passport at post office", "Check mail for letters and packages", "Schedule dentist appointment for next month", "Get oil change at auto repair shop", "Buy gift for friend's birthday", "Return books to library"};
    private static final String[] DEPARTMENT = {"iste", "ipvs", "sec"};

    private TestUtil() {
    }

    public static String getRandomTitle(int index) {
        index = Math.max(0, Math.min(index, TODO_TITLES.length - 1));
        return TODO_TITLES[index];
    }

    static String[] getRandomTodoDetails() {
        int index = getRandomBetween(0, TODO_TITLES.length - 1);
        return new String[] {getRandomTitle(index), getRandomDescription(index)};
    }

    private static String getRandomDescription(int index) {
        index = Math.max(0, Math.min(index, TODO_DESCRIPTIONS.length - 1));
        return TODO_DESCRIPTIONS[index];
    }

    public static String getRandomName() {
        return NAMES[getRandomBetween(0, NAMES.length - 1)];
    }

    public static String getRandomPrename() {
        return PRENAMES[getRandomBetween(0, PRENAMES.length - 1)];
    }

    private static int getRandomBetween(int min, int max) {
        return new Random().nextInt(min, max + 1);
    }

    public static String getRandomMail(String beforeAt) {
        String dept = DEPARTMENT[getRandomBetween(0, DEPARTMENT.length - 1)];
        return (beforeAt + "@" + dept + ".uni-stuttgart.de").toLowerCase();
    }

    public static String getRandomMail(String prename, String name) {
        return getRandomMail(prename + "." + name);
    }

    private static String getRandomPriority() {
        String[] priorities = {"LOW", "MEDIUM", "HIGH"};
        return priorities[getRandomBetween(0, priorities.length - 1)];
    }

    public static void setEmail(JSONObject obj, String email) throws Exception {
        obj.put("email", email);
    }

    public static void setPrename(JSONObject obj, String prename) throws Exception {
        obj.put("prename", prename);
    }

    public static void setName(JSONObject obj, String name) throws Exception {
        obj.put("name", name);
    }

    public static void setId(JSONObject obj, long id) throws Exception {
        obj.put("id", id);
    }

    public static String getEmail(JSONObject obj) throws Exception {
        return obj.getString("email");
    }

    public static String getPrename(JSONObject obj) throws Exception {
        return obj.getString("prename");
    }

    public static String getName(JSONObject obj) throws Exception {
        return obj.getString("name");
    }

    public static long getId(JSONObject obj) throws Exception {
        return obj.getLong("id");
    }

    public static JSONArray getAssigneeIdList(JSONObject obj) throws Exception {
        return obj.getJSONArray("assigneeIdList");
    }

    public static void setAssigneeIdList(JSONObject obj, JSONArray assigneeIdList) throws Exception {
        obj.put("assigneeIdList", assigneeIdList);
    }

    public static void setFinished(JSONObject obj, boolean finished) throws Exception {
        obj.put("finished", finished);
    }

    public static boolean getFinished(JSONObject obj) throws Exception {
        return obj.getBoolean("finished");
    }

    public static String getTitle(JSONObject obj) throws Exception {
        return obj.getString("title");
    }

    public static void setTitle(JSONObject obj, String title) throws Exception {
        obj.put("title", title);
    }

    public static String getDescription(JSONObject obj) throws Exception {
        return obj.getString("description");
    }

    public static void setDescription(JSONObject obj, String description) throws Exception {
        obj.put("description", description);
    }

    public static String getDueDate(JSONObject obj) throws Exception {
        return obj.getString("dueDate");
    }

    public static void setDueDate(JSONObject obj, String dueDate) throws Exception {
        obj.put("dueDate", dueDate);
    }

    public static String getPriority(JSONObject obj) throws Exception {
        return obj.getString("priority");
    }

    public static void setPriority(JSONObject obj, String priority) throws Exception {
        obj.put("priority", priority);
    }

    public static JSONArray getAssigneeList(JSONObject obj) throws Exception {
        return obj.getJSONArray("assigneeList");
    }

    public static String getCategory(JSONObject obj) throws Exception {
        return obj.getString("category");
    }

    public static void setCategory(JSONObject obj, String category) throws Exception {
        obj.put("category", category);
    }

    public static String getCreatedDate(JSONObject obj) throws Exception {
        return obj.getString("createdDate");
    }

    public static void setCreationDate(JSONObject obj, String creationDate) throws Exception {
        obj.put("createdDate", creationDate);
    }

    public static String getFinishedDate(JSONObject obj) throws Exception {
        return obj.getString("finishedDate");
    }

    public static void setFinishedDate(JSONObject obj, String finishedDate) throws Exception {
        obj.put("finishedDate", finishedDate);
    }

    public static JSONObject testAssigneeReq() {
        try {
            String prename = getRandomPrename();
            String name = getRandomName();
            JSONObject assignee = new JSONObject();
            assignee.put("prename", prename);
            assignee.put("name", name);
            assignee.put("email", getRandomMail(prename, name));
            return assignee;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static JSONObject testTodoReq() {
        try {
            LocalDate now = LocalDate.now();
            int index = getRandomBetween(0, TODO_TITLES.length - 1);
            JSONObject todo = new JSONObject();
            todo.put("title", getRandomTitle(index));
            todo.put("description", getRandomDescription(index));
            todo.put("finished", false);
            todo.put("dueDate", LocalDate.ofEpochDay(now.toEpochDay() + getRandomBetween(10, 30)).format(DateTimeFormatter.ISO_LOCAL_DATE));
            todo.put("assigneeIdList", new JSONArray());
            todo.put("priority", getRandomPriority());
            return todo;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
}
