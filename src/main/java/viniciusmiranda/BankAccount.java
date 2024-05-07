package viniciusmiranda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import viniciusmiranda.db.DB;
import viniciusmiranda.db.DbException;

public abstract class BankAccount {
    private String accountNumber;
    private Person accountHolder;
    private double balance;
    private final Random random = new Random();

    public BankAccount(String firstName, String middleName, String lastName, boolean isSavings) {
        this.balance = 0;
        this.accountHolder = new Person(firstName, middleName, lastName);
        this.accountNumber = String.valueOf(100000000 + random.nextInt(900000000));
    }
    
    public BankAccount(Person accountHolder, String accountNumber, double balance, boolean isSavings) {
        this.balance = balance;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
    }

    public void withdraw(double value) {
        if (value > balance) {
            System.out.println("Valor maior que saldo disponível.");
            return;
        }

        updateBalance(value, false);
    }

    public void deposit(double value) {
        if (value < 1) {
            System.out.println("Valor de depósito inválido");
            return;
        }

        updateBalance(value, true);
    }

    private void updateBalance(double value, boolean isDeposit) {
        double updatedBalance = isDeposit ? balance + value : balance - value;

        Connection conn = null;
        PreparedStatement st = null;
        int rowsAffectedBalance = 0, rowsAffectedTransaction;

        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false);// desabilita commit a cada comando

            st = updateBalanceStatement(conn, updatedBalance, accountNumber);
            rowsAffectedBalance = st.executeUpdate();

            st = prepareInsertTransaction(conn, value, updatedBalance, isDeposit);
            rowsAffectedTransaction = st.executeUpdate();

            if (rowsAffectedBalance <= 0 || rowsAffectedTransaction <= 0) {
                System.out.println("Erro ao atualizar saldo");
                conn.rollback();
                return;
            }

            balance = updatedBalance;

            conn.commit();
            System.out.println("Saldo atualizado com sucesso.");
        } catch (SQLException e) {
            try {
                conn.rollback();
                throw new DbException("Transação deu rollback! Causado por: " + e.getMessage());
            } catch (SQLException rollbackException) {
                throw new DbException("Erro ao tentar rollback, Causado por: " + rollbackException.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                DB.closeStatement(st);
                DB.closeConnection(conn);
            } catch (Exception e) {
                System.err.println("Erro ao fechar recursos. Causado por: " + e.getMessage());
            }
        }

    }

    private PreparedStatement updateBalanceStatement(Connection conn, double updatedBalance, String accountNumber)
            throws SQLException {
        PreparedStatement st = conn.prepareStatement("UPDATE account SET balance = ? WHERE account_number = ?");

        st.setDouble(1, updatedBalance);
        st.setString(2, accountNumber);

        return st;
    }

    private PreparedStatement prepareInsertTransaction(Connection conn, double previousBalance, double currentBalance,
            boolean isDeposit) throws SQLException {
        PreparedStatement st = conn.prepareStatement(
                "INSERT INTO account_transaction(operation, balance_before, balance_after, account_number) VALUES(?,?,?,?)");

        st.setString(1, isDeposit ? "DEPÓSITO" : "SAQUE");
        st.setDouble(2, previousBalance);
        st.setDouble(3, currentBalance);
        st.setString(4, accountNumber);

        return st;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Person getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }
}