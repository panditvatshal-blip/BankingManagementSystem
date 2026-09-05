Task 3 - Banking Management System (Slab 2: Intermediate)
============================================================

File: BankingManagementSystem.java

Description:
-------------
A console-based Banking Management System built in Java. It supports
customer registration, account creation (Savings/Current), deposits,
withdrawals, fund transfers, balance checking, and transaction history.

How to Compile and Run:
-------------------------
1. Open terminal/command prompt in the folder containing this file.
2. Compile:
     javac BankingManagementSystem.java
3. Run:
     java BankingManagementSystem

Note: If the Rupee symbol (₹) shows as garbled characters in your
terminal, run with UTF-8 encoding:
     java -Dfile.encoding=UTF-8 BankingManagementSystem

Features:
----------
- Register Customer
- Open Account (Savings or Current)
- Deposit
- Withdraw (Savings enforces minimum balance; Current allows overdraft
  up to a limit)
- Transfer Funds between accounts
- Check Balance
- View Transaction History (with timestamps)
- Display All Accounts
- Exit

Concepts Demonstrated:
------------------------
- Classes, Objects (Customer, Account, Transaction, Bank)
- Inheritance (abstract Account class -> SavingsAccount, CurrentAccount)
- Abstraction (abstract withdraw() and getAccountType() methods)
- Encapsulation (protected/private fields)
- Collections (HashMap<Integer, Customer>, HashMap<Integer, Account>, List<Transaction>)
- Custom Exceptions (AccountNotFoundException, InsufficientFundsException,
  InvalidBankInputException)
- Input validation
- Exception handling (try-catch with multi-catch blocks)
- Menu-driven console interface
- Clean, modular, end-to-end application structure

Notes:
-------
Data is stored in-memory during the session (no file persistence).
Tested successfully by compiling and running sample register/open
account/deposit/balance-check operations.
