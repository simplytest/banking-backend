Feature: Immobilien-Finanzierungskonto Sondertilgung

  Rule: Sondertilgungstransfer nur bei einem Wert unter < 5% des Kreditvolumens möglich

    @UnitTest
    Scenario Outline: Erfolgreiche Prüfung der Möglichkeit der Sondertilgung eines Betrages unter 5% des Kreditvolumens <name>
      Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich die Transfermöglichkeit <transferAmount> €  von "Giro Konto" auf das "Immobilien-Finanzierungskonto" prüfe
      Then Ich erhalte eine Bestätigung

      Examples:
        | name                     | balance | credit | transferAmount |
        | Betrag deutlich unter 5% | 1000    | 1000   | 10             |
        | Betrag knapp unter 5%    | 1000    | 1000   | 49             |
        | Betrag exakt 5%          | 1000    | 1000   | 50             |

    @UnitTest @Negative
    Scenario Outline: Abgelehnte Prüfung der Möglichkeit der Sondertilgung eines Betrages oberhalb 5% des Kreditvolumens <name>
      Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich die Transfermöglichkeit <transferAmount> €  von "Giro Konto" auf das "Immobilien-Finanzierungskonto" prüfe
      Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "<expectedError>"
      Examples:
        | name                        | balance | credit | transferAmount | expectedError                                       |
        | Betrag knapp oberhalb 5%    | 1000    | 1000   | 51             | Überweisung wegen Limitüberschreitung nicht möglich |
        | Betrag deutlich oberhalb 5% | 1000    | 1000   | 100            | Überweisung wegen Limitüberschreitung nicht möglich |




    @IntegrationTest
    Scenario Outline: Erfolgreiche Sondertilgung eines Betrages unter 5% des Kreditvolumens <name>
      Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich von "Giro Konto" <transferAmount> € auf das "Immobilien-Finanzierungskonto" transferiere
      Then Ich erhalte eine Bestätigung des erfolgten Transfers
      And der aktuelle Kontostand von "Giro Konto" beträgt <expectedAmount> €
      And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt <expectedAmount> €
      Examples:
        | name                     | balance | credit | transferAmount | expectedAmount |
        | Betrag deutlich unter 5% | 1000    | 1000   | 10             | 990            |
        | Betrag knapp unter 5%    | 1000    | 1000   | 49             | 951            |
        | Betrag exakt 5%          | 1000    | 1000   | 50             | 950            |


    @IntegrationTest @Negative
    Scenario Outline: Abgelehnte Sondertilgung eines Betrages oberhalb 5% des Kreditvolumens <name>
      Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich von "Giro Konto" <transferAmount> € auf das "Immobilien-Finanzierungskonto" transferiere
      Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "<expectedError>"
      And der aktuelle Kontostand von "Giro Konto" beträgt <expectedAmount> €
      And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt <expectedAmount> €
      Examples:
        | name                        | balance | credit | transferAmount | expectedAmount | expectedError                                       |
        | Betrag knapp oberhalb 5%    | 1000    | 1000   | 51             | 1000           | Überweisung wegen Limitüberschreitung nicht möglich |
        | Betrag deutlich oberhalb 5% | 1000    | 1000   | 100            | 1000           | Überweisung wegen Limitüberschreitung nicht möglich |



  Rule: Transfer nur vom einem Girokonto zulässig
    @UnitTest  @Negative
    Scenario Outline: Abgelehnte Prüfung der Möglichkeit der Sondertilgung on einem Konto von Typ <name>
      Given Als Privatkunde habe ich ein Konto von Typ "<sourceAccount>" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich die Transfermöglichkeit <transferAmount> €  von "<sourceAccount>" auf das "Immobilien-Finanzierungskonto" prüfe
      Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "<expectedError>"

      Examples:
        | name            | sourceAccount   | balance | credit | transferAmount | expectedError                                   |
        | Tagesgeld Konto | Tagesgeld Konto | 1000    | 1000   | 10             | Transaktion von diesem Quellkonto nicht erlaubt |

    @IntegrationTest @Negative
    Scenario Outline: Abgelehnte Sondertilgung von einem Konto von Typ <name>
      Given Als Privatkunde habe ich ein Konto von Typ "<sourceAccount>" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich von "<sourceAccount>" <transferAmount> € auf das "Immobilien-Finanzierungskonto" transferiere
      Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "<expectedError>"
      And der aktuelle Kontostand von "<sourceAccount>" beträgt <expectedAmount> €
      And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt <expectedAmount> €
      Examples:
        | name            | sourceAccount   | balance | credit | transferAmount | expectedAmount | expectedError                                   |
        | Tagesgeld Konto | Tagesgeld Konto | 1000    | 1000   | 10             | 1000           | Transaktion von diesem Quellkonto nicht erlaubt |



  Rule: Transfer nur einmal pro Kalenderjahr
    @IntegrationTest @Negative
    Scenario Outline: Abgelehnte Sondertilgung bei mehrfachen Transfer im gleichen Kalenderjahr
      Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich von "Giro Konto" <transferAmount> € auf das "Immobilien-Finanzierungskonto" transferiere
      Then Ich erhalte eine Bestätigung des erfolgten Transfers
      And der aktuelle Kontostand von "Giro Konto" beträgt <expectedAmount1> €
      And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt <expectedAmount1> €
      When Ich von "Giro Konto" <transferAmount> € auf das "Immobilien-Finanzierungskonto" transferiere
      Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "<expectedError>"
      And der aktuelle Kontostand von "Giro Konto" beträgt <expectedAmount2> €
      And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt <expectedAmount2> €
      Examples:
        | balance | credit | transferAmount | expectedAmount1 | expectedAmount2 | expectedError                                       |
        | 1000    | 1000   | 10             | 990             | 990             | Überweisung wegen Limitüberschreitung nicht möglich |



  Rule: Sondertilgung darf Kreditschuld nicht übersteigen
    @UnitTest @Negative
    Scenario Outline: Abgelehnte Prüfung der Möglichkeit der Sondertilgung wegen Übererfüllung des Kredits <name>
      Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand <balance> €
      And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von <credit> € und einer Tilgungsrate von 100 €
      When Ich die Transfermöglichkeit <transferAmount> €  von "Giro Konto" auf das "Immobilien-Finanzierungskonto" prüfe
      Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "<expectedError>"
      Examples:
        | name                     | balance | credit | transferAmount | expectedError         |
        | Betrag knapp oberhalb 5% | 1000    | 40     | 45             | Betrag nicht zulässig |
