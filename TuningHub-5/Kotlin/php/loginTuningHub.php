<?php
header('Content-Type: application/json');
$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options); //Autenticação no BD

    $usuario = $_GET['usuario'] ?? ''; //"Nulla" os dados, caso não fornecido
    $senha = $_GET['senha'] ?? '';

    if (empty($usuario) || empty($senha)) {
        echo json_encode(['success' => false, 'message' => 'Usuário e senha são obrigatórios.']);
        exit;
    }

    $sql = "SELECT idUsuario, Username, Email, Senha 
            FROM Usuario 
            WHERE Username = :usuario";
    
    $stmt = $pdo->prepare($sql); //Evita SQL injection
    $stmt->execute(['usuario' => $usuario]); 
    $usuarioData = $stmt->fetch(); //Retona o res da consulta em 32

    if ($usuarioData && password_verify($senha, $usuarioData['Senha'])) { // CASO dados válidos
        echo json_encode([
            'success' => true,
            'idUsuario' => $usuarioData['idUsuario'],
            'nome' => $usuarioData['Username'],
            'email' => $usuarioData['Email']
        ]);
    } else { // CASO dados inválidos
        echo json_encode([
            'success' => false,
            'message' => 'Usuário ou senha inválidos'
        ]);
    }

} catch (\PDOException $e) { // Tratamento de exceções
    echo json_encode([
        'success' => false,
        'message' => 'Erro de conexão: ' . $e->getMessage()
    ]);
    exit;
}
?>
