import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Task 3 (Slab 2) - Banking Management System
 * Console-based application demonstrating:
 * - Customer, Account, Transaction classes
 * - Encapsulation, inheritance (SavingsAccount / CurrentAccount)
 * - Collections for data management
 * - Deposits, withdrawals, fund transfers, balance check, transaction history
 * - Exception handling & input validation
 */

// ---------- Custom Exceptions ----------
class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) { super(message); }
}

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) { super(message); }
}

class InvalidBankInputException extends Exception {
    public InvalidBankInputException(String message) { super(message); }
}

// ---------- Transaction Class ----------
class Transaction {
    private String type;
    private double amount;
    private String timestamp;
    private String details;

    public Transaction(String type, double amount, String details) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s ₹%-10.2f %s", timestamp, type, amount, details);
    }
}

// ---------- Customer Class ----------
class Customer {
    private int customerId;
    private String name;
    private String phone;

    public Customer(int customerId, String name, String phone) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
    }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return String.format("Customer ID: %-5d | Name: %-15s | Phone: %s", customerId, name, phone);
    }
}

// ---------- Abstract Account Class (Inheritance) ----------
abstract class Account {
    protected int accountNumber;
    protected Customer owner;
    protected double balance;
    protected List<Transaction> history;

    public Account(int accountNumber, Customer owner, double initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
        this.history = new ArrayList<>();
    }

    public int getAccountNumber() { return accountNumber; }
    public Customer getOwner() { return owner; }
    public double getBalance() { return balance; }
    public List<Transaction> getHistory() { return history; }

    public void deposit(double amount) throws InvalidBankInputException {
        if (amount <= 0) throw new InvalidBankInputException("Deposit amount must be positive!");
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount, "Balance: " + balance));
    }

    // Abstract method — withdrawal rules differ per account type
    public abstract void withdraw(double amount) throws InsufficientFundsException, InvalidBankInputException;

    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("A/C No: %-6d | Type: %-10s | Owner: %-15s | Balance: ₹%.2f",
                accountNumber, getAccountType(), owner.getName(), balance);
    }
}

// ---------- SavingsAccount (no overdraft allowed) ----------
class SavingsAccount extends Account {
    private static final double MIN_BALANCE = 500.0;

    public SavingsAccount(int accountNumber, Customer owner, double initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException, InvalidBankInputException {
        if (amount <= 0) throw new InvalidBankInputException("Withdrawal amount must be positive!");
        if (balance - amount < MIN_BALANCE) {
            throw new InsufficientFundsException("Withdrawal denied! Minimum balance of ₹" + MIN_BALANCE + " must be maintained.");
        }
        balance -= amount;
        history.add(new Transaction("WITHDRAW", amount, "Balance: " + balance));
    }

    @Override
    public String getAccountType() { return "Savings"; }
}

// ---------- CurrentAccount (overdraft allowed up to a limit) ----------
class CurrentAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 5000.0;

    public CurrentAccount(int accountNumber, Customer owner, double initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException, InvalidBankInputException {
        if (amount <= 0) throw new InvalidBankInputException("Withdrawal amount must be positive!");
        if (balance - amount < -OVERDRAFT_LIMIT) {
            throw new InsufficientFundsException("Withdrawal denied! Overdraft limit of ₹" + OVERDRAFT_LIMIT + " exceeded.");
        }
        balance -= amount;
        history.add(new Transaction("WITHDRAW", amount, "Balance: " + balance));
    }

    @Override
    public String getAccountType() { return "Current"; }
}

// ---------- Bank Class (Business Logic) ----------
class Bank {
    private Map<Integer, Customer> customers = new HashMap<>();
    private Map<Integer, Account> accounts = new HashMap<>();

    public void addCustomer(Customer c) throws InvalidBankInputException {
        if (customers.containsKey(c.getCustomerId())) {
            throw new InvalidBankInputException("Customer ID already exists!");
        }
        customers.put(c.getCustomerId(), c);
    }

    public Customer findCustomer(int id) throws AccountNotFoundException {
        Customer c = customers.get(id);
        if (c == null) throw new AccountNotFoundException("Customer ID " + id + " not found!");
        return c;
    }

    public void openAccount(Account acc) throws InvalidBankInputException {
        if (accounts.containsKey(acc.getAccountNumber())) {
            throw new InvalidBankInputException("Account number already exists!");
        }
        accounts.put(acc.getAccountNumber(), acc);
    }

    public Account findAccount(int accNo) throws AccountNotFoundException {
        Account a = accounts.get(accNo);
        if (a == null) throw new AccountNotFoundException("Account " + accNo + " not found!");
        return a;
    }

    public void transfer(int fromAcc, int toAcc, double amount)
            throws AccountNotFoundException, InsufficientFundsException, InvalidBankInputException {
        Account from = findAccount(fromAcc);
        Account to = findAccount(toAcc);
        from.withdraw(amount);
        to.deposit(amount);
        from.getHistory().add(new Transaction("TRANSFER-OUT", amount, "To A/C " + toAcc));
        to.getHistory().add(new Transaction("TRANSFER-IN", amount, "From A/C " + fromAcc));
    }

    public void displayAllAccounts() {
        if (accounts.isEmpty()) { System.out.println("No accounts found."); return; }
        System.out.println("----------------------------------------------------------------------");
        for (Account a : accounts.values()) System.out.println(a);
        System.out.println("----------------------------------------------------------------------");
    }
}

// ---------- Main Application ----------
public class BankingManagementSystem {
    private static Scanner sc = new Scanner(System.in);
    private static Bank bank = new Bank();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("     BANKING MANAGEMENT SYSTEM");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1: registerCustomer(); break;
                case 2: openAccount(); break;
                case 3: deposit(); break;
                case 4: withdraw(); break;
                case 5: transfer(); break;
                case 6: checkBalance(); break;
                case 7: transactionHistory(); break;
                case 8: bank.displayAllAccounts(); break;
                case 9:
                    running = false;
                    System.out.println("Thank you for banking with us!");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n----------- MENU -----------");
        System.out.println("1. Register Customer");
        System.out.println("2. Open Account");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer Funds");
        System.out.println("6. Check Balance");
        System.out.println("7. Transaction History");
        System.out.println("8. Display All Accounts");
        System.out.println("9. Exit");
        System.out.println("-----------------------------");
    }

    private static void registerCustomer() {
        try {
            int id = readInt("Enter Customer ID: ");
            System.out.print("Enter Name: ");
            String name = sc.nextLine().trim();
            if (name.isEmpty()) throw new InvalidBankInputException("Name cannot be empty!");
            System.out.print("Enter Phone: ");
            String phone = sc.nextLine().trim();
            bank.addCustomer(new Customer(id, name, phone));
            System.out.println("Customer registered successfully!");
        } catch (InvalidBankInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void openAccount() {
        try {
            int custId = readInt("Enter Customer ID: ");
            Customer c = bank.findCustomer(custId);
            int accNo = readInt("Enter New Account Number: ");
            System.out.print("Enter Account Type (savings/current): ");
            String type = sc.nextLine().trim().toLowerCase();
            double initial = readDouble("Enter Initial Deposit Amount: ");
            if (initial < 0) throw new InvalidBankInputException("Initial deposit cannot be negative!");

            Account acc;
            if (type.equals("savings")) {
                acc = new SavingsAccount(accNo, c, initial);
            } else if (type.equals("current")) {
                acc = new CurrentAccount(accNo, c, initial);
            } else {
                throw new InvalidBankInputException("Invalid account type! Use 'savings' or 'current'.");
            }
            bank.openAccount(acc);
            System.out.println("Account opened successfully!");
        } catch (AccountNotFoundException | InvalidBankInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deposit() {
        try {
            int accNo = readInt("Enter Account Number: ");
            Account acc = bank.findAccount(accNo);
            double amount = readDouble("Enter Deposit Amount: ");
            acc.deposit(amount);
            System.out.println("Deposit successful! New Balance: ₹" + acc.getBalance());
        } catch (AccountNotFoundException | InvalidBankInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void withdraw() {
        try {
            int accNo = readInt("Enter Account Number: ");
            Account acc = bank.findAccount(accNo);
            double amount = readDouble("Enter Withdrawal Amount: ");
            acc.withdraw(amount);
            System.out.println("Withdrawal successful! New Balance: ₹" + acc.getBalance());
        } catch (AccountNotFoundException | InsufficientFundsException | InvalidBankInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void transfer() {
        try {
            int fromAcc = readInt("Enter Sender Account Number: ");
            int toAcc = readInt("Enter Receiver Account Number: ");
            double amount = readDouble("Enter Transfer Amount: ");
            bank.transfer(fromAcc, toAcc, amount);
            System.out.println("Transfer successful!");
        } catch (AccountNotFoundException | InsufficientFundsException | InvalidBankInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void checkBalance() {
        try {
            int accNo = readInt("Enter Account Number: ");
            Account acc = bank.findAccount(accNo);
            System.out.println("Current Balance: ₹" + acc.getBalance());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void transactionHistory() {
        try {
            int accNo = readInt("Enter Account Number: ");
            Account acc = bank.findAccount(accNo);
            if (acc.getHistory().isEmpty()) {
                System.out.println("No transactions yet for this account.");
            } else {
                System.out.println("----------------------------------------------------------------------");
                for (Transaction t : acc.getHistory()) System.out.println(t);
                System.out.println("----------------------------------------------------------------------");
            }
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }
}
