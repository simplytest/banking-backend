Feature: Geldüberweisung vom Giro Konto
  Als registrierter privater Kunde möchte ich von meinem Giro-Konto eine Überweisung unter Berücksichtigung des Überweisungslimits und des Dispovolumens tätigen.

  Scenario Outline: Geldüberweisung von <amount> mit <status>
    Given Als Privatkunde habe ich ein Konto von Typ <accountType> mit aktuellem Kontostand <balance> €
    And mein Dispovolumen beträgt <dispo> €
    And mein Überweisungslimit beträgt <limit> €
    When Ich von <accountType> <amount> € auf ein gültiges externes Konto überweise
    Then Ich erhalte eine <status> meiner Überweisung mit der Meldung <message>
    And der aktuelle Kontostand von <accountType> beträgt <result> €
    Examples:
      | accountType  | balance | dispo | limit | amount | status        | result | message |
      | "Giro Konto" | 1000    | 1000  | 3000  | 500    | "Bestätigung" | 500    | ""      |
      | "Giro Konto" | 5000    | 1000  | 3000  | 3000   | "Bestätigung" | 2000   | ""      |
      | "Giro Konto" | 5000    | 1000  | 3000  | 3100   | "Ablehnung"   | 5000   | "Überweisung wegen Limitüberschreitung nicht möglich"      |
