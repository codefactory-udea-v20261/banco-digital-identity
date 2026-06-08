Feature: Seguridad y acceso a la plataforma

  @CPHU11-01
  Scenario: Login exitoso con credenciales correctas
    Given el usuario "cliente@test.com" existe con clave codificada y no esta bloqueado
    When el cliente hace login con correo "cliente@test.com" y clave "Test1234!"
    Then el sistema retorna un token JWT valido
    And no se guarda ninguna modificacion sobre el usuario

  @CPHU11-02
  Scenario: Login fallido por contrasena incorrecta
    Given el usuario "cliente@test.com" existe con 1 intento fallido previo
    When el cliente hace login con correo "cliente@test.com" y clave "mala"
    Then el sistema lanza CredencialesInvalidasException
    And los intentos fallidos del usuario quedan en 2

  @CPHU11-03
  Scenario: Cuenta bloqueada al tercer intento fallido
    Given el usuario "cliente@test.com" existe con 2 intentos fallidos previos
    When el cliente hace login con correo "cliente@test.com" y clave "mala"
    Then el sistema lanza CuentaBloqueadaException
    And la cuenta del usuario queda bloqueada

  @CPHU11-05
  Scenario: Cierre de sesion exitoso revoca el token por JTI
    Given el usuario tiene una sesion activa con token "jwt-token" con JTI "jti-abc" y expiracion futura
    When el cliente hace logout con ese token
    Then el token queda registrado en la blacklist con JTI "jti-abc"

  @CPHU12-01
  Scenario: Cambio exitoso de contrasena
    Given el usuario existe con clave codificada "encoded"
    When cambia la contrasena con actual "old" nueva "New1234!" confirmacion "New1234!"
    And la contrasena actual es correcta y la nueva es valida y no reutilizada
    Then el resultado indica success true
    And la nueva clave queda guardada en la base de datos

  @CPHU12-02
  Scenario: Cambio fallido por contrasena actual incorrecta
    Given el usuario existe con clave codificada "encoded"
    When cambia la contrasena con actual "wrong" nueva "New1234!" confirmacion "New1234!"
    And la contrasena actual no coincide con la almacenada
    Then el sistema lanza InvalidPasswordException
    And no se guarda ninguna modificacion sobre el usuario

  @CPHU12-03
  Scenario: Cambio fallido por campo passwordActual vacio
    Given el usuario existe con clave codificada "encoded"
    When cambia la contrasena con actual "" nueva "New1234!" confirmacion "New1234!"
    And la contrasena actual no coincide con la almacenada
    Then el sistema lanza InvalidPasswordException
    And no se guarda ninguna modificacion sobre el usuario

  @CPHU12-04
  Scenario: Cambio fallido por nueva contrasena igual a la actual
    Given el usuario existe con clave codificada "encoded"
    When cambia la contrasena con actual "old" nueva "Same1234!" confirmacion "Same1234!"
    And la contrasena actual es correcta y la nueva coincide y cumple politica pero esta reutilizada
    Then el sistema lanza PasswordChangeException
    And no se guarda ninguna modificacion sobre el usuario

  @CPHU12-05
  Scenario: Cambio fallido porque confirmacion no coincide con nueva contrasena
    Given el usuario existe con clave codificada "encoded"
    When cambia la contrasena con actual "old" nueva "new1" confirmacion "new2"
    And la contrasena actual es correcta pero nueva y confirmacion no coinciden
    Then el sistema lanza PasswordChangeException
    And no se guarda ninguna modificacion sobre el usuario

  @CPHU12-06
  Scenario: Cambio fallido por nueva contrasena debil
    Given el usuario existe con clave codificada "encoded"
    When cambia la contrasena con actual "old" nueva "weak" confirmacion "weak"
    And la contrasena actual es correcta y las nuevas coinciden pero no cumplen la politica
    Then el sistema lanza PasswordChangeException
    And no se guarda ninguna modificacion sobre el usuario