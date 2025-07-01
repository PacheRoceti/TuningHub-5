<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL); //Habilita exibição explicita dos erros PHP

header('Content-Type: application/json');

ini_set('upload_max_filesize', '10M');
ini_set('post_max_size', '12M'); //Aumenta os limites de upload para evitar falhas

$logs = [];

$logs[] = 'Iniciando script de upload.';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
  PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
  PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
  PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
  $logs[] = 'Tentando conectar ao banco de dados...';
  $pdo = new PDO($dsn, $user, $pass, $options);
  $logs[] = 'Conexão com o banco de dados estabelecida com sucesso.';

  if (!isset($_FILES['imagem'])) { //Verifica se o arquivo e o ID do usuário foram enviados
    echo json_encode([
      'success' => false,
      'message' => 'Arquivo "imagem" ausente.',
      'logs' => $logs
    ]);
    exit;
  }

  if (!isset($_POST['id_usuario'])) {
    echo json_encode([
      'success' => false,
      'message' => 'Parâmetro "id_usuario" ausente.',
      'logs' => $logs
    ]);
    exit;
  }

  if (!isset($_POST['filtro'])) { //Verifica se o filtro foi enviado
    echo json_encode([
      'success' => false,
      'message' => 'Parâmetro "filtro" ausente.',
      'logs' => $logs
    ]);
    exit;
  }

  $imagem = $_FILES['imagem'];
  $idUsuario = $_POST['id_usuario'];
  $filtro = $_POST['filtro']; //Novo parâmetro filtro
  $logs[] = 'Parâmetros recebidos. ID do usuário: ' . $idUsuario . ' Filtro: ' . $filtro;

  if ($imagem['error'] !== UPLOAD_ERR_OK) { //Caso retorno da exeção != de 200
    $logs[] = 'Erro no upload da imagem: código ' . $imagem['error'];
    echo json_encode([
      'success' => false,
      'message' => 'Erro no upload da imagem.',
      'logs' => $logs
    ]);
    exit;
  }

  $uploadDir = __DIR__ . '/uploads/'; //Define o diretório de upload
  if (!file_exists($uploadDir)) {
    if (mkdir($uploadDir, 0755, true)) {
      $logs[] = "Diretório '$uploadDir' criado.";
    } else {
      $logs[] = "Falha ao criar diretório '$uploadDir'.";
      echo json_encode([
        'success' => false,
        'message' => 'Erro ao criar o diretório de upload.',
        'logs' => $logs
      ]);
      exit;
    }
  } else {
    $logs[] = "Diretório '$uploadDir' já existe.";
  }

  $nomeArquivo = uniqid() . '_' . basename($imagem['name']);
  $caminhoCompleto = $uploadDir . $nomeArquivo;
  $logs[] = "Nome do arquivo gerado: $nomeArquivo"; //Gera id da imagem salva no server

  if (move_uploaded_file($imagem['tmp_name'], $caminhoCompleto)) { //Caminho público para acessar a imagem
    $urlImagem = 'http://truulcorreio1.hospedagemdesites.ws/tuninghub/uploads/' . $nomeArquivo;
    $logs[] = "Arquivo movido com sucesso para: $caminhoCompleto";
    $logs[] = "URL pública da imagem: $urlImagem";

    $sql = "INSERT INTO Postagem (idUsuario, urlImagem, filtro) VALUES (:idUsuario, :urlImagem, :filtro)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
      'idUsuario' => $idUsuario,
      'urlImagem' => $urlImagem,
      'filtro' => $filtro 
    ]); //Registra URL e filtro no BD
    $logs[] = 'Registro inserido no banco de dados com sucesso.';

    echo json_encode([
      'success' => true,
      'message' => 'Imagem enviada com sucesso.',
      'imageUrl' => $urlImagem,
      'logs' => $logs
    ]);
  } else {
    $logs[] = 'Falha ao mover o arquivo enviado.';
    echo json_encode([
      'success' => false,
      'message' => 'Falha ao mover o arquivo.',
      'logs' => $logs
    ]);
  }

} catch (PDOException $e) {
  $logs[] = 'Erro de PDO: ' . $e->getMessage();
  echo json_encode([
    'success' => false,
    'message' => 'Erro ao conectar ou executar no banco de dados.',
    'logs' => $logs
  ]);
}
