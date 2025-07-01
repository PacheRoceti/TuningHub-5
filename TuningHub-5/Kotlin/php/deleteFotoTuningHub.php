<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json');

$logs = [];

$logs[] = 'Iniciando script de exclusão de foto.';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
  PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
  PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
  PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
  $logs[] = 'Conectando ao banco de dados...';
  $pdo = new PDO($dsn, $user, $pass, $options);
  $logs[] = 'Conexão com o banco estabelecida.';

  if (!isset($_POST['id_postagem'])) { //Verifica se o ID da postagem foi enviado corretamente
    echo json_encode([
      'success' => false,
      'message' => 'Parâmetro "id_postagem" ausente.',
      'logs' => $logs
    ]);
    exit;
  }

  $idPostagem = $_POST['id_postagem'];
  $logs[] = "ID da postagem recebido: $idPostagem";

  $stmt = $pdo->prepare("SELECT urlImagem FROM Postagem WHERE idPostagem = :idPostagem");
  $stmt->execute(['idPostagem' => $idPostagem]);
  $resultado = $stmt->fetch(); //Buscar a URL da imagem no banco de dados

  if (!$resultado) {
    $logs[] = 'Postagem não encontrada no banco de dados.';
    echo json_encode([
      'success' => false,
      'message' => 'Postagem não encontrada.',
      'logs' => $logs
    ]);
    exit;
  }

  $urlImagem = $resultado['urlImagem'];
  $logs[] = "URL da imagem obtida: $urlImagem";

  $nomeArquivo = basename($urlImagem);
  $caminhoArquivo = __DIR__ . '/uploads/' . $nomeArquivo;
  $logs[] = "Caminho do arquivo no servidor: $caminhoArquivo"; //Extrai o nome do arquivo a partir da URL

  if (file_exists($caminhoArquivo)) { //Remove o arquivo do servidor
    if (unlink($caminhoArquivo)) {
      $logs[] = "Arquivo '$nomeArquivo' excluído com sucesso.";
    } else {
      $logs[] = "Falha ao excluir o arquivo '$nomeArquivo'.";
    }
  } else {
    $logs[] = "Arquivo '$nomeArquivo' não encontrado no servidor.";
  }

  $stmt = $pdo->prepare("DELETE FROM Postagem WHERE idPostagem = :idPostagem");
  $stmt->execute(['idPostagem' => $idPostagem]);
  $logs[] = "Postagem com ID $idPostagem removida do banco de dados."; // Remove a entrada do banco de dados

  echo json_encode([
    'success' => true,
    'message' => 'Foto excluída com sucesso.',
    'arquivo' => $nomeArquivo,
    'logs' => $logs
  ]);

} catch (PDOException $e) {
  $logs[] = 'Erro de PDO: ' . $e->getMessage();
  echo json_encode([
    'success' => false,
    'message' => 'Erro ao excluir a foto.',
    'logs' => $logs
  ]);
}
