package viniciusmiranda;

public class SavingsAccount extends BankAccount {

    public SavingsAccount(String firstName, String middleName, String lastName, boolean isSavings) {
        super(firstName, middleName, lastName, isSavings);
    }

    public SavingsAccount(Person accountHolder, String accountNumber, double balance, boolean isSavings) {
        super(accountHolder, accountNumber, balance, isSavings);
    }

    /**
     * Simula investimento em renda fixa na modalidade CDB.
     *
     * @param initialInvestment Investimento inicial.
     * @param interest          Taxa de juros anual, normalmente a CDI.
     * @param profitability     Rentabilidade(ex: 120% da CDI)
     * @param period            Período em meses ou em anos, em meses caso perYear
     *                          seja falso, em anos caso verdadeiro.
     * @param perYear           Verdadeiro se periodo for anual, falso se for
     *                          mensal.
     * @return O valor final simulado pelo cálculo de CDB, com desconto de 20% de
     *         imposto
     */
    public double calculateYield(double initialInvestment, double interest, double profitability,
            int period, boolean perYear) {
        double result = 0;

        if (perYear) {
            result = (interest * (profitability / 100) * period) * initialInvestment;
        } else {
            result = ((interest / 12) * ((profitability / 100) / 12) * period) * initialInvestment;
        }

        return (result * 0.2) + initialInvestment;
    }
}