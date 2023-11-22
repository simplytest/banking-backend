Feature: Geldüberweisung vom Giro Konto an externes Konto
  Als registrierter Kunde möchte ich von meinem Giro-Konto eine Überweisung unter Berücksichtigung des Überweisungslimits und des Dispovolumens tätigen.

  Scenario: Erfolgreiche Geldüberweisung
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
    When Ich von "Giro Konto" 500 € auf ein gültiges externes Konto überweise
    Then Ich erhalte eine "Bestätigung" meiner Überweisung mit der Meldung ""
     And der aktuelle Kontostand von "Giro Konto" beträgt 500 €

  Scenario: Abgelehnte Geldüberweisung wegen Unterdeckung
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 1000 €
     And mein Dispovolumen beträgt 1000 €
    When Ich von "Giro Konto" 2100 € auf ein gültiges externes Konto überweise
    Then Ich erhalte eine "Ablehnung" meiner Überweisung mit der Meldung "Überweisung wegen Unterdeckung nicht möglich"
     And der aktuelle Kontostand von "Giro Konto" beträgt 1000 €

  Scenario: Abgelehnte Geldüberweisung wegen Limitüberschreitung
    Given Als Privatkunde habe ich ein Konto von Typ "Giro Konto" mit aktuellem Kontostand 5000 €
     And mein Überweisungslimit beträgt 3000 €
    When Ich von "Giro Konto" 3100 € auf ein gültiges externes Konto überweise
    Then Ich erhalte eine "Ablehnung" meiner Überweisung mit der Meldung "Überweisung wegen Limitüberschreitung nicht möglich"
     And der aktuelle Kontostand von "Giro Konto" beträgt 5000 €
