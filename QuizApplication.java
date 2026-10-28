import java.util.Scanner;

public class QuizApplication {
    private static Scanner input = new Scanner(System.in);
    private static boolean inputEnded = false;

    public static void main(String[] args) {
        Quiz quiz = new Quiz();

        quiz.outHeader();
        quiz.outSection("student details");

        System.out.print("  [>] Enter your name: ");
        quiz.setStudentName(readLine());

        System.out.print("  [>] Enter your student ID: ");
        quiz.setStudentID(readLine());
        System.out.println();

        if (inputEnded) {
            endMessage();
            return;
        }

        quiz.outSection("take quiz");
        System.out.println("  [i] Read each question carefully. Input only the letter of your choice.\n");

        Question[] questions = quiz.getQuestions();
        String[] answers = new String[questions.length];
        String[] letters = { "A", "B", "C", "D" };

        for (int i = 0; i < questions.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + questions[i].getText());

            String[] choices = questions[i].getChoices();

            for (int j = 0; j < choices.length && j < letters.length; j++) {
                System.out.println("      " + letters[j] + ". " + choices[j]);
            }

            System.out.print("\n  [>] Answer: ");
            answers[i] = readLine();
            System.out.println();

            if (inputEnded) { break; }
        }

        if (inputEnded) {
            endMessage();
            return;
        }

        quiz.setUserAnswers(answers);
        quiz.setScore(quiz.reviewAnswers());
        quiz.showResult();

        input.close();
    }

    private static String readLine() {
        if (input.hasNextLine()) { return input.nextLine().trim(); }

        inputEnded = true;
        return "";
    }

    private static void endMessage() {
        System.out.println("  [i] Input ended. Closing the Quiz Application.\n");
    }
}

class Question {
    private String text, correctAnswer;
    private String[] choices;

    public Question() {
        text = "";
        choices = new String[4];
        correctAnswer = "";
    }

    public void setText(String newText) { text = newText; }
    public void setCorrectAnswer(String newCorrectAnswer) { correctAnswer = newCorrectAnswer; }

    public void setChoices(String[] newChoices) {
        choices = new String[newChoices.length];

        for (int i = 0; i < newChoices.length; i++) {
            choices[i] = newChoices[i];
        }
    }

    public String getText() { return text; }
    public String getCorrectAnswer() { return correctAnswer; }

    public String[] getChoices() {
        String[] copy = new String[choices.length];

        for (int i = 0; i < choices.length; i++) {
            copy[i] = choices[i];
        }
        return copy;
    }
}

class Score {
    private int rawScore, totalItems;

    public Score() {
        rawScore = 0;
        totalItems = 0;
    }

    public void setRawScore(int newRawScore) { rawScore = newRawScore; }
    public void setTotalItems(int newTotalItems) { totalItems = newTotalItems; }

    public int getRawScore() { return rawScore; }
    public int getTotalItems() { return totalItems; }

    public int calcPercentage(int rawScore, int totalItems) {
        if (totalItems == 0) { return 0; }
        return 100 * rawScore / totalItems;
    }

    public void outResult(String name, String studentID) {
        int percentage = calcPercentage(rawScore, totalItems);

        System.out.println("  ======================================================="
                + "\n  Student Name     : " + name
                + "\n  Student ID       : " + studentID
                + "\n  Score            : " + rawScore + " out of " + totalItems
                + "\n  Percentage       : " + percentage + "%"
                + "\n  =======================================================");
    }
}

class Quiz {
    // ADD A QUESTION BLOCK HERE. question(tanong(text), choices(a, b, c, d), sagot(correct))
    private Question[] questions = {

        question(
            tanong("Who created Java?"),
            choices("James Gosling", "Dennis Ritchie", "Bjarne Stroustrup", "Guido van Rossum"),
            sagot("A")),

        question(
            tanong("What does JVM stand for?"),
            choices("Java Version Manager", "Java Virtual Machine", "Joint Variable Method", "Java Verified Module"),
            sagot("B")),

        question(
            tanong("Which data type stores true or false?"),
            choices("int", "char", "boolean", "double"),
            sagot("C")),

        question(
            tanong("Which operator returns the remainder of a division?"),
            choices("+", "/", "*", "%"),
            sagot("D")),

        question(
            tanong("Which loop is guaranteed to execute at least once?"),
            choices("for", "while", "do-while", "for-each"),
            sagot("C"))
    };

    private String[] userAnswers;
    private String studentName;
    private String studentID;
    private int score;

    public Quiz() {
        userAnswers = new String[questions.length];
        studentName = "";
        studentID = "";
        score = 0;
    }

    private static String tanong(String text) { return text; }

    private static String[] choices(String a, String b, String c, String d) { return new String[] { a, b, c, d }; }

    private static String sagot(String correct) { return correct; }

    private static Question question(String text, String[] choices, String correctAnswer) {
        Question question = new Question();
        question.setText(text);
        question.setChoices(choices);
        question.setCorrectAnswer(correctAnswer);
        return question;
    }

    public void setStudentName(String newStudentName) { studentName = newStudentName; }
    public void setStudentID(String newStudentID) { studentID = newStudentID; }
    public void setScore(int newScore) { score = newScore; }

    public void setUserAnswers(String[] newUserAnswers) {
        userAnswers = new String[questions.length];

        for (int i = 0; i < questions.length && i < newUserAnswers.length; i++) {
            userAnswers[i] = newUserAnswers[i];
        }
    }

    public String getStudentName() { return studentName; }
    public String getStudentID() { return studentID; }
    public int getScore() { return score; }
    public int getTotalItems() { return questions.length; }

    public Question[] getQuestions() {
        Question[] copy = new Question[questions.length];

        for (int i = 0; i < questions.length; i++) {
            copy[i] = questions[i];
        }
        return copy;
    }

    public String[] getUserAnswers() {
        String[] copy = new String[userAnswers.length];

        for (int i = 0; i < userAnswers.length; i++) {
            copy[i] = userAnswers[i];
        }
        return copy;
    }

    public void outHeader() { System.out.println("\nQuiz Application\n"); }

    public void outSection(String title) {
        String line = "--- " + title + " ";

        while (line.length() < 57) {
            line = line + "-";
        }
        System.out.println(line);
    }

    private boolean isValidInput(String input, int numberOfChoices) {
        String[] letters = { "A", "B", "C", "D" };

        for (int i = 0; i < numberOfChoices && i < letters.length; i++) {
            if (input.equalsIgnoreCase(letters[i])) { return true; }
        }
        return false;
    }

    public int reviewAnswers() {
        int correctCount = 0;

        outSection("answers");

        for (int i = 0; i < questions.length; i++) {
            String userInput = "";

            if (userAnswers[i] != null) { userInput = userAnswers[i].trim(); }

            int numberOfChoices = questions[i].getChoices().length;

            if (userInput.isEmpty()) {
                System.out.println("  [!] " + (i + 1) + ". No answer given.");
            } else if (!isValidInput(userInput, numberOfChoices)) {
                System.out.println("  [!] " + (i + 1) + ". \"" + userInput + "\" is not a choice.");
            } else if (userInput.equalsIgnoreCase(questions[i].getCorrectAnswer())) {
                System.out.println("  [OK] " + (i + 1) + ". Correct");
                correctCount++;
            } else {
                System.out.println("  [!] " + (i + 1) + ". Wrong. The answer is " + questions[i].getCorrectAnswer() + ".");
            }
        }

        System.out.println();
        return correctCount;
    }

    public void showResult() {
        Score result = new Score();
        result.setRawScore(score);
        result.setTotalItems(getTotalItems());
        result.outResult(studentName, studentID);
    }
}
