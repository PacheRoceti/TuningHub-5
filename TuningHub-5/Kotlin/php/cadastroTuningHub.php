<?php
header('Content-Type: application/json');

$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);

    $usuario = $_GET['usuario'] ?? ''; //"Nulla" os dados, caso não fornecido
    $email = $_GET['email'] ?? '';
    $senha = $_GET['senha'] ?? '';

    if (empty($usuario) || empty($email) || empty($senha)) {
        echo json_encode(['success' => false, 'message' => 'Preencha todos os campos.']);
        exit;
    }

    // Consulta se o nome de usuário já existe
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM Usuario WHERE Username = :usuario");
    $stmt->execute(['usuario' => $usuario]);
    if ($stmt->fetchColumn() > 0) {
        echo json_encode(['success' => false, 'message' => 'Nome de usuário já existe.']);
        exit;
    }

    // Criptografa pré registro no BD
    $senhaHash = password_hash($senha, PASSWORD_DEFAULT);

    $sql = "INSERT INTO Usuario (Username, Email, Senha) VALUES (:usuario, :email, :senha)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        'usuario' => $usuario,
        'email' => $email,
        'senha' => $senhaHash
    ]); // Query efetivada no BD

    echo json_encode(['success' => true, 'message' => 'Usuário cadastrado com sucesso!']);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Erro no servidor: ' . $e->getMessage()]);
    exit;
}
?>
