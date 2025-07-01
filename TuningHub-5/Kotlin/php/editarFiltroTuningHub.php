<?php
header("Content-Type: application/json");

$conn = new mysqli($host, $user, $password, $database);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Erro na conexão com o banco de dados: " . $conn->connect_error]);
    exit;
}

// Corrigir os nomes dos campos recebidos
$idPostagem = isset($_POST['id_postagem']) ? intval($_POST['id_postagem']) : 0;
$novoFiltro = isset($_POST['novo_filtro']) ? $conn->real_escape_string($_POST['novo_filtro']) : '';

if ($idPostagem <= 0 || empty($novoFiltro)) {
    echo json_encode(["success" => false, "message" => "Dados inválidos"]);
    exit;
}

// Corrigir nome da tabela e da coluna
$sql = "UPDATE Postagem SET filtro = ? WHERE idPostagem = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("si", $novoFiltro, $idPostagem);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Filtro atualizado com sucesso"]);
} else {
    echo json_encode(["success" => false, "message" => "Erro ao atualizar filtro: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>