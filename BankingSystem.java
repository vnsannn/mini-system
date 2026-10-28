import java.util.Locale;
import java.util.Scanner;

public class BankingSystem {
    private static Scanner input = new Scanner(System.in);
    private static Bank bank = new Bank();
    private static boolean inputEnded = false;
    private static final int INPUT_ATTEMPTS = 3;

    public static void main(String[] args) {
        boolean running = true;

        while (running && !inputEnded) {
            bank.outHeader();
            bank.outMenu();

            System.out.print("  [>] Enter your choice: ");
            String choice = readLine();
            System.out.println();

            if (!inputEnded) {
                switch (choice) {
                    case "1" -> openAccount();
                    case "2" -> login();
                    case "3" -> {
                        System.out.println("  [i] Closing the Banking System.\n");
                        running = false;
                    }
                    default -> {
                        System.out.println("  [!] Invalid choice. Enter 1 to 3.\n");
                        pause();
                    }
                }
            }
        }

        if (inputEnded) { System.out.println("  [i] Input ended. Closing the Banking System.\n"); }

        input.close();
    }

    // ----------------------------------------------------------- REGISTRATION
    private static void openAccount() {
        bank.outSection("open account");

        System.out.print("  [>] Enter your name: ");
        String name = readLine();

        System.out.print("  [>] Enter your account number: ");
        String accountNumber = readLine();

        String pin = readNewPin();

        if (pin.isEmpty()) {
            pause();
            return;
        }

        double initialDeposit = readOpeningDeposit();

        if (Double.isNaN(initialDeposit)) {
            pause();
            return;
        }

        String problem = "";

        if (name.isEmpty()) {
            problem = "Name cannot be empty.";
        } else if (accountNumber.isEmpty()) {
            problem = "Account number cannot be empty.";
        } else if (bank.findAccount(accountNumber) >= 0) {
            problem = "That account number already exists.";
        } else if (bank.isFull()) {
            problem = "The bank is full. Cannot open more accounts.";
        }

        if (!problem.isEmpty()) {
            System.out.println("  [!] " + problem + " Nothing was saved.");
            pause();
            return;
        }

        String customerId = generateCustomerID();

        Customer customer = new Customer();
        customer.setName(name);
        customer.setCustomerID(customerId);

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setPin(pin);
        account.setOwner(customer);

        if (initialDeposit > 0) { account.deposit(initialDeposit); }

        if (bank.addAccount(account)) {
            System.out.println("  [OK] Account opened. Your customer ID is " + customerId + ".\n"
                    + "  [i] There are now " + bank.getAccountCount() + " account/s in the bank.");
        } else {
            System.out.println("  [!] The account could not be saved.");
        }
        pause();
    }

    // ------------------------------------------------------------------ LOGIN
    private static void login() {
        bank.outSection("login");

        int slot = verifyAccount();

        if (slot < 0) {
            pause();
            return;
        }

        accountSession(bank.getAccount(slot));
    }

    // -------------------------------------------------------- LOGGED-IN SESSION
    private static void accountSession(Account account) {
        while (true) {
            account.displayCard();
            account.outMenu();

            System.out.print("  [>] Enter your choice: ");
            String choice = readLine();
            System.out.println();

            if (inputEnded) { return; }

            switch (choice) {
                case "1" -> deposit(account);
                case "2" -> withdraw(account);
                case "3" -> transfer(account);
                case "4" -> viewTransactions(account);
                case "5" -> {
                    System.out.println("  [i] Logged out.\n");
                    return;
                }
                default -> {
                    System.out.println("  [!] Invalid choice. Enter 1 to 5.\n");
                    pause();
                }
            }
        }
    }

    // -------------------------------------------------------------- DEPOSIT
    private static void deposit(Account account) {
        bank.outSection("deposit");

        if (blockIfHistoryFull(account)) { return; }

        System.out.print("  [>] Enter amount: ");
        double amount = readDouble();
        System.out.println();

        if (account.deposit(amount)) {
            System.out.println("  [OK] Deposit successful.");
        } else {
            System.out.println("  [!] Deposit failed. Enter a number above 0.");
        }
        pause();
    }

    // ------------------------------------------------------------- WITHDRAW
    private static void withdraw(Account account) {
        bank.outSection("withdraw");

        if (blockIfHistoryFull(account)) { return; }

        System.out.print("  [>] Enter amount: ");
        double amount = readDouble();
        System.out.println();

        if (!confirmPin(account, "Confirm your PIN")) {
            pause();
            return;
        }

        if (account.withdraw(amount)) {
            System.out.println("  [OK] Withdrawal successful.");
        } else {
            System.out.println("  [!] Withdrawal failed. Enter a number above 0 and at most PHP " + formatPeso(account.getBalance()) + ".");
        }
        pause();
    }

    // ------------------------------------------------------------- TRANSFER
    private static void transfer(Account sender) {
        bank.outSection("transfer");

        if (blockIfHistoryFull(sender)) { return; }

        System.out.print("  [>] Enter recipient customer ID: ");
        String customerId = readLine();

        System.out.print("  [>] Enter amount: ");
        double amount = readDouble();
        System.out.println();

        Account receiver = bank.findByCustomerID(customerId);
        String problem = "";

        if (customerId.isEmpty()) {
            problem = "Customer ID cannot be empty.";
        } else if (receiver == null) {
            problem = "No account belongs to customer " + customerId + ".";
        } else if (receiver == sender) {
            problem = "You cannot transfer to your own account.";
        } else if (receiver.isHistoryFull()) {
            problem = "The recipient's transaction history is full.";
        }

        if (!problem.isEmpty()) {
            System.out.println("  [!] " + problem);
            pause();
            return;
        }

        if (!confirmPin(sender, "Confirm your PIN")) {
            pause();
            return;
        }

        if (bank.transfer(sender, receiver, amount)) {
            System.out.println("  [OK] Transferred PHP " + formatPeso(amount) + " to " + receiver.getOwner().getName() + ".");
        } else {
            System.out.println("  [!] Transfer failed. Enter a number above 0 and at most PHP " + formatPeso(sender.getBalance()) + ".");
        }

        pause();
    }

    // ----------------------------------------------------- VIEW TRANSACTIONS
    private static void viewTransactions(Account account) {
        bank.outSection("view transactions");

        account.displayHistory();
        pause();
    }

    // ---------------------------------------------------------------- SHARED
    private static int verifyAccount() {
        System.out.print("  [>] Enter account number: ");
        String accountNumber = readLine();

        int slot = bank.findAccount(accountNumber);

        if (slot < 0) {
            System.out.println();
            System.out.println("  [!] Account not found.");
            return -1;
        }

        if (!confirmPin(bank.getAccount(slot), "Enter PIN")) { return -1; }

        return slot;
    }

    private static String readNewPin() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter your 4-digit PIN: ");
            String pin = readLine();
            System.out.println();

            if (pin.matches("[0-9]{4}")) { return pin; }

            if (inputEnded) { return ""; }

            System.out.println("  [!] PIN must be exactly 4 digits. Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid PINs. Nothing was saved.");
        return "";
    }

    private static double readOpeningDeposit() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter your initial deposit: ");
            double amount = readDouble();
            System.out.println();

            if (!Double.isNaN(amount) && amount >= 0) { return amount; }

            if (inputEnded) { return Double.NaN; }

            String reason = Double.isNaN(amount) ? "must be a number" : "cannot be negative";
            System.out.println("  [!] Initial deposit " + reason + ". Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid deposits. Nothing was saved.");
        return Double.NaN;
    }

    private static boolean confirmPin(Account account, String label) {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] " + label + ": ");
            String pin = readLine();
            System.out.println();

            if (account.verifyPin(pin)) { return true; }

            if (inputEnded) { return false; }

            System.out.println("  [!] Incorrect PIN. Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many incorrect attempts.");
        return false;
    }

    private static boolean blockIfHistoryFull(Account account) {
        if (!account.isHistoryFull()) { return false; }

        System.out.println("  [!] The transaction history is full. No more deposits, withdrawals or transfers.");
        pause();
        return true;
    }

    private static String generateCustomerID() {
        String customerId = "";

        do {
            customerId = "C-" + (100000 + (int) (Math.random() * 900000));
        } while (bank.isCustomerIDTaken(customerId));

        return customerId;
    }

    private static String formatPeso(double amount) { return String.format(Locale.US, "%,.2f", amount); }

    // ----------------------------------------------------------------- INPUT
    private static String readLine() {
        if (input.hasNextLine()) { return input.nextLine().trim(); }

        inputEnded = true;
        return "";
    }

    private static double readDouble() {
        if (!input.hasNextLine()) {
            inputEnded = true;
            return Double.NaN;
        }

        String text = input.nextLine().trim();

        try {
            double value = Double.parseDouble(text);

            if (!Double.isFinite(value)) { return Double.NaN; }
            return value;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static void pause() {
        System.out.println("  [INFO] Press any key to continue...");
        readLine();
        System.out.println();
    }
}

class Customer {
    private String customerID, name;

    public Customer() {
        customerID = "";
        name = "";
    }

    public void setCustomerID(String newCustomerID) { customerID = newCustomerID; }
    public void setName(String newName) { name = newName; }

    public String getCustomerID() { return customerID; }
    public String getName() { return name; }
}

class Transaction {
    private String type;
    private double amount, balanceAfter;

    public Transaction() {
        type = "";
        amount = 0;
        balanceAfter = 0;
    }

    public void setType(String newType) { type = newType; }
    public void setAmount(double newAmount) { amount = newAmount; }
    public void setBalanceAfter(double newBalanceAfter) { balanceAfter = newBalanceAfter; }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
}

class Account {
    private String accountNumber, pin;
    private double balance;
    private Customer owner;
    private Transaction[] history;
    private int transactionCount;

    public Account() {
        accountNumber = "";
        pin = "";
        balance = 0;
        owner = new Customer();
        history = new Transaction[50];
        transactionCount = 0;
    }

    public void setAccountNumber(String newAccountNumber) { accountNumber = newAccountNumber; }
    public void setPin(String newPin) { pin = newPin; }
    public void setOwner(Customer newOwner) { owner = newOwner; }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerID() { return owner.getCustomerID(); }
    public double getBalance() { return balance; }
    public Customer getOwner() {
        Customer copy = new Customer();
        copy.setCustomerID(owner.getCustomerID());
        copy.setName(owner.getName());
        return copy;
    }

    public boolean verifyPin(String inputPin) { return pin.equals(inputPin); }

    public boolean deposit(double amount) { return credit("Deposit", amount); }
    public boolean withdraw(double amount) { return debit("Withdraw", amount); }
    public boolean transferIn(double amount) { return credit("Transfer In", amount); }
    public boolean transferOut(double amount) { return debit("Transfer Out", amount); }

    private boolean credit(String type, double amount) {
        if (!Double.isFinite(amount)) { return false; }

        double value = roundToCentavos(amount);

        if (value <= 0 || transactionCount >= history.length) { return false; }

        balance = roundToCentavos(balance + value);
        record(type, value);

        return true;
    }

    private boolean debit(String type, double amount) {
        if (!Double.isFinite(amount)) { return false; }

        double value = roundToCentavos(amount);

        if (value <= 0 || value > balance || transactionCount >= history.length) { return false; }

        balance = roundToCentavos(balance - value);
        record(type, value);

        return true;
    }

    public void displayHistory() {
        if (transactionCount == 0) {
            System.out.println("  [!] No transactions yet.");
            return;
        }

        System.out.println(String.format(Locale.US, "  %-4s %-12s %14s %16s",
                "#", "Type", "Amount", "Balance")
                + "\n  -------------------------------------------------");

        for (int i = 0; i < transactionCount; i++) {
            System.out.println(String.format(Locale.US, "  %-4d %-12s %14s %16s",
                    (i + 1), history[i].getType(), formatPeso(history[i].getAmount()), formatPeso(history[i].getBalanceAfter())));
        }
    }

    public void displayCard() {
        System.out.println("  ======================================================="
                + "\n  Account Name     : " + owner.getName() + " (" + owner.getCustomerID() + ")"
                + "\n  Account Number   : " + accountNumber
                + "\n  Balance          : PHP " + formatPeso(balance)
                + "\n  =======================================================");
    }

    public void outMenu() {
        System.out.println("  [1] Deposit\n" + "  [2] Withdraw\n" + "  [3] Transfer\n" + "  [4] View Transactions\n" + "  [5] Logout\n");
    }

    public boolean isHistoryFull() { return transactionCount >= history.length; }
    private double roundToCentavos(double amount) { return Math.round(amount * 100) / 100.0; }

    private void record(String type, double amount) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balance);

        history[transactionCount] = transaction;
        transactionCount++;
    }

    private String formatPeso(double amount) { return String.format(Locale.US, "%,.2f", amount); }
}

class Bank {
    private Account[] accounts = new Account[100];
    private int accountCount = 0;

    public int getAccountCount() { return accountCount; }
    public boolean isFull() { return accountCount >= accounts.length; }

    public int findAccount(String accountNumber) {
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountNumber().equals(accountNumber)) { return i; }
        }
        return -1;
    }

    public Account findByCustomerID(String customerId) {
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getCustomerID().equals(customerId)) { return accounts[i]; }
        }
        return null;
    }

    public boolean isCustomerIDTaken(String customerId) { return findByCustomerID(customerId) != null; }

    public boolean transfer(Account sender, Account receiver, double amount) {
        if (sender == null || receiver == null || sender == receiver) { return false; }

        if (sender.isHistoryFull() || receiver.isHistoryFull()) { return false; }

        if (!sender.transferOut(amount)) { return false; }

        receiver.transferIn(amount);
        return true;
    }

    public Account getAccount(int index) {
        if (index < 0 || index >= accountCount) { return null; }
        return accounts[index];
    }

    public boolean addAccount(Account newAccount) {
        if (isFull() || findAccount(newAccount.getAccountNumber()) >= 0) { return false; }

        accounts[accountCount] = newAccount;
        accountCount++;
        return true;
    }

    public void outHeader() { System.out.println("\nBanking System\n"); }

    public void outMenu() { System.out.println("  [1] Open Account\n" + "  [2] Login\n" + "  [3] Exit\n"); }

    public void outSection(String title) {
        String line = "--- " + title + " ";

        while (line.length() < 57) {
            line = line + "-";
        }
        System.out.println(line);
    }
}
