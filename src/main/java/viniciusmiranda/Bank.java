package viniciusmiranda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import viniciusmiranda.db.DB;
import viniciusmiranda.db.DbException;

public class Bank {
    List<BankAccount> accounts = new ArrayList<>();

    Bank() {
        try {
            syncAccountList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAccount(String firstName, String middleName, String lastName, boolean isSavings) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet keys = null;
        Person person = new Person(firstName, middleName, lastName);

        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false);// desabilita commit a cada comando

            st = prepareInsertPerson(conn, person);
            int rowsPerson = st.executeUpdate();

            if (rowsPerson > 0) {
                BankAccount bankAccount = createAccountInstance(firstName, middleName, lastName, isSavings);
                // pega id gerado pelo db na tabela person da ultima pessoa inserida
                keys = st.getGeneratedKeys();
                long personId = (keys.next()) ? keys.getInt(1) : 0;

                st = prepareInsertAccount(conn, bankAccount.getAccountNumber(), personId, isSavings);
                st.executeUpdate();

                conn.commit();// commitar transaction
                accounts.add(bankAccount);
            } else {
                conn.rollback();
            }
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
                if (keys != null)
                    keys.close();
                DB.closeStatement(st);
                DB.closeConnection(conn);
            } catch (Exception e) {
                System.err.println("Erro ao fechar recursos. Causado por: " + e.getMessage());
            }
        }
    }

    public void deleteAccount(String accountNumber) {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = DB.getConnection();

            st = conn.prepareStatement("DELETE FROM account WHERE account_number = ?");

            st.setString(1, accountNumber);

            st.executeUpdate();

            accounts.removeIf(account -> account.getAccountNumber() == accountNumber);
            System.out.println("Conta apagada com sucesso");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    public void listAccounts() {
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount bankAccount = accounts.get(i);
            System.out.println("-----------------------------------------");
            System.out.printf("Digite (%d) para escolher: ", i + 1);
            System.out.println("Conta de: " + bankAccount.getAccountHolder().getFullName());
            System.out.println(";Número: " + bankAccount.getAccountNumber());
            System.out.println("-----------------------------------------");
        }
    }

    public BankAccount getAccountByHumanReadableIndex(int index) {
        if (index == 0)
            return null;
        return accounts.get(index - 1);
    }

    public void printTransactionsLog() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();

            String getLogSql = "SELECT * FROM account_transaction at JOIN account a ON at.account_number = a.account_number";

            st = conn.createStatement();

            rs = st.executeQuery(getLogSql);

            while (rs.next()) {
                printLog(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    private BankAccount createAccountInstance(String firstName, String middleName, String lastName, boolean isSavings) {
        return isSavings ? new SavingsAccount(firstName, middleName, lastName, true)
                : new CheckingAccount(firstName, middleName, lastName, false);
    }

    // insere person e retorna numero de rows affected
    private PreparedStatement prepareInsertPerson(Connection conn, Person person) throws SQLException {
        String personInsertSql = "INSERT INTO person (first_name, middle_name, last_name) VALUES (?, ?, ?)";
        PreparedStatement st = conn.prepareStatement(personInsertSql, Statement.RETURN_GENERATED_KEYS);// retorna id
                                                                                                       // gerado pelo db
        st.setString(1, person.getFirstName());
        st.setString(2, person.getMiddleName());
        st.setString(3, person.getLastName());

        return st;
    }

    private PreparedStatement prepareInsertAccount(Connection conn, String accountNumber, long personId,
            boolean isSavings)
            throws SQLException {
        PreparedStatement st = conn
                .prepareStatement(
                        "INSERT INTO account (balance, account_number, account_type, account_holder_id) VALUES (?, ?, ?, ?)");

        st.setInt(1, 0);
        st.setString(2, accountNumber);
        st.setShort(3, isSavings ? (short) 1 : (short) 2);
        st.setLong(4, personId);

        return st;
    }

    private void printLog(ResultSet rs) throws SQLException {
        System.out.println("-----------------------------------------");
        System.out.println("Número da conta: " + rs.getInt("account_number"));
        System.out.printf("Tipo de conta: %s", rs.getInt("account_type") == 1 ? "Poupança" : "Corrente" + "\n");
        System.out.println("Saldo antes: " + rs.getString("balance_before"));
        System.out.println("Saldo após: " + rs.getString("balance_after"));
        System.out.println("Operação: " + rs.getString("operation"));
        System.out.println("-----------------------------------------");
    }

    private void syncAccountList() throws SQLException {
        BankAccount account = null;
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();

            String getAccountsSql = "SELECT * FROM account a JOIN person p ON a.account_holder_id = p.id";

            st = conn.createStatement();

            rs = st.executeQuery(getAccountsSql);

            while (rs.next()) {
                short accountType = rs.getShort("account_type");
                String accountNumber = rs.getString("account_number");
                double balance = rs.getDouble("balance");
                String firstName = rs.getString("first_name");
                String middleName = rs.getString("middle_name");
                String lastName = rs.getString("last_name");

                Person holder = new Person(firstName, middleName, lastName);
                if (accountType == 1) {
                    account = new SavingsAccount(holder, accountNumber, balance, true);
                } else {
                    account = new CheckingAccount(holder, accountNumber, balance, false);
                }
                accounts.add(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }

    }
}
