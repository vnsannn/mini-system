import java.util.Locale;
import java.util.Scanner;

public class ShoppingCartSystem {
    private static Scanner input = new Scanner(System.in);
    private static Store store = new Store();
    private static Cart cart = new Cart();
    private static boolean inputEnded = false;

    public static void main(String[] args) {
        store.loadCatalog();

        boolean running = true;

        while (running && !inputEnded) {
            store.outHeader();
            store.outMenu();

            System.out.print("  [>] Enter your choice: ");
            String choice = readLine();
            System.out.println();

            if (!inputEnded) {
                switch (choice) {
                    case "1" -> viewProducts();
                    case "2" -> viewCart();
                    case "3" -> {
                        System.out.println("  [i] Closing the Shopping Cart System.\n");
                        running = false;
                    }
                    default -> {
                        System.out.println("  [!] Invalid choice. Enter 1 to 3.\n");
                        pause();
                    }
                }
            }
        }

        if (inputEnded) { System.out.println("  [i] Input ended. Closing the Shopping Cart System.\n"); }

        input.close();
    }

    // ------------------------------------------------------------ VIEW PRODUCTS
    private static void viewProducts() {
        store.outSection("view products");

        store.displayCatalog();
        System.out.println();
        System.out.println("  [i] Type a product name to add it, or leave it blank to go back.\n");

        int added = 0;

        while (!inputEnded) {
            int slot = readProductSlot();

            if (slot < 0) { break; }

            int quantity = readPurchaseQuantity(slot);

            if (quantity == 0) { break; }

            Product product = store.getProduct(slot);
            cart.addOrMerge(product, quantity);
            added++;

            System.out.println("  [OK] " + product.getName() + " x" + quantity + " added to the cart.");

            System.out.print("  [>] Is that all? (Y/N): ");
            String done = readLine();
            System.out.println();

            if (inputEnded || done.equalsIgnoreCase("Y")) { break; }
        }

        if (added > 0) {
            System.out.println("  [i] " + added + " product line/s in the cart. Subtotal: PHP " + formatPeso(cart.getSubtotal()));
        } else {
            System.out.println("  [i] Nothing was added to the cart.");
        }

        pause();
    }

    private static int readProductSlot() {
        while (!inputEnded) {
            System.out.print("  [>] Enter product name (leave blank to stop): ");
            String name = readLine();
            System.out.println();

            if (name.isEmpty() || inputEnded) { return -1; }

            int slot = store.findProduct(name);

            if (slot < 0) {
                System.out.println("  [!] No product is named \"" + name + "\". Please try again.");
                continue;
            }

            if (store.getProduct(slot).getStock() - cart.getQuantityOf(store.getProduct(slot).getName()) <= 0) {
                System.out.println("  [!] " + store.getProduct(slot).getName() + " is out of stock.");
                continue;
            }

            return slot;
        }
        return -1;
    }

    private static int readPurchaseQuantity(int slot) {
        Product product = store.getProduct(slot);
        int available = product.getStock() - cart.getQuantityOf(product.getName());

        if (available <= 0) { return 0; }

        while (!inputEnded) {
            System.out.print("  [>] How many do you want to buy? (1 to " + available + "): ");
            int quantity = readInt();
            System.out.println();

            if (inputEnded) { return 0; }

            if (quantity < 1) {
                System.out.println("  [!] Quantity must be a number from 1 to " + available + ". Please try again.");
                continue;
            }

            if (quantity > available) {
                System.out.println("  [!] Only " + available + " of " + product.getName() + " left. Please try again.");
                continue;
            }

            return quantity;
        }
        return 0;
    }

    // ---------------------------------------------------------------- VIEW CART
    private static void viewCart() {
        store.outSection("view cart");

        if (cart.isEmpty()) {
            System.out.println("  [!] The cart is empty.");
            pause();
            return;
        }

        cart.displayContents();
        System.out.println();

        System.out.println("  [1] Checkout\n" + "  [2] Remove Item\n" + "  [3] Back\n");

        System.out.print("  [>] Enter your choice: ");
        String choice = readLine();
        System.out.println();

        if (inputEnded) { return; }

        switch (choice) {
            case "1" -> checkout();
            case "2" -> removeFromCart();
            case "3" -> { }
            default -> {
                System.out.println("  [!] Invalid choice. Enter 1 to 3.\n");
                pause();
            }
        }
    }

    // -------------------------------------------------------------- REMOVE ITEM
    private static void removeFromCart() {
        store.outSection("remove item");

        int slot = -1;

        while (!inputEnded) {
            System.out.print("  [>] Enter the item name to remove: ");
            String name = readLine();
            System.out.println();

            if (inputEnded) { return; }

            if (name.isEmpty()) {
                System.out.println("  [i] Nothing was removed.");
                pause();
                return;
            }

            slot = cart.findItem(name);

            if (slot >= 0) { break; }

            System.out.println("  [!] \"" + name + "\" is not in the cart. Please try again.");
        }

        if (inputEnded) { return; }

        CartItem line = cart.getItem(slot);
        System.out.println("  [1] " + line.getProduct().getName() + " - " + line.getQuantity()
                + " x PHP " + formatPeso(line.getProduct().getPrice()));
        System.out.println();

        int amount = readRemoveQuantity(slot);

        if (amount == 0) {
            pause();
            return;
        }

        System.out.print("  [>] Remove " + amount + " from the cart? (Y/N): ");
        String answer = readLine();
        System.out.println();

        if (!answer.equalsIgnoreCase("Y")) {
            System.out.println("  [i] Removal cancelled.");
            pause();
            return;
        }

        if (amount == line.getQuantity()) {
            cart.removeItem(slot);
            System.out.println("  [OK] " + line.getProduct().getName() + " removed. " + cart.getCount() + " item/s left.");
            if (cart.isEmpty()) { System.out.println("  [i] The cart is now empty."); }
        } else {
            cart.removeFromItem(slot, amount);
            System.out.println("  [OK] " + line.getProduct().getName() + " reduced to "
                    + line.getQuantity() + " in the cart.");
        }

        pause();
    }

    private static int readRemoveQuantity(int slot) {
        int available = cart.getItem(slot).getQuantity();

        while (!inputEnded) {
            System.out.print("  [>] How many do you want to remove? (1 to " + available + "): ");
            int amount = readInt();
            System.out.println();

            if (inputEnded) { return 0; }

            if (amount < 1 || amount > available) {
                System.out.println("  [!] Quantity must be a number from 1 to " + available + ". Please try again.");
                continue;
            }

            return amount;
        }
        return 0;
    }

    // ---------------------------------------------------------------- CHECKOUT
    private static void checkout() {
        store.outSection("checkout");

        System.out.print("  [>] Enter customer name: ");
        String customerName = readLine();

        String phoneNumber = readPhoneNumber();

        if (phoneNumber.isEmpty()) { pause(); return; }

        System.out.print("  [>] Enter delivery address: ");
        String address = readLine();
        System.out.println();

        if (inputEnded) { return; }

        if (customerName.isEmpty()) {
            System.out.println("  [!] Customer name cannot be empty. Nothing was placed.");
            pause();
            return;
        }

        if (address.isEmpty()) {
            System.out.println("  [!] Delivery address cannot be empty. Nothing was placed.");
            pause();
            return;
        }

        String payment = readPaymentMethod();

        if (payment.isEmpty()) { pause(); return; }

        User customer = new User();
        customer.setFullName(customerName);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);

        double total = cart.getTotal();

        System.out.println("  Customer : " + customer.getFullName()
                + "\n  Phone    : " + customer.getPhoneNumber()
                + "\n  Address  : " + customer.getAddress()
                + "\n  Payment  : " + payment);
        cart.displayContents();

        for (int i = 0; i < cart.getCount(); i++) {
            store.sell(cart.getItem(i).getProduct().getName(), cart.getItem(i).getQuantity());
        }

        cart.clear();

        String status = payment.equals("Cash on Delivery") ? "to be paid on delivery" : "paid online";
        System.out.println("  [OK] Order placed. PHP " + formatPeso(total) + " " + status + ".");
        System.out.println("  [i] Stock updated. The cart is now empty.");
        pause();
    }

    private static String readPhoneNumber() {
        while (!inputEnded) {
            System.out.print("  [>] Enter phone number: ");
            String number = readLine();
            System.out.println();

            if (inputEnded) { return ""; }

            if (isPhoneNumber(number)) { return number; }

            System.out.println("  [!] Phone number needs 7 to 15 digits, using only digits, spaces or hyphens."
                    + " Please try again.");
        }
        return "";
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

    private static String readPaymentMethod() {
        while (!inputEnded) {
            System.out.println("  [1] Cash on Delivery\n" + "  [2] Online Payment\n");

            System.out.print("  [>] Enter payment method: ");
            String choice = readLine();
            System.out.println();

            if (inputEnded) { return ""; }

            if (choice.equals("1")) { return "Cash on Delivery"; }
            if (choice.equals("2")) { return "Online Payment"; }

            System.out.println("  [!] Invalid payment method. Enter 1 or 2. Please try again.");
        }
        return "";
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

    private static String formatPeso(double amount) { return String.format(Locale.US, "%,.2f", amount); }
}

class Product {
    private String name;
    private double price;
    private int stock;

    public Product() {
        name = "";
        price = 0.0;
        stock = 0;
    }

    public void setName(String newName) { name = newName; }
    public void setPrice(double newPrice) { price = roundToCentavos(newPrice); }
    public void setStock(int newStock) { stock = newStock; }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    private double roundToCentavos(double amount) { return Math.round(amount * 100) / 100.0; }
}

class CartItem {
    private Product product;
    private int quantity;

    public CartItem() {
        product = new Product();
        quantity = 0;
    }

    public void setProduct(Product newProduct) {
        product = new Product();
        product.setName(newProduct.getName());
        product.setPrice(newProduct.getPrice());
        product.setStock(newProduct.getStock());
    }

    public void setQuantity(int newQuantity) { quantity = newQuantity; }
    public void addQuantity(int extra) { quantity = quantity + extra; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public double getSubtotal() { return Math.round(product.getPrice() * quantity * 100) / 100.0; }
}

class Cart {
    private CartItem[] items = new CartItem[100];
    private int itemCount = 0;

    public static final double SHIPPING_FEE = 38.00;

    public int getCount() { return itemCount; }
    public boolean isEmpty() { return itemCount == 0; }

    public CartItem getItem(int index) {
        if (index < 0 || index >= itemCount) { return null; }
        return items[index];
    }

    public boolean addOrMerge(Product product, int quantity) {
        int slot = findItem(product.getName());

        if (slot >= 0) {
            items[slot].addQuantity(quantity);
            return true;
        }

        if (itemCount >= items.length) { return false; }

        CartItem line = new CartItem();
        line.setProduct(product);
        line.setQuantity(quantity);
        items[itemCount] = line;
        itemCount++;
        return true;
    }

    public boolean removeItem(int index) {
        if (index < 0 || index >= itemCount) { return false; }

        for (int i = index; i < itemCount - 1; i++) {
            items[i] = items[i + 1];
        }

        items[itemCount - 1] = null;
        itemCount--;
        return true;
    }

    public boolean removeFromItem(int index, int quantity) {
        if (index < 0 || index >= itemCount) { return false; }
        if (quantity <= 0 || quantity > items[index].getQuantity()) { return false; }

        if (quantity == items[index].getQuantity()) { return removeItem(index); }

        items[index].addQuantity(-quantity);
        return true;
    }

    public void clear() {
        for (int i = 0; i < itemCount; i++) {
            items[i] = null;
        }
        itemCount = 0;
    }

    public int findItem(String name) {
        for (int i = 0; i < itemCount; i++) {
            if (items[i].getProduct().getName().equalsIgnoreCase(name)) { return i; }
        }
        return -1;
    }

    public int getQuantityOf(String name) {
        int slot = findItem(name);

        if (slot < 0) { return 0; }
        return items[slot].getQuantity();
    }

    public int getUnitCount() {
        int total = 0;

        for (int i = 0; i < itemCount; i++) {
            total = total + items[i].getQuantity();
        }
        return total;
    }

    public double getSubtotal() {
        double total = 0.0;

        for (int i = 0; i < itemCount; i++) {
            total = total + items[i].getSubtotal();
        }
        return Math.round(total * 100) / 100.0;
    }

    public double getShipping() {
        if (isEmpty()) { return 0.0; }
        return SHIPPING_FEE;
    }

    public double getTotal() { return Math.round((getSubtotal() + getShipping()) * 100) / 100.0; }

    public void displayContents() {
        System.out.println(String.format(Locale.US, "  %-4s %-20s %10s %5s %12s",
                "#", "Item", "Price", "Qty", "Subtotal")
                + "\n  " + "-------------------------------------------------------");

        for (int i = 0; i < itemCount; i++) {
            System.out.println(String.format(Locale.US, "  %-4d %-20s %10s %5d %12s",
                    (i + 1), fit(items[i].getProduct().getName(), 20),
                    formatPeso(items[i].getProduct().getPrice()),
                    items[i].getQuantity(), formatPeso(items[i].getSubtotal())));
        }

        System.out.println("  -------------------------------------------------------"
                + "\n  " + getUnitCount() + " item/s"
                + "\n  Subtotal : PHP " + formatPeso(getSubtotal())
                + "\n  Shipping : PHP " + formatPeso(getShipping())
                + "\n  Total    : PHP " + formatPeso(getTotal()));
    }

    private String fit(String text, int width) {
        if (text.length() <= width) { return text; }
        return text.substring(0, width - 2) + "..";
    }

    private String formatPeso(double amount) { return String.format(Locale.US, "%,.2f", amount); }
}

class User {
    private String fullName;
    private String phoneNumber;
    private String address;

    public User() {
        fullName = "";
        phoneNumber = "";
        address = "";
    }

    public void setFullName(String newFullName) { fullName = newFullName; }
    public void setPhoneNumber(String newPhoneNumber) { phoneNumber = newPhoneNumber; }
    public void setAddress(String newAddress) { address = newAddress; }

    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
}

class Store {
    private Product[] products = new Product[8];
    private int productCount = 0;

    public int getCount() { return productCount; }
    public boolean isFull() { return productCount >= products.length; }

    public Product getProduct(int index) {
        if (index < 0 || index >= productCount) { return null; }
        return products[index];
    }

    public void loadCatalog() {
        addCatalogItem("Ballpen", 12.50, 20);
        addCatalogItem("Notebook", 45.00, 15);
        addCatalogItem("Pencil", 8.75, 30);
        addCatalogItem("Eraser", 6.00, 25);
        addCatalogItem("Ruler", 18.25, 10);
        addCatalogItem("Backpack", 850.00, 5);
    }

    private void addCatalogItem(String name, double price, int stock) {
        if (isFull()) { return; }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);

        products[productCount] = product;
        productCount++;
    }

    public int findProduct(String name) {
        for (int i = 0; i < productCount; i++) {
            if (products[i].getName().equalsIgnoreCase(name)) { return i; }
        }
        return -1;
    }

    public boolean sell(String name, int quantity) {
        int slot = findProduct(name);

        if (slot < 0 || quantity <= 0) { return false; }
        if (products[slot].getStock() < quantity) { return false; }

        products[slot].setStock(products[slot].getStock() - quantity);
        return true;
    }

    public void displayCatalog() {
        System.out.println(String.format(Locale.US, "  %-4s %-20s %12s %8s",
                "#", "Product", "Price", "Stock")
                + "\n  " + "---------------------------------------------");

        for (int i = 0; i < productCount; i++) {
            System.out.println(String.format(Locale.US, "  %-4d %-20s %12s %8d",
                    (i + 1), products[i].getName(), formatPeso(products[i].getPrice()), products[i].getStock()));
        }
    }

    private String formatPeso(double amount) { return String.format(Locale.US, "%,.2f", amount); }
    public void outHeader() { System.out.println("\nShopping Cart System\n"); }

    public void outMenu() {
        System.out.println("  [1] View Products\n" + "  [2] View Cart\n" + "  [3] Exit\n");
    }

    public void outSection(String title) {
        String line = "--- " + title + " ";

        while (line.length() < 57) {
            line = line + "-";
        }
        System.out.println(line);
    }
}
