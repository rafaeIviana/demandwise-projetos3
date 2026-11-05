package br.com.demandwise.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class AutenticacaoService {
    private final String usuario;
    private final String senhaHash;

    public AutenticacaoService(
            @Value("${demandwise.auth.username:demo}") String usuario,
            @Value("${demandwise.auth.password-hash:100000:ZGVtYW5kd2lzZS1kZW1v:Z7PpJBrAIAruVGws2jBCm5jMQ8Nrxv5mXLLPMlFQsUw=}") String senhaHash) {
        this.usuario = usuario;
        this.senhaHash = senhaHash;
    }

    public boolean autenticar(String usuarioInformado, String senha) {
        if (usuarioInformado == null || senha == null || !MessageDigest.isEqual(
                usuario.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                usuarioInformado.getBytes(java.nio.charset.StandardCharsets.UTF_8))) return false;
        try {
            String[] partes = senhaHash.split(":");
            int iteracoes = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, esperado.length * 8);
            byte[] obtido = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return MessageDigest.isEqual(esperado, obtido);
        } catch (Exception e) {
            return false;
        }
    }
}
