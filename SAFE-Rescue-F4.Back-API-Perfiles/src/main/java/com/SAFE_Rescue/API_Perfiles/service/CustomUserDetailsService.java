package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String idUsuario) throws UsernameNotFoundException {

        // Convertimos el ID del token a Integer
        Integer userId = Integer.parseInt(idUsuario);

        // Buscamos el usuario por ID
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        List<GrantedAuthority> authorities = new ArrayList<>();
        // En tu caso, el tipo de perfil es el rol
        authorities.add(new SimpleGrantedAuthority(usuario.getTipoUsuario().getNombreUpperCased()));

        return new User(
                usuario.getIdUsuario().toString(), // El subject (ID) para el Principal
                usuario.getContrasenia(),
                authorities
        );
    }
}