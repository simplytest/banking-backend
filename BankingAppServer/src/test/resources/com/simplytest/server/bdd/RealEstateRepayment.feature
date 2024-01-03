Feature:  RealEstate Repayment Rate
  Als Dritt-Hersteller Software möchte ich über die Schnittstelle die Möglichkeit nutzen
  für einen bestehenden Kredit eine Sondertilgung durchzuführen, solange diese kleiner als 5% des Kreditvolumens beträgt
  ist.

  Rule: Sondertilgung nur bei einem Wert unter < 5% des Kreditvolumens möglich

    Scenario Outline: Erfolgreiche Sondertilgung eines Betrages unter 5% des Kreditvolumens <name>
      Given Ich bin registrierter Privatkunde mit Konto von Typ <accountType> mit aktuellem Kontostand <balance> €
      And Ich ein neues Immobilien-Finanzierungskonto mit Kredit von <amount> € und Tilgung von <repaymentRate> % erstelle
      When Ich per API von "Giro Konto" <transferAmount> € auf ein "Immobilien-Finanzierungskonto" übertrage
      Then die Transaktion war erfolgreich
      And beträgt der aktuelle Kontostand von "Giro Konto" <expectedAmountGiro> €
      And beträgt der aktuelle Kontostand von "Immobilien-Finanzierungskonto" <expectedAmountImmo> €
      Examples:
        | name                     | accountType  | balance | transferAmount | amount | repaymentRate | expectedAmountGiro | expectedAmountImmo |
        | Betrag deutlich unter 5% | "Giro Konto" | 500     | 10             | 1000   | 5             | 490                | -990               |


    @Negative
    Scenario Outline: Abgelehnte Sondertilgung eines Betrages oberhalb 5% des Kreditvolumens <name>
      Given Ich bin registrierter Privatkunde mit Konto von Typ <accountType> mit aktuellem Kontostand <balance> €
      And Ich ein neues Immobilien-Finanzierungskonto mit Kredit von <amount> € und Tilgung von <repaymentRate> % erstelle
      When Ich per API von "Giro Konto" <transferAmount> € auf ein "Immobilien-Finanzierungskonto" übertrage
      Then Die Transaktion wirft einen Fehler "LimitExceeded"
      And beträgt der aktuelle Kontostand von "Giro Konto" <expectedAmountGiro> €
      And beträgt der aktuelle Kontostand von "Immobilien-Finanzierungskonto" <expectedAmountImmo> €
      Examples:
        | name                        | accountType  | balance | transferAmount | amount | repaymentRate | expectedAmountGiro | expectedAmountImmo |
        | Betrag deutlich oberhalb 5% | "Giro Konto" | 500     | 100            | 1000   | 5             | 500                | -1000              |

