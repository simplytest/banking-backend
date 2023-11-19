Feature: Beantragung eines Dispovolumens
  Als registrierter Kunde möchte ich ein Dispovolumen beantragen. Dazu soll die Applikation meine Kreditwürdigkeit anhand von Kunden-Kernmerkrmalen ermitteln.

  Scenario: Dispo Bewilligung für privaten Kunden
    Given Als Privatkunde habe ich einen Schufa Score von 9.0
     And mein Dispovolumen beträgt 0 €
    When Ich einen Antrag auf Dispovolumen von 1000 € stelle
    Then Ich erhalte eine Bestätigung für die Gewährung des Dispovolumens
     And mein Dispovolumen beträgt 1000 €

  Scenario: Dispo Ablehnung für privaten Kunden
    Given Als Privatkunde habe ich einen Schufa Score von 3.0
    When Ich einen Antrag auf Dispovolumen von 1000 € stelle
    Then Ich erhalte eine Ablehnung des Dispovolumens mit der Meldung "Unzureichende Kreditwürdigkeit"
     And mein Dispovolumen beträgt 0 €

  Scenario: Dispo Ablehnung für privaten Kunden aus EU Ausland
    Given Ich bin ein Privatkunde aus "Österreich"
    When Ich einen Antrag auf Dispovolumen von 1000 € stelle
    Then Ich erhalte eine Ablehnung des Dispovolumens mit der Meldung "Dispovolumen für Kunden aus Ausland nicht unterstützt"
     And mein Dispovolumen beträgt 0 €

  Scenario: Dispo Bewilligung für Geschäftskunden
    Given Als Geschäftskunde habe ich einen Jahresumsatz von 1000000
     And mein Dispovolumen beträgt 0 €
    When Ich einen Antrag auf Dispovolumen von 1000 € stelle
    Then Ich erhalte eine Bestätigung für die Gewährung des Dispovolumens
     And mein Dispovolumen beträgt 5000 €

  Scenario: Dispo Ablehnung für Geschäftskunden
    Given Als Geschäftskunde habe ich einen Jahresumsatz von 299000
    When Ich einen Antrag auf Dispovolumen von 1000 € stelle
    Then Ich erhalte eine Ablehnung des Dispovolumens mit der Meldung "Unzureichende Kreditwürdigkeit"
     And mein Dispovolumen beträgt 0 €