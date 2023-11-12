Feature:  Account API Access
  Als Dritt-Hersteller Software möchte ich über die Account REST API Schnittstelle den aktuellen Kontostand abfragen
  und Geld von und auf das Giro-Konto senden und empfangen können

  Scenario: Kontostand über API abfragen
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    Then beträgt der aktuelle Kontostand von "Giro Konto" 500 €

  Scenario: Zahlung über API empfangen
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich auf "Giro Konto" 500 € von einem gültigen externen Konto empfange
    Then die Transaktion war erfolgreich
    And beträgt der aktuelle Kontostand von "Giro Konto" 1000 €


  Scenario: Geld über API senden
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API von "Giro Konto" 500 € auf ein gültiges externes Konto überweise
    Then die Transaktion war erfolgreich
    And beträgt der aktuelle Kontostand von "Giro Konto" 0 €


  Scenario: Geld über API transferieren
    Given Ich bin registrierter Privatkunde mit Konto von Typ "Giro Konto" mit aktuellem Kontostand 500 €
    When Ich per API von "Giro Konto" 5 € auf ein gültiges internes Konto überweise
    Then die Transaktion war erfolgreich
    And beträgt der aktuelle Kontostand von "Giro Konto" 495 €
