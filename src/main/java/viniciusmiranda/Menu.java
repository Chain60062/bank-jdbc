package viniciusmiranda;

import java.util.Scanner;

public class Menu {
    static final Scanner scanner = new Scanner(System.in);
    static final Bank bank = new Bank();
    BankAccount currentAccount = null;

    public void loadMenu() {
        boolean leaveAccount = true, leaveProgram = false;
        int option = 0;

        // addMockAccounts();

        while (!leaveProgram) {
            option = printMenu();
            switch (option) {
                case 0:
                    leaveProgram = true;
                    continue;
                case 1:
                    createAccount();
                    continue;
                case 2:
                    removeAccount();
                    continue;
                case 3:
                    selectAccount();
                    leaveAccount = false;
                    break;
                case 4:
                    bank.printTransactionsLog();
                    break;
                default:
                    leaveProgram = true;
                    break;
            }
            while (!leaveAccount) {
                option = printMenuAccount();
                switch (option) {
                    case 1:
                        System.out.println("Seu saldo é: R$" + currentAccount.getBalance());
                        break;
                    case 2:
                        System.out.println("Você escolheu sacar, digite a quantia: ");
                        double valueWithdraw = scanner.nextDouble();
                        currentAccount.withdraw(valueWithdraw);
                        break;
                    case 3:
                        System.out.println("Você escolheu depositar, digite a quantia: ");
                        double valueDeposit = scanner.nextDouble();
                        currentAccount.deposit(valueDeposit);
                        break;
                    case 0:
                        leaveAccount = true;
                        break;
                    default:
                        break;
                }
            }
        }

    }

    private int printMenu() {
        System.out.println("Bem vindo ao banco, escolha uma ação: ");
        System.out.println("(1) para criar conta");
        System.out.println("(2) para remover uma conta");
        System.out.println("(3) para escolher uma conta");
        System.out.println("(4) para ver log de transações");
        System.out.println("(0) para sair");
        return scanner.nextInt();
    }

    private int printMenuAccount() {
        System.out.println(
                "Bem vindo(a) " + currentAccount.getAccountHolder().getFirstName() + ", escolha uma ação: ");
        System.out.println("(1) para ver saldo");
        System.out.println("(2) para sacar");
        System.out.println("(3) para depositar");
        System.out.println("(0) para volta");
        return scanner.nextInt();
    }

    private void removeAccount() {
        System.out.println("Escolha a conta a ser excluída: ");
        bank.listAccounts();
        int option = scanner.nextInt();
        
        bank.deleteAccount(bank.getAccountByHumanReadableIndex(option).getAccountNumber());
    }

    private void selectAccount() {
        System.out.println("Escolha uma conta: ");
        bank.listAccounts();
        int option = scanner.nextInt();

        currentAccount = bank.getAccountByHumanReadableIndex(option);

        System.out.println("Você escolheu a conta: "
                + currentAccount.getAccountNumber()
                + " pertencente a: "
                + currentAccount.getAccountHolder().getFullName());
    }

    private void createAccount() {
        scanner.nextLine();
        System.out.println("Digite seu nome: ");
        String firstName = scanner.nextLine();

        System.out.println("Digite seu sobrenome: ");
        String middleName = scanner.nextLine();

        System.out.println("Digite seu último: ");
        String lastName = scanner.nextLine();

        System.out.println("Qual o tipo da conta: ");
        System.out.println("(1)Poupança ");
        System.out.println("(2)Corrente ");
        int accountType = scanner.nextInt();

        if (accountType == 1) {
            bank.addAccount(firstName, middleName, lastName, true);
        } else {
            bank.addAccount(firstName, middleName, lastName, false);
        }
    }

    private void addMockAccounts() {
        bank.addAccount("Miguel", "Santos", "Silva", true);
        bank.addAccount("Maria", "Souza", "Oliveira", true);
        bank.addAccount("Arthur", "Pereira", "Batista", true);
        bank.addAccount("Sofia", "Alves", "Dias", true);
        bank.addAccount("Laura", "Campos", "Cardoso", true);
        bank.addAccount("Isabella", "Mendes", "Marques", true);
        bank.addAccount("Gabriel", "Santos", "Pereira", true);
        bank.addAccount("Beatriz", "Silva", "Souza", true);
        bank.addAccount("Guilherme", "Oliveira", "Batista", true);
        bank.addAccount("Valentina", "Alves", "Dias", true);
    }
}