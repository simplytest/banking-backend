Feature:  RealEstate Repayment Rate
  Als Dritt-Hersteller Software möchte ich über die Schnittstelle die Möglichkeit nutzen
  für einen bestehenden Kredit eine Extratilgung durchzuführen, solange diese kleiner als 5% des Kreditvolumens beträgt
  ist.

  Scenario: Extratilgung < 5% des Kreditvolumens
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API 10 € auf ein Immobilienkredit Konto mit einem Kredit von 1000 € und Tilgung von 5 % übertrage
    Then die Transaktion war erfolgreich

  Scenario: Extratilgung < 5% des Kreditvolumens, Grenzwert
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API 49 € auf ein Immobilienkredit Konto mit einem Kredit von 1000 € und Tilgung von 5 % übertrage
    Then die Transaktion war erfolgreich

  Scenario: Extratilgung = 5% des Kreditvolumens
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API 50 € auf ein Immobilienkredit Konto mit einem Kredit von 1000 € und Tilgung von 5 % übertrage
    Then die Transaktion war erfolgreich

  Scenario: Extratilgung > 5% des Kreditvolumens, Grenzwert
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API 51 € auf ein Immobilienkredit Konto mit einem Kredit von 1000 € und Tilgung von 5 % übertrage
    Then Die Transaktion wirft einen Fehler

  Scenario: Extratilgung > 5% des Kreditvolumens, Grenzwert
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API 100 € auf ein Immobilienkredit Konto mit einem Kredit von 1000 € und Tilgung von 5 % übertrage
    Then Die Transaktion wirft einen Fehler