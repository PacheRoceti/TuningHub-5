package com.example.tuninghub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * Tela de splash exibida ao iniciar o aplicativo.
 * Verifica se o usuário já está logado e redireciona para a tela correta.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o layout da tela de splash (ex: logo, animação, etc.)
        setContentView(R.layout.activity_splash)

        // Aguarda 2 segundos antes de prosseguir (tempo de exibição do splash)
        Handler(Looper.getMainLooper()).postDelayed({
            verificarSessao()
        }, 2000) // 2000 milissegundos = 2 segundos
    }

    /**
     * Verifica se o usuário já está logado.
     * - Se estiver, vai para a tela de perfil.
     * - Caso contrário, redireciona para a tela de cadastro/login.
     */
    private fun verificarSessao() {
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        val idUsuario = prefs.getInt("idUsuario", -1) // -1 é o valor padrão se não existir

        if (idUsuario != -1) {
            // Sessão ativa → envia para o perfil
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        } else {
            // Nenhum usuário logado → envia para tela de cadastro/login
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // Finaliza a SplashActivity para que o usuário não volte para ela
        finish()
    }
}
