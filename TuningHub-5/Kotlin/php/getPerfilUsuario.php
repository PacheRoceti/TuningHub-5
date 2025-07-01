<?php
header('Content-Type: application/json');

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);

    $idUsuario = $_GET['idUsuario'] ?? '';

    if (!filter_var($idUsuario, FILTER_VALIDATE_INT)) { //Valida se o ID é um número inteiro
        echo json_encode(['success' => false, 'message' => 'ID de usuário inválido.']);
        exit;
    }

    $stmt = $pdo->prepare("SELECT Username FROM Usuario WHERE idUsuario = :idUsuario");
    $stmt->execute(['idUsuario' => $idUsuario]);
    $usuario = $stmt->fetch(); //Busca o nome do usuário

    if (!$usuario) {
        echo json_encode(['success' => false, 'message' => 'Usuário não encontrado.']);
        exit;
    }

    $stmt = $pdo->prepare("SELECT idPostagem, urlImagem, filtro FROM Postagem WHERE idUsuario = :idUsuario");
    $stmt->execute(['idUsuario' => $idUsuario]);
    $fotos = $stmt->fetchAll(); //Executa a query no  BD

    echo json_encode([
        'success' => true,
        'username' => $usuario['Username'],
        'fotos' => $fotos
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Erro: ' . $e->getMessage()]); //Tratamento de Exceções
}
?>
