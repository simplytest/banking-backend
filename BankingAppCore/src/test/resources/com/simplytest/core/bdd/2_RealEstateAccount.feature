Feature: Immobilien-Finanzierungskonto Sondertilgung

  Scenario: Sondertilgungstransfer möglich bei einem Wert unter < 5% des Kreditvolumens
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
    And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von 1000 € und einer Tilgungsrate von 100 €
    When Ich von "Giro Konto" 10 € auf das "Immobilien-Finanzierungskonto" transferiere
    Then Ich erhalte eine Bestätigung des erfolgten Transfers
    And der aktuelle Kontostand von "Giro Konto" beträgt 990 €
    And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt 990 €

  Scenario: Sondertilgungstransfer möglich bei einem Wert unter < 5% des Kreditvolumens, Grenzwert
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
    And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von 1000 € und einer Tilgungsrate von 100 €
    When Ich von "Giro Konto" 49 € auf das "Immobilien-Finanzierungskonto" transferiere
    Then Ich erhalte eine Bestätigung des erfolgten Transfers
    And der aktuelle Kontostand von "Giro Konto" beträgt 951 €
    And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt 951 €

  Scenario: Sondertilgungstransfer möglich bei einem Wert unter = 5% des Kreditvolumens
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
    And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von 1000 € und einer Tilgungsrate von 100 €
    When Ich von "Giro Konto" 50 € auf das "Immobilien-Finanzierungskonto" transferiere
    Then Ich erhalte eine Bestätigung des erfolgten Transfers
    And der aktuelle Kontostand von "Giro Konto" beträgt 950 €
    And der aktuelle Kontostand von "Immobilien-Finanzierungskonto" beträgt 950 €

  Scenario: Sondertilgungstransfer nicht möglich bei einem Wert unter > 5% , Grenzwert
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
    And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von 1000 € und einer Tilgungsrate von 100 €
    When Ich von "Giro Konto" 51 € auf das "Immobilien-Finanzierungskonto" transferiere
    Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "Überweisung wegen Limitüberschreitung nicht möglich"

  Scenario: Sondertilgungstransfer nicht möglich bei einem Wert unter > 5%
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
    And Als Privatkunde habe ich ein Konto von Typ "Immobilien-Finanzierungskonto" mit einem Kredit von 1000 € und einer Tilgungsrate von 100 €
    When Ich von "Giro Konto" 100 € auf das "Immobilien-Finanzierungskonto" transferiere
    Then Ich erhalte eine Ablehnung für die Sondertilgung mit der Meldung "Überweisung wegen Limitüberschreitung nicht möglich"