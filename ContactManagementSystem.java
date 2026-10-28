import java.util.Locale;
import java.util.Scanner;

public class ContactManagementSystem {
    private static Scanner input = new Scanner(System.in);
    private static ContactBook book = new ContactBook();
    private static boolean inputEnded = false;
    private static final int INPUT_ATTEMPTS = 3;

    public static void main(String[] args) {
        boolean running = true;

        while (running && !inputEnded) {
            book.outHeader();
            book.outMenu();

            System.out.print("  [>] Enter your choice: ");
            String choice = readLine();
            System.out.println();

            if (!inputEnded) {
                switch (choice) {
                    case "1" -> addContact();
                    case "2" -> viewContacts();
                    case "3" -> removeContact();
                    case "4" -> {
                        System.out.println("  [i] Closing the Contact Management System.\n");
                        running = false;
                    }
                    default -> {
                        System.out.println("  [!] Invalid choice. Enter 1 to 4.\n");
                        pause();
                    }
                }
            }
        }

        if (inputEnded) { System.out.println("  [i] Input ended. Closing the Contact Management System.\n"); }

        input.close();
    }

    // ------------------------------------------------------------ ADD CONTACT
    private static void addContact() {
        book.outSection("add contact");

        if (book.isFull()) {
            System.out.println("  [!] The contact book is full. Cannot add more contacts.");
            pause();
            return;
        }

        System.out.print("  [>] Enter name: ");
        String name = readLine();

        int age = readAge();

        if (age < 0) {
            pause();
            return;
        }

        String email = readEmail();

        if (email.isEmpty()) {
            pause();
            return;
        }

        System.out.print("  [>] Enter street: ");
        String street = readLine();

        System.out.print("  [>] Enter barangay: ");
        String barangay = readLine();

        System.out.print("  [>] Enter municipality: ");
        String municipality = readLine();

        System.out.print("  [>] Enter province: ");
        String province = readLine();

        String zipCode = readZipCode();

        if (zipCode.isEmpty()) {
            pause();
            return;
        }

        System.out.print("  [>] Enter phone label (Mobile/Home): ");
        String label = readLine();

        String number = readPhoneNumber();

        if (number.isEmpty()) {
            pause();
            return;
        }

        String problem = "";

        if (name.isEmpty()) {
            problem = "Name cannot be empty.";
        } else if (street.isEmpty() || barangay.isEmpty() || municipality.isEmpty() || province.isEmpty()) {
            problem = "Every address field is required.";
        } else if (label.isEmpty()) {
            problem = "Phone label cannot be empty.";
        }

        if (!problem.isEmpty()) {
            System.out.println("  [!] " + problem + " Nothing was saved.");
            pause();
            return;
        }

        Address address = new Address();
        address.setStreet(street);
        address.setBarangay(barangay);
        address.setMunicipality(municipality);
        address.setProvince(province);
        address.setZipCode(zipCode);

        PhoneNumber phone = new PhoneNumber();
        phone.setLabel(label);
        phone.setNumber(number);

        Contact contact = new Contact();
        contact.setName(name);
        contact.setAge(age);
        contact.setEmail(email);
        contact.setAddress(address);
        contact.setPhone(phone);

        if (book.addContact(contact)) {
            System.out.println("  [OK] Contact saved. " + book.getCount() + " contact/s in the book.");
        } else {
            System.out.println("  [!] The contact could not be saved.");
        }
        pause();
    }

    // ----------------------------------------------------------- VIEW CONTACTS
    private static void viewContacts() {
        book.outSection("view contacts");

        if (book.isEmpty()) {
            System.out.println("  [!] There are no contacts yet.");
            pause();
            return;
        }

        System.out.print("  [>] Search a name or address (leave blank to show all): ");
        String keyword = readLine();
        System.out.println();

        int shown = book.displayMatches(keyword);

        if (shown == 0) { System.out.println("  [!] No contact matched \"" + keyword + "\"."); }
        pause();
    }

    // --------------------------------------------------------- REMOVE CONTACT
    private static void removeContact() {
        book.outSection("remove contact");

        if (book.isEmpty()) {
            System.out.println("  [!] There are no contacts yet.");
            pause();
            return;
        }

        System.out.print("  [>] Search the contact to remove: ");
        String keyword = readLine();
        System.out.println();

        int matches = book.countMatches(keyword);

        if (matches == 0) {
            System.out.println("  [!] No contact matched \"" + keyword + "\".");
            pause();
            return;
        }

        int index;

        if (matches == 1) {
            index = book.findFirstMatch(keyword);
        } else {
            System.out.println("  [!] " + matches + " contacts matched. Type the exact full name.");
            System.out.print("  [>] Exact full name: ");
            String exactName = readLine();
            System.out.println();

            index = book.findContact(exactName);

            if (index < 0) {
                System.out.println("  [!] No contact is named \"" + exactName + "\".");
                pause();
                return;
            }
        }

        book.getContact(index).display(index + 1);
        System.out.println();

        System.out.print("  [>] Remove this contact? (Y/N): ");
        String answer = readLine();
        System.out.println();

        if (!answer.equalsIgnoreCase("Y")) {
            System.out.println("  [i] Removal cancelled.");
            pause();
            return;
        }

        book.removeContact(index);
        System.out.println("  [OK] Contact removed. " + book.getCount() + " contact/s left.");
        pause();
    }

    // ----------------------------------------------------------------- INPUT
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
    private static int readAge() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter age: ");
            int age = readInt();
            System.out.println();

            if (age >= 1 && age <= 120) { return age; }

            if (inputEnded) { return -1; }

            System.out.println("  [!] Age must be a number from 1 to 120. Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid ages. Nothing was saved.");
        return -1;
    }

    private static String readEmail() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter email: ");
            String email = readLine();
            System.out.println();

            if (isEmail(email)) { return email; }

            if (inputEnded) { return ""; }

            System.out.println("  [!] Email must look like name@mail.com."
                    + " Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid emails. Nothing was saved.");
        return "";
    }

    private static String readZipCode() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter zip code: ");
            String zipCode = readLine();
            System.out.println();

            if (zipCode.matches("[0-9]{4}")) { return zipCode; }

            if (inputEnded) { return ""; }

            System.out.println("  [!] Zip code must be exactly 4 digits. Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid zip codes. Nothing was saved.");
        return "";
    }

    private static String readPhoneNumber() {
        for (int attempt = 1; attempt <= INPUT_ATTEMPTS; attempt++) {
            System.out.print("  [>] Enter phone number: ");
            String number = readLine();
            System.out.println();

            if (isPhoneNumber(number)) { return number; }

            if (inputEnded) { return ""; }

            System.out.println("  [!] Phone number needs 7 to 15 digits, using only digits, spaces or hyphens."
                    + " Attempt " + attempt + " of " + INPUT_ATTEMPTS + ".");
        }

        System.out.println("  [!] Too many invalid phone numbers. Nothing was saved.");
        return "";
    }

    private static boolean isEmail(String email) {
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');

        if (at < 1 || email.indexOf('@', at + 1) >= 0) { return false; }
        if (dot < at + 2 || dot == email.length() - 1) { return false; }

        return !email.contains(" ");
    }

    private static boolean isPhoneNumber(String number) {
        int digits = 0;

        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);

            if (c >= '0' && c <= '9') {
                digits++;
            } else if (c != '-' && c != ' ' && c != '+') {
                return false;
            }
        }

        return digits >= 7 && digits <= 15;
    }
}

class Contact {
    private String name, email;
    private int age;
    private Address address;
    private PhoneNumber phone;

    public Contact() {
        name = "";
        email = "";
        age = 0;
        address = new Address();
        phone = new PhoneNumber();
    }

    public void setName(String newName) { name = newName; }
    public void setEmail(String newEmail) { email = newEmail; }
    public void setAge(int newAge) { age = newAge; }
    public void setAddress(Address newAddress) { address = newAddress; }
    public void setPhone(PhoneNumber newPhone) { phone = newPhone; }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public Address getAddress() { return address.copy(); }
    public PhoneNumber getPhone() { return phone.copy(); }

    public boolean matches(String keyword) {
        return name.toLowerCase(Locale.US).contains(keyword.toLowerCase(Locale.US)) || address.matches(keyword);
    }

    public void display(int slot) {
        System.out.println("  [" + slot + "] " + name + " (" + age + ")"
                + "\n      Email   : " + email
                + "\n      Address : " + address.getFullAddress()
                + "\n      Phone   : " + phone.getLabel() + " " + phone.getNumber() + "\n");
    }
}

class Address {
    private String street, barangay, municipality, province, zipCode;

    public Address() {
        street = "";
        barangay = "";
        municipality = "";
        province = "";
        zipCode = "";
    }

    public void setStreet(String newStreet) { street = newStreet; }
    public void setBarangay(String newBarangay) { barangay = newBarangay; }
    public void setMunicipality(String newMunicipality) { municipality = newMunicipality; }
    public void setProvince(String newProvince) { province = newProvince; }
    public void setZipCode(String newZipCode) { zipCode = newZipCode; }

    public String getStreet() { return street; }
    public String getBarangay() { return barangay; }
    public String getMunicipality() { return municipality; }
    public String getProvince() { return province; }
    public String getZipCode() { return zipCode; }

    public String getFullAddress() { return street + ", " + barangay + ", " + municipality + ", " + province + " " + zipCode; }

    public boolean matches(String keyword) {
        String text = getFullAddress().toLowerCase(Locale.US);
        return text.contains(keyword.toLowerCase(Locale.US));
    }

    public Address copy() {
        Address duplicate = new Address();
        duplicate.setStreet(street);
        duplicate.setBarangay(barangay);
        duplicate.setMunicipality(municipality);
        duplicate.setProvince(province);
        duplicate.setZipCode(zipCode);
        return duplicate;
    }
}

class PhoneNumber {
    private String label, number;

    public PhoneNumber() {
        label = "";
        number = "";
    }

    public void setLabel(String newLabel) { label = newLabel; }
    public void setNumber(String newNumber) { number = newNumber; }

    public String getLabel() { return label; }
    public String getNumber() { return number; }

    public PhoneNumber copy() {
        PhoneNumber duplicate = new PhoneNumber();
        duplicate.setLabel(label);
        duplicate.setNumber(number);
        return duplicate;
    }
}

class ContactBook {
    private Contact[] contacts = new Contact[100];
    private int contactCount = 0;

    public int getCount() { return contactCount; }
    public boolean isEmpty() { return contactCount == 0; }
    public boolean isFull() { return contactCount >= contacts.length; }

    public Contact getContact(int index) {
        if (index < 0 || index >= contactCount) { return null; }
        return contacts[index];
    }

    public boolean addContact(Contact newContact) {
        if (isFull()) { return false; }

        contacts[contactCount] = newContact;
        contactCount++;
        return true;
    }

    public boolean removeContact(int index) {
        if (index < 0 || index >= contactCount) { return false; }

        for (int i = index; i < contactCount - 1; i++) {
            contacts[i] = contacts[i + 1];
        }

        contacts[contactCount - 1] = null;
        contactCount--;
        return true;
    }

    public int findContact(String name) {
        for (int i = 0; i < contactCount; i++) {
            if (contacts[i].getName().equalsIgnoreCase(name)) { return i; }
        }
        return -1;
    }

    public int findFirstMatch(String keyword) {
        for (int i = 0; i < contactCount; i++) {
            if (contacts[i].matches(keyword)) { return i; }
        }
        return -1;
    }

    public int countMatches(String keyword) {
        int total = 0;

        for (int i = 0; i < contactCount; i++) {
            if (contacts[i].matches(keyword)) { total++; }
        }
        return total;
    }

    public int displayMatches(String keyword) {
        int shown = 0;

        for (int i = 0; i < contactCount; i++) {
            if (keyword.isEmpty() || contacts[i].matches(keyword)) {
                shown++;
                contacts[i].display(shown);
            }
        }
        return shown;
    }

    public void outHeader() { System.out.println("\nContact Management System\n"); }

    public void outMenu() {
        System.out.println("  [1] Add Contact\n" + "  [2] View Contacts\n"
                + "  [3] Remove Contact\n" + "  [4] Exit\n");
    }

    public void outSection(String title) {
        String line = "--- " + title + " ";

        while (line.length() < 57) {
            line = line + "-";
        }
        System.out.println(line);
    }
}
