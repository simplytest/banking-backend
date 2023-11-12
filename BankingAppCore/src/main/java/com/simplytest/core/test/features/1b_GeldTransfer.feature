Feature: Geldtransfer zwischen eigenen Konten
  Als registrierter privater Kunde möchte ich zwischen eigenen Kontotypen Geld transferieren können.

  Scenario: Erfolgreicher Geldtransfer
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
     And Als Privatkunde habe ich ein Konto von Typ "Tagesgeld Konto" mit aktuellem Kontostand 0 €
    When Ich von "Giro Konto" 500 € auf das "Tagesgeld Konto" transferiere
    Then Ich erhalte eine Bestätigung des erfolgten Transfers
     And der aktuelle Kontostand von "Giro Konto" beträgt 500 €
     And der aktuelle Kontostand von "Tagesgeld Konto" beträgt 500 €


  Scenario: Abgelehnter Geldtransfer von laufendes Festgeld Konto
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
     And Als Privatkunde habe ich ein Konto von Typ "Festgeld Konto" mit aktuellem Kontostand 5000 €
     And verbleibender gebundener Laufzeit von "Festgeld Konto" von 5.0 Jahren
    When Ich von "Festgeld Konto" 1100 € auf das "Giro Konto" transferiere
    Then Ich erhalte eine "Ablehnung" meines Transfers mit der Meldung "Transfer während der gebundener Laufzeit nicht möglich"
     And der aktuelle Kontostand von "Giro Konto" beträgt 1000 €
     And der aktuelle Kontostand von "Festgeld Konto" beträgt 5000 €


  Scenario: Abgelehnter Geldtransfer auf laufendes Festgeld Konto
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
     And Als Privatkunde habe ich ein Konto von Typ "Festgeld Konto" mit aktuellem Kontostand 5000 €
     And verbleibender gebundener Laufzeit von "Festgeld Konto" von 5.0 Jahren
    When Ich von "Giro Konto" 500 € auf das "Festgeld Konto" transferiere
    Then Ich erhalte eine "Ablehnung" meines Transfers mit der Meldung "Transfer während der gebundener Laufzeit nicht möglich"
     And der aktuelle Kontostand von "Giro Konto" beträgt 1000 €
     And der aktuelle Kontostand von "Festgeld Konto" beträgt 5000 €


  Scenario: Abgelehnter Geldtransfer wegen Unterdeckung ungeachtet Dispovolumen
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
      And mein Dispovolumen beträgt 1000 €
      And Als Privatkunde habe ich ein Konto von Typ "Tagesgeld Konto" mit aktuellem Kontostand 0 €
    When Ich von "Giro Konto" 1100 € auf das "Tagesgeld Konto" transferiere
    Then Ich erhalte eine "Ablehnung" meines Transfers mit der Meldung "Transfer wegen Unterdeckung nicht möglich"
     And der aktuelle Kontostand von "Giro Konto" beträgt 1000 €
     And der aktuelle Kontostand von "Tagesgeld Konto" beträgt 0 €
