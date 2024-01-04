Feature:  Contract API Access
  Als Dritt-Hersteller Software möchte ich über die Contract REST API Schnittstelle einen neuen Vertrag abschliessen,
  einen bestehenden Vertrag kündigen, Kundenadresse ändern, weitere Konto hinzufügen oder ein geöffnetes Konto
  schliessen

  Scenario: Vertrag über API abschliessen
    Given Ich bin ein Privatkunde
    When Ich einen neuen Vertrag abschliesse
    Then erhalte ich eine gültige Vertrag-ID
    And erhalte ich ein Konto von Typ "Giro Konto"
    And beträgt der aktuelle Kontostand von "Giro Konto" 0 €

  Scenario: Vertrag über API kündigen
    Given Ich bin ein registrierter Privatkunde
    When Ich meinen Vertrag kündige
    Then die Transaktion war erfolgreich
    And mein Vertrag wurde entfernt

  Scenario: Immobilien-Finanzierungs Vertrag über API abschliessen
    Given Ich bin ein Privatkunde
    When Ich einen neuen Vertrag abschliesse
    And Ich ein neues Immobilien-Finanzierungskonto mit Kredit von 100 € und Tilgung von 5 % erstelle
    Then erhalte ich eine gültige Vertrag-ID
    And erhalte ich ein Konto von Typ "Immobilien-Finanzierungskonto"
    And beträgt der aktuelle Kontostand von "Immobilien-Finanzierungskonto" -100 €
