Feature: Abschluss eines neuen Vertrages
  Als angehende Kunde möchte ich einen neuen Vertrag mit der  Firma SmartMoney abschliessen.

  Scenario: Erfolgreicher Vertragsabschluss eines neuen Privatkunden
    Given Ich bin ein neuer Privatkunde
    When Ich als Vorname "Max" eintrage
     And Ich als Name "Mustermann" eintrage
     And Ich als Geburtsdatum "01.01.1990" eintrage
     And Ich als Strasse "Schillerstrasse" eintrage
     And Ich als Hausnummer "1" eintrage
     And Ich als Ort "Berlin" eintrage
     And Ich als PLZ "10432" eintrage
     And Ich als Mail-Addresse "max@mustermann.de" eintrage
     And Vertragabschluss starte
    Then Ich erhalte eine gültige ID des erfolgten Vertragsabschlusses


  Scenario: Abgelehnter Vertragsabschluss eines bereits existierenden Privatkunden
    Given Ich bin ein existierender Privatkunde mit Vorname "Max" und Nachname "Mustermann" und Geburtsdatum "01.01.1990"
    When Ich als Vorname "Max" eintrage
     And Ich als Name "Mustermann" eintrage
     And Ich als Geburtsdatum "01.01.1990" eintrage
     And Ich als Strasse "Göethestr" eintrage
     And Ich als Hausnummer "5a" eintrage
     And Ich als Ort "Frankfurt" eintrage
     And Ich als PLZ "60320" eintrage
     And Ich als Mail-Addresse "max@mustermann.de" eintrage
     And Vertragabschluss starte
    Then Ich erhalte eine Ablehnung des Vertragsabschlusses mit der Meldung "Kunde bereits registriert"


  Scenario: Erfolgreicher Vertragsabschluss eines neuen Geschäftskunden
    Given Ich bin ein neuer Geschäftskunde
    When Ich als Vorname "Max" eintrage
     And Ich als Name "Mustermann" eintrage
     And Ich als Firmenname "Deutsche Bank AG" eintrage
     And Ich als Umsatzsteuernummer "DE123456789" eintrage
     And Ich als Strasse "Schillerstrasse" eintrage
     And Ich als Hausnummer "1" eintrage
     And Ich als Ort "Berlin" eintrage
     And Ich als PLZ "10432" eintrage
     And Ich als Mail-Addresse "max@dbank.de" eintrage
     And Vertragabschluss starte
    Then Ich erhalte eine gültige ID des erfolgten Vertragsabschlusses


  Scenario: Abgelehnter Vertragsabschluss eines bereits existierenden Geschäftskunden
    Given Ich bin ein existierender Geschäftskunde mit Firmenname "Deutsche Bank AG" und Umsatzsteuernummer "DE123456789"
    When Ich als Vorname "Robert" eintrage
     And Ich als Name "Müeller" eintrage
     And Ich als Firmenname "Deutsche Bank AG" eintrage
     And Ich als Umsatzsteuernummer "DE123456789" eintrage
     And Ich als Strasse "Göethestr" eintrage
     And Ich als Hausnummer "5a" eintrage
     And Ich als Ort "Frankfurt" eintrage
     And Ich als PLZ "60320" eintrage
     And Ich als Mail-Addresse "robert@dbank.de" eintrage
     And Vertragabschluss starte
    Then Ich erhalte eine Ablehnung des Vertragsabschlusses mit der Meldung "Kunde bereits registriert"


  Scenario: Abgelehnter Vertragsabschluss eines neuen Privatkunden aus Dritt-Land
    Given Ich bin ein neuer Privatkunde
    When Ich als Vorname "Max" eintrage
     And Ich als Name "Mustermann" eintrage
     And Ich als Geburtsdatum "01.01.1990" eintrage
     And Ich als Strasse "Independence Street" eintrage
     And Ich als Hausnummer "1" eintrage
     And Ich als Ort "London" eintrage
     And Ich als PLZ "N11" eintrage
     And Ich als Land "UK" eintrage
     And Ich als Mail-Addresse "max@mustermann.uk" eintrage
     And Vertragabschluss starte
    Then Ich erhalte eine Ablehnung des Vertragsabschlusses mit der Meldung "Vertragsabschlusses für Kunden mit Wohnsitz aussesrhalb EU nicht möglich"


  Scenario: Abgelehnter Vertragsabschluss eines neuen minderjährigen Kunden
    Given Ich bin ein neuer Privatkunde
    When Ich als Vorname "Max" eintrage
     And Ich als Name "Mustermann" eintrage
     And Ich als Geburtsdatum "01.01.2020" eintrage
     And Ich als Strasse "Schillerstrasse" eintrage
     And Ich als Hausnummer "1" eintrage
     And Ich als Ort "Berlin" eintrage
     And Ich als PLZ "10432" eintrage
     And Ich als Mail-Addresse "max@mustermann.de" eintrage
     And Vertragabschluss starte
    Then Ich erhalte eine Ablehnung des Vertragsabschlusses mit der Meldung "Vertragsabschlusses für minderjährige Personen nicht möglich"